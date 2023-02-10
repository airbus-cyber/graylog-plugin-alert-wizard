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

import com.airbus_cyber_security.graylog.wizard.alert.model.AlertRule;
import com.airbus_cyber_security.graylog.wizard.alert.model.AlertRuleStream;
import com.airbus_cyber_security.graylog.wizard.alert.model.FieldRule;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.requests.AlertRuleRequest;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.responses.GetDataAlertRule;
import com.airbus_cyber_security.graylog.events.config.LoggingAlertConfig;
import com.airbus_cyber_security.graylog.events.config.SeverityType;
import com.airbus_cyber_security.graylog.events.notifications.types.LoggingNotificationConfig;
import com.airbus_cyber_security.graylog.events.processor.correlation.CorrelationCountProcessorConfig;
import com.airbus_cyber_security.graylog.events.processor.correlation.checks.OrderType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.graylog.events.conditions.Expr;
import org.graylog.events.conditions.Expression;
import org.graylog.events.notifications.*;
import org.graylog.events.processor.DBEventDefinitionService;
import org.graylog.events.processor.EventDefinitionDto;
import org.graylog.events.processor.EventDefinitionHandler;
import org.graylog.events.processor.EventProcessorConfig;
import org.graylog.events.processor.aggregation.AggregationConditions;
import org.graylog.events.processor.aggregation.AggregationEventProcessorConfig;
import org.graylog.events.processor.aggregation.AggregationFunction;
import org.graylog.events.processor.aggregation.AggregationSeries;
import org.graylog.security.UserContext;
import org.graylog2.alerts.Alert;
import org.graylog2.alerts.AlertService;
import org.graylog2.database.NotFoundException;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.streams.StreamService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import java.util.*;

// TODO split this class according
//  => extract everything which is related to EventsDefinition CRUD into a EventsDefinitionUtils/Service?
//  => extract everything which is related to Notification CRUD into a NotificationUtils/Service?
public class AlertRuleUtilsService {

    private static final Logger LOG = LoggerFactory.getLogger(AlertRuleUtilsService.class);

    private final AlertRuleService alertRuleService;
    private final StreamService streamService;
    private final AlertService alertService;
    private final AlertRuleUtils alertRuleUtils;
    private final EventDefinitionHandler eventDefinitionHandler;

    private final DBEventDefinitionService eventDefinitionService;
    private final DBNotificationService notificationService;

    private final NotificationResourceHandler notificationHandler;
    private final ClusterConfigService clusterConfigService;

    @Inject
    public AlertRuleUtilsService(AlertRuleService alertRuleService,
                                 StreamService streamService,
                                 AlertService alertService,
                                 EventDefinitionHandler eventDefinitionHandler,
                                 DBEventDefinitionService eventDefinitionService,
                                 NotificationResourceHandler notificationHandler,
                                 DBNotificationService notificationService,
                                 ClusterConfigService clusterConfigService) {
        this.alertRuleUtils = new AlertRuleUtils();
        this.alertRuleService = alertRuleService;
        this.streamService = streamService;
        this.alertService = alertService;
        this.eventDefinitionHandler = eventDefinitionHandler;
        this.eventDefinitionService = eventDefinitionService;
        this.notificationHandler = notificationHandler;
        this.notificationService = notificationService;
        this.clusterConfigService = clusterConfigService;
    }

    public void checkIsValidRequest(AlertRuleRequest request) {
        if (!this.alertRuleService.isValidRequest(request)) {
            LOG.error("Invalid alert rule request");
            throw new BadRequestException("Invalid alert rule request.");
        }
    }

    private int countAlerts(String streamID, DateTime since) {
        List<Alert> alerts = this.alertService.loadRecentOfStream(streamID, since, 999);
        return alerts.size();
    }

