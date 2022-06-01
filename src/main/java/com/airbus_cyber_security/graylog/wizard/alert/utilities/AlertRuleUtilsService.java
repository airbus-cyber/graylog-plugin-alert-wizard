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
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.requests.AlertRuleRequest;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.responses.GetDataAlertRule;
import com.airbus_cyber_security.graylog.wizard.config.rest.AlertWizardConfig;
import com.airbus_cyber_security.graylog.wizard.config.rest.DefaultValues;
import com.airbus_cyber_security.graylog.events.config.LoggingAlertConfig;
import com.airbus_cyber_security.graylog.events.config.SeverityType;
import com.airbus_cyber_security.graylog.events.notifications.types.LoggingNotificationConfig;
import com.airbus_cyber_security.graylog.events.processor.aggregation.AggregationCountProcessorConfig;
import com.airbus_cyber_security.graylog.events.processor.correlation.CorrelationCountProcessorConfig;
import com.google.common.collect.ImmutableList;
import org.graylog.events.conditions.Expr;
import org.graylog.events.conditions.Expression;
import org.graylog.events.notifications.EventNotificationHandler;
import org.graylog.events.notifications.EventNotificationSettings;
import org.graylog.events.notifications.NotificationDto;
import org.graylog.events.processor.EventDefinitionDto;
import org.graylog.events.processor.EventProcessorConfig;
import org.graylog.events.processor.aggregation.AggregationConditions;
import org.graylog.events.processor.aggregation.AggregationEventProcessorConfig;
import org.graylog.events.processor.aggregation.AggregationFunction;
import org.graylog.events.processor.aggregation.AggregationSeries;
import org.graylog.events.rest.EventDefinitionsResource;
import org.graylog.events.rest.EventNotificationsResource;
import org.graylog.security.UserContext;
import org.graylog2.alerts.Alert;
import org.graylog2.alerts.AlertService;
import org.graylog2.database.NotFoundException;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.graylog2.plugin.rest.ValidationResult;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.streams.StreamService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import java.util.*;

public class AlertRuleUtilsService {

    private static final Logger LOG = LoggerFactory.getLogger(AlertRuleUtilsService.class);

    private final AlertRuleServiceImpl alertRuleService;
    private final StreamService streamService;
    private final AlertService alertService;
    private final AlertRuleUtils alertRuleUtils;
    private final EventDefinitionsResource eventDefinitionsResource;
    private final EventNotificationsResource eventNotificationsResource;
    private final ClusterConfigService clusterConfigService;

    public AlertRuleUtilsService(AlertRuleServiceImpl alertRuleService,
                                 StreamService streamService,
                                 AlertService alertService,
                                 AlertRuleUtils alertRuleUtils,
                                 EventDefinitionsResource eventDefinitionsResource,
                                 EventNotificationsResource eventNotificationsResource,
                                 ClusterConfigService clusterConfigService) {
        this.alertRuleService = alertRuleService;
        this.streamService = streamService;
        this.alertService = alertService;
        this.alertRuleUtils = alertRuleUtils;
        this.eventDefinitionsResource = eventDefinitionsResource;
        this.eventNotificationsResource = eventNotificationsResource;
        this.clusterConfigService = clusterConfigService;
    }

    public void checkIsValidRequest(AlertRuleRequest request) {
        if (!alertRuleService.isValidRequest(request)) {
            LOG.error("Invalid alert rule request");
            throw new BadRequestException("Invalid alert rule request.");
        }
    }

    private int countAlerts(String streamID, DateTime since) {
        final List<Alert> alerts = alertService.loadRecentOfStream(streamID, since, 999);
        return alerts.size();
    }

