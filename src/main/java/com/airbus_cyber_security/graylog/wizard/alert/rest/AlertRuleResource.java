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

import com.airbus_cyber_security.graylog.wizard.alert.business.*;
import com.airbus_cyber_security.graylog.wizard.alert.business.AlertRuleService;
import com.airbus_cyber_security.graylog.wizard.alert.model.AlertRule;
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
        String eventIdentifier = alert.getEventID();
        EventDefinitionDto event = this.eventDefinitionService.getEventDefinition(eventIdentifier);
        NotificationDto notification = this.notificationService.get(alert.getNotificationID());
        Stream stream = this.loadStream(alert.getStreamIdentifier());
        DateTime lastModified = alert.getLastModified();
        Stream secondStream = this.loadSecondStream(alert.getSecondStreamID());
        return this.conversions.constructDataAlertRule(
                alert.getTitle(),
                stream,
                event,
                notification,
                alert.getCreatedAt(),
                alert.getCreatorUserId(),
                lastModified,
                alert.getConditionType(),
                secondStream,
                alert.getSecondEventID(),
                alert.getPipelineFieldRules(),
                alert.getSecondPipelineFieldRules());
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
        RuleDao pipelineRule = this.streamPipelineService.createPipelineRule(alertTitle, pipelineFieldRules, stream);
        PipelineDao pipeline = this.streamPipelineService.createPipeline(alertTitle, matchingType);
        return new Pipeline(pipeline.id(), pipelineRule.id());
    }

    private String createEvent(String alertTitle, String description, String notificationIdentifier, String conditionType, Map<String, Object> conditionParameters, UserContext userContext, String streamIdentifier, String streamIdentifier2) {
        EventProcessorConfig configuration = this.conversions.createCondition(conditionType, conditionParameters, streamIdentifier, streamIdentifier2);
        return this.eventDefinitionService.createEvent(alertTitle, description, notificationIdentifier, configuration, userContext);
    }

    private String createSecondEvent(String alertTitle, String description, String notificationIdentifier, String conditionType, Map<String, Object> conditionParameters, UserContext userContext, String streamIdentifier2) {
        if (!conditionType.equals("OR")) {
            return null;
        }
        EventProcessorConfig configuration2 = this.conversions.createAggregationCondition(streamIdentifier2, conditionParameters);
        return this.eventDefinitionService.createEvent(alertTitle + "#2", description, notificationIdentifier, configuration2, userContext);
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
        AlertRuleStream streamConfiguration2 = request.getSecondStream();
        String severity = request.getSeverity();
        String conditionType = request.getConditionType();
        String description = request.getDescription();
        Map<String, Object> conditionParameters = request.conditionParameters();

        String notificationIdentifier = this.notificationService.createNotification(alertTitle, severity, userContext);

        // Create stream and pipeline
        Stream stream = this.streamPipelineService.createStream(streamConfiguration, alertTitle, userName);
        List<FieldRule> fieldRules = this.streamPipelineService.extractPipelineFieldRules(streamConfiguration.getFieldRules());
        Pipeline pipeline = this.createPipelineAndRule(stream, alertTitle, fieldRules, streamConfiguration.getMatchingType());
        String streamIdentifier = stream.getId();

        // Create second stream and pipeline
        String streamIdentifier2 = null;
        List<FieldRule> fieldRules2 = null;
        Pipeline pipeline2 = new Pipeline(null, null);
        if (conditionType.equals("THEN") || conditionType.equals("AND") || conditionType.equals("OR")) {
            Stream stream2 = this.streamPipelineService.createStream(streamConfiguration2, alertTitle + "#2", userName);
            streamIdentifier2 = stream2.getId();
            fieldRules2 = this.streamPipelineService.extractPipelineFieldRules(streamConfiguration2.getFieldRules());
            pipeline2 = this.createPipelineAndRule(stream2, alertTitle + "#2", fieldRules2, streamConfiguration.getMatchingType());
        }

        //Create Events
        String eventIdentifier = createEvent(alertTitle, description, notificationIdentifier, conditionType, conditionParameters, userContext, streamIdentifier, streamIdentifier2);
        String eventIdentifier2 = createSecondEvent(alertTitle, description, notificationIdentifier, conditionType, conditionParameters, userContext, streamIdentifier2);

        this.clusterEventBus.post(StreamsChangedEvent.create(streamIdentifier));
        AlertRule alertRule = AlertRule.create(
                alertTitle,
                streamIdentifier,
                eventIdentifier,
                notificationIdentifier,
                DateTime.now(),
                userName,
                DateTime.now(),
                conditionType,
                streamIdentifier2,
                eventIdentifier2,
                pipeline.getPipelineID(),
                pipeline.getPipelineRuleID(),
                fieldRules,
                pipeline2.getPipelineID(),
                pipeline2.getPipelineRuleID(),
                fieldRules2);
        alertRule = this.alertRuleService.create(alertRule);

        //Update list usage
        for (FieldRule fieldRule: this.nullSafe(fieldRules)) {
            this.alertListUtilsService.incrementUsage(fieldRule.getValue());
        }
        for (FieldRule fieldRule: this.nullSafe(fieldRules2)) {
            this.alertListUtilsService.incrementUsage(fieldRule.getValue());
        }

        GetDataAlertRule result = this.constructDataAlertRule(alertRule);
        return Response.ok().entity(result).build();
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

        AlertRule oldAlert = this.alertRuleService.load(title);
        String alertTitle = request.getTitle();
        String userName = getCurrentUser().getName();

        // Update stream.
        Stream stream = this.loadStream(oldAlert.getStreamIdentifier());
        this.streamPipelineService.updateStream(stream, request.getStream(), alertTitle);

        //update pipeline
        this.streamPipelineService.deletePipeline(oldAlert.getPipelineID(), oldAlert.getPipelineRuleID());
        List<FieldRule> fieldRules = this.streamPipelineService.extractPipelineFieldRules(request.getStream().getFieldRules());
        Pipeline pipeline = this.createPipelineAndRule(stream, alertTitle, fieldRules, request.getStream().getMatchingType());

        // Update stream 2.
        Stream stream2 = this.streamPipelineService.createOrUpdateSecondStream(request.getSecondStream(), alertTitle, userName, request.getConditionType(), oldAlert);
        String streamID2 = null;

        // update pipeline 2
        this.streamPipelineService.deletePipeline(oldAlert.getSecondPipelineID(), oldAlert.getSecondPipelineRuleID());
        Pipeline pipeline2 = new Pipeline(null, null);
        List<FieldRule> fieldRules2 = null;
        if (stream2 != null) {
            streamID2 = stream2.getId();
            fieldRules2 = this.streamPipelineService.extractPipelineFieldRules(request.getSecondStream().getFieldRules());
            pipeline2 = this.createPipelineAndRule(stream2, alertTitle + "#2", fieldRules2, request.getStream().getMatchingType());
        }

        //update Notification
        this.notificationService.updateNotification(alertTitle, oldAlert.getNotificationID(), request.getSeverity());

        //Create Condition
        EventProcessorConfig configuration = this.conversions.createCondition(request.getConditionType(), request.conditionParameters(), stream.getId(), streamID2);

        // Update Event
        this.eventDefinitionService.updateEvent(alertTitle, request.getDescription(), oldAlert.getEventID(), configuration);

        String eventIdentifier2 = oldAlert.getSecondEventID();
        //Or Condition for Second Stream
        if (request.getConditionType().equals("OR") && stream2 != null) {
            EventProcessorConfig configuration2 = this.conversions.createAggregationCondition(stream2.getId(), request.conditionParameters());
            if (oldAlert.getConditionType().equals("OR")) {
                // Update Event
                this.eventDefinitionService.updateEvent(alertTitle + "#2", request.getDescription(), eventIdentifier2, configuration2);
            } else {
                //Create Event
                eventIdentifier2 = this.eventDefinitionService.createEvent(alertTitle + "#2", request.getDescription(), oldAlert.getNotificationID(), configuration2, userContext);
            }
        } else if (oldAlert.getConditionType().equals("OR")) {
            //Delete Event
            this.eventDefinitionService.delete(eventIdentifier2);
        }

        AlertRule alertRule = AlertRule.create(
                alertTitle,
                oldAlert.getStreamIdentifier(),
                oldAlert.getEventID(),
                oldAlert.getNotificationID(),
                oldAlert.getCreatedAt(),
                userName,
                DateTime.now(),
                request.getConditionType(),
                streamID2,
                eventIdentifier2,
                pipeline.getPipelineID(),
                pipeline.getPipelineRuleID(),
                fieldRules,
                pipeline2.getPipelineID(),
                pipeline2.getPipelineRuleID(),
                fieldRules2);
        alertRule = this.alertRuleService.update(java.net.URLDecoder.decode(title, ENCODING), alertRule);

        // Decrement list usage
        for (FieldRule fieldRule: this.nullSafe(oldAlert.getPipelineFieldRules())) {
            this.alertListUtilsService.decrementUsage(fieldRule.getValue());
        }
        for (FieldRule fieldRule: this.nullSafe(oldAlert.getSecondPipelineFieldRules())) {
            this.alertListUtilsService.decrementUsage(fieldRule.getValue());
        }
        // Increment list usage
        for (FieldRule fieldRule: this.nullSafe(fieldRules)) {
            this.alertListUtilsService.incrementUsage(fieldRule.getValue());
        }
        for (FieldRule fieldRule: this.nullSafe(fieldRules2)) {
            this.alertListUtilsService.incrementUsage(fieldRule.getValue());
        }

        GetDataAlertRule result = this.constructDataAlertRule(alertRule);
        return Response.accepted().entity(result).build();
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
            this.streamPipelineService.deleteStreamFromIdentifier(alertRule.getStreamIdentifier());

            //Delete second Stream
            if (alertRule.getSecondStreamID() != null && !alertRule.getSecondStreamID().isEmpty()) {
                this.streamPipelineService.deleteStreamFromIdentifier(alertRule.getSecondStreamID());
            }

            // Delete Event
            if (alertRule.getEventID() != null && !alertRule.getEventID().isEmpty()) {
                this.eventDefinitionService.delete(alertRule.getEventID());
            }
            if (alertRule.getNotificationID() != null && !alertRule.getNotificationID().isEmpty()) {
                // TODO move this down into AlertRuleUtilsService and remove the use for eventNotificationsResource
                this.eventNotificationsResource.delete(alertRule.getNotificationID(), userContext);
            }
            if (alertRule.getSecondEventID() != null && !alertRule.getSecondEventID().isEmpty()) {
                this.eventDefinitionService.delete(alertRule.getSecondEventID());
            }

            // Delete Pipeline
            if (alertRule.getPipelineID() != null && alertRule.getPipelineRuleID() != null) {
                this.streamPipelineService.deletePipeline(alertRule.getPipelineID(), alertRule.getPipelineRuleID());
            }

            if (alertRule.getSecondPipelineID() != null && alertRule.getSecondPipelineRuleID() != null) {
                this.streamPipelineService.deletePipeline(alertRule.getSecondPipelineID(), alertRule.getSecondPipelineRuleID());
            }

            //Update list usage
            for (FieldRule fieldRule : this.nullSafe(alertRule.getPipelineFieldRules())) {
                this.alertListUtilsService.decrementUsage(fieldRule.getValue());
            }
            for (FieldRule fieldRule : this.nullSafe(alertRule.getSecondPipelineFieldRules())) {
                this.alertListUtilsService.decrementUsage(fieldRule.getValue());
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