    public GetDataAlertRule constructDataAlertRule(AlertRule alert) {
        LOG.debug("Get data alert: " + alert.getTitle());
        String streamIdentifier = alert.getStreamIdentifier();

        Map<String, Object> parametersCondition = convertEventDefinitionToParametersCondition(alert.getEventID());
        Stream stream = this.loadStream(streamIdentifier);
        AlertRuleStream alertRuleStream = constructAlertRuleStream(alert, stream, alert.getPipelineFieldRules());
        AlertRuleStream alertRuleStream2 = constructSecondAlertRuleStream(alert);
        NotificationDto notification = this.getNotification(alert.getNotificationID());
        LoggingNotificationConfig loggingNotificationConfig = (LoggingNotificationConfig) notification.config();
        long alertCount = countAlerts(streamIdentifier, alert.getLastModified());

        boolean isDisabled = streamIsDisabled(stream);

        return GetDataAlertRule.create(alert.getTitle(),
                loggingNotificationConfig.severity().getType(),
                alert.getEventID(),
                alert.getNotificationID(),
                alert.getCreatedAt(),
                alert.getCreatorUserId(),
                alert.getLastModified(),
                isDisabled,
                alert.getDescription(),
                alertCount,
                alert.getConditionType(),
                parametersCondition,
                alertRuleStream,
                alertRuleStream2);
    }

    private NotificationDto getNotification(String notificationIdentifier) {
        return this.notificationService.get(notificationIdentifier)
                .orElseThrow(() -> new javax.ws.rs.NotFoundException("Notification " + notificationIdentifier + " doesn't exist"));
    }

    private EventDefinitionDto getEventDefinition(String eventDefinitionIdentifier) {
        return this.eventDefinitionService.get(eventDefinitionIdentifier)
                .orElseThrow(() -> new javax.ws.rs.NotFoundException("Event definition <" + eventDefinitionIdentifier + "> doesn't exist"));
    }

    private Map<String, Object> convertEventDefinitionToParametersCondition(String eventIdentifier) {
        // TODO should try to remove this condition...
        if (eventIdentifier == null || eventIdentifier.isEmpty()) {
            LOG.error("Alert is broken event id is null");
            return null;
        }
        EventDefinitionDto event = this.getEventDefinition(eventIdentifier);
        return this.alertRuleUtils.getConditionParameters(event.config());
    }

    private static boolean streamIsDisabled(Stream stream) {
        boolean isDisabled = false;
        if (stream != null) {
            isDisabled = stream.getDisabled();
        }
        return isDisabled;
    }

    private AlertRuleStream constructSecondAlertRuleStream(AlertRule alert) {
        String streamIdentifier = alert.getSecondStreamID();
        // TODO are these two checks really necessary? (Isn't it either one or the other?)
        if (streamIdentifier == null) {
            return null;
        }
        if (streamIdentifier.isEmpty()) {
            return null;
        }
        Stream stream = this.loadStream(streamIdentifier);
        return this.constructAlertRuleStream(alert, stream, alert.getSecondPipelineFieldRules());
    }

    private AlertRuleStream constructAlertRuleStream(AlertRule alert, Stream stream, List<FieldRule> pipelineFieldRules) {
        if (stream == null) {
            return null;
        }

        List<FieldRule> fieldRules = new ArrayList<>();
        Optional.ofNullable(pipelineFieldRules).ifPresent(fieldRules::addAll);
        Optional.ofNullable(this.alertRuleUtils.getListFieldRule(stream.getStreamRules())).ifPresent(fieldRules::addAll);
        return AlertRuleStream.create(stream.getId(), stream.getMatchingType().toString(), fieldRules);
    }

    private Stream loadStream(String streamIdentifier) {
        try {
            return this.streamService.load(streamIdentifier);
        } catch (NotFoundException e) {
            return null;
        }
    }

    private String convertThresholdTypeToCorrelation(String thresholdType) {
        if (thresholdType.equals(AlertRuleService.THRESHOLD_TYPE_MORE)) {
            return "MORE";
        } else {
            return "LESS";
        }
    }

