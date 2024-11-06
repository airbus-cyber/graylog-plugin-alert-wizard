/*
 * Copyright (C) 2018 Airbus CyberSecurity (SAS)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package com.airbus_cyber_security.graylog.wizard.alert.business;

import com.airbus_cyber_security.graylog.wizard.alert.model.FieldRule;
import com.airbus_cyber_security.graylog.wizard.alert.model.Pipeline;
import com.airbus_cyber_security.graylog.wizard.alert.model.TriggeringConditions;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.AlertRuleStream;
import com.airbus_cyber_security.graylog.wizard.list.utilities.AlertListUtilsService;
import jakarta.inject.Inject;
import org.graylog.plugins.pipelineprocessor.db.PipelineDao;
import org.graylog.plugins.pipelineprocessor.db.RuleDao;
import org.graylog2.plugin.database.ValidationException;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.plugin.streams.StreamRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TriggeringConditionsService {

    private final StreamPipelineService streamPipelineService;
    private final AlertListUtilsService alertListUtilsService;
    private final FieldRulesUtilities fieldRulesUtilities;

    @Inject
    public TriggeringConditionsService(StreamPipelineService streamPipelineService,
                                       AlertListUtilsService alertListUtilsService,
                                       FieldRulesUtilities fieldRulesUtilities) {
        this.streamPipelineService = streamPipelineService;
        this.alertListUtilsService = alertListUtilsService;
        this.fieldRulesUtilities = fieldRulesUtilities;
    }

    public TriggeringConditions createTriggeringConditions(AlertRuleStream streamConfiguration, String title, String userName) throws ValidationException {
        Stream stream = this.streamPipelineService.createStream(streamConfiguration.getMatchingType(), title, userName);
        this.streamPipelineService.createStreamRule(streamConfiguration.getFieldRules(), stream.getId());
        return createTriggeringConditionsFromStream(streamConfiguration, title, stream.getId(), userName);
    }

    public TriggeringConditions updateTriggeringConditions(TriggeringConditions previousConditions, String alertTitle,
                                                            AlertRuleStream streamConfiguration, String userName) throws ValidationException {
        // update filtering stream
        String streamIdentifier = previousConditions.filteringStreamIdentifier();
        Stream stream = this.streamPipelineService.loadStream(streamIdentifier);
        this.streamPipelineService.updateStream(stream, streamConfiguration, alertTitle);

        if (!previousConditions.outputStreamIdentifier().equals(streamIdentifier)) {
            this.streamPipelineService.deleteStreamFromIdentifier(previousConditions.outputStreamIdentifier());
        }
        deletePipelineIfAny(previousConditions.pipeline());

        return createTriggeringConditionsFromStream(streamConfiguration, alertTitle, stream.getId(), userName);
    }

    public void deleteTriggeringConditions(TriggeringConditions conditions) {
        this.streamPipelineService.deleteStreamFromIdentifier(conditions.filteringStreamIdentifier());
        if (!conditions.outputStreamIdentifier().equals(conditions.filteringStreamIdentifier())) {
            this.streamPipelineService.deleteStreamFromIdentifier(conditions.outputStreamIdentifier());
        }
        deletePipelineIfAny(conditions.pipeline());
    }

    public List<FieldRule> getFieldRules(TriggeringConditions conditions) {
        List<FieldRule> fieldRules = new ArrayList<>();
        if (conditions.pipeline() != null) {
            List<FieldRule> pipelineFieldRules = conditions.pipeline().fieldRules();
            fieldRules.addAll(pipelineFieldRules);
        }
        String streamIdentifier = conditions.filteringStreamIdentifier();
        Stream stream = this.streamPipelineService.loadStream(streamIdentifier);
        if (stream != null) {
            fieldRules.addAll(this.getListFieldRule(stream.getStreamRules()));
        }
        return fieldRules;
    }

    private void deletePipelineIfAny(Pipeline pipeline) {
        if (pipeline == null) {
            return;
        }
        this.streamPipelineService.deletePipeline(pipeline.identifier(), pipeline.ruleIdentifier());
        for (FieldRule fieldRule: this.nullSafe(pipeline.fieldRules())) {
            this.alertListUtilsService.decrementUsage(fieldRule.getValue());
        }
    }

    public boolean isDisabled(TriggeringConditions conditions) {
        String streamIdentifier = conditions.filteringStreamIdentifier();
        Stream stream = this.streamPipelineService.loadStream(streamIdentifier);
        if (stream == null) {
            return false;
        }
        return stream.getDisabled();
    }

    private TriggeringConditions createTriggeringConditionsFromStream(AlertRuleStream streamConfiguration, String title,
                                                                      String filteringStreamIdentifier, String userName) throws ValidationException {
        List<FieldRule> fieldRulesWithList = this.streamPipelineService.extractPipelineFieldRules(streamConfiguration.getFieldRules());

        TriggeringConditions.Builder builder = TriggeringConditions.builder().filteringStreamIdentifier(filteringStreamIdentifier);
        if (fieldRulesWithList.isEmpty()) {
            return builder.outputStreamIdentifier(filteringStreamIdentifier).build();
        }

        for (FieldRule fieldRule: fieldRulesWithList) {
            this.alertListUtilsService.incrementUsage(fieldRule.getValue());
        }

        Stream.MatchingType matchingType = streamConfiguration.getMatchingType();
        builder.matchingType(matchingType);
        if (matchingType.equals(Stream.MatchingType.AND) && this.fieldRulesUtilities.hasStreamRules(streamConfiguration.getFieldRules())) {
            PipelineDao graylogPipeline = this.streamPipelineService.createPipeline(title, matchingType, filteringStreamIdentifier);
            Stream outputStream = this.streamPipelineService.createStream(matchingType, title + " output", userName);
            RuleDao pipelineRule = this.streamPipelineService.createPipelineRule(title, fieldRulesWithList, matchingType, outputStream.getId());
            Pipeline pipeline = Pipeline.builder()
                    .identifier(graylogPipeline.id()).ruleIdentifier(pipelineRule.id()).fieldRules(fieldRulesWithList)
                    .build();
            return builder.outputStreamIdentifier(outputStream.getId()).pipeline(pipeline).build();
        } else {
            PipelineDao graylogPipeline = this.streamPipelineService.createPipeline(title, matchingType, Stream.DEFAULT_STREAM_ID);
            RuleDao pipelineRule = this.streamPipelineService.createPipelineRule(title, fieldRulesWithList, matchingType, filteringStreamIdentifier);
            Pipeline pipeline = Pipeline.builder()
                    .identifier(graylogPipeline.id()).ruleIdentifier(pipelineRule.id()).fieldRules(fieldRulesWithList)
                    .build();
            return builder.outputStreamIdentifier(filteringStreamIdentifier).pipeline(pipeline).build();
        }
    }

    private List<FieldRule> getListFieldRule(List<StreamRule> listStreamRule) {
        List<FieldRule> listFieldRule = new ArrayList<>();
        for (StreamRule streamRule: listStreamRule) {
            if (streamRule.getInverted()) {
                listFieldRule.add(FieldRule.create(streamRule.getId(), streamRule.getField(), -streamRule.getType().toInteger(), streamRule.getValue()));
            } else {
                listFieldRule.add(FieldRule.create(streamRule.getId(), streamRule.getField(), streamRule.getType().toInteger(), streamRule.getValue()));
            }
        }
        return listFieldRule;
    }

    // TODO remove this method => should have a more regular code (empty lists instead of null)!!!
    private <T> Collection<T> nullSafe(Collection<T> c) {
        return (c == null) ? Collections.<T>emptyList() : c;
    }
}
