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
import com.airbus_cyber_security.graylog.wizard.alert.model.AlertRule;
import com.airbus_cyber_security.graylog.wizard.alert.model.TriggeringConditions;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.AlertRuleStream;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.FieldRule;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.requests.AlertRuleRequest;
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
import org.graylog2.streams.events.StreamsChangedEvent;
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

    private Stream loadSecondStream(String streamIdentifier)  {
        if (streamIdentifier == null) {
            return null;
        }
        return this.loadStream(streamIdentifier);
    }

    private GetDataAlertRule constructDataAlertRule(AlertRule alert) {
        String eventIdentifier = alert.event1();
        EventDefinitionDto event = this.eventDefinitionService.getEventDefinition(eventIdentifier);
        NotificationDto notification = this.notificationService.get(alert.getNotificationID());
        TriggeringConditions conditions1 = alert.conditions1();
        Stream stream = this.loadStream(conditions1.streamIdentifier());
        DateTime lastModified = alert.getLastModified();
        Map<String, Object> parametersCondition = this.conversions.getConditionParameters(event.config());
        AlertRuleStream alertRuleStream = this.conversions.constructAlertRuleStream(stream, conditions1.pipelineFieldRules());
        AlertRuleStream alertRuleStream2 = null;
        TriggeringConditions conditions2 = alert.conditions2();
        if (conditions2 != null) {
            Stream secondStream = this.loadSecondStream(conditions2.streamIdentifier());
            alertRuleStream2 = this.conversions.constructSecondAlertRuleStream(secondStream, conditions2.pipelineFieldRules());
        }
        LoggingNotificationConfig loggingNotificationConfig = (LoggingNotificationConfig) notification.config();

        boolean isDisabled = false;
        if (stream != null) {
            isDisabled = stream.getDisabled();
        }

        return GetDataAlertRule.create(alert.getTitle(),
                loggingNotificationConfig.severity().getType(),
                event.id(),
                alert.event2(),
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

    public Pipeline createPipelineAndRule(Stream stream, String alertTitle, List<FieldRule> pipelineFieldRules, String matchingType) {
        if (pipelineFieldRules.isEmpty()) {
            return new Pipeline(null, null);
        }
        PipelineDao pipeline = this.streamPipelineService.createPipeline(alertTitle, matchingType);
        RuleDao pipelineRule = this.streamPipelineService.createPipelineRule(alertTitle, pipelineFieldRules, stream);
        return new Pipeline(pipeline.id(), pipelineRule.id());
    }

    private String createEvent(String alertTitle, String description, String notificationIdentifier, String conditionType, Map<String, Object> conditionParameters, UserContext userContext, TriggeringConditions conditions1, TriggeringConditions conditions2) {
        // TODO this is a code smell.
        // TODO should rather start with getting the conditionType, then according to its value, should go on performing different kind of creation/update => then there wouldn't be a null
        String streamIdentifier2 = null;
        if (conditions2 != null) {
            streamIdentifier2 = conditions2.streamIdentifier();
        }
        EventProcessorConfig configuration = this.conversions.createCondition(conditionType, conditionParameters, conditions1.streamIdentifier(), streamIdentifier2);
        return this.eventDefinitionService.createEvent(alertTitle, description, notificationIdentifier, configuration, userContext);
    }

    private String createSecondEvent(String alertTitle, String description, String notificationIdentifier, Map<String, Object> conditionParameters, UserContext userContext, String streamIdentifier2) {
        EventProcessorConfig configuration2 = this.conversions.createAggregationCondition(streamIdentifier2, conditionParameters);
        return this.eventDefinitionService.createEvent(alertTitle + "#2", description, notificationIdentifier, configuration2, userContext);
    }

    private TriggeringConditions createTriggeringConditions(AlertRuleStream streamConfiguration, String title, String userName) throws ValidationException {
        List<FieldRule> fieldRulesWithList = this.streamPipelineService.extractPipelineFieldRules(streamConfiguration.getFieldRules());
        Stream stream = this.streamPipelineService.createStream(streamConfiguration, title, userName);

        String pipelineIdentifier = null;
        String pipelineRuleIdentifier = null;
        if (!fieldRulesWithList.isEmpty()) {
            PipelineDao pipeline = this.streamPipelineService.createPipeline(title, streamConfiguration.getMatchingType());
            RuleDao pipelineRule = this.streamPipelineService.createPipelineRule(title, fieldRulesWithList, stream);
            pipelineIdentifier = pipeline.id();
            pipelineRuleIdentifier = pipelineRule.id();
        }

        return TriggeringConditions.create(stream.getId(), pipelineIdentifier, pipelineRuleIdentifier, fieldRulesWithList);
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
        AlertRuleStream streamConfiguration = request.getStream();
        String severity = request.getSeverity();
        String conditionType = request.getConditionType();
        String description = request.getDescription();
        Map<String, Object> conditionParameters = request.conditionParameters();

        String notificationIdentifier = this.notificationService.createNotification(alertTitle, severity, userContext);

        TriggeringConditions conditions1 = createTriggeringConditions(streamConfiguration, alertTitle, userName);

        // Create second stream and pipeline
        TriggeringConditions conditions2 = null;
        if (conditionType.equals("THEN") || conditionType.equals("AND") || conditionType.equals("OR")) {
            conditions2 = this.createTriggeringConditions(streamConfiguration, alertTitle + "#2", userName);
        }

        //Create Events
        String eventIdentifier = createEvent(alertTitle, description, notificationIdentifier, conditionType, conditionParameters, userContext, conditions1, conditions2);
        String eventIdentifier2 = null;
        if (conditionType.equals("OR")) {
            eventIdentifier2 = createSecondEvent(alertTitle, description, notificationIdentifier, conditionParameters, userContext, conditions2.streamIdentifier());
        }

        this.clusterEventBus.post(StreamsChangedEvent.create(conditions1.streamIdentifier()));

        AlertRule alertRule = AlertRule.create(
                alertTitle,
                conditionType,
                conditions1,
                conditions2,
                eventIdentifier,
                eventIdentifier2,
                notificationIdentifier,
                DateTime.now(),
                userName,
                DateTime.now());
        alertRule = this.alertRuleService.create(alertRule);

        //Update list usage
        for (FieldRule fieldRule: this.nullSafe(conditions1.pipelineFieldRules())) {
            this.alertListUtilsService.incrementUsage(fieldRule.getValue());
        }
        if (conditions2 != null) {
            for (FieldRule fieldRule : this.nullSafe(conditions2.pipelineFieldRules())) {
                this.alertListUtilsService.incrementUsage(fieldRule.getValue());
            }
        }

        GetDataAlertRule result = this.constructDataAlertRule(alertRule);
        return Response.ok().entity(result).build();
    }

    private TriggeringConditions updateConditions(TriggeringConditions previousConditions, String alertTitle, AlertRuleStream streamRequest) throws ValidationException {
        List<FieldRule> fieldRulesWithList = this.streamPipelineService.extractPipelineFieldRules(streamRequest.getFieldRules());

        // update stream
        Stream stream = this.loadStream(previousConditions.streamIdentifier());
        this.streamPipelineService.updateStream(stream, streamRequest, alertTitle);

        // update pipeline
        this.streamPipelineService.deletePipeline(previousConditions.pipelineIdentifier(), previousConditions.pipelineRuleIdentifier());

        if (fieldRulesWithList.isEmpty()) {
            return TriggeringConditions.create(previousConditions.streamIdentifier(),
                    null,
                    null,
                    fieldRulesWithList);
        }

        PipelineDao pipeline = this.streamPipelineService.createPipeline(alertTitle, streamRequest.getMatchingType());
        RuleDao pipelineRule = this.streamPipelineService.createPipelineRule(alertTitle, fieldRulesWithList, stream);

        return TriggeringConditions.create(previousConditions.streamIdentifier(),
                pipeline.id(),
                pipelineRule.id(),
                fieldRulesWithList);
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
        String alertTitle = request.getTitle();
        String userName = getCurrentUser().getName();

        AlertRuleStream streamRequest = request.getStream();
        TriggeringConditions previousConditions1 = previousAlert.conditions1();
        TriggeringConditions conditions1 = updateConditions(previousConditions1, alertTitle, streamRequest);

        TriggeringConditions previousConditions2 = previousAlert.conditions2();
        TriggeringConditions conditions2 = null;

        // Update stream 2.
        Stream stream2 = this.streamPipelineService.createOrUpdateSecondStream(request.getSecondStream(), alertTitle, userName, request.getConditionType(), previousAlert);
        String streamID2 = null;

        // update pipeline 2
        if (previousConditions2 != null) {
            this.streamPipelineService.deletePipeline(previousConditions2.pipelineIdentifier(), previousConditions2.pipelineRuleIdentifier());
        }
        if (stream2 != null) {
            streamID2 = stream2.getId();
            List<FieldRule> fieldRules2 = this.streamPipelineService.extractPipelineFieldRules(request.getSecondStream().getFieldRules());
            Pipeline pipeline2 = this.createPipelineAndRule(stream2, alertTitle + "#2", fieldRules2, request.getStream().getMatchingType());
            // TODO try using updateConditions instead
            conditions2 = TriggeringConditions.create(streamID2, pipeline2.getPipelineID(), pipeline2.getPipelineRuleID(), fieldRules2);
        }

        // update Notification
        this.notificationService.updateNotification(alertTitle, previousAlert.getNotificationID(), request.getSeverity());

        // Create Type
        EventProcessorConfig configuration = this.conversions.createCondition(request.getConditionType(), request.conditionParameters(), conditions1.streamIdentifier(), streamID2);

        // Update Event
        this.eventDefinitionService.updateEvent(alertTitle, request.getDescription(), previousAlert.event1(), configuration);

        String eventIdentifier2 = previousAlert.event2();
        //Or Condition for Second Stream
        if (request.getConditionType().equals("OR") && stream2 != null) {
            EventProcessorConfig configuration2 = this.conversions.createAggregationCondition(stream2.getId(), request.conditionParameters());
            if (previousAlert.getAlertType().equals("OR")) {
                // Update Event
                this.eventDefinitionService.updateEvent(alertTitle + "#2", request.getDescription(), eventIdentifier2, configuration2);
            } else {
                //Create Event
                eventIdentifier2 = this.eventDefinitionService.createEvent(alertTitle + "#2", request.getDescription(), previousAlert.getNotificationID(), configuration2, userContext);
            }
        } else if (previousAlert.getAlertType().equals("OR")) {
            //Delete Event
            this.eventDefinitionService.delete(eventIdentifier2);
        }

        AlertRule alertRule = AlertRule.create(
                alertTitle,
                request.getConditionType(),
                conditions1,
                conditions2,
                previousAlert.event1(),
                eventIdentifier2,
                previousAlert.getNotificationID(),
                previousAlert.getCreatedAt(),
                userName,
                DateTime.now());
        alertRule = this.alertRuleService.update(java.net.URLDecoder.decode(title, ENCODING), alertRule);

        // Decrement list usage
        for (FieldRule fieldRule: this.nullSafe(previousConditions1.pipelineFieldRules())) {
            this.alertListUtilsService.decrementUsage(fieldRule.getValue());
        }
        // Increment list usage
        for (FieldRule fieldRule: this.nullSafe(conditions1.pipelineFieldRules())) {
            this.alertListUtilsService.incrementUsage(fieldRule.getValue());
        }
        if (previousConditions2 != null) {
            for (FieldRule fieldRule : this.nullSafe(previousConditions2.pipelineFieldRules())) {
                this.alertListUtilsService.decrementUsage(fieldRule.getValue());
            }
        }
        if (conditions2 != null) {
            for (FieldRule fieldRule : this.nullSafe(conditions2.pipelineFieldRules())) {
                this.alertListUtilsService.incrementUsage(fieldRule.getValue());
            }
        }

        GetDataAlertRule result = this.constructDataAlertRule(alertRule);
        return Response.accepted().entity(result).build();
    }

    private void deleteTriggeringConditions(TriggeringConditions conditions) {
        this.streamPipelineService.deleteStreamFromIdentifier(conditions.streamIdentifier());
        if (conditions.pipelineIdentifier() != null && conditions.pipelineRuleIdentifier() != null) {
            this.streamPipelineService.deletePipeline(conditions.pipelineIdentifier(), conditions.pipelineRuleIdentifier());
        }
        for (FieldRule fieldRule: this.nullSafe(conditions.pipelineFieldRules())) {
            this.alertListUtilsService.decrementUsage(fieldRule.getValue());
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
            deleteTriggeringConditions(alertRule.conditions1());

            TriggeringConditions conditions2 = alertRule.conditions2();
            if (conditions2 != null) {
                deleteTriggeringConditions(conditions2);
            }

            // Delete Event
            if (alertRule.event1() != null && !alertRule.event1().isEmpty()) {
                this.eventDefinitionService.delete(alertRule.event1());
            }
            if (alertRule.event2() != null && !alertRule.event2().isEmpty()) {
                this.eventDefinitionService.delete(alertRule.event2());
            }
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
