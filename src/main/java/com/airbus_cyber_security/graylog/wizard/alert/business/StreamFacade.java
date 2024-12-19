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
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.AlertRuleStream;
import com.airbus_cyber_security.graylog.wizard.database.Description;
import com.google.common.collect.Maps;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import org.bson.types.ObjectId;
import org.graylog2.events.ClusterEventBus;
import org.graylog2.indexer.IndexSetRegistry;
import org.graylog2.plugin.database.ValidationException;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.plugin.streams.StreamRule;
import org.graylog2.rest.resources.streams.requests.CreateStreamRequest;
import org.graylog2.streams.StreamRuleImpl;
import org.graylog2.streams.StreamRuleService;
import org.graylog2.streams.events.StreamsChangedEvent;
import org.graylog2.streams.StreamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class StreamFacade {

    private static final Logger LOG = LoggerFactory.getLogger(StreamFacade.class);

    private final StreamService streamService;
    private final String indexSetID;
    private final StreamRuleService streamRuleService;
    private final ClusterEventBus clusterEventBus;
    private final FieldRulesUtilities fieldRulesUtilities;

    @Inject
    public StreamFacade(org.graylog2.streams.StreamService streamService,
                        StreamRuleService streamRuleService,
                        ClusterEventBus clusterEventBus,
                        IndexSetRegistry indexSetRegistry,
                        FieldRulesUtilities fieldRulesUtilities) {
        this.streamService = streamService;
        this.streamRuleService = streamRuleService;
        this.clusterEventBus = clusterEventBus;
        this.fieldRulesUtilities = fieldRulesUtilities;
        this.indexSetID = indexSetRegistry.getDefault().getConfig().id();
    }

    public Stream createStream(Stream.MatchingType matchingType, String title, String userName, List<FieldRule> fieldRules, boolean disabled) throws ValidationException {
        Stream stream = this.createStream(matchingType, title, userName, disabled);
        this.createStreamRule(fieldRules, stream.getId());
        return stream;
    }

    public Stream createStream(Stream.MatchingType matchingType, String title, String userName, boolean disabled) throws ValidationException {
        LOG.debug("Create Stream: " + title);
        CreateStreamRequest request = CreateStreamRequest.create(title, Description.COMMENT_ALERT_WIZARD,
                Collections.emptyList(), "", matchingType.name(), false, indexSetID);
        Stream stream = this.streamService.create(request, userName);
        stream.setDisabled(disabled);

        if (!stream.getIndexSet().getConfig().isWritable()) {
            throw new BadRequestException("Assigned index set must be writable!");
        }
        this.streamService.save(stream);

        return stream;
    }

    public void updateStream(Stream stream, AlertRuleStream alertRuleStream, String title) throws ValidationException {
        LOG.debug("Update Stream: " + stream.getId());
        stream.setTitle(title);
        try {
            stream.setMatchingType(alertRuleStream.getMatchingType());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid matching type '" + alertRuleStream.getMatchingType()
                    + "' specified. Should be one of: " + Arrays.toString(Stream.MatchingType.values()));
        }
        this.streamService.save(stream);

        //TODO do it better (don't destroy if update)
        // Destroy existing stream rules
        for (StreamRule streamRule: stream.getStreamRules()) {
            this.streamRuleService.destroy(streamRule);
        }
        // Create stream rules.
        List<FieldRule> streamFieldRules = this.getStreamFieldRules(alertRuleStream.getFieldRules());
        createStreamRule(streamFieldRules, stream.getId());

        this.clusterEventBus.post(StreamsChangedEvent.create(stream.getId()));
    }

    private void createStreamRule(List<FieldRule> fieldRules, String streamID) throws ValidationException {
        for (FieldRule fieldRule: fieldRules) {
            Map<String, Object> streamRuleData = Maps.newHashMapWithExpectedSize(6);

            if (fieldRule.getType() >= 0) {
                streamRuleData.put(StreamRuleImpl.FIELD_TYPE, fieldRule.getType());
                streamRuleData.put(StreamRuleImpl.FIELD_INVERTED, false);
            } else {
                streamRuleData.put(StreamRuleImpl.FIELD_TYPE, innerAbs(fieldRule.getType()));
                streamRuleData.put(StreamRuleImpl.FIELD_INVERTED, true);
            }
            streamRuleData.put(StreamRuleImpl.FIELD_FIELD, fieldRule.getField());
            streamRuleData.put(StreamRuleImpl.FIELD_VALUE, fieldRule.getValue());
            streamRuleData.put(StreamRuleImpl.FIELD_STREAM_ID, new ObjectId(streamID));
            streamRuleData.put(StreamRuleImpl.FIELD_DESCRIPTION, Description.COMMENT_ALERT_WIZARD);

            StreamRule newStreamRule = this.streamRuleService.create(streamRuleData);
            this.streamRuleService.save(newStreamRule);
        }
    }

    // TODO most probably move up (this is a rather a conversion in the rest layer)
    public List<FieldRule> getStreamFieldRules(List<FieldRule> fieldRules) {
        List<FieldRule> streamFieldRules = new ArrayList<FieldRule>();
        for (FieldRule fieldRule: fieldRules) {
            if (this.fieldRulesUtilities.isListFieldRule(fieldRule)) {
                continue;
            }
            streamFieldRules.add(fieldRule);
        }
        return streamFieldRules;
    }

    private int innerAbs(int value) {
        if (value < 0) {
            return -value;
        } else {
            return value;
        }
    }
}