    public GetDataAlertRule constructDataAlertRule(AlertRule alert) throws NotFoundException {
        LOG.debug("Get data alert: " + alert.getTitle());
        try {
            String streamID = alert.getStreamID();
            Stream stream = streamService.load(streamID);

            // Get the event
            Map<String, Object> parametersCondition = null;
            String eventTitle;
            if (alert.getEventID() != null && !alert.getEventID().isEmpty()) {
                EventDefinitionDto event = eventDefinitionsResource.get(alert.getEventID());
                eventTitle = event.title();
                parametersCondition = alertRuleUtils.getConditionParameters(event.config());
            } else {
                eventTitle = alert.getTitle();
                LOG.error("Alert " + alert.getTitle() + " is broken event id is null");
            }

            List<FieldRuleImpl> fieldRules = new ArrayList<>();
            Optional.ofNullable(alert.getPipelineFieldRules()).ifPresent(fieldRules::addAll);
            Optional.ofNullable(alertRuleUtils.getListFieldRule(stream.getStreamRules())).ifPresent(fieldRules::addAll);
            AlertRuleStream alertRuleStream = AlertRuleStreamImpl.create(streamID, stream.getMatchingType().toString(), fieldRules);

            AlertRuleStream alertRuleStream2 = null;
            if (alert.getSecondStreamID() != null && !alert.getSecondStreamID().isEmpty()) {
                Stream stream2 = streamService.load(alert.getSecondStreamID());
                List<FieldRuleImpl> fieldRules2 = new ArrayList<>();
                Optional.ofNullable(alert.getSecondPipelineFieldRules()).ifPresent(fieldRules2::addAll);
                Optional.ofNullable(alertRuleUtils.getListFieldRule(stream2.getStreamRules())).ifPresent(fieldRules2::addAll);
                alertRuleStream2 = AlertRuleStreamImpl.create(alert.getSecondStreamID(), stream2.getMatchingType().toString(), fieldRules2);
            }

            LoggingNotificationConfig loggingNotificationConfig = (LoggingNotificationConfig) eventNotificationsResource.get(alert.getNotificationID()).config();
            LOG.debug("Severity: " + loggingNotificationConfig.severity().getType());

            return GetDataAlertRule.create(alert.getTitle(), eventTitle,
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

        } catch (Exception e) {
            throw new NotFoundException(e);
        }
    }

    private HashSet<String> convertToHashSet(Object object) {
        if (object instanceof HashSet) {
            return (HashSet<String>) object;
        } else if (object instanceof List) {
            return new HashSet<>((List<String>) object);
        } else {
            return new HashSet<>();
        }
    }

    private EventProcessorConfig createCorrelationCondition(String type, String streamID, String streamID2, Map<String, Object> conditionParameter) {
        String messageOrder;
        if (type.equals("THEN")) {
            messageOrder = "AFTER";
        } else {
            messageOrder = "ANY";
        }

        return CorrelationCountProcessorConfig.builder()
                .stream(streamID)
                .thresholdType((String) conditionParameter.get("threshold_type"))
                .threshold((int) conditionParameter.get("threshold"))
                .additionalStream(streamID2)
                .additionalThresholdType((String) conditionParameter.get("additional_threshold_type"))
                .additionalThreshold((int) conditionParameter.get("additional_threshold"))
                .messagesOrder(messageOrder)
                .searchWithinMs(Long.parseLong(conditionParameter.get("time").toString()) * 60 * 1000)
                .executeEveryMs(Long.parseLong(conditionParameter.get("grace").toString()) * 60 * 1000)
                .groupingFields(convertToHashSet(conditionParameter.get("grouping_fields")))
                .comment(AlertRuleUtils.COMMENT_ALERT_WIZARD)
                .searchQuery("*")
                .build();
    }

    public EventProcessorConfig createAggregationCondition(String streamID, Map<String, Object> conditionParameter) {
        return AggregationCountProcessorConfig.builder()
                .stream(streamID)
                .thresholdType((String) conditionParameter.get("threshold_type"))
                .threshold((int) conditionParameter.get("threshold"))
                .searchWithinMs(Long.parseLong(conditionParameter.get("time").toString()) * 60 * 1000)
                .executeEveryMs(Long.parseLong(conditionParameter.get("grace").toString()) * 60 * 1000)
                .groupingFields(convertToHashSet(conditionParameter.get("grouping_fields")))
                .distinctionFields(convertToHashSet(conditionParameter.get("distinction_fields")))
                .comment(AlertRuleUtils.COMMENT_ALERT_WIZARD)
                .searchQuery("*")
                .build();
    }

    private AggregationFunction mapTypeToAggregationFunction(String type) {
        switch (type) {
            case "MEAN": //For Compatibility with older version
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

    private Expression<Boolean> createExpressionFromThreshold(String ID, String thresholdType, int threshold) {
        final Expr.NumberReference left = Expr.NumberReference.create(ID);
        final Expr.NumberValue right = Expr.NumberValue.create(threshold);
        switch (thresholdType) {
            case "HIGHER": //For Compatibility with older version
            case ">":
                return Expr.Greater.create(left, right);
            case ">=":
                return Expr.GreaterEqual.create(left, right);
            case "LOWER": //For Compatibility with older version
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
        LOG.debug("Begin Stat, type: " + conditionParameter.get("type"));

        String ID = UUID.randomUUID().toString();
        final AggregationSeries serie = AggregationSeries.builder()
                .id(ID)
                .function(mapTypeToAggregationFunction(conditionParameter.get("type").toString()))
                .field(conditionParameter.get("field").toString())
                .build();

        final Expression<Boolean> expression = createExpressionFromThreshold(ID,
                conditionParameter.get("threshold_type").toString(),
                (int) conditionParameter.get("threshold"));

        return AggregationEventProcessorConfig.builder()
                .query("")
                .streams(new HashSet<>(Collections.singleton(streamID)))
                .series(ImmutableList.of(serie))
                .groupBy(ImmutableList.of())
                .conditions(AggregationConditions.builder()
                        .expression(expression)
                        .build())
                .executeEveryMs(Long.parseLong(conditionParameter.get("grace").toString()) * 60 * 1000)
                .searchWithinMs(Long.parseLong(conditionParameter.get("time").toString()) * 60 * 1000)
                .build();
    }

    private String createNotificationFromDto(NotificationDto notification, UserContext userContext) {
        Response response = this.eventNotificationsResource.create(notification, userContext);
        if (Response.Status.Family.familyOf(response.getStatus()) == Response.Status.Family.SUCCESSFUL) {
            notification = (NotificationDto) response.getEntity();
            return notification.id();
        } else {
            ValidationResult validationResult = (ValidationResult) response.getEntity();
            LOG.error("Failed to create Notification for alert: " + notification.title() + " Errors: " + validationResult.getErrors());
            return null;
        }
    }

    private String updateNotificationFromDto(String notificationID, NotificationDto notification) {
        Response response = this.eventNotificationsResource.update(notificationID, notification);
        if (Response.Status.Family.familyOf(response.getStatus()) == Response.Status.Family.SUCCESSFUL) {
            notification = (NotificationDto) response.getEntity();
            return notification.id();
        } else {
            ValidationResult validationResult = (ValidationResult) response.getEntity();
            LOG.error("Failed to update Notification for alert: " + notification.title() + " Errors: " + validationResult.getErrors());
            return null;
        }
    }

    private String getDefaultLogBody() {
        final LoggingAlertConfig generalConfig = clusterConfigService.getOrDefault(LoggingAlertConfig.class,
                LoggingAlertConfig.createDefault());
        return generalConfig.accessLogBody();
    }

    private int getDefaultTime() {
        // TODO would it work to get the configuration once directly in the constructor? Or better even, inject the AlertWizardConfig?
        LoggingAlertConfig configuration = clusterConfigService.getOrDefault(LoggingAlertConfig.class,
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

    public String createNotificationFromParameters(String alertTitle, Map<String, Object> parametersNotification, UserContext userContext) {
        LOG.debug("Create Notification " + alertTitle);
        LoggingNotificationConfig loggingNotificationConfig = LoggingNotificationConfig.builder()
                .singleMessage((boolean) parametersNotification.getOrDefault("single_notification", false))
                .severity(SeverityType.valueOf(parametersNotification.get("severity").toString().toUpperCase()))
                .logBody(parametersNotification.getOrDefault("log_body", this.getDefaultLogBody()).toString())
                .splitFields(convertToHashSet(parametersNotification.get("split_fields")))
                .aggregationTime((int) parametersNotification.getOrDefault("aggregation_time", 0))
                .alertTag(parametersNotification.getOrDefault("alert_tag", "LoggingAlert").toString())
                .build();
        NotificationDto notification = NotificationDto.builder()
                .config(loggingNotificationConfig)
                .title(alertTitle)
                .description(AlertRuleUtils.COMMENT_ALERT_WIZARD)
                .build();
        return this.createNotificationFromDto(notification, userContext);
    }

    public void updateNotification(String title, String notificationID, String severity) {
        NotificationDto notification = eventNotificationsResource.get(notificationID);
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
            updateNotificationFromDto(notificationID, notification);
        }
    }

    public EventProcessorConfig createCondition(String conditionType, Map<String, Object> conditionParameter, String streamID, String streamID2) {
        LOG.debug("Create condition type:" + conditionType);

        if (conditionType.equals("THEN") || conditionType.equals("AND")) {
            return createCorrelationCondition(conditionType, streamID, streamID2, conditionParameter);
        } else if (conditionType.equals("STATISTICAL")) {
            return createStatisticalCondition(streamID, conditionParameter);
        } else {
            return createAggregationCondition(streamID, conditionParameter);
        }
    }

    private String createEventFromDto(EventDefinitionDto eventDefinition, UserContext userContext) {
        Response response = this.eventDefinitionsResource.create(true, eventDefinition, userContext);
        if (Response.Status.Family.familyOf(response.getStatus()) == Response.Status.Family.SUCCESSFUL) {
            eventDefinition = (EventDefinitionDto) response.getEntity();
            return eventDefinition.id();
        } else {
            ValidationResult validationResult = (ValidationResult) response.getEntity();
            LOG.error("Failed to create Event for alert: " + eventDefinition.title() + " Errors: " + validationResult.getErrors());
            return null;
        }
    }

    private String updateEventFromDto(String definitionID, EventDefinitionDto eventDefinition) {
        Response response = this.eventDefinitionsResource.update(definitionID, true, eventDefinition);
        if (Response.Status.Family.familyOf(response.getStatus()) == Response.Status.Family.SUCCESSFUL) {
            eventDefinition = (EventDefinitionDto) response.getEntity();
            return eventDefinition.id();
        } else {
            ValidationResult validationResult = (ValidationResult) response.getEntity();
            LOG.error("Failed to create Event for alert: " + eventDefinition.title() + " Errors: " + validationResult.getErrors());
            return null;
        }
    }

    public String createEvent(String alertTitle, String notificationID, EventProcessorConfig configuration, UserContext userContext) {
        LOG.debug("Create Event: " + alertTitle);
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

        return this.createEventFromDto(eventDefinition, userContext);
    }

    public void updateEvent(String alertTitle, String eventID, EventProcessorConfig configuration) {
        LOG.debug("Update Event: " + alertTitle + " ID: " + eventID);
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
        this.updateEventFromDto(eventID, event);
    }

}