    // TODO move method to AlertRuleUtils?
    // TODO instead of a String, the type could already be a com.airbus_cyber_security.graylog.events.processor.correlation.checks.OrderType
    private EventProcessorConfig createCorrelationCondition(String type, String streamID, String streamID2, Map<String, Object> conditionParameter) {
        OrderType messageOrder;
        if (type.equals("THEN")) {
            messageOrder = OrderType.AFTER;
        } else {
            messageOrder = OrderType.ANY;
        }
        String thresholdType = convertThresholdTypeToCorrelation((String) conditionParameter.get(AlertRuleUtils.THRESHOLD_TYPE));
        String additionalThresholdType = convertThresholdTypeToCorrelation((String) conditionParameter.get(AlertRuleUtils.ADDITIONAL_THRESHOLD_TYPE));       

        long searchWithinMs = this.alertRuleUtils.convertMinutesToMilliseconds(Long.parseLong(conditionParameter.get(AlertRuleUtils.TIME).toString()));
        long executeEveryMs = this.alertRuleUtils.convertMinutesToMilliseconds(Long.parseLong(conditionParameter.get(AlertRuleUtils.GRACE).toString()));

        return CorrelationCountProcessorConfig.builder()
                .stream(streamID)
                .thresholdType(thresholdType)
                .threshold((int) conditionParameter.get(AlertRuleUtils.THRESHOLD))
                .additionalStream(streamID2)
                .additionalThresholdType(additionalThresholdType)
                .additionalThreshold((int) conditionParameter.get(AlertRuleUtils.ADDITIONAL_THRESHOLD))
                .messagesOrder(messageOrder)
                .searchWithinMs(searchWithinMs)
                .executeEveryMs(executeEveryMs)
                // TODO CorrelationCountProcessorConfig.groupingFields should be of type List (or better just Collection/Iterable) rather than Set
                .groupingFields((List<String>) conditionParameter.get(AlertRuleUtils.GROUPING_FIELDS))
                .comment(AlertRuleUtils.COMMENT_ALERT_WIZARD)
                .searchQuery("*")
                .build();
    }

    private Expression<Boolean> createExpressionFromNumberThreshold(String identifier, String thresholdType, int threshold) {
        Expr.NumberReference left = Expr.NumberReference.create(identifier);
        Expr.NumberValue right = Expr.NumberValue.create(threshold);
        switch (thresholdType) {
            case AlertRuleService.THRESHOLD_TYPE_MORE:
                return Expr.Greater.create(left, right);
            case AlertRuleService.THRESHOLD_TYPE_LESS:
                return Expr.Lesser.create(left, right);
            default:
                throw new BadRequestException("createExpressionFromNumberThreshold: unexpected threshold type " + thresholdType);
        }
    }

    public EventProcessorConfig createAggregationCondition(String streamIdentifier, Map<String, Object> conditionParameter) {
        List<String> groupByFields = (List<String>) conditionParameter.get(AlertRuleUtils.GROUPING_FIELDS);
        String distinctBy = (String) conditionParameter.get(AlertRuleUtils.DISTINCT_BY);

        Set<String> streams = ImmutableSet.of(streamIdentifier);
        // TODO extract method to parse searchWithinMs
        long searchWithinMs = this.alertRuleUtils.convertMinutesToMilliseconds(Long.parseLong(conditionParameter.get(AlertRuleUtils.TIME).toString()));
        // TODO extract method to parse executeEveryMs
        long executeEveryMs = this.alertRuleUtils.convertMinutesToMilliseconds(Long.parseLong(conditionParameter.get(AlertRuleUtils.GRACE).toString()));

        String thresholdType = (String) conditionParameter.get(AlertRuleUtils.THRESHOLD_TYPE);
        int threshold = (int) conditionParameter.get(AlertRuleUtils.THRESHOLD);

        String identifier = UUID.randomUUID().toString();
        AggregationSeries.Builder seriesBuilder = AggregationSeries.builder().id(identifier);

        if (distinctBy.isEmpty()) {
            seriesBuilder.function(AggregationFunction.COUNT);
        } else {
            seriesBuilder.function(AggregationFunction.CARD).field(distinctBy);
        }

        AggregationSeries series = seriesBuilder.build();

        Expression<Boolean> expression = createExpressionFromNumberThreshold(identifier, thresholdType, threshold);
        AggregationConditions conditions = AggregationConditions.builder()
                .expression(expression)
                .build();

        return AggregationEventProcessorConfig.builder()
                .query("")
                .streams(streams)
                .groupBy(groupByFields)
                .series(ImmutableList.of(series))
                .conditions(conditions)
                .executeEveryMs(executeEveryMs)
                .searchWithinMs(searchWithinMs)
                .build();
    }

