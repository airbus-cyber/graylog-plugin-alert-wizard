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
import com.google.common.collect.Maps;
import org.apache.commons.lang3.RandomStringUtils;
import org.bson.types.ObjectId;
import org.graylog.plugins.pipelineprocessor.db.*;
import org.graylog.plugins.pipelineprocessor.rest.PipelineConnections;
import org.graylog2.configuration.HttpConfiguration;
import org.graylog2.database.NotFoundException;
import org.graylog2.events.ClusterEventBus;
import org.graylog2.lookup.LookupDefaultMultiValue;
import org.graylog2.lookup.LookupDefaultSingleValue;
import org.graylog2.lookup.adapters.HTTPJSONPathDataAdapter;
import org.graylog2.lookup.caches.NullCache;
import org.graylog2.lookup.db.DBCacheService;
import org.graylog2.lookup.db.DBDataAdapterService;
import org.graylog2.lookup.db.DBLookupTableService;
import org.graylog2.lookup.dto.CacheDto;
import org.graylog2.lookup.dto.DataAdapterDto;
import org.graylog2.lookup.dto.LookupTableDto;
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
    private final DBDataAdapterService dbDataAdapterService;
    private final HttpConfiguration httpConfiguration;
    private final DBCacheService dbCacheService;
    private final DBLookupTableService dbTableService;
    private final PipelineStreamConnectionsService pipelineStreamConnectionsService;

    public StreamPipelineService(StreamService streamService,
                                 StreamRuleService streamRuleService,
                                 ClusterEventBus clusterEventBus,
                                 String indexSetID,
                                 RuleService ruleService,
                                 PipelineService pipelineService,
                                 DBDataAdapterService dbDataAdapterService,
                                 HttpConfiguration httpConfiguration,
                                 DBCacheService dbCacheService,
                                 DBLookupTableService dbTableService,
                                 PipelineStreamConnectionsService pipelineStreamConnectionsService){
        this.streamService = streamService;
        this.streamRuleService = streamRuleService;
        this.clusterEventBus = clusterEventBus;
        this.indexSetID = indexSetID;
        this.ruleService = ruleService;
        this.pipelineService = pipelineService;
        this.dbDataAdapterService = dbDataAdapterService;
        this.httpConfiguration = httpConfiguration;
        this.dbCacheService = dbCacheService;
        this.dbTableService = dbTableService;
        this.pipelineStreamConnectionsService = pipelineStreamConnectionsService;
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

                final StreamRule newStreamRule = streamRuleService.create(streamRuleData);
                streamRuleService.save(newStreamRule);
            }
        }
    }

    private String createStringField(FieldRule fieldRule, String condition) {
        return "  (has_field(\"" + fieldRule.getField() + "\")" + condition + "contains(to_string(lookup_value(\"wizard_lookup\", \"" +
                fieldRule.getValue() + "\", \"\")), to_string($message." + fieldRule.getField() + "), true))\n";
    }

    private String createRuleSource(String alertTitle, List<FieldRule> listfieldRule, Stream stream){
        StringBuilder fields = new StringBuilder();

        int nbList = 0;
        for (FieldRule fieldRule : listfieldRule) {
            if (fieldRule.getType() == 7 || fieldRule.getType() == -7){
                if( nbList > 0) {
                    fields.append("  ");
                    fields.append(stream.getMatchingType());
                }
                nbList++;
                if (fieldRule.getType() == 7) {
                    fields.append(createStringField(fieldRule, " AND "));
                } else {
                    fields.append(createStringField(fieldRule, " AND NOT "));
                }
            }
        }

        return "rule \"function " + alertTitle + "\"\nwhen\n" + fields + "then\n  route_to_stream(\"" + alertTitle + "\", \"" + stream.getId() + "\");\nend";
    }

    public RuleDao createPipelineRule(String alertTitle, List<FieldRule> listfieldRule, Stream stream, String ruleID) {

        final DateTime now = DateTime.now(DateTimeZone.UTC);

        if (ruleID == null) {
            ruleID = RandomStringUtils.random(RANDOM_COUNT, RANDOM_CHARS);
        }
        final RuleDao cr = RuleDao.create(ruleID, "function "+alertTitle, AlertRuleUtils.COMMENT_ALERT_WIZARD, createRuleSource(alertTitle, listfieldRule, stream), now, now);

        final RuleDao save = ruleService.save(cr);

        LOG.debug("Created new rule {}", save);
        return save;
    }

    private String createPipelineStringSource(String alertTitle, String matchingType) {
        String match;
        if (matchingType.equals("OR")){
            match="either";
        }else{
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
        final Stream stream = streamService.create(cr, userName);
        stream.setDisabled(false);

        if (!stream.getIndexSet().getConfig().isWritable()) {
            throw new BadRequestException("Assigned index set must be writable!");
        }
        final String streamID = streamService.save(stream);

        // Create stream rules.
        createStreamRule(alertRuleStream.getFieldRules(), streamID);

        return stream;
    }

    public Stream createOrUpdateSecondStream(AlertRuleStream alertRuleStream, String title, String userName, String conditionType, AlertRule oldAlert) throws ValidationException, NotFoundException {
        if (conditionType.equals("THEN") || conditionType.equals("AND") || conditionType.equals("OR")) {
            if(oldAlert.getSecondStreamID() != null) {
                Stream stream2 = streamService.load(oldAlert.getSecondStreamID());
                updateStream(stream2, alertRuleStream, title+"#2");
                return stream2;
            }else {
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
        streamService.save(stream);

        //TODO do it better (don't destroy if update)
        // Destroy existing stream rules
        for (StreamRule streamRule:stream.getStreamRules()) {
            streamRuleService.destroy(streamRule);
        }
        // Create stream rules.
        createStreamRule(alertRuleStream.getFieldRules(), stream.getId());

        clusterEventBus.post(StreamsChangedEvent.create(stream.getId()));
    }

    public Stream cloneStream(Stream sourceStream, String newTitle, String creatorUser) throws ValidationException {
        LOG.debug("Clone Stream: " + sourceStream.getId());
        // Create stream.
        final Map<String, Object> streamData = Maps.newHashMap();
        streamData.put(StreamImpl.FIELD_TITLE, newTitle);
        streamData.put(StreamImpl.FIELD_DESCRIPTION, AlertRuleUtils.COMMENT_ALERT_WIZARD);
        streamData.put(StreamImpl.FIELD_CREATOR_USER_ID, creatorUser);
        streamData.put(StreamImpl.FIELD_CREATED_AT, Tools.nowUTC());
        streamData.put(StreamImpl.FIELD_MATCHING_TYPE, sourceStream.getMatchingType().toString());
        streamData.put(StreamImpl.FIELD_REMOVE_MATCHES_FROM_DEFAULT_STREAM, sourceStream.getRemoveMatchesFromDefaultStream());
        streamData.put(StreamImpl.FIELD_INDEX_SET_ID, indexSetID);

        final Stream stream = streamService.create(streamData);
        stream.setDisabled(false);

        final String streamID = streamService.save(stream);

        final List<StreamRule> sourceStreamRules = streamRuleService.loadForStream(sourceStream);
        for (StreamRule streamRule : sourceStreamRules) {
            final Map<String, Object> streamRuleData = Maps.newHashMapWithExpectedSize(6);

            streamRuleData.put(StreamRuleImpl.FIELD_TYPE, streamRule.getType().toInteger());
            streamRuleData.put(StreamRuleImpl.FIELD_FIELD, streamRule.getField());
            streamRuleData.put(StreamRuleImpl.FIELD_VALUE, streamRule.getValue());
            streamRuleData.put(StreamRuleImpl.FIELD_INVERTED, streamRule.getInverted());
            streamRuleData.put(StreamRuleImpl.FIELD_STREAM_ID, new ObjectId(streamID));
            streamRuleData.put(StreamRuleImpl.FIELD_DESCRIPTION, streamRule.getDescription());

            final StreamRule newStreamRule = streamRuleService.create(streamRuleData);
            streamRuleService.save(newStreamRule);
        }
        return stream;
    }

    private void deleteStream(Stream stream){
        try {
            if(stream != null) {
                streamService.destroy(stream);
                clusterEventBus.post(StreamsChangedEvent.create(stream.getId()));
                clusterEventBus.post(StreamDeletedEvent.create(stream.getId()));
            }
        } catch (NotFoundException e) {
            LOG.error("Cannot find the stream ", e);
        }
    }

    public void deleteStreamFromID(String streamID){
        try {
            deleteStream(streamService.load(streamID));
        } catch(NotFoundException e) {
            LOG.error("Cannot find the stream ", e);
        }
    }

    public void createUniqueLookup(CacheDto cache, DataAdapterDto adapter) {

        final Collection<LookupTableDto> tables = dbTableService.findAll();
        for (LookupTableDto lookupTableDto:tables) {
            if (lookupTableDto.title().equals("wizard lookup")) {
                return;
            }
        }

        LookupTableDto dto = LookupTableDto.builder()
                .title("wizard lookup")
                .description(AlertRuleUtils.COMMENT_ALERT_WIZARD)
                .name("wizard_lookup")
                .cacheId(cache.id())
                .dataAdapterId(adapter.id())
                .defaultSingleValue("")
                .defaultSingleValueType(LookupDefaultSingleValue.Type.NULL)
                .defaultMultiValue("")
                .defaultMultiValueType(LookupDefaultMultiValue.Type.NULL)
                .build();

        dbTableService.save(dto);
    }

    public CacheDto createUniqueCache() {
        final Collection<CacheDto> caches = dbCacheService.findAll();
        for (CacheDto cacheDto:caches) {
            if(cacheDto.title().equals("wizard cache")){
                return cacheDto;
            }
        }

        final String cacheID = RandomStringUtils.random(RANDOM_COUNT, RANDOM_CHARS);

        NullCache.Config config = NullCache.Config.builder()
                .type("none")
                .build();

        CacheDto dto = CacheDto.builder()
                .id(cacheID)
                .name("wizard-cache")
                .description(AlertRuleUtils.COMMENT_ALERT_WIZARD)
                .title("wizard cache")
                .config(config)
                .build();

        return dbCacheService.save(dto);
    }

    public DataAdapterDto createUniqueDataAdapter(String userName) {

        final Collection<DataAdapterDto> adapters = dbDataAdapterService.findAll();
        for (DataAdapterDto dataAdapters:adapters) {
            if (dataAdapters.title().equals("Wizard data adapter")){
                return dataAdapters;
            }
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic user:password(base64)");

        final String adapterID = RandomStringUtils.random(RANDOM_COUNT, RANDOM_CHARS);
        final String url = httpConfiguration.getHttpPublishUri().resolve(HttpConfiguration.PATH_API).toString() + "plugins/com.airbus_cyber_security.graylog/lists/${key}";

        HTTPJSONPathDataAdapter.Config config = HTTPJSONPathDataAdapter.Config.builder()
                .type("")
                .url(url)
                .singleValueJSONPath("$.lists.lists")
                .multiValueJSONPath("$.lists.lists")
                .userAgent(userName)
                .headers(headers)
                .build();

        DataAdapterDto dto = DataAdapterDto.builder()
                .id(adapterID)
                .title("Wizard data adapter")
                .description(AlertRuleUtils.COMMENT_ALERT_WIZARD)
                .name("wizard-data-adapter")
                .contentPack(null)
                .config(config)
                .build();

        return dbDataAdapterService.save(dto);
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
