package com.airbus_cyber_security.graylog.alert.utilities;

import com.airbus_cyber_security.graylog.alert.*;
import com.airbus_cyber_security.graylog.alert.rest.models.requests.AlertRuleRequest;
import com.airbus_cyber_security.graylog.alert.rest.models.responses.GetDataAlertRule;
import com.airbus_cyber_security.graylog.config.LoggingAlertConfig;
import com.airbus_cyber_security.graylog.config.LoggingNotificationConfig;
import com.airbus_cyber_security.graylog.config.SeverityType;
import com.airbus_cyber_security.graylog.events.processor.aggregation.AggregationCountProcessorConfig;
import com.airbus_cyber_security.graylog.events.processor.correlation.CorrelationCountProcessorConfig;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.graylog.events.conditions.Expr;
import org.graylog.events.conditions.Expression;
import org.graylog.events.notifications.EventNotificationHandler;
import org.graylog.events.notifications.EventNotificationSettings;
import org.graylog.events.notifications.NotificationDto;
import org.graylog.events.notifications.NotificationResourceHandler;
import org.graylog.events.processor.EventDefinitionDto;
import org.graylog.events.processor.EventDefinitionHandler;
import org.graylog.events.processor.EventProcessorConfig;
import org.graylog.events.processor.aggregation.AggregationConditions;
import org.graylog.events.processor.aggregation.AggregationEventProcessorConfig;
import org.graylog.events.processor.aggregation.AggregationFunction;
import org.graylog.events.processor.aggregation.AggregationSeries;
import org.graylog.events.rest.EventDefinitionsResource;
import org.graylog.events.rest.EventNotificationsResource;
import org.graylog2.alarmcallbacks.AlarmCallbackConfiguration;
import org.graylog2.alarmcallbacks.AlarmCallbackConfigurationImpl;
import org.graylog2.alarmcallbacks.AlarmCallbackConfigurationService;
import org.graylog2.alarmcallbacks.AlarmCallbackFactory;
import org.graylog2.alerts.Alert;
import org.graylog2.alerts.AlertService;
import org.graylog2.database.NotFoundException;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.database.ValidationException;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.rest.models.alarmcallbacks.requests.CreateAlarmCallbackRequest;
import org.graylog2.rest.models.streams.alerts.requests.CreateConditionRequest;
import org.graylog2.streams.StreamService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import java.util.*;

public class AlertRuleUtilsService {

    private static final Logger LOG = LoggerFactory.getLogger(AlertRuleUtilsService.class);

    private static final String ERROR_ALARM_CONDITION_CONFIGURATION = "Invalid alarm Condition configuration.";
    public static final String ERROR_ALARM_CALLBACK_CONFIGURATION = "Invalid alarm callback configuration.";
    private static final String ERROR_ALARM_CALLBACK_TYPE = "Invalid alarm callback type.";

    private final AlertRuleService alertRuleService;
    private final StreamService streamService;
    private final AlertService alertService;
    private final AlarmCallbackConfigurationService alarmCallbackConfigurationService;
    private final AlarmCallbackFactory alarmCallbackFactory;
    private final ClusterConfigService clusterConfigService;
    private final AlertRuleUtils alertRuleUtils;
    private final EventDefinitionsResource eventDefinitionsResource;
    private final EventNotificationsResource eventNotificationsResource;
    private final NotificationResourceHandler notificationResourceHandler;
    private final EventDefinitionHandler eventDefinitionHandler;