    private AggregationFunction mapTypeToAggregationFunction(String type) {
        switch (type) {
            case "AVG":
                return AggregationFunction.AVG;
            case "MIN":
                return AggregationFunction.MIN;
            case "MAX":
                return AggregationFunction.MAX;
            case "SUM":
                return AggregationFunction.SUM;
            case "STDDEV":
                return AggregationFunction.STDDEV;
            case "CARD":
                return AggregationFunction.CARD;
            case "COUNT":
                return AggregationFunction.COUNT;
            case "SUMOFSQUARES":
                return AggregationFunction.SUMOFSQUARES;
            case "VARIANCE":
                return AggregationFunction.VARIANCE;
            default:
                throw new BadRequestException();
        }
    }

    private Expression<Boolean> createExpressionFromThreshold(String identifier, String thresholdType, int threshold) {
        Expr.NumberReference left = Expr.NumberReference.create(identifier);
        Expr.NumberValue right = Expr.NumberValue.create(threshold);
        switch (thresholdType) {
            case ">":
                return Expr.Greater.create(left, right);
            case ">=":
                return Expr.GreaterEqual.create(left, right);
            case "<":
                return Expr.Lesser.create(left, right);
            case "<=":
                return Expr.LesserEqual.create(left, right);
            case "==":
                return Expr.Equal.create(left, right);
            default:
                throw new BadRequestException();
        }
    }

    public EventProcessorConfig createStatisticalCondition(String streamID, Map<String, Object> conditionParameter) {
        String type = conditionParameter.get(AlertRuleUtils.TYPE).toString();
        LOG.debug("Begin Stat, type: {}", type);
        // TODO extract method to parse searchWithinMs
        long searchWithinMs = this.alertRuleUtils.convertMinutesToMilliseconds(Long.parseLong(conditionParameter.get(AlertRuleUtils.TIME).toString()));
        // TODO extract method to parse executeEveryMs
        long executeEveryMs = this.alertRuleUtils.convertMinutesToMilliseconds(Long.parseLong(conditionParameter.get(AlertRuleUtils.GRACE).toString()));

        String identifier = UUID.randomUUID().toString();
        AggregationSeries serie = AggregationSeries.builder()
                .id(identifier)
                .function(mapTypeToAggregationFunction(type))
                .field(conditionParameter.get(AlertRuleUtils.FIELD).toString())
                .build();

        Expression<Boolean> expression = createExpressionFromThreshold(identifier,
                conditionParameter.get(AlertRuleUtils.THRESHOLD_TYPE).toString(),
                (int) conditionParameter.get(AlertRuleUtils.THRESHOLD));

        return AggregationEventProcessorConfig.builder()
                .query("")
                .streams(new HashSet<>(Collections.singleton(streamID)))
                .series(ImmutableList.of(serie))
                .groupBy(ImmutableList.of())
                .conditions(AggregationConditions.builder()
                        .expression(expression)
                        .build())
                .searchWithinMs(searchWithinMs)
                .executeEveryMs(executeEveryMs)
                .build();
    }

    private String createNotificationFromDto(NotificationDto notification, UserContext userContext) {
        NotificationDto result = this.notificationHandler.create(notification, Optional.ofNullable(userContext.getUser()));
        return result.id();
    }

    private String updateNotificationFromDto(NotificationDto notification) {
        NotificationDto result = this.notificationHandler.update(notification);
        return result.id();
    }

    private String getDefaultLogBody() {
        LoggingAlertConfig generalConfig = this.clusterConfigService.getOrDefault(LoggingAlertConfig.class,
                LoggingAlertConfig.createDefault());
        return generalConfig.accessLogBody();
    }

