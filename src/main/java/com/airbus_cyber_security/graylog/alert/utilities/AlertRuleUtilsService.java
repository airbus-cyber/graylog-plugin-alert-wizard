package com.airbus_cyber_security.graylog.alert.utilities;

import com.airbus_cyber_security.graylog.alert.*;
import com.airbus_cyber_security.graylog.alert.rest.models.requests.AlertRuleRequest;
import com.airbus_cyber_security.graylog.alert.rest.models.responses.GetDataAlertRule;
import com.airbus_cyber_security.graylog.config.LoggingAlertConfig;
import com.google.common.collect.Maps;
import org.bson.types.ObjectId;
import org.graylog2.alarmcallbacks.AlarmCallbackConfiguration;
import org.graylog2.alarmcallbacks.AlarmCallbackConfigurationImpl;
import org.graylog2.alarmcallbacks.AlarmCallbackConfigurationService;
import org.graylog2.alarmcallbacks.AlarmCallbackFactory;
import org.graylog2.alerts.Alert;
import org.graylog2.alerts.AlertService;
import org.graylog2.database.NotFoundException;
import org.graylog2.events.ClusterEventBus;
import org.graylog2.plugin.Tools;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.database.ValidationException;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.plugin.streams.StreamRule;
import org.graylog2.rest.models.alarmcallbacks.requests.CreateAlarmCallbackRequest;
import org.graylog2.rest.models.streams.alerts.requests.CreateConditionRequest;
import org.graylog2.rest.resources.streams.requests.CreateStreamRequest;
import org.graylog2.streams.StreamImpl;
import org.graylog2.streams.StreamRuleImpl;
import org.graylog2.streams.StreamRuleService;
import org.graylog2.streams.StreamService;
import org.graylog2.streams.events.StreamDeletedEvent;
import org.graylog2.streams.events.StreamsChangedEvent;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AlertRuleUtilsService {

    private static final Logger LOG = LoggerFactory.getLogger(AlertRuleUtilsService.class);

    private static final String ERROR_ALARM_CONDITION_CONFIGURATION = "Invalid alarm Condition configuration.";
    public static final String ERROR_ALARM_CALLBACK_CONFIGURATION = "Invalid alarm callback configuration.";
    private static final String ERROR_ALARM_CALLBACK_TYPE = "Invalid alarm callback type.";

    private final AlertRuleService alertRuleService;
    private final StreamService streamService;
    private final StreamRuleService streamRuleService;
    private final ClusterEventBus clusterEventBus;
    private final AlertService alertService;
    private final AlarmCallbackConfigurationService alarmCallbackConfigurationService;
    private final AlarmCallbackFactory alarmCallbackFactory;
    private final ClusterConfigService clusterConfigService;
    private final String indexSetID;
    private final AlertRuleUtils alertRuleUtils;

    public AlertRuleUtilsService(AlertRuleService alertRuleService,
                                 StreamService streamService,
                                 StreamRuleService streamRuleService,
                                 ClusterEventBus clusterEventBus,
                                 String indexSetID,
                                 AlertService alertService,
                                 AlarmCallbackConfigurationService alarmCallbackConfigurationService,
                                 AlarmCallbackFactory alarmCallbackFactory,
                                 ClusterConfigService clusterConfigService,
                                 AlertRuleUtils alertRuleUtils) {
        this.alertRuleService = alertRuleService;
        this.streamService = streamService;
        this.streamRuleService = streamRuleService;
        this.clusterEventBus = clusterEventBus;
        this.alertService = alertService;
        this.alarmCallbackConfigurationService = alarmCallbackConfigurationService;
        this.alarmCallbackFactory = alarmCallbackFactory;
        this.clusterConfigService = clusterConfigService;
        this.indexSetID = indexSetID;
        this.alertRuleUtils = alertRuleUtils;
    }

    public void checkIsValidRequest(AlertRuleRequest request){
        if(!alertRuleService.isValidRequest(request)){
            LOG.error("Invalid alert rule request");
            throw new BadRequestException("Invalid alert rule request.");
        }
    }

    private int countAlerts(String streamID, DateTime since){
        final List<Alert> alerts = alertService.loadRecentOfStream(streamID, since, 999);
        return alerts.size();
    }

    private void createStreamRule(List<FieldRuleImpl> listfieldRule, String streamID) throws ValidationException {
        for (FieldRule fieldRule:listfieldRule) {
            final Map<String, Object> streamRuleData = Maps.newHashMapWithExpectedSize(6);

            if(fieldRule.getType() >= 0){
                streamRuleData.put(StreamRuleImpl.FIELD_TYPE, fieldRule.getType());
                streamRuleData.put(StreamRuleImpl.FIELD_INVERTED, false);
            }else{
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

    public Stream createStream(AlertRuleStream alertRuleStream, String title, String userName) throws ValidationException {

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
        if(conditionType.equals("THEN") || conditionType.equals("AND") || conditionType.equals("OR")) {
            if(oldAlert.getSecondStreamID() != null) {
                Stream stream2 = streamService.load(oldAlert.getSecondStreamID());
                updateStream(stream2, alertRuleStream, title+"#2");
                // If request condition is not "OR" and the old one is "OR" remove stream condition and notification
                if(!conditionType.equals("OR") && oldAlert.getConditionType().equals("OR")) {
                    removeConditionAndNotificationFromStream(stream2);
                }
                return stream2;
            }else {
                return createStream(alertRuleStream, title+"#2", userName);
            }
        //Delete old stream if one
        }else if(oldAlert.getSecondStreamID() != null && !oldAlert.getSecondStreamID().isEmpty()) {
            deleteStreamFromID(oldAlert.getSecondStreamID());
        }
        return null;
    }

    public void updateStream(Stream stream, AlertRuleStream alertRuleStream, String title) throws ValidationException {

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



    public String updateCondition(Stream stream, AlertCondition oldAlertCondition, String title, String alertRuleCondType,
                                  Map<String, Object> alertRuleCondParameters, String streamID2, String userName) throws ValidationException {
        String alertConditionID = oldAlertCondition.getId();
        String conditionType = alertRuleUtils.getGraylogConditionType(alertRuleCondType);
        Map<String, Object> parameters = alertRuleUtils.getConditionParameters(streamID2, alertRuleCondType, alertRuleCondParameters);
        try {
            CreateConditionRequest ccr = CreateConditionRequest.create(conditionType , title, parameters);
            //If same condition type update
            if(oldAlertCondition.getType().equals(conditionType)) {
                final AlertCondition updatedCondition = alertService.updateFromRequest(oldAlertCondition, ccr);
                streamService.updateAlertCondition(stream, updatedCondition);
            }else {
                streamService.removeAlertCondition(stream, alertConditionID);
                final AlertCondition newAlertCondition = alertService.fromRequest(ccr, stream, userName);
                streamService.addAlertCondition(stream, newAlertCondition);
                alertConditionID = newAlertCondition.getId();
            }
        } catch (ConfigurationException e) {
            throw new BadRequestException("Invalid alert condition parameters", e);
        }
        return alertConditionID;
    }

    public  Stream cloneStream(Stream sourceStream, String newTitle, String creatorUser) throws NotFoundException, ValidationException {
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
        try{
            deleteStream(streamService.load(streamID));
        }catch(NotFoundException e){
            LOG.error("Cannot find the stream ", e);
        }
    }

    public void cleanAlertRule(Stream mainStream, Stream secondStream) {
        deleteStream(mainStream);
        deleteStream(secondStream);
    }

    public GetDataAlertRule constructDataAlertRule(AlertRule alert) throws NotFoundException {
        final String streamID = alert.getStreamID();
        final Stream stream = streamService.load(streamID);
        final AlertCondition alertCondition = streamService.getAlertCondition(stream, alert.getConditionID());
        Map<String, Object> parametersCondition = Maps.newHashMap();
        parametersCondition.putAll(alertCondition.getParameters());
        if(alert.getConditionType().equals("THEN") || alert.getConditionType().equals("AND")) {
            parametersCondition.put(AlertRuleUtils.THRESHOLD, parametersCondition.remove(AlertRuleUtils.MAIN_THRESHOLD));
            parametersCondition.put(AlertRuleUtils.THRESHOLD_TYPE, parametersCondition.remove(AlertRuleUtils.MAIN_THRESHOLD_TYPE));
        }

        AlertRuleStream alertRuleStream = AlertRuleStreamImpl.create(streamID,
                stream.getMatchingType().toString(), alertRuleUtils.getListFieldRule(stream.getStreamRules()));

        AlertRuleStream alertRuleStream2 = null;
        if(alert.getSecondStreamID() != null && !alert.getSecondStreamID().isEmpty()) {
            final Stream stream2 = streamService.load(alert.getSecondStreamID());
            alertRuleStream2 = AlertRuleStreamImpl.create(alert.getSecondStreamID(),
                    stream2.getMatchingType().toString(), alertRuleUtils.getListFieldRule(stream2.getStreamRules()));
        }

        final AlarmCallbackConfiguration callbackConfiguration = alarmCallbackConfigurationService.load(alert.getNotificationID());
        String severity = "";
        if(callbackConfiguration != null) {
            severity = callbackConfiguration.getConfiguration().getOrDefault(AlertRuleUtils.SEVERITY, "").toString();
        }

        return GetDataAlertRule.create(alert.getTitle(), alertCondition.getTitle(),
                severity,
                alert.getConditionID(),
                alert.getNotificationID(),
                alert.getCreatedAt(),
                alert.getCreatorUserId(),
                alert.getLastModified(),
                stream.getDisabled(),
                alert.getDescription(),
                countAlerts(streamID, alert.getLastModified()),
                alert.getConditionType(),
                parametersCondition,
                alertRuleStream,
                alertRuleStream2);
    }

    private void removeConditionFromStream(Stream stream) {
        List <AlertCondition> listAlertCondition = streamService.getAlertConditions(stream);
        if(listAlertCondition != null && !listAlertCondition.isEmpty()) {
            for (AlertCondition alertCondition : listAlertCondition) {
                streamService.removeAlertCondition(stream, alertCondition.getId());
            }
        }
    }
    public void removeNotificationFromStream(Stream stream) {
        List <AlarmCallbackConfiguration> listAlarmCallbackConfiguration = alarmCallbackConfigurationService.getForStream(stream);
        if(listAlarmCallbackConfiguration != null && !listAlarmCallbackConfiguration.isEmpty()) {
            for (AlarmCallbackConfiguration alarmCallbackConfiguration : listAlarmCallbackConfiguration) {
                alarmCallbackConfigurationService.destroy(alarmCallbackConfiguration);
            }
        }
    }
    public void removeConditionAndNotificationFromStream(Stream stream) {
        removeConditionFromStream(stream);
        removeNotificationFromStream(stream);
    }

    private Map<String, Object> getParametersNotification(String severity){
        final LoggingAlertConfig configGeneral = clusterConfigService.getOrDefault(LoggingAlertConfig.class,
                LoggingAlertConfig.createDefault());

        Map<String, Object> parametersNotification = Maps.newHashMap();
        parametersNotification.put(AlertRuleUtils.SEVERITY, severity);
        parametersNotification.put(AlertRuleUtils.CONTENT, configGeneral.accessLogBody());
        parametersNotification.put(AlertRuleUtils.SPLIT_FIELDS, Collections.emptyList());
        parametersNotification.put(AlertRuleUtils.AGGREGATION_TIME, configGeneral.accessAggregationTime());
        parametersNotification.put(AlertRuleUtils.LIMIT_OVERFLOW, configGeneral.accessLimitOverflow());
        parametersNotification.put(AlertRuleUtils.COMMENT, AlertRuleUtils.COMMENT_ALERT_WIZARD);

        return parametersNotification;
    }

    private String createNotification(String streamID, CreateAlarmCallbackRequest cacr, String userName){
        try {
            final AlarmCallbackConfiguration alarmCallbackConfiguration =
                    alarmCallbackConfigurationService.create(streamID, cacr, userName);
            alarmCallbackFactory.create(alarmCallbackConfiguration).checkConfiguration();
            return alarmCallbackConfigurationService.save(alarmCallbackConfiguration);
        } catch (ValidationException | AlarmCallbackConfigurationException | ConfigurationException e) {
            LOG.error(ERROR_ALARM_CALLBACK_CONFIGURATION, e);
        } catch (ClassNotFoundException e) {
            LOG.error(ERROR_ALARM_CALLBACK_TYPE, e);
        }
        return null;
    }

    public String createNotificationFromParameters(String title, Stream stream, Map<String, Object> parameters, String userName){
        CreateAlarmCallbackRequest cacr = CreateAlarmCallbackRequest.create(AlertRuleUtils.TYPE_LOGGING_ALERT, title, parameters);
        return createNotification(stream.getId(), cacr, userName);
    }

    public String createNotificationFromConfiguration(String title, Stream stream, AlarmCallbackConfiguration alarmCallbackConfig, String userName){
        final AlarmCallbackConfiguration updatedAlarmCallbackConfig = ((AlarmCallbackConfigurationImpl) alarmCallbackConfig).toBuilder()
                .setTitle(title)
                .build();
        final CreateAlarmCallbackRequest cacr = CreateAlarmCallbackRequest.create(updatedAlarmCallbackConfig);
        return createNotification(stream.getId(), cacr, userName);
    }

    public String createDefaultNotification(String title, Stream stream, String severity, String userName){
        if(alertRuleUtils.isValidSeverity(severity)){
            return createNotificationFromParameters(title, stream, getParametersNotification(severity), userName);
        }
        return null;
    }

    public boolean updateNotification(String title, String notificationID, String severity) {
        if(alertRuleUtils.isValidSeverity(severity)){
            try {
                final AlarmCallbackConfiguration callbackConfiguration = alarmCallbackConfigurationService.load(notificationID);
                if (callbackConfiguration != null) {
                    Map<String, Object> configuration = callbackConfiguration.getConfiguration();
                    configuration.replace(AlertRuleUtils.SEVERITY, severity);
                    final AlarmCallbackConfiguration updatedConfig = ((AlarmCallbackConfigurationImpl) callbackConfiguration).toBuilder()
                            .setTitle(title)
                            .setConfiguration(configuration)
                            .build();

                    alarmCallbackFactory.create(updatedConfig).checkConfiguration();
                    alarmCallbackConfigurationService.save(updatedConfig);
                }
                return true;
            } catch (ValidationException | AlarmCallbackConfigurationException | ConfigurationException e) {
                LOG.error(ERROR_ALARM_CALLBACK_CONFIGURATION, e);
            } catch (ClassNotFoundException e) {
                LOG.error(ERROR_ALARM_CALLBACK_TYPE, e);
            }
        }
        return false;
    }

    public String createCondition(String conditionType ,String alertTitle, Map<String, Object>  parameters, Stream conditionStream, Stream stream2, String userName){
        CreateConditionRequest ccr = CreateConditionRequest.create(conditionType, alertTitle, parameters);
        AlertCondition alertCondition;
        try {
            alertCondition = alertService.fromRequest(ccr, conditionStream, userName);
            streamService.addAlertCondition(conditionStream, alertCondition);
            return alertCondition.getId();
        } catch (ConfigurationException | ValidationException e) {
            cleanAlertRule(conditionStream, stream2);
            LOG.error(ERROR_ALARM_CONDITION_CONFIGURATION, e);
            throw new BadRequestException(e.getMessage(), e);
        }
    }

}
