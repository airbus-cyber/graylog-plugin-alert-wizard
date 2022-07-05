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

package com.airbus_cyber_security.graylog.wizard.alert.utilities;

import com.airbus_cyber_security.graylog.wizard.alert.*;
import com.airbus_cyber_security.graylog.wizard.database.LookupService;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.RandomStringUtils;
import org.bson.types.ObjectId;
import org.graylog.plugins.pipelineprocessor.db.*;
import org.graylog.plugins.pipelineprocessor.rest.PipelineConnections;
import org.graylog2.database.NotFoundException;
import org.graylog2.events.ClusterEventBus;
import org.graylog2.plugin.Tools;
import org.graylog2.plugin.database.ValidationException;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.plugin.streams.StreamRule;
import org.graylog2.rest.resources.streams.requests.CreateStreamRequest;
import org.graylog2.streams.StreamImpl;
import org.graylog2.streams.StreamRuleImpl;
import org.graylog2.streams.StreamRuleService;
import org.graylog2.streams.StreamService;
import org.graylog2.streams.events.StreamDeletedEvent;
import org.graylog2.streams.events.StreamsChangedEvent;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import java.util.*;

public class StreamPipelineService {

    private static final Logger LOG = LoggerFactory.getLogger(StreamPipelineService.class);
    private static final String RANDOM_CHARS = "0123456789abcdef";
    private static final int RANDOM_COUNT = 24;

    private final StreamService streamService;
    private final StreamRuleService streamRuleService;
    private final ClusterEventBus clusterEventBus;
    private final String indexSetID;
    private final RuleService ruleService;
    private final PipelineService pipelineService;
    private final LookupService lookupService;
    private final PipelineStreamConnectionsService pipelineStreamConnectionsService;

    public StreamPipelineService(StreamService streamService,
                                 StreamRuleService streamRuleService,
                                 ClusterEventBus clusterEventBus,
                                 String indexSetID,
                                 RuleService ruleService,
                                 PipelineService pipelineService,
                                 LookupService lookupService,
                                 PipelineStreamConnectionsService pipelineStreamConnectionsService){
        this.streamService = streamService;
        this.streamRuleService = streamRuleService;
        this.clusterEventBus = clusterEventBus;
        this.indexSetID = indexSetID;
        this.ruleService = ruleService;
        this.pipelineService = pipelineService;
        this.pipelineStreamConnectionsService = pipelineStreamConnectionsService;
        this.lookupService = lookupService;
    }