    private int getDefaultTime() {
        LoggingAlertConfig configuration = this.clusterConfigService.getOrDefault(LoggingAlertConfig.class,
                LoggingAlertConfig.createDefault());
        return configuration.accessAggregationTime();
    }

    public String createNotification(String alertTitle, String severity, UserContext userContext) {
        LoggingNotificationConfig loggingNotificationConfig = LoggingNotificationConfig.builder()
                .singleMessage(false)
                .severity(SeverityType.valueOf(severity.toUpperCase()))
                .logBody(this.getDefaultLogBody())
                .aggregationTime(this.getDefaultTime())
                .build();
        NotificationDto notification = NotificationDto.builder()
                .config(loggingNotificationConfig)
                .title(alertTitle)
                .description(AlertRuleUtils.COMMENT_ALERT_WIZARD)
                .build();
        return this.createNotificationFromDto(notification, userContext);
    }

    public void updateNotification(String title, String notificationID, String severity) {
        NotificationDto notification = this.getNotification(notificationID);
        LoggingNotificationConfig loggingNotificationConfig = (LoggingNotificationConfig) notification.config();
        if (!loggingNotificationConfig.severity().getType().equals(severity) || !notification.title().equals(title)) {
            LOG.debug("Update Notification " + title);
            if (!loggingNotificationConfig.severity().getType().equals(severity)) {
                LOG.debug("Update severity, old one: " + loggingNotificationConfig.severity().getType() + " New one: " + severity);
                loggingNotificationConfig = LoggingNotificationConfig.builder()
                        .severity(SeverityType.valueOf(severity.toUpperCase()))
                        .logBody(loggingNotificationConfig.logBody())
                        .splitFields(loggingNotificationConfig.splitFields())
                        .aggregationTime(loggingNotificationConfig.aggregationTime())
                        .alertTag(loggingNotificationConfig.alertTag())
                        .singleMessage(loggingNotificationConfig.singleMessage())
                        .build();
            }
            notification = NotificationDto.builder()
                    .id(notification.id())
                    .config(loggingNotificationConfig)
                    .title(title)
                    .description(notification.description())
                    .build();
            updateNotificationFromDto(notification);
        }
    }

    public EventProcessorConfig createCondition(String conditionType, Map<String, Object> conditionParameter, String streamID, String streamID2) {
        LOG.debug("Create condition type: {}", conditionType);

        if (conditionType.equals("THEN") || conditionType.equals("AND")) {
            return createCorrelationCondition(conditionType, streamID, streamID2, conditionParameter);
        } else if (conditionType.equals("STATISTICAL")) {
            return createStatisticalCondition(streamID, conditionParameter);
        } else {
            return createAggregationCondition(streamID, conditionParameter);
        }
    }

    private String createEventFromDto(EventDefinitionDto eventDefinition, UserContext userContext) {
        EventDefinitionDto result = this.eventDefinitionHandler.create(eventDefinition, Optional.of(userContext.getUser()));
        return result.id();
    }

    private String updateEventFromDto(EventDefinitionDto eventDefinition) {
        EventDefinitionDto result = this.eventDefinitionHandler.update(eventDefinition, true);
        return result.id();
    }

    public String createEvent(String alertTitle, String notificationIdentifier, EventProcessorConfig configuration, UserContext userContext) {
        LOG.debug("Create Event: " + alertTitle);
        EventNotificationHandler.Config notificationConfiguration = EventNotificationHandler.Config.builder()
                .notificationId(notificationIdentifier)
                .build();

        EventDefinitionDto eventDefinition = EventDefinitionDto.builder()
                .title(alertTitle)
                .description(AlertRuleUtils.COMMENT_ALERT_WIZARD)
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

        return this.createEventFromDto(eventDefinition, userContext);
    }

    public void updateEvent(String alertTitle, String eventID, EventProcessorConfig configuration) {
        LOG.debug("Update event: {}, identifier: {}", alertTitle, eventID);
        EventDefinitionDto event = this.getEventDefinition(eventID);
        EventDefinitionDto updatedEvent = EventDefinitionDto.builder()
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
        this.updateEventFromDto(updatedEvent);
    }
}
