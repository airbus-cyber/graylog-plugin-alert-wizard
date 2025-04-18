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
import com.airbus_cyber_security.graylog.wizard.database.Description;
import com.airbus_cyber_security.graylog.wizard.database.LookupService;
import org.apache.commons.lang3.RandomStringUtils;
import org.graylog.plugins.pipelineprocessor.db.PipelineDao;
import org.graylog.plugins.pipelineprocessor.db.PipelineService;
import org.graylog.plugins.pipelineprocessor.db.PipelineStreamConnectionsService;
import org.graylog.plugins.pipelineprocessor.db.RuleDao;
import org.graylog.plugins.pipelineprocessor.db.RuleService;
import org.graylog.plugins.pipelineprocessor.rest.PipelineConnections;
import org.graylog2.database.NotFoundException;
import org.graylog2.events.ClusterEventBus;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.streams.StreamGuardException;
import org.graylog2.streams.StreamService;
import org.graylog2.streams.events.StreamDeletedEvent;
import org.graylog2.streams.events.StreamsChangedEvent;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// TODO move stream related code into StreamFacade
//      and rename to PipelineFacade
public class StreamPipelineService {

    private static final Logger LOG = LoggerFactory.getLogger(StreamPipelineService.class);
    private static final String RANDOM_CHARS = "0123456789abcdef";
    private static final int RANDOM_COUNT = 24;

    private final StreamService streamService;
    private final ClusterEventBus clusterEventBus;
    private final RuleService ruleService;
    private final PipelineService pipelineService;
    private final LookupService lookupService;
    private final PipelineStreamConnectionsService pipelineStreamConnectionsService;
    private final FieldRulesUtilities fieldRulesUtilities;

    @Inject
    public StreamPipelineService(StreamService streamService,
                                 ClusterEventBus clusterEventBus,
                                 RuleService ruleService,
                                 PipelineService pipelineService,
                                 LookupService lookupService,
                                 PipelineStreamConnectionsService pipelineStreamConnectionsService,
                                 FieldRulesUtilities fieldRulesUtilities) {
        this.streamService = streamService;
        this.clusterEventBus = clusterEventBus;
        this.ruleService = ruleService;
        this.pipelineService = pipelineService;
        this.pipelineStreamConnectionsService = pipelineStreamConnectionsService;
        this.lookupService = lookupService;
        this.fieldRulesUtilities = fieldRulesUtilities;
    }

    private String createStringField(FieldRule fieldRule, boolean negate) {
        String rule = "  (";
        rule += "has_field(\"" + fieldRule.getField() + "\")";
        rule += " AND ";
        if (negate) {
            rule += "NOT ";
        }
        String lookupTableName = this.lookupService.getLookupTableName(fieldRule.getValue());
        rule += "is_not_null(lookup_value(\"" + lookupTableName + "\",$message." + fieldRule.getField() + "))";
        return rule + ")\n";
    }

    private String createRuleSource(String alertTitle, List<FieldRule> listfieldRule, Stream.MatchingType matchingType, String targetStreamIdentifier){
        StringBuilder fields = new StringBuilder();

        int nbList = 0;
        for (FieldRule fieldRule: listfieldRule) {
            if (!this.fieldRulesUtilities.isListFieldRule(fieldRule)) {
                continue;
            }
            if (nbList > 0) {
                fields.append("  ");
                fields.append(matchingType.name());
            }
            nbList++;
            boolean negate = this.fieldRulesUtilities.hasTypeNotInList(fieldRule);
            fields.append(createStringField(fieldRule, negate));
        }

        return "rule \"function " + alertTitle + "\"\nwhen\n" + fields + "then\n  route_to_stream(\"" + alertTitle + "\", \"" + targetStreamIdentifier + "\");\nend";
    }

    public RuleDao createPipelineRule(String alertTitle, List<FieldRule> listfieldRule, Stream.MatchingType matchingType, String targetStreamIdentifier) {
        DateTime now = DateTime.now(DateTimeZone.UTC);

        String ruleID = RandomStringUtils.random(RANDOM_COUNT, RANDOM_CHARS);
        String ruleSource = createRuleSource(alertTitle, listfieldRule, matchingType, targetStreamIdentifier);
        RuleDao cr = RuleDao.create(ruleID, "function " + alertTitle, Description.COMMENT_ALERT_WIZARD, ruleSource, now, now, null, null);

        return ruleService.save(cr);
    }

    private String createPipelineStringSource(String alertTitle, Stream.MatchingType matchingType) {
        String match;
        if (matchingType.equals(Stream.MatchingType.OR)) {
            match="either";
        } else {
            match="all";
        }
        return "pipeline \""+alertTitle+"\"\nstage 0 match "+match+"\nrule \"function "+alertTitle+"\"\nend";
    }

    public PipelineDao createPipeline(String title, Stream.MatchingType matchingType, String inputStreamIdentifier) {
        DateTime now = DateTime.now(DateTimeZone.UTC);

        String pipelineID = RandomStringUtils.random(RANDOM_COUNT, RANDOM_CHARS);
        PipelineDao cr = PipelineDao.create(pipelineID, title, Description.COMMENT_ALERT_WIZARD, createPipelineStringSource(title, matchingType), now, now);
        PipelineDao save = pipelineService.save(cr);

        Set<String> pipelineIds;
        try {
            // retrieves the identifiers of the pipelines connected to the input stream
            pipelineIds = pipelineStreamConnectionsService.load(inputStreamIdentifier).pipelineIds();
        } catch (NotFoundException e) {
            pipelineIds = new HashSet<>();
        }
        // add the identifier of the new pipeline
        pipelineIds.add(save.id());
        // and updates the pipeline connection
        pipelineStreamConnectionsService.save(PipelineConnections.create(null, inputStreamIdentifier, pipelineIds));

        LOG.debug("Created new pipeline {}", save);
        return save;
    }

    public void deletePipeline(String pipelineID, String ruleID){
        if (pipelineID != null && !pipelineID.isEmpty()) {
            pipelineService.delete(pipelineID);
        }
        if (ruleID != null && !ruleID.isEmpty()) {
            ruleService.delete(ruleID);
        }
    }

    public void deleteStreamFromIdentifier(String streamIdentifier){
        try {
            Stream stream = this.streamService.load(streamIdentifier);
            this.streamService.destroy(stream);
            this.clusterEventBus.post(StreamsChangedEvent.create(stream.getId()));
            this.clusterEventBus.post(StreamDeletedEvent.create(stream.getId()));
        } catch(NotFoundException | StreamGuardException e) {
            LOG.debug("Couldn't find the stream when deleting", e);
        }
    }

    // TODO maybe should just split (in Conversions) the fieldRules into stream field rules and list field rules
    public List<FieldRule> extractPipelineFieldRules(List<FieldRule> listFieldRule){
        List<FieldRule> listPipelineFieldRule = new ArrayList<>();
        for (FieldRule fieldRule: listFieldRule) {
            if (!this.fieldRulesUtilities.isListFieldRule(fieldRule)) {
                continue;
            }
            listPipelineFieldRule.add(fieldRule);
        }
        return listPipelineFieldRule;
    }

    public Stream loadStream(String streamIdentifier) {
        try {
            return this.streamService.load(streamIdentifier);
        } catch (NotFoundException e) {
            // this may happen if the underlying stream was deleted
            // see test test_get_all_rules_should_not_fail_when_a_stream_is_deleted_issue105 and related issue
            // TODO in this case, maybe the rule should rather be converted into a corrupted rule than this aspect being handled by the interface?
            return null;
        }
    }
}
