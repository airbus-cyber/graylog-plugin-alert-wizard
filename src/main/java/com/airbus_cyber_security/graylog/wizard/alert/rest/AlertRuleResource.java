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

import com.airbus_cyber_security.graylog.wizard.alert.model.AlertRule;
import com.airbus_cyber_security.graylog.wizard.alert.business.AlertRuleService;
import com.airbus_cyber_security.graylog.wizard.alert.model.AlertRuleStream;
import com.airbus_cyber_security.graylog.wizard.alert.model.FieldRule;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.requests.AlertRuleRequest;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.responses.GetDataAlertRule;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.responses.GetListAlertRule;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.responses.GetListDataAlertRule;
import com.airbus_cyber_security.graylog.wizard.alert.business.AlertRuleUtils;
import com.airbus_cyber_security.graylog.wizard.alert.business.AlertRuleUtilsService;
import com.airbus_cyber_security.graylog.wizard.alert.business.Pipeline;
import com.airbus_cyber_security.graylog.wizard.alert.business.StreamPipelineService;
import com.airbus_cyber_security.graylog.wizard.audit.AlertWizardAuditEventTypes;
import com.airbus_cyber_security.graylog.wizard.config.rest.AlertWizardConfig;
import com.airbus_cyber_security.graylog.wizard.config.rest.AlertWizardConfigurationService;
import com.airbus_cyber_security.graylog.wizard.config.rest.ImportPolicyType;
import com.airbus_cyber_security.graylog.wizard.database.LookupService;
import com.airbus_cyber_security.graylog.wizard.list.AlertListService;
import com.airbus_cyber_security.graylog.wizard.list.utilities.AlertListUtilsService;
import com.airbus_cyber_security.graylog.wizard.permissions.AlertRuleRestPermissions;
import com.codahale.metrics.annotation.Timed;
import com.mongodb.MongoException;
import io.swagger.annotations.*;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.graylog.events.processor.EventProcessorConfig;
import org.graylog.events.rest.EventDefinitionsResource;
import org.graylog.events.rest.EventNotificationsResource;
import org.graylog.plugins.pipelineprocessor.db.*;
import org.graylog.security.UserContext;
import org.graylog2.alerts.AlertService;
import org.graylog2.audit.jersey.AuditEvent;
import org.graylog2.database.NotFoundException;
import org.graylog2.events.ClusterEventBus;
import org.graylog2.indexer.IndexSetRegistry;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.graylog2.plugin.database.ValidationException;
import org.graylog2.plugin.rest.PluginRestResource;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.shared.rest.resources.RestResource;
import org.graylog2.streams.StreamRuleService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private final AlertWizardConfigurationService configurationService;
    private final EventDefinitionsResource eventDefinitionsResource;
    private final EventNotificationsResource eventNotificationsResource;

    private final AlertRuleService alertRuleService;
    private final AlertRuleUtils alertRuleUtils;
    private final AlertRuleUtilsService alertRuleUtilsService;
    private final StreamPipelineService streamPipelineService;
    private final AlertListUtilsService alertListUtilsService;


    @Inject
    public AlertRuleResource(AlertRuleService alertRuleService,
                             LookupService lookupService,
                             RuleService ruleService,
                             PipelineService pipelineService,
                             StreamService streamService,
                             StreamRuleService streamRuleService,
                             ClusterEventBus clusterEventBus,
                             IndexSetRegistry indexSetRegistry,
                             AlertService alertService,
                             AlertWizardConfigurationService configurationService,
                             ClusterConfigService clusterConfigService,
                             PipelineStreamConnectionsService pipelineStreamConnectionsService,
                             AlertListService alertListService,
                             EventDefinitionsResource eventDefinitionsResource,
                             EventNotificationsResource eventNotificationsResource) {
        this.alertRuleService = alertRuleService;
        this.streamService = streamService;
        this.clusterEventBus = clusterEventBus;
        this.configurationService = configurationService;
        this.eventDefinitionsResource = eventDefinitionsResource;
        this.eventNotificationsResource = eventNotificationsResource;

        this.alertRuleUtils = new AlertRuleUtils();
        this.alertListUtilsService = new AlertListUtilsService(alertListService);
        // TODO should probably inject AlertRuleUtilsService instead of instanciating it
        this.alertRuleUtilsService = new AlertRuleUtilsService(
                alertRuleService,
                streamService,
                alertService,
                this.alertRuleUtils,
                eventDefinitionsResource,
                eventNotificationsResource,
                clusterConfigService);
        // TODO should probably inject StreamPipelineService instead of instanciating it
        this.streamPipelineService = new StreamPipelineService(
                streamService,
                streamRuleService,
                clusterEventBus,
                indexSetRegistry.getDefault().getConfig().id(),
                ruleService,
                pipelineService,
                lookupService,
                pipelineStreamConnectionsService);
    }

    @GET
    @Timed
    @ApiOperation(value = "Lists all existing alerts")
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_READ)
    public GetListAlertRule list() {
        final List<AlertRule> alerts = this.alertRuleService.all();
        return GetListAlertRule.create(alerts);
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
        return this.alertRuleUtilsService.constructDataAlertRule(alert);
    }

    // TODO is this really necessary? Should either be just the GET, or the GET with a formatting option (if performance are really an issue)
    @GET
    @Path("/data")
    @Timed
    @ApiOperation(value = "Lists all existing alerts with additional data")
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_READ)
    public GetListDataAlertRule listWithData() {
        List<AlertRule> alerts = this.alertRuleService.all();

        List<GetDataAlertRule> alertsData = new ArrayList<>();
        for (AlertRule alert: alerts) {
            alertsData.add(this.alertRuleUtilsService.constructDataAlertRule(alert));
        }

        return GetListDataAlertRule.create(alertsData);
    }

    private String checkImportPolicyAndGetTitle(String title) {
        String alertTitle = title;
        if (this.alertRuleService.isPresent(alertTitle)) {
            // TODO should be get or default here: it will return null when starting with a fresh instance of graylog
            // Idem in AlertListRessource. Add a test that creates two alerts with same title
            AlertWizardConfig configGeneral = this.configurationService.getConfiguration();
            ImportPolicyType importPolicy = configGeneral.accessImportPolicy();
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
                    this.delete(alertTitle);
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

    public Pipeline createPipelineAndRule(Stream stream, String alertTitle, List<FieldRule> pipelineFieldRules, String matchingType){
        if (pipelineFieldRules.isEmpty()) {
            return new Pipeline(null, null);
        }
        RuleDao pipelineRule = this.streamPipelineService.createPipelineRule(alertTitle, pipelineFieldRules, stream);
        PipelineDao pipeline = this.streamPipelineService.createPipeline(alertTitle, matchingType);
        return new Pipeline(pipeline.id(), pipelineRule.id());
    }

    private String createEvent(String alertTitle, String notificationID, String conditionType, Map<String, Object> conditionParameters, UserContext userContext, String streamIdentifier, String streamIdentifier2) {
        EventProcessorConfig configuration = this.alertRuleUtilsService.createCondition(conditionType, conditionParameters, streamIdentifier, streamIdentifier2);
        return this.alertRuleUtilsService.createEvent(alertTitle, notificationID, configuration, userContext);
    }

    private String createSecondEvent(String alertTitle, String notificationID, String conditionType, Map<String, Object> conditionParameters, UserContext userContext, String streamIdentifier2) {
        if (!conditionType.equals("OR")) {
            return null;
        }
        EventProcessorConfig configuration2 = this.alertRuleUtilsService.createAggregationCondition(streamIdentifier2, conditionParameters);
        return this.alertRuleUtilsService.createEvent(alertTitle + "#2", notificationID, configuration2, userContext);
    }

    @POST
    @Timed
    @ApiOperation(value = "Create an alert")
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_CREATE)
    @ApiResponses(value = {@ApiResponse(code = 400, message = "The supplied request is not valid.")})
    @AuditEvent(type = AlertWizardAuditEventTypes.WIZARD_ALERTS_RULES_CREATE)
    public Response create(@ApiParam(name = "JSON body", required = true) @Valid @NotNull AlertRuleRequest request, @Context UserContext userContext)
            throws ValidationException, BadRequestException {

        this.alertRuleUtilsService.checkIsValidRequest(request);

        String userName = getCurrentUser().getName();
        String title = request.getTitle();
        String alertTitle = checkImportPolicyAndGetTitle(title);
        AlertRuleStream streamConfiguration = request.getStream();
        AlertRuleStream streamConfiguration2 = request.getSecondStream();
        String severity = request.getSeverity();
        String conditionType = request.getConditionType();
        String description = request.getDescription();
        Map<String, Object> conditionParameters = request.conditionParameters();

        String notificationID = this.alertRuleUtilsService.createNotification(alertTitle, severity, userContext);

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
        String eventIdentifier = createEvent(alertTitle, notificationID, conditionType, conditionParameters, userContext, streamIdentifier, streamIdentifier2);
        String eventIdentifier2 = createSecondEvent(alertTitle, notificationID, conditionType, conditionParameters, userContext, streamIdentifier2);

        this.clusterEventBus.post(StreamsChangedEvent.create(streamIdentifier));
        AlertRule alertRule = AlertRule.create(
                alertTitle,
                streamIdentifier,
                eventIdentifier,
                notificationID,
                DateTime.now(),
                userName,
                DateTime.now(),
                description,
                conditionType,
                streamIdentifier2,
                eventIdentifier2,
                pipeline.getPipelineID(),
                pipeline.getPipelineRuleID(),
                fieldRules,
                pipeline2.getPipelineID(),
                pipeline2.getPipelineRuleID(),
                fieldRules2);
        this.alertRuleService.create(alertRule);

        //Update list usage
        for (FieldRule fieldRule: this.alertRuleUtils.nullSafe(fieldRules)) {
            this.alertListUtilsService.incrementUsage(fieldRule.getValue());
        }
        for (FieldRule fieldRule: this.alertRuleUtils.nullSafe(fieldRules2)) {
            this.alertListUtilsService.incrementUsage(fieldRule.getValue());
        }

        return Response.ok().build();
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

        this.alertRuleUtilsService.checkIsValidRequest(request);

        AlertRule oldAlert = this.alertRuleService.load(title);
        String alertTitle = request.getTitle();
        String userName = getCurrentUser().getName();

        // Update stream.
        Stream stream = this.streamService.load(oldAlert.getStreamIdentifier());
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
        this.alertRuleUtilsService.updateNotification(alertTitle, oldAlert.getNotificationID(), request.getSeverity());

        //Create Condition
        EventProcessorConfig configuration = this.alertRuleUtilsService.createCondition(request.getConditionType(), request.conditionParameters(), stream.getId(), streamID2);

        // Update Event
        this.alertRuleUtilsService.updateEvent(alertTitle, oldAlert.getEventID(), configuration);

        String eventID2 = oldAlert.getSecondEventID();
        //Or Condition for Second Stream
        if (request.getConditionType().equals("OR") && stream2 != null) {
            EventProcessorConfig configuration2 = this.alertRuleUtilsService.createAggregationCondition(stream2.getId(), request.conditionParameters());
            if (oldAlert.getConditionType().equals("OR")) {
                // Update Event
                this.alertRuleUtilsService.updateEvent(alertTitle + "#2", eventID2, configuration2);
            } else {
                //Create Event
                eventID2 = this.alertRuleUtilsService.createEvent(alertTitle + "#2", oldAlert.getNotificationID(), configuration2, userContext);
            }
        } else if (oldAlert.getConditionType().equals("OR")) {
            //Delete Event
            this.eventDefinitionsResource.delete(eventID2);
        }

        AlertRule alertRule = AlertRule.create(
                alertTitle,
                oldAlert.getStreamIdentifier(),
                oldAlert.getEventID(),
                oldAlert.getNotificationID(),
                oldAlert.getCreatedAt(),
                userName,
                DateTime.now(),
                request.getDescription(),
                request.getConditionType(),
                streamID2,
                eventID2,
                pipeline.getPipelineID(),
                pipeline.getPipelineRuleID(),
                fieldRules,
                pipeline2.getPipelineID(),
                pipeline2.getPipelineRuleID(),
                fieldRules2);
        this.alertRuleService.update(java.net.URLDecoder.decode(title, ENCODING), alertRule);

        // Decrement list usage
        for (FieldRule fieldRule: this.alertRuleUtils.nullSafe(oldAlert.getPipelineFieldRules())) {
            this.alertListUtilsService.decrementUsage(fieldRule.getValue());
        }
        for (FieldRule fieldRule: this.alertRuleUtils.nullSafe(oldAlert.getSecondPipelineFieldRules())) {
            this.alertListUtilsService.decrementUsage(fieldRule.getValue());
        }
        // Increment list usage
        for (FieldRule fieldRule: this.alertRuleUtils.nullSafe(fieldRules)) {
            this.alertListUtilsService.incrementUsage(fieldRule.getValue());
        }
        for (FieldRule fieldRule: this.alertRuleUtils.nullSafe(fieldRules2)) {
            this.alertListUtilsService.incrementUsage(fieldRule.getValue());
        }

        return Response.accepted().build();
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
                       @PathParam(TITLE) String title
    ) throws MongoException, UnsupportedEncodingException {
        String alertTitle = java.net.URLDecoder.decode(title, ENCODING);

        try {
            AlertRule alertRule = this.alertRuleService.load(alertTitle);
            this.streamPipelineService.deleteStreamFromID(alertRule.getStreamIdentifier());

            //Delete second Stream
            if (alertRule.getSecondStreamID() != null && !alertRule.getSecondStreamID().isEmpty()) {
                this.streamPipelineService.deleteStreamFromID(alertRule.getSecondStreamID());
            }

            // Delete Event
            if (alertRule.getEventID() != null && !alertRule.getEventID().isEmpty()) {
                this.eventDefinitionsResource.delete(alertRule.getEventID());
            }
            if (alertRule.getNotificationID() != null && !alertRule.getNotificationID().isEmpty()) {
                this.eventNotificationsResource.delete(alertRule.getNotificationID());
            }
            if (alertRule.getSecondEventID() != null && !alertRule.getSecondEventID().isEmpty()) {
                this.eventDefinitionsResource.delete(alertRule.getSecondEventID());
            }

            // Delete Pipeline
            if (alertRule.getPipelineID() != null && alertRule.getPipelineRuleID() != null) {
                this.streamPipelineService.deletePipeline(alertRule.getPipelineID(), alertRule.getPipelineRuleID());
            }

            if (alertRule.getSecondPipelineID() != null && alertRule.getSecondPipelineRuleID() != null) {
                this.streamPipelineService.deletePipeline(alertRule.getSecondPipelineID(), alertRule.getSecondPipelineRuleID());
            }

            //Update list usage
            for (FieldRule fieldRule : this.alertRuleUtils.nullSafe(alertRule.getPipelineFieldRules())) {
                this.alertListUtilsService.decrementUsage(fieldRule.getValue());
            }
            for (FieldRule fieldRule : this.alertRuleUtils.nullSafe(alertRule.getSecondPipelineFieldRules())) {
                this.alertListUtilsService.decrementUsage(fieldRule.getValue());
            }
        } catch (NotFoundException e) {
            LOG.error("Cannot find alert " + alertTitle, e);
        }

        this.alertRuleService.destroy(alertTitle);
    }
}