    public AlertRuleUtilsService(AlertRuleService alertRuleService,
                                 StreamService streamService,
                                 AlertService alertService,
                                 AlarmCallbackConfigurationService alarmCallbackConfigurationService,
                                 AlarmCallbackFactory alarmCallbackFactory,
                                 ClusterConfigService clusterConfigService,
                                 AlertRuleUtils alertRuleUtils,
                                 EventDefinitionsResource eventDefinitionsResource,
                                 EventNotificationsResource eventNotificationsResource,
                                 NotificationResourceHandler notificationResourceHandler,
                                 EventDefinitionHandler eventDefinitionHandler) {
        this.alertRuleService = alertRuleService;
        this.streamService = streamService;
        this.alertService = alertService;
        this.alarmCallbackConfigurationService = alarmCallbackConfigurationService;
        this.alarmCallbackFactory = alarmCallbackFactory;
        this.clusterConfigService = clusterConfigService;
        this.alertRuleUtils = alertRuleUtils;
        this.eventDefinitionsResource = eventDefinitionsResource;
        this.eventNotificationsResource = eventNotificationsResource;
        this.notificationResourceHandler = notificationResourceHandler;
        this.eventDefinitionHandler = eventDefinitionHandler;
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

    private String mapAggregationFunctionToType(String aggregationFunction){
        switch (aggregationFunction) {
            case "AVG": return "MEAN";
            default:
                return aggregationFunction;
        }
    }

    public GetDataAlertRule constructDataAlertRule(AlertRule alert) throws NotFoundException {
        final String streamID = alert.getStreamID();
        final Stream stream = streamService.load(streamID);

        //Get the event
        EventDefinitionDto event = eventDefinitionsResource.get(alert.getEventID());
        LOG.info("Event type: " + event.config().type());

        Map<String, Object> parametersCondition = Maps.newHashMap();
        if(event.config().type().equals("aggregation-count")) {
            AggregationCountProcessorConfig aggregationCountConfig = (AggregationCountProcessorConfig) event.config();
            parametersCondition.put("threshold", aggregationCountConfig.threshold());
            parametersCondition.put("threshold_type", aggregationCountConfig.thresholdType());
            parametersCondition.put("time", aggregationCountConfig.searchWithinMs() / 60 / 1000);
            parametersCondition.put("grouping_fields", aggregationCountConfig.groupingFields());
            parametersCondition.put("distinction_fields", aggregationCountConfig.distinctionFields());
            parametersCondition.put("grace",aggregationCountConfig.executeEveryMs());
        }else if(event.config().type().equals("correlation-count")) {
            CorrelationCountProcessorConfig correlationConfig = (CorrelationCountProcessorConfig) event.config();
            parametersCondition.put("threshold", correlationConfig.threshold());
            parametersCondition.put("threshold_type", correlationConfig.thresholdType());
            parametersCondition.put("additional_threshold", correlationConfig.threshold());
            parametersCondition.put("additional_threshold_type", correlationConfig.thresholdType());
            parametersCondition.put("time", correlationConfig.searchWithinMs() / 60 / 1000);
            parametersCondition.put("grouping_fields", correlationConfig.groupingFields());
            parametersCondition.put("grace", correlationConfig.executeEveryMs());
        }else if(event.config().type().equals("aggregation-v1")){
            AggregationEventProcessorConfig aggregationConfig = (AggregationEventProcessorConfig) event.config();
            LOG.info("Expr: "+ aggregationConfig.conditions().get().expression().get().expr());
            LOG.info("type: "+ aggregationConfig.series().get(0).function().toString());
            LOG.info("field: "+ aggregationConfig.series().get(0).field().get());
            LOG.info("threshold: "+ aggregationConfig.conditions().get().expression().get());

            parametersCondition.put("time", aggregationConfig.searchWithinMs() / 60 / 1000);
            parametersCondition.put("threshold", 0);
            parametersCondition.put("threshold_type", aggregationConfig.conditions().get().expression().get().expr());
            parametersCondition.put("type", mapAggregationFunctionToType(aggregationConfig.series().get(0).function().toString()));
            parametersCondition.put("field", aggregationConfig.series().get(0).field().get());
            parametersCondition.put("grace", aggregationConfig.executeEveryMs());
        }

        List<FieldRuleImpl> fieldRules = new ArrayList<>();
        Optional.ofNullable(alert.getPipelineFieldRules()).ifPresent(fieldRules::addAll);
        Optional.ofNullable(alertRuleUtils.getListFieldRule(stream.getStreamRules())).ifPresent(fieldRules::addAll);
        AlertRuleStream alertRuleStream = AlertRuleStreamImpl.create(streamID, stream.getMatchingType().toString(), fieldRules);

        AlertRuleStream alertRuleStream2 = null;
        if(alert.getSecondStreamID() != null && !alert.getSecondStreamID().isEmpty()) {
            final Stream stream2 = streamService.load(alert.getSecondStreamID());
            List<FieldRuleImpl> fieldRules2 = new ArrayList<>();
            Optional.ofNullable(alert.getSecondPipelineFieldRules()).ifPresent(fieldRules2::addAll);
            Optional.ofNullable(alertRuleUtils.getListFieldRule(stream2.getStreamRules())).ifPresent(fieldRules2::addAll);
            alertRuleStream2 = AlertRuleStreamImpl.create(alert.getSecondStreamID(), stream2.getMatchingType().toString(), fieldRules2);
        }

        LoggingNotificationConfig loggingNotificationConfig = (LoggingNotificationConfig) eventNotificationsResource.get(alert.getNotificationID()).config();
        LOG.info("Severity: " + loggingNotificationConfig.severity().getType());

        return GetDataAlertRule.create(alert.getTitle(), event.title(),
                loggingNotificationConfig.severity().getType(),
                alert.getEventID(),
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

    public String createCondition(String conditionType ,String alertTitle, Map<String, Object>  parameters, Stream conditionStream, Stream stream2, String userName){
        CreateConditionRequest ccr = CreateConditionRequest.create(conditionType, alertTitle, parameters);
        AlertCondition alertCondition;
        try {
            alertCondition = alertService.fromRequest(ccr, conditionStream, userName);
            streamService.addAlertCondition(conditionStream, alertCondition);
            return alertCondition.getId();
        } catch (ConfigurationException | ValidationException e) {
            // cleanAlertRule(conditionStream, stream2);
            LOG.error(ERROR_ALARM_CONDITION_CONFIGURATION, e);
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    private EventProcessorConfig createCorrelationCondition(String type, String streamID, String streamID2, Map<String, Object> conditionParameter){
        String messsageOrder;
        if(type.equals("THEN")){
            messsageOrder = "AFTER";
        }else{
            messsageOrder = "ANY";
        }

        return CorrelationCountProcessorConfig.builder()
                .stream(streamID)
                .thresholdType((String) conditionParameter.get("threshold_type"))
                .threshold((int) conditionParameter.get("threshold"))
                .additionalStream(streamID2)
                .additionalThresholdType((String) conditionParameter.get("additional_threshold_type"))
                .additionalThreshold((int) conditionParameter.get("additional_threshold"))
                .messagesOrder(messsageOrder)
                .searchWithinMs(((int) conditionParameter.get("time")) * 60 * 1000)
                .executeEveryMs(((int) conditionParameter.get("grace")) * 60 * 1000)
                .groupingFields(new HashSet<String>((List<String>) conditionParameter.get("grouping_fields")))
                .comment(AlertRuleUtils.COMMENT_ALERT_WIZARD)
                .searchQuery("*")
                .build();
    }

    public EventProcessorConfig createAggregationCondition(String streamID, Map<String, Object> conditionParameter){
        return AggregationCountProcessorConfig.builder()
                .stream(streamID)
                .thresholdType((String) conditionParameter.get("threshold_type"))
                .threshold((int) conditionParameter.get("threshold"))
                .searchWithinMs(((int) conditionParameter.get("time")) * 60 * 1000)
                .executeEveryMs(((int) conditionParameter.get("grace")) * 60 * 1000)
                .groupingFields(new HashSet<String>((List<String>) conditionParameter.get("grouping_fields")))
                .distinctionFields(new HashSet<String>((List<String>) conditionParameter.get("distinction_fields")))
                .comment(AlertRuleUtils.COMMENT_ALERT_WIZARD)
                .searchQuery("*")
                .build();
    }

    public EventProcessorConfig createStatisticalCondition(String streamID, Map<String, Object> conditionParameter){
        LOG.info("Begin Stat, type: " + conditionParameter.get("type"));
        AggregationFunction agregationFunction;
        switch (conditionParameter.get("type").toString()) {
            case "MEAN":
                agregationFunction = AggregationFunction.AVG;
                break;
            case "MIN":
                agregationFunction = AggregationFunction.MIN;
                break;
            case "MAX":
                agregationFunction = AggregationFunction.MAX;
                break;
            case "SUM":
                agregationFunction = AggregationFunction.SUM;
                break;
            case "STDDEV":
                agregationFunction = AggregationFunction.STDDEV;
                break;
            default:
                throw new BadRequestException();
        }

        String ID = UUID.randomUUID().toString();
        final AggregationSeries serie = AggregationSeries.builder()
                .id(ID)
                .function(agregationFunction)
                .field(conditionParameter.get("field").toString())
                .build();

        final Expr.NumberReference left = Expr.NumberReference.create(ID);
        final Expr.NumberValue right = Expr.NumberValue.create((int) conditionParameter.get("threshold"));
        final Expression<Boolean> expression;
        switch (conditionParameter.get("threshold_type").toString()) {
            case ">":
                expression = Expr.Greater.create(left, right);
                break;
            case ">=":
                expression = Expr.GreaterEqual.create(left, right);
                break;
            case "<":
                expression = Expr.Lesser.create(left, right);
                break;
            case "<=":
                expression = Expr.LesserEqual.create(left, right);
                break;
            case "=":
                expression = Expr.Equal.create(left, right);
                break;
            default:
                throw new BadRequestException();
        }

        return AggregationEventProcessorConfig.builder()
                .query("")
                .streams(new HashSet<String> (Collections.singleton(streamID)))
                .series(ImmutableList.of(serie))
                .groupBy(ImmutableList.of())
                .conditions(AggregationConditions.builder()
                        .expression(expression)
                        .build())
                .executeEveryMs(((int) conditionParameter.get("grace")) * 60 * 1000)
                .searchWithinMs(((int) conditionParameter.get("time")) * 60 * 1000)
                .build();
    }

    public String createNotification(String alertTitle, String severity){
        LoggingNotificationConfig loggingNotificationConfig = LoggingNotificationConfig.builder()
                .singleMessage(false)
                .severity(SeverityType.valueOf(severity.toUpperCase()))
                .logBody("Test")
                .build();
        NotificationDto notification = NotificationDto.builder()
                .config(loggingNotificationConfig)
                .title(alertTitle)
                .description(AlertRuleUtils.COMMENT_ALERT_WIZARD)
                .build();
        notification = this.notificationResourceHandler.create(notification);
        return notification.id();
    }

    public void updateNotification(String title, String notificationID, String severity){
        NotificationDto notification = eventNotificationsResource.get(notificationID);
        LoggingNotificationConfig loggingNotificationConfig = (LoggingNotificationConfig) notification.config();
        if(!loggingNotificationConfig.severity().getType().equals(severity) || !notification.title().equals(title)){
            LOG.info("Update Notification");
            if(!loggingNotificationConfig.severity().getType().equals(severity)){
                LOG.info("Update severity, old one: " + loggingNotificationConfig.severity().getType() + " New one: " + severity);
                loggingNotificationConfig = LoggingNotificationConfig.builder()
                        .severity(SeverityType.valueOf(severity.toUpperCase()))
                        .logBody(loggingNotificationConfig.logBody())
                        .splitFields(loggingNotificationConfig.splitFields())
                        .aggregationStream(loggingNotificationConfig.aggregationStream())
                        .aggregationTime(loggingNotificationConfig.aggregationTime())
                        .limitOverflow(loggingNotificationConfig.limitOverflow())
                        .fieldAlertId(loggingNotificationConfig.fieldAlertId())
                        .alertTag(loggingNotificationConfig.alertTag())
                        .overflowTag(loggingNotificationConfig.overflowTag())
                        .singleMessage(loggingNotificationConfig.singleMessage())
                        .build();
            }
            notification = NotificationDto.builder()
                    .id(notification.id())
                    .config(loggingNotificationConfig)
                    .title(title)
                    .description(notification.description())
                    .build();
            notificationResourceHandler.update(notification);
        }
    }

    public EventProcessorConfig createCondition(String conditionType, Map<String, Object> conditionParameter, String streamID, String streamID2){
        LOG.info("Create condition type:" + conditionType);

        if(conditionType.equals("THEN") || conditionType.equals("AND")){
            return createCorrelationCondition(conditionType, streamID, streamID2, conditionParameter);
        } else if (conditionType.equals("STATISTICAL")){
            return createStatisticalCondition(streamID, conditionParameter);
        } else {
            return createAggregationCondition(streamID, conditionParameter);
        }
    }

    public String createEvent(String alertTitle, String notificationID, EventProcessorConfig configuration){
        LOG.info("Create Event: " + alertTitle);
        EventNotificationHandler.Config notificationConfiguration = EventNotificationHandler.Config.builder()
                .notificationId(notificationID)
                .build();

        EventDefinitionDto eventDefinition = EventDefinitionDto.builder()
                .title(alertTitle)
                .description(AlertRuleUtils.COMMENT_ALERT_WIZARD)
                //.description(request.getDescription())
                .config(configuration)
                .alert(true)
                .priority(2)
                .keySpec(ImmutableList.of())
                .notifications(ImmutableList.<EventNotificationHandler.Config>builder().add(notificationConfiguration).build())
                .notificationSettings(EventNotificationSettings.builder()
                        .gracePeriodMs(0L)
                        .backlogSize(500)
                        .build())
                .build();

        //TODO do it with eventDefinitionsResource to have the validation but need to get the event ID back
        //this.eventDefinitionsResource.create(eventDefinition);
        eventDefinition = this.eventDefinitionHandler.create(eventDefinition);
        return eventDefinition.id();
    }

    public void updateEvent(String alertTitle, String eventID, EventProcessorConfig configuration){
        LOG.info("Update Event: " + alertTitle + " ID: " + eventID);
        EventDefinitionDto event = eventDefinitionsResource.get(eventID);
        event = EventDefinitionDto.builder()
                .id(event.id())
                .title(alertTitle)
                .description(event.description())
                .priority(event.priority())
                .alert(event.alert())
                .config(configuration)
                .fieldSpec(event.fieldSpec())
                .keySpec(event.keySpec())
                .notificationSettings(event.notificationSettings())
                .notifications(event.notifications())
                .storage(event.storage())
                .build();
        this.eventDefinitionHandler.update(event);
    }

}