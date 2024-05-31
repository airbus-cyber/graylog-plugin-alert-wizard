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

// TODO should rename package rest into resources
package com.airbus_cyber_security.graylog.wizard.alert.rest;

import com.airbus_cyber_security.graylog.events.notifications.types.LoggingNotificationConfig;
import com.airbus_cyber_security.graylog.wizard.alert.business.*;
import com.airbus_cyber_security.graylog.wizard.alert.business.AlertRuleService;
import com.airbus_cyber_security.graylog.wizard.alert.model.*;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.AlertRuleStream;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.FieldRule;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.requests.AlertRuleRequest;
import com.airbus_cyber_security.graylog.wizard.alert.model.AlertType;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.responses.GetDataAlertRule;
import com.airbus_cyber_security.graylog.wizard.audit.AlertWizardAuditEventTypes;
import com.airbus_cyber_security.graylog.wizard.config.rest.AlertWizardConfig;
import com.airbus_cyber_security.graylog.wizard.config.rest.AlertWizardConfigurationService;
import com.airbus_cyber_security.graylog.wizard.config.rest.ImportPolicyType;
import com.airbus_cyber_security.graylog.wizard.list.utilities.AlertListUtilsService;
import com.airbus_cyber_security.graylog.wizard.permissions.AlertRuleRestPermissions;
import com.codahale.metrics.annotation.Timed;
import com.mongodb.MongoException;
import io.swagger.annotations.*;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.graylog.events.notifications.NotificationDto;
import org.graylog.events.processor.EventDefinitionDto;
import org.graylog.events.processor.EventProcessorConfig;
import org.graylog.events.rest.EventNotificationsResource;
import org.graylog.plugins.pipelineprocessor.db.*;
import org.graylog.security.UserContext;
import org.graylog2.audit.jersey.AuditEvent;
import org.graylog2.database.NotFoundException;
import org.graylog2.events.ClusterEventBus;
import org.graylog2.plugin.database.ValidationException;
import org.graylog2.plugin.rest.PluginRestResource;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.shared.rest.resources.RestResource;
import org.graylog2.streams.StreamService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Api(value = "Wizard/Alerts", description = "Management of Wizard alerts rules.")
@Path("/alerts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AlertRuleResource extends RestResource implements PluginRestResource {
    private static final Logger LOG = LoggerFactory.getLogger(AlertRuleResource.class);

    private static final String ENCODING = "UTF-8";
    private static final String TITLE = "title";

    private final StreamService streamService;
    private final ClusterEventBus clusterEventBus;
    // TODO try to remove this field => move it down in business
    private final AlertWizardConfigurationService configurationService;

    // TODO try to remove this field => Use AlertRuleUtilsService
    private final EventNotificationsResource eventNotificationsResource;
    private final EventDefinitionService eventDefinitionService;

    private final AlertRuleService alertRuleService;
    private final Conversions conversions;
    private final StreamPipelineService streamPipelineService;
    private final AlertListUtilsService alertListUtilsService;
    private final NotificationService notificationService;

    @Inject
    public AlertRuleResource(AlertRuleService alertRuleService,
                             StreamService streamService,
                             StreamPipelineService streamPipelineService,
                             ClusterEventBus clusterEventBus,
                             AlertWizardConfigurationService configurationService,
                             AlertListUtilsService alertListUtilsService,
                             EventNotificationsResource eventNotificationsResource,
                             Conversions conversions,
                             EventDefinitionService eventDefinitionService,
                             NotificationService notificationService) {
        // TODO should probably move these fields down into the business namespace
        this.alertRuleService = alertRuleService;
        this.streamService = streamService;
        this.clusterEventBus = clusterEventBus;
        this.configurationService = configurationService;
        this.eventNotificationsResource = eventNotificationsResource;
        this.eventDefinitionService = eventDefinitionService;

        this.alertListUtilsService = alertListUtilsService;
        this.conversions = conversions;
        this.streamPipelineService = streamPipelineService;
        this.notificationService = notificationService;
    }

    private Stream loadStream(String streamIdentifier) {
        try {
            return this.streamService.load(streamIdentifier);
        } catch (NotFoundException e) {
            // this may happen if the underlying stream was deleted
            // see test test_get_all_rules_should_not_fail_when_a_stream_is_deleted_issue105 and related issue
            // TODO in this case, maybe the rule should rather be converted into a corrupted rule than this aspect being handled by the interface?
            return null;
        }
    }

    // the output stream into which filtered logs are put
    private String getTriggeringConditionsOutputStreamIdentifier(TriggeringConditions conditions) {
        if (conditions instanceof StreamConditions streamConditions) {
            return streamConditions.streamIdentifier();
        }
        if (conditions instanceof ListAndStreamConditions listAndStreamConditions) {
            return listAndStreamConditions.outputStreamIdentifier();
        }
        throw new RuntimeException("Unreachable code");
    }

    // the stream which carries the conditions to filter the logs
    private String getTriggeringConditionsFilteringStreamIdentifier(TriggeringConditions conditions) {
        if (conditions instanceof StreamConditions streamConditions) {
            return streamConditions.streamIdentifier();
        }
        if (conditions instanceof ListAndStreamConditions listAndStreamConditions) {
            return listAndStreamConditions.filteringStreamIdentifier();
        }
        throw new RuntimeException("Unreachable code");
    }

    private AlertRuleStream constructAlertRuleStream(TriggeringConditions conditions) {
        String streamIdentifier = this.getTriggeringConditionsFilteringStreamIdentifier(conditions);
        Stream stream = this.loadStream(streamIdentifier);
        return this.conversions.constructAlertRuleStream(stream, conditions);
    }

    private boolean isDisabled(TriggeringConditions conditions) {
        String streamIdentifier = this.getTriggeringConditionsFilteringStreamIdentifier(conditions);
        Stream stream = this.loadStream(streamIdentifier);
        if (stream == null) {
            return false;
        }
        return stream.getDisabled();
    }

    private GetDataAlertRule constructDataAlertRule(AlertRule alert) {
        NotificationDto notification = this.notificationService.get(alert.getNotificationID());
        AlertPattern alertPattern = alert.pattern();
        DateTime lastModified = alert.getLastModified();
        EventDefinitionDto event = null;
        Map<String, Object> parametersCondition = null;
        boolean isDisabled = false;
        AlertRuleStream alertRuleStream = null;
        AlertRuleStream alertRuleStream2 = null;
        String eventIdentifier2 = null;
        if (alertPattern instanceof CorrelationAlertPattern pattern) {
            event = this.eventDefinitionService.getEventDefinition(pattern.eventIdentifier());
            parametersCondition = this.conversions.getConditionParameters(event.config());
            TriggeringConditions conditions1 = pattern.conditions1();
            alertRuleStream = this.constructAlertRuleStream(conditions1);
            TriggeringConditions conditions2 = pattern.conditions2();
            alertRuleStream2 = this.constructAlertRuleStream(conditions2);
            isDisabled = this.isDisabled(conditions1);
        } else if (alertPattern instanceof DisjunctionAlertPattern pattern) {
            event = this.eventDefinitionService.getEventDefinition(pattern.eventIdentifier1());
            parametersCondition = this.conversions.getConditionParameters(event.config());
            TriggeringConditions conditions = pattern.conditions1();
            alertRuleStream = this.constructAlertRuleStream(conditions);
            TriggeringConditions conditions2 = pattern.conditions2();
            alertRuleStream2 = this.constructAlertRuleStream(conditions2);
            isDisabled = this.isDisabled(conditions);
            eventIdentifier2 = pattern.eventIdentifier2();
        } else if (alertPattern instanceof AggregationAlertPattern pattern) {
            event = this.eventDefinitionService.getEventDefinition(pattern.eventIdentifier());
            parametersCondition = this.conversions.getConditionParameters(event.config());
            TriggeringConditions conditions = pattern.conditions();
            alertRuleStream = this.constructAlertRuleStream(conditions);
            isDisabled = this.isDisabled(conditions);
        }
        LoggingNotificationConfig loggingNotificationConfig = (LoggingNotificationConfig) notification.config();

        return GetDataAlertRule.create(alert.getTitle(),
                loggingNotificationConfig.severity().getType(),
                event.id(),
                eventIdentifier2,
                notification.id(),
                alert.getCreatedAt(),
                alert.getCreatorUserId(),
                lastModified,
                isDisabled,
                event.description(),
                alert.getAlertType(),
                parametersCondition,
                alertRuleStream,
                alertRuleStream2);
    }

    @GET
    @Timed
    @ApiOperation(value = "Lists all existing alerts")
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_READ)
    public List<GetDataAlertRule> list() {
        List<AlertRule> alerts = this.alertRuleService.all();

        List<GetDataAlertRule> alertsData = new ArrayList<>();
        for (AlertRule alert: alerts) {
            alertsData.add(this.constructDataAlertRule(alert));
        }

        return alertsData;
    }

    @GET
    @Path("/{title}")
    @Timed
    @ApiOperation(value = "Get a alert")
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_READ)
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Alert not found."),
    })
    public GetDataAlertRule get(@ApiParam(name = TITLE, required = true) @PathParam(TITLE) String title)
            throws UnsupportedEncodingException, NotFoundException {
        String alertTitle = java.net.URLDecoder.decode(title, ENCODING);
        AlertRule alert = this.alertRuleService.load(alertTitle);
        if (alert == null) {
            throw new NotFoundException("Alert <" + alertTitle + "> not found!");
        }
        return this.constructDataAlertRule(alert);
    }

    private String checkImportPolicyAndGetTitle(String title, UserContext userContext) {
        String alertTitle = title;
        if (this.alertRuleService.isPresent(alertTitle)) {
            // TODO should be get or default here: it will return null when starting with a fresh instance of graylog
            // Idem in AlertListRessource. Add a test that creates two alerts with same title
            AlertWizardConfig configuration = this.configurationService.getConfiguration();
            ImportPolicyType importPolicy = configuration.accessImportPolicy();
            if (importPolicy != null && importPolicy.equals(ImportPolicyType.RENAME)) {
                String newAlertTitle;
                int i = 1;
                do {
                    newAlertTitle = alertTitle + "(" + i + ")";
                    i++;
                } while (this.alertRuleService.isPresent(newAlertTitle));
                alertTitle = newAlertTitle;
            } else if (importPolicy != null && importPolicy.equals(ImportPolicyType.REPLACE)) {
                try {
                    this.delete(alertTitle, userContext);
                } catch (MongoException | UnsupportedEncodingException e) {
                    LOG.error("Failed to replace alert rule");
                    throw new BadRequestException("Failed to replace alert rule.");
                }
            } else {
                LOG.info("Failed to create alert rule: Alert rule title already exist");
                throw new BadRequestException("Failed to create alert rule: Alert rule title already exist.");
            }
        }
        return alertTitle;
    }

    // TODO this should somehow be factored with the code in StreamPipelineService
    private boolean hasStreamRules(List<FieldRule> fieldRules) {
        for (FieldRule fieldRule: fieldRules) {
            if (!this.streamPipelineService.isListFieldRule(fieldRule)) {
                return true;
            }
        }
        return false;
    }

    private TriggeringConditions createTriggeringConditionsFromStream(AlertRuleStream streamConfiguration, String title,
                                                                      Stream filteringStream, String userName) throws ValidationException {
        List<FieldRule> fieldRulesWithList = this.streamPipelineService.extractPipelineFieldRules(streamConfiguration.getFieldRules());

        if (fieldRulesWithList.isEmpty()) {
            StreamConditions.Builder builder = StreamConditions.builder().streamIdentifier(filteringStream.getId());
            return builder.build();
        }

        for (FieldRule fieldRule: fieldRulesWithList) {
            this.alertListUtilsService.incrementUsage(fieldRule.getValue());
        }

        Stream.MatchingType matchingType = streamConfiguration.getMatchingType();
        if (matchingType.equals(Stream.MatchingType.AND) && hasStreamRules(streamConfiguration.getFieldRules())) {
            PipelineDao graylogPipeline = this.streamPipelineService.createPipeline(title, matchingType, filteringStream.getId());
            Stream outputStream = this.streamPipelineService.createStream(matchingType, title + " output", userName);
            RuleDao pipelineRule = this.streamPipelineService.createPipelineRule(title, fieldRulesWithList, matchingType, outputStream);
            Pipeline pipeline = Pipeline.builder()
                    .identifier(graylogPipeline.id()).ruleIdentifier(pipelineRule.id()).fieldRules(fieldRulesWithList)
                    .build();
            return ListAndStreamConditions.builder()
                    .filteringStreamIdentifier(filteringStream.getId())
                    .outputStreamIdentifier(outputStream.getId())
                    .pipeline(pipeline).build();
        } else {
            PipelineDao graylogPipeline = this.streamPipelineService.createPipeline(title, matchingType);
            RuleDao pipelineRule = this.streamPipelineService.createPipelineRule(title, fieldRulesWithList, matchingType, filteringStream);
            Pipeline pipeline = Pipeline.builder()
                    .identifier(graylogPipeline.id()).ruleIdentifier(pipelineRule.id()).fieldRules(fieldRulesWithList)
                    .build();
            return ListAndStreamConditions.builder()
                    .filteringStreamIdentifier(filteringStream.getId()).outputStreamIdentifier(filteringStream.getId())
                    .pipeline(pipeline).build();
        }
    }

    private TriggeringConditions createTriggeringConditions(AlertRuleStream streamConfiguration, String title, String userName) throws ValidationException {
        Stream stream = this.streamPipelineService.createStream(streamConfiguration.getMatchingType(), title, userName);
        this.streamPipelineService.createStreamRule(streamConfiguration.getFieldRules(), stream.getId());
        return createTriggeringConditionsFromStream(streamConfiguration, title, stream, userName);
    }

    private TriggeringConditions updateTriggeringConditions(TriggeringConditions previousConditions, String alertTitle,
                                                            AlertRuleStream streamConfiguration, String userName) throws ValidationException {
        // update filtering stream
        String streamIdentifier = this.getTriggeringConditionsFilteringStreamIdentifier(previousConditions);
        Stream stream = this.loadStream(streamIdentifier);
        this.streamPipelineService.updateStream(stream, streamConfiguration, alertTitle);

        if (previousConditions instanceof  ListAndStreamConditions previousListAndStreamConditions) {
            this.streamPipelineService.deleteStreamFromIdentifier(previousListAndStreamConditions.outputStreamIdentifier());
            deletePipeline(previousListAndStreamConditions.pipeline());
        }

        return createTriggeringConditionsFromStream(streamConfiguration, alertTitle, stream, userName);
    }

    @POST
    // TODO is this annotation @Timed necessary? What is it for? Remove?
    @Timed
    @ApiOperation(value = "Create an alert")
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_CREATE)
    @ApiResponses(value = {@ApiResponse(code = 400, message = "The supplied request is not valid.")})
    @AuditEvent(type = AlertWizardAuditEventTypes.WIZARD_ALERTS_RULES_CREATE)
    public Response create(@ApiParam(name = "JSON body", required = true) @Valid @NotNull AlertRuleRequest request, @Context UserContext userContext)
            throws ValidationException, BadRequestException {

        this.conversions.checkIsValidRequest(request);

        String userName = getCurrentUser().getName();
        String title = request.getTitle();
        String alertTitle = checkImportPolicyAndGetTitle(title, userContext);
        AlertType alertType = request.getConditionType();

        String notificationIdentifier = this.notificationService.createNotification(alertTitle, request.getSeverity(), userContext);
        AlertPattern pattern = createAlertPattern(notificationIdentifier, request, alertTitle, userContext, userName);

        AlertRule alertRule = AlertRule.create(
                alertTitle,
                alertType,
                pattern,
                notificationIdentifier,
                DateTime.now(),
                userName,
                DateTime.now());
        alertRule = this.alertRuleService.create(alertRule);

        GetDataAlertRule result = this.constructDataAlertRule(alertRule);
        return Response.ok().entity(result).build();
    }

    private AlertPattern createAlertPattern(String notificationIdentifier, AlertRuleRequest request, String alertTitle,
                                            UserContext userContext, String userName) throws ValidationException {
        AlertType alertType = request.getConditionType();

        TriggeringConditions conditions = createTriggeringConditions(request.getStream(), alertTitle, userName);

        switch (alertType) {
            case THEN:
            case AND:
                return createCorrelationAlertPattern(notificationIdentifier, request, alertTitle, userContext, userName, conditions);
            case OR:
                return createDisjunctionAlertPattern(notificationIdentifier, request, alertTitle, userContext, userName, conditions);
            default:
                String description = request.getDescription();
                Map<String, Object> conditionParameters = request.conditionParameters();
                String streamIdentifier = this.getTriggeringConditionsOutputStreamIdentifier(conditions);
                EventProcessorConfig configuration1 = this.conversions.createEventConfiguration(alertType, conditionParameters, streamIdentifier);
                String eventIdentifier = this.eventDefinitionService.createEvent(alertTitle, description, notificationIdentifier, configuration1, userContext);

                return AggregationAlertPattern.builder().conditions(conditions).eventIdentifier(eventIdentifier).build();
        }
    }

    private DisjunctionAlertPattern createDisjunctionAlertPattern(String notificationIdentifier, AlertRuleRequest request, String alertTitle, UserContext userContext, String userName, TriggeringConditions conditions) throws ValidationException {
        String description = request.getDescription();
        Map<String, Object> conditionParameters = request.conditionParameters();

        TriggeringConditions conditions2 = createTriggeringConditions(request.getSecondStream(), alertTitle + "#2", userName);
        String streamIdentifier = this.getTriggeringConditionsOutputStreamIdentifier(conditions);
        EventProcessorConfig configuration = this.conversions.createAggregationCondition(streamIdentifier, conditionParameters);
        String eventIdentifier = this.eventDefinitionService.createEvent(alertTitle, description, notificationIdentifier, configuration, userContext);
        String streamIdentifier2 = this.getTriggeringConditionsOutputStreamIdentifier(conditions2);
        EventProcessorConfig configuration2 = this.conversions.createAggregationCondition(streamIdentifier2, conditionParameters);
        String eventIdentifier2 = this.eventDefinitionService.createEvent(alertTitle + "#2", description, notificationIdentifier, configuration2, userContext);

        return DisjunctionAlertPattern.builder()
                .conditions1(conditions).conditions2(conditions2).eventIdentifier1(eventIdentifier).eventIdentifier2(eventIdentifier2)
                .build();
    }

    private CorrelationAlertPattern createCorrelationAlertPattern(String notificationIdentifier, AlertRuleRequest request, String alertTitle, UserContext userContext, String userName, TriggeringConditions conditions) throws ValidationException {
        String description = request.getDescription();
        AlertType alertType = request.getConditionType();
        Map<String, Object> conditionParameters = request.conditionParameters();

        TriggeringConditions conditions2 = createTriggeringConditions(request.getSecondStream(), alertTitle + "#2", userName);
        String streamIdentifier = getTriggeringConditionsOutputStreamIdentifier(conditions);
        String streamIdentifier2 = getTriggeringConditionsOutputStreamIdentifier(conditions2);
        EventProcessorConfig configuration = this.conversions.createCorrelationCondition(alertType, streamIdentifier, streamIdentifier2, conditionParameters);
        String eventIdentifier = this.eventDefinitionService.createEvent(alertTitle, description, notificationIdentifier, configuration, userContext);
        return CorrelationAlertPattern.builder().conditions1(conditions).conditions2(conditions2).eventIdentifier(eventIdentifier).build();
    }

    private AlertPattern updateAlertPattern(AlertPattern previousAlertPattern, String notificationIdentifier,
                                            AlertRuleRequest request, AlertType previousAlertType, String title,
                                            UserContext userContext, String userName) throws ValidationException {
        AlertRuleStream streamConfiguration = request.getStream();
        AlertRuleStream streamConfiguration2 = request.getSecondStream();
        AlertType alertType = request.getConditionType();
        if (previousAlertType != alertType) {
            deleteAlertPattern(previousAlertPattern);
            return createAlertPattern(notificationIdentifier, request, title, userContext, userName);
        }

        String title2 = title + "#2";
        // TODO increase readability: extract three methods?
        if (previousAlertPattern instanceof CorrelationAlertPattern previousPattern) {
            TriggeringConditions previousConditions = previousPattern.conditions1();
            TriggeringConditions conditions = updateTriggeringConditions(previousConditions, title, streamConfiguration, userName);
            TriggeringConditions previousConditions2 = previousPattern.conditions2();
            TriggeringConditions conditions2 = this.updateTriggeringConditions(previousConditions2, title2, streamConfiguration2, userName);

            String streamIdentifier = getTriggeringConditionsOutputStreamIdentifier(conditions);
            String streamIdentifier2 = getTriggeringConditionsOutputStreamIdentifier(conditions2);
            EventProcessorConfig configuration = this.conversions.createCorrelationCondition(alertType, streamIdentifier, streamIdentifier2, request.conditionParameters());
            this.eventDefinitionService.updateEvent(title, request.getDescription(), previousPattern.eventIdentifier(), configuration);

            return previousPattern.toBuilder().conditions1(conditions).build();
        } else if (previousAlertPattern instanceof DisjunctionAlertPattern previousPattern) {
            TriggeringConditions previousConditions = previousPattern.conditions1();
            TriggeringConditions conditions = updateTriggeringConditions(previousConditions, title, streamConfiguration, userName);
            TriggeringConditions previousConditions2 = previousPattern.conditions2();
            TriggeringConditions conditions2 = this.updateTriggeringConditions(previousConditions2, title2, streamConfiguration2, userName);

            String streamIdentifier = this.getTriggeringConditionsOutputStreamIdentifier(conditions);
            EventProcessorConfig configuration = this.conversions.createEventConfiguration(request.getConditionType(), request.conditionParameters(), streamIdentifier);
            this.eventDefinitionService.updateEvent(title, request.getDescription(), previousPattern.eventIdentifier1(), configuration);

            String streamIdentifier2 = this.getTriggeringConditionsOutputStreamIdentifier(conditions2);
            EventProcessorConfig configuration2 = this.conversions.createAggregationCondition(streamIdentifier2, request.conditionParameters());
            this.eventDefinitionService.updateEvent(title2, request.getDescription(), previousPattern.eventIdentifier2(), configuration2);

            return previousPattern.toBuilder().conditions1(conditions).build();
        } else if (previousAlertPattern instanceof AggregationAlertPattern previousPattern) {
            TriggeringConditions previousConditions = previousPattern.conditions();
            TriggeringConditions conditions = updateTriggeringConditions(previousConditions, title, streamConfiguration, userName);
            String streamIdentifier = this.getTriggeringConditionsOutputStreamIdentifier(conditions);
            EventProcessorConfig configuration = this.conversions.createEventConfiguration(request.getConditionType(), request.conditionParameters(), streamIdentifier);
            this.eventDefinitionService.updateEvent(title, request.getDescription(), previousPattern.eventIdentifier(), configuration);

            return previousPattern.toBuilder().conditions(conditions).build();
        }

        throw new RuntimeException("Unreachable code");
    }

    @PUT
    @Path("/{title}")
    @Timed
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_UPDATE)
    @ApiOperation(value = "Update a alert")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "The supplied request is not valid.")})
    @AuditEvent(type = AlertWizardAuditEventTypes.WIZARD_ALERTS_RULES_UPDATE)
    public Response update(@ApiParam(name = TITLE, required = true)
                           @PathParam(TITLE) String title,
                           @ApiParam(name = "JSON body", required = true) @Valid @NotNull AlertRuleRequest request,
                           @Context UserContext userContext
    ) throws UnsupportedEncodingException, NotFoundException, ValidationException {

        this.conversions.checkIsValidRequest(request);

        AlertRule previousAlert = this.alertRuleService.load(title);
        String notificationIdentifier = previousAlert.getNotificationID();
        String userName = getCurrentUser().getName();

        this.notificationService.updateNotification(title, notificationIdentifier, request.getSeverity());

        AlertType previousAlertType = previousAlert.getAlertType();
        AlertPattern pattern = updateAlertPattern(previousAlert.pattern(), notificationIdentifier, request,
                previousAlertType, title, userContext, userName);

        AlertRule alertRule = AlertRule.create(
                title,
                request.getConditionType(),
                pattern,
                previousAlert.getNotificationID(),
                previousAlert.getCreatedAt(),
                userName,
                DateTime.now());
        alertRule = this.alertRuleService.update(java.net.URLDecoder.decode(title, ENCODING), alertRule);

        GetDataAlertRule result = this.constructDataAlertRule(alertRule);
        return Response.accepted().entity(result).build();
    }

    private void deleteTriggeringConditions(TriggeringConditions conditions) {
        if (conditions instanceof StreamConditions streamConditions) {
            this.streamPipelineService.deleteStreamFromIdentifier(streamConditions.streamIdentifier());
        } else if (conditions instanceof ListAndStreamConditions listAndStreamConditions) {
            this.streamPipelineService.deleteStreamFromIdentifier(listAndStreamConditions.filteringStreamIdentifier());
            if (!listAndStreamConditions.outputStreamIdentifier().equals(listAndStreamConditions.filteringStreamIdentifier())) {
                this.streamPipelineService.deleteStreamFromIdentifier(listAndStreamConditions.outputStreamIdentifier());
            }
            deletePipeline(listAndStreamConditions.pipeline());
        }
    }

    private void deletePipeline(Pipeline pipeline) {
        this.streamPipelineService.deletePipeline(pipeline.identifier(), pipeline.ruleIdentifier());
        for (FieldRule fieldRule: this.nullSafe(pipeline.fieldRules())) {
            this.alertListUtilsService.decrementUsage(fieldRule.getValue());
        }
    }

    private void deleteEvent(String eventIdentifier) {
        if (eventIdentifier == null) {
            return;
        }
        this.eventDefinitionService.delete(eventIdentifier);
    }

    private void deleteAlertPattern(AlertPattern alertPattern) {
        if (alertPattern instanceof CorrelationAlertPattern pattern) {
            deleteTriggeringConditions(pattern.conditions1());
            deleteTriggeringConditions(pattern.conditions2());
            deleteEvent(pattern.eventIdentifier());
        } else if (alertPattern instanceof DisjunctionAlertPattern pattern) {
            deleteTriggeringConditions(pattern.conditions1());
            deleteTriggeringConditions(pattern.conditions2());
            deleteEvent(pattern.eventIdentifier1());
            deleteEvent(pattern.eventIdentifier2());
        } else if (alertPattern instanceof AggregationAlertPattern pattern) {
            deleteTriggeringConditions(pattern.conditions());
            deleteEvent(pattern.eventIdentifier());
        }
    }

    @DELETE
    @Path("/{title}")
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_DELETE)
    @ApiOperation(value = "Delete a alert")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Alert not found."),
            @ApiResponse(code = 400, message = "Invalid ObjectId.")
    })
    @AuditEvent(type = AlertWizardAuditEventTypes.WIZARD_ALERTS_RULES_DELETE)
    public void delete(@ApiParam(name = TITLE, required = true)
                       @PathParam(TITLE) String title,
                       @Context UserContext userContext
    ) throws MongoException, UnsupportedEncodingException {
        String alertTitle = java.net.URLDecoder.decode(title, ENCODING);

        try {
            AlertRule alertRule = this.alertRuleService.load(alertTitle);

            deleteAlertPattern(alertRule.pattern());
            if (alertRule.getNotificationID() != null && !alertRule.getNotificationID().isEmpty()) {
                // TODO move this down into AlertRuleUtilsService and remove the use for eventNotificationsResource
                this.eventNotificationsResource.delete(alertRule.getNotificationID(), userContext);
            }
        } catch (NotFoundException e) {
            LOG.error("Cannot find alert " + alertTitle, e);
        }

        this.alertRuleService.destroy(alertTitle);
    }

    // TODO remove this method => should have a more regular code (empty lists instead of null)!!!
    private <T> Collection<T> nullSafe(Collection<T> c) {
        return (c == null) ? Collections.<T>emptyList() : c;
    }
}