    private void createStreamRule(List<FieldRule> listfieldRule, String streamID) throws ValidationException {
        for (FieldRule fieldRule:listfieldRule) {
            if (fieldRule.getType() != -7 && fieldRule.getType() != 7) {
                final Map<String, Object> streamRuleData = Maps.newHashMapWithExpectedSize(6);

                if (fieldRule.getType() >= 0) {
                    streamRuleData.put(StreamRuleImpl.FIELD_TYPE, fieldRule.getType());
                    streamRuleData.put(StreamRuleImpl.FIELD_INVERTED, false);
                } else {
                    streamRuleData.put(StreamRuleImpl.FIELD_TYPE, Math.abs(fieldRule.getType()));
                    streamRuleData.put(StreamRuleImpl.FIELD_INVERTED, true);
                }
                streamRuleData.put(StreamRuleImpl.FIELD_FIELD, fieldRule.getField());
                streamRuleData.put(StreamRuleImpl.FIELD_VALUE, fieldRule.getValue());
                streamRuleData.put(StreamRuleImpl.FIELD_STREAM_ID, new ObjectId(streamID));
                streamRuleData.put(StreamRuleImpl.FIELD_DESCRIPTION, AlertRuleUtils.COMMENT_ALERT_WIZARD);

                StreamRule newStreamRule = this.streamRuleService.create(streamRuleData);
                this.streamRuleService.save(newStreamRule);
            }
        }
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

    private String createRuleSource(String alertTitle, List<FieldRule> listfieldRule, Stream stream){
        StringBuilder fields = new StringBuilder();

        int nbList = 0;
        for (FieldRule fieldRule: listfieldRule) {
            if (fieldRule.getType() == 7 || fieldRule.getType() == -7) {
                if (nbList > 0) {
                    fields.append("  ");
                    fields.append(stream.getMatchingType());
                }
                nbList++;
                boolean negate = (fieldRule.getType() == -7);
                fields.append(createStringField(fieldRule, negate));
            }
        }

        return "rule \"function " + alertTitle + "\"\nwhen\n" + fields + "then\n  route_to_stream(\"" + alertTitle + "\", \"" + stream.getId() + "\");\nend";
    }

    public RuleDao createPipelineRule(String alertTitle, List<FieldRule> listfieldRule, Stream stream, String ruleID) {
        DateTime now = DateTime.now(DateTimeZone.UTC);

        if (ruleID == null) {
            // TODO factor the random utility (with LookupService)!!!
            ruleID = RandomStringUtils.random(RANDOM_COUNT, RANDOM_CHARS);
        }
        String ruleSource = createRuleSource(alertTitle, listfieldRule, stream);
        RuleDao cr = RuleDao.create(ruleID, "function " + alertTitle, AlertRuleUtils.COMMENT_ALERT_WIZARD, ruleSource, now, now);

        return ruleService.save(cr);
    }

    private String createPipelineStringSource(String alertTitle, String matchingType) {
        String match;
        if (matchingType.equals("OR")) {
            match="either";
        } else {
            match="all";
        }
        return "pipeline \""+alertTitle+"\"\nstage 0 match "+match+"\nrule \"function "+alertTitle+"\"\nend";
    }

    public PipelineDao createPipeline(String alertTitle, String pipelineID, String matchingType) {

        final DateTime now = DateTime.now(DateTimeZone.UTC);

        if (pipelineID == null) {
            pipelineID = RandomStringUtils.random(RANDOM_COUNT, RANDOM_CHARS);
        }
        final PipelineDao cr = PipelineDao.create(pipelineID, alertTitle, AlertRuleUtils.COMMENT_ALERT_WIZARD, createPipelineStringSource(alertTitle, matchingType), now, now);
        final PipelineDao save = pipelineService.save(cr);

        Set<String> pipelineIds;
        try {
            pipelineIds = pipelineStreamConnectionsService.load("000000000000000000000001").pipelineIds();
        } catch (NotFoundException e) {
            pipelineIds =  new HashSet<>();
        }
        pipelineIds.add(save.id());
        pipelineStreamConnectionsService.save(PipelineConnections.create(null, "000000000000000000000001", pipelineIds));

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

    private Stream createStream(AlertRuleStream alertRuleStream, String title, String userName) throws ValidationException {
        LOG.debug("Create Stream: " + title);
        final CreateStreamRequest cr = CreateStreamRequest.create(title, AlertRuleUtils.COMMENT_ALERT_WIZARD,
                Collections.emptyList(), "", alertRuleStream.getMatchingType(), false, indexSetID);
        Stream stream = this.streamService.create(cr, userName);
        stream.setDisabled(false);

        if (!stream.getIndexSet().getConfig().isWritable()) {
            throw new BadRequestException("Assigned index set must be writable!");
        }
        final String streamID = this.streamService.save(stream);

        // Create stream rules.
        createStreamRule(alertRuleStream.getFieldRules(), streamID);

        return stream;
    }

    public Stream createOrUpdateSecondStream(AlertRuleStream alertRuleStream, String title, String userName, String conditionType, AlertRule oldAlert) throws ValidationException, NotFoundException {
        if (conditionType.equals("THEN") || conditionType.equals("AND") || conditionType.equals("OR")) {
            if (oldAlert.getSecondStreamID() != null) {
                Stream stream2 = this.streamService.load(oldAlert.getSecondStreamID());
                updateStream(stream2, alertRuleStream, title+"#2");
                return stream2;
            } else {
                return createStream(alertRuleStream, title+"#2", userName);
            }
            //Delete old stream if one
        } else if (oldAlert.getSecondStreamID() != null && !oldAlert.getSecondStreamID().isEmpty()) {
            deleteStreamFromID(oldAlert.getSecondStreamID());
        }
        return null;
    }

    public void updateStream(Stream stream, AlertRuleStream alertRuleStream, String title) throws ValidationException {
        LOG.debug("Update Stream: " + stream.getId());
        stream.setTitle(title);
        if (alertRuleStream.getMatchingType() != null) {
            try {
                stream.setMatchingType(Stream.MatchingType.valueOf(alertRuleStream.getMatchingType()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid matching type '" + alertRuleStream.getMatchingType()
                        + "' specified. Should be one of: " + Arrays.toString(Stream.MatchingType.values()));
            }
        }
        this.streamService.save(stream);

        //TODO do it better (don't destroy if update)
        // Destroy existing stream rules
        for (StreamRule streamRule:stream.getStreamRules()) {
            this.streamRuleService.destroy(streamRule);
        }
        // Create stream rules.
        createStreamRule(alertRuleStream.getFieldRules(), stream.getId());

        this.clusterEventBus.post(StreamsChangedEvent.create(stream.getId()));
    }

    private void deleteStream(Stream stream){
        try {
            if(stream != null) {
                this.streamService.destroy(stream);
                this.clusterEventBus.post(StreamsChangedEvent.create(stream.getId()));
                this.clusterEventBus.post(StreamDeletedEvent.create(stream.getId()));
            }
        } catch (NotFoundException e) {
            LOG.error("Cannot find the stream ", e);
        }
    }

    public void deleteStreamFromID(String streamID){
        try {
            deleteStream(this.streamService.load(streamID));
        } catch(NotFoundException e) {
            LOG.error("Cannot find the stream ", e);
        }
    }

    private List<FieldRule> extractPipelineFieldRules(List<FieldRule> listFieldRule){
        List<FieldRule> listPipelineFieldRule = new ArrayList<>();
        for (FieldRule fieldRule : listFieldRule) {
            if (fieldRule.getType() == 7 || fieldRule.getType() == -7) {
                listPipelineFieldRule.add(fieldRule);
            }
        }
        return listPipelineFieldRule;
    }

    private StreamPipelineObject createPipelineAndRule(Stream stream, String alertTitle, List<FieldRule> listfieldRule, String matchingType){
        String pipelineID = null;
        String pipelineRuleID = null;
        List<FieldRule> listPipelineFieldRule = extractPipelineFieldRules(listfieldRule);
        if (!listPipelineFieldRule.isEmpty()) {
            RuleDao pipelineRule = createPipelineRule(alertTitle, listPipelineFieldRule, stream, null);
            PipelineDao pipeline = createPipeline(alertTitle, null, matchingType);
            pipelineID = pipeline.id();
            pipelineRuleID = pipelineRule.id();
        }
        return new StreamPipelineObject(stream, pipelineID, pipelineRuleID, listPipelineFieldRule);
    }

    // TODO remove this method and remove stream from the StreamPipelineObject (which I am not very fond of)
    public StreamPipelineObject createStreamAndPipeline(AlertRuleStream alertRuleStream, String alertTitle, String userName, String matchingType)
            throws ValidationException {
        Stream stream = createStream(alertRuleStream, alertTitle, userName);
        return createPipelineAndRule(stream, alertTitle, alertRuleStream.getFieldRules(), matchingType);
    }

    public StreamPipelineObject updatePipeline(String alertTitle, String pipelineID, String pipelineRuleID, List<FieldRule> listfieldRule, Stream stream, String matchingType) {
        deletePipeline(pipelineID, pipelineRuleID);
        return createPipelineAndRule(stream, alertTitle, listfieldRule, matchingType);
    }

}
