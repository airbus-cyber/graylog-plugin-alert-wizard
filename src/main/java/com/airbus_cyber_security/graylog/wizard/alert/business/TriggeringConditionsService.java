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
    private final StreamFacade streamService;
    private final AlertListUtilsService alertListUtilsService;
    private final FieldRulesUtilities fieldRulesUtilities;

    @Inject
    public TriggeringConditionsService(StreamFacade streamService,
                                       StreamPipelineService streamPipelineService,
                                       AlertListUtilsService alertListUtilsService,
                                       FieldRulesUtilities fieldRulesUtilities) {
        this.streamService = streamService;
        this.streamPipelineService = streamPipelineService;

        this.alertListUtilsService = alertListUtilsService;
        this.fieldRulesUtilities = fieldRulesUtilities;
    }

    private String createFilteringStream(AlertRuleStream streamConfiguration, String title, String userName, boolean disabled) throws ValidationException {
        List<FieldRule> streamFieldRules = this.streamService.getStreamFieldRules(streamConfiguration.getFieldRules());
        if (streamFieldRules.isEmpty()) {
            return null;
        }
        Stream filteringStream = this.streamService.createStream(streamConfiguration.getMatchingType(), title, userName, streamFieldRules, disabled);
        return filteringStream.getId();
    }

    public TriggeringConditions createTriggeringConditions(AlertRuleStream streamConfiguration, String title, String userName, boolean disabled) throws ValidationException {
        String filteringStreamIdentifier = this.createFilteringStream(streamConfiguration, title, userName, disabled);
        return createTriggeringConditionsFromStream(streamConfiguration, title, filteringStreamIdentifier, userName, disabled);
    }

    private String updateFilteringString(TriggeringConditions previousConditions, String title,
                                         AlertRuleStream streamConfiguration, String userName, boolean disabled) throws ValidationException {
        List<FieldRule> streamFieldRules = this.streamService.getStreamFieldRules(streamConfiguration.getFieldRules());
        String previousFilteringStreamIdentifier = previousConditions.filteringStreamIdentifier();
        if (previousFilteringStreamIdentifier == null) {
            return this.createFilteringStream(streamConfiguration, title, userName, disabled);
        }
        if (streamFieldRules.isEmpty()) {
            this.streamPipelineService.deleteStreamFromIdentifier(previousFilteringStreamIdentifier);
            return null;
        }
        Stream stream = this.streamPipelineService.loadStream(previousFilteringStreamIdentifier);
        this.streamService.updateStream(stream, streamConfiguration, title);
        return previousFilteringStreamIdentifier;
    }

    public TriggeringConditions updateTriggeringConditions(TriggeringConditions previousConditions, String title,
                                                            AlertRuleStream streamConfiguration, String userName, boolean disabled) throws ValidationException {
        String filteringStreamIdentifier = this.updateFilteringString(previousConditions, title, streamConfiguration, userName, disabled);

        // second part of the condition here is probably incorrect...
        if (previousConditions.outputStreamIdentifier() != null
                && !previousConditions.outputStreamIdentifier().equals(previousConditions.filteringStreamIdentifier())) {
            this.streamPipelineService.deleteStreamFromIdentifier(previousConditions.outputStreamIdentifier());
        }
        deletePipelineIfAny(previousConditions.pipeline());

        return createTriggeringConditionsFromStream(streamConfiguration, title, filteringStreamIdentifier, userName, disabled);
    }

    public void deleteTriggeringConditions(TriggeringConditions conditions) {
        if (conditions.filteringStreamIdentifier() != null) {
            this.streamPipelineService.deleteStreamFromIdentifier(conditions.filteringStreamIdentifier());
        }
        if (conditions.outputStreamIdentifier() != null && !conditions.outputStreamIdentifier().equals(conditions.filteringStreamIdentifier())) {
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
                                                                      String filteringStreamIdentifier, String userName, boolean disabled) throws ValidationException {
        List<FieldRule> fieldRulesWithList = this.streamPipelineService.extractPipelineFieldRules(streamConfiguration.getFieldRules());

        TriggeringConditions.Builder builder = TriggeringConditions.builder().filteringStreamIdentifier(filteringStreamIdentifier);
        Stream.MatchingType matchingType = streamConfiguration.getMatchingType();
        builder.matchingType(matchingType);
        if (fieldRulesWithList.isEmpty()) {
            String outputStreamIdentifier;
            if (filteringStreamIdentifier == null) {
                outputStreamIdentifier = null;
            } else {
                outputStreamIdentifier = filteringStreamIdentifier;
            }
            return builder.outputStreamIdentifier(outputStreamIdentifier).build();
        }

        for (FieldRule fieldRule: fieldRulesWithList) {
            this.alertListUtilsService.incrementUsage(fieldRule.getValue());
        }

        if (!this.fieldRulesUtilities.hasStreamRules(streamConfiguration.getFieldRules())) {
            PipelineDao graylogPipeline = this.streamPipelineService.createPipeline(title, matchingType, Stream.DEFAULT_STREAM_ID);
            Stream outputStream = this.streamService.createStream(matchingType, title + " output", userName, disabled);
            RuleDao pipelineRule = this.streamPipelineService.createPipelineRule(title, fieldRulesWithList, matchingType, outputStream.getId());
            Pipeline pipeline = Pipeline.builder()
                    .identifier(graylogPipeline.id()).ruleIdentifier(pipelineRule.id()).fieldRules(fieldRulesWithList)
                    .build();
            return builder.outputStreamIdentifier(outputStream.getId()).pipeline(pipeline).build();
        } else if (matchingType.equals(Stream.MatchingType.OR)) {
            PipelineDao graylogPipeline = this.streamPipelineService.createPipeline(title, matchingType, Stream.DEFAULT_STREAM_ID);
            RuleDao pipelineRule = this.streamPipelineService.createPipelineRule(title, fieldRulesWithList, matchingType, filteringStreamIdentifier);
            Pipeline pipeline = Pipeline.builder()
                    .identifier(graylogPipeline.id()).ruleIdentifier(pipelineRule.id()).fieldRules(fieldRulesWithList)
                    .build();
            return builder.outputStreamIdentifier(filteringStreamIdentifier).pipeline(pipeline).build();
        } else {
            PipelineDao graylogPipeline = this.streamPipelineService.createPipeline(title, matchingType, filteringStreamIdentifier);
            Stream outputStream = this.streamService.createStream(matchingType, title + " output", userName, disabled);
            RuleDao pipelineRule = this.streamPipelineService.createPipelineRule(title, fieldRulesWithList, matchingType, outputStream.getId());
            Pipeline pipeline = Pipeline.builder()
                    .identifier(graylogPipeline.id()).ruleIdentifier(pipelineRule.id()).fieldRules(fieldRulesWithList)
                    .build();
            return builder.outputStreamIdentifier(outputStream.getId()).pipeline(pipeline).build();
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
