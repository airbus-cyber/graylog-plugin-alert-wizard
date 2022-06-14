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

package com.airbus_cyber_security.graylog.wizard.alert.rest;

import com.airbus_cyber_security.graylog.wizard.alert.AlertRule;
import com.airbus_cyber_security.graylog.wizard.alert.AlertRuleService;
import com.airbus_cyber_security.graylog.wizard.alert.AlertRuleStream;
import com.airbus_cyber_security.graylog.wizard.alert.FieldRule;
import com.airbus_cyber_security.graylog.wizard.alert.bundles.AlertRuleExporter;
import com.airbus_cyber_security.graylog.wizard.alert.bundles.ExportAlertRule;
import com.airbus_cyber_security.graylog.wizard.alert.bundles.ExportAlertRuleRequest;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.requests.AlertRuleRequest;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.requests.CloneAlertRuleRequest;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.responses.GetDataAlertRule;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.responses.GetListAlertRule;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.responses.GetListDataAlertRule;
import com.airbus_cyber_security.graylog.wizard.alert.utilities.AlertRuleUtils;
import com.airbus_cyber_security.graylog.wizard.alert.utilities.AlertRuleUtilsService;
import com.airbus_cyber_security.graylog.wizard.alert.utilities.StreamPipelineObject;
import com.airbus_cyber_security.graylog.wizard.alert.utilities.StreamPipelineService;
import com.airbus_cyber_security.graylog.wizard.audit.AlertWizardAuditEventTypes;
import com.airbus_cyber_security.graylog.wizard.config.rest.AlertWizardConfig;
import com.airbus_cyber_security.graylog.wizard.config.rest.ImportPolicyType;
import com.airbus_cyber_security.graylog.events.notifications.types.LoggingNotificationConfig;
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
import org.graylog2.configuration.HttpConfiguration;
import org.graylog2.database.NotFoundException;
import org.graylog2.events.ClusterEventBus;
import org.graylog2.indexer.IndexSetRegistry;
import org.graylog2.lookup.db.DBCacheService;
import org.graylog2.lookup.db.DBDataAdapterService;
import org.graylog2.lookup.db.DBLookupTableService;
import org.graylog2.lookup.dto.CacheDto;
import org.graylog2.lookup.dto.DataAdapterDto;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.graylog2.plugin.database.ValidationException;
import org.graylog2.plugin.rest.PluginRestResource;
import org.graylog2.plugin.streams.Output;
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
    private final ClusterConfigService clusterConfigService;
    private final EventDefinitionsResource eventDefinitionsResource;
    private final EventNotificationsResource eventNotificationsResource;

    private final AlertRuleService alertRuleService;
    private final AlertRuleUtils alertRuleUtils;
    private final AlertRuleUtilsService alertRuleUtilsService;
    private final StreamPipelineService streamPipelineService;
    private final AlertListUtilsService alertListUtilsService;
    private final AlertRuleExporter alertRuleExporter;


    @Inject
    public AlertRuleResource(AlertRuleService alertRuleService,
                             RuleService ruleService,
                             PipelineService pipelineService,
                             DBDataAdapterService dbDataAdapterService,
                             HttpConfiguration httpConfiguration,
                             DBCacheService dbCacheService,
                             DBLookupTableService dbTableService,
                             StreamService streamService,
                             StreamRuleService streamRuleService,
                             ClusterEventBus clusterEventBus,
                             IndexSetRegistry indexSetRegistry,
                             AlertService alertService,
                             ClusterConfigService clusterConfigService,
                             PipelineStreamConnectionsService pipelineStreamConnectionsService,
                             AlertListService alertListService,
                             EventDefinitionsResource eventDefinitionsResource,
                             EventNotificationsResource eventNotificationsResource) {
        this.alertRuleService = alertRuleService;
        this.streamService = streamService;
        this.clusterEventBus = clusterEventBus;
        this.clusterConfigService = clusterConfigService;
        this.eventDefinitionsResource = eventDefinitionsResource;
        this.eventNotificationsResource = eventNotificationsResource;

        this.alertRuleUtils = new AlertRuleUtils();
        this.alertListUtilsService = new AlertListUtilsService(alertListService);
        this.alertRuleExporter = new AlertRuleExporter(
                alertRuleService,
                streamService,
                alertRuleUtils,
                eventDefinitionsResource,
                eventNotificationsResource);
        this.alertRuleUtilsService = new AlertRuleUtilsService(
                alertRuleService,
                streamService,
                alertService,
                alertRuleUtils,
                eventDefinitionsResource,
                eventNotificationsResource,
                clusterConfigService);
        this.streamPipelineService = new StreamPipelineService(
                streamService,
                streamRuleService,
                clusterEventBus,
                indexSetRegistry.getDefault().getConfig().id(),
                ruleService,
                pipelineService,
                dbDataAdapterService,
                httpConfiguration,
                dbCacheService,
                dbTableService,
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

    @GET
    @Path("/data")
    @Timed
    @ApiOperation(value = "Lists all existing alerts with additional data")
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_READ)
    public GetListDataAlertRule listWithData() {
        final List<AlertRule> alerts = this.alertRuleService.all();

        List<GetDataAlertRule> alertsData = new ArrayList<>();
        for (AlertRule alert : alerts) {
            try {
                alertsData.add(this.alertRuleUtilsService.constructDataAlertRule(alert));
            } catch (NotFoundException e) {
                LOG.warn("Alert " + alert.getTitle() + " is broken: " + e.getMessage());
                alertsData.add(GetDataAlertRule.create(alert.getTitle(), alert.getTitle(),
                        "",
                        alert.getEventID(),
                        alert.getNotificationID(),
                        alert.getCreatedAt(),
                        alert.getCreatorUserId(),
                        alert.getLastModified(),
                        false,
                        alert.getDescription(),
                        0,
                        alert.getConditionType(),
                        null,
                        null,
                        null));
            }
        }

        return GetListDataAlertRule.create(alertsData);
    }

    private String checkImportPolicyAndGetTitle(String title) {
        String alertTitle = title;
        if (this.alertRuleService.isPresent(alertTitle)) {
            final AlertWizardConfig configGeneral = clusterConfigService.get(AlertWizardConfig.class);
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
                LOG.error("Failed to create alert rule : Alert rule title already exist");
                throw new BadRequestException("Failed to create alert rule : Alert rule title already exist.");
            }
        }
        return alertTitle;
    }

    private StreamPipelineObject createStreamAndPipeline(AlertRuleStream stream, String title) throws ValidationException {
        String alertTitle = checkImportPolicyAndGetTitle(title);
        String userName = getCurrentUser().getName();
        StreamPipelineObject streamPipelineObject = this.streamPipelineService.createStreamAndPipeline(stream, alertTitle, userName, stream.getMatchingType());

        //Create unique data adapter
        DataAdapterDto adapter = this.streamPipelineService.createUniqueDataAdapter(userName);
        CacheDto cache = this.streamPipelineService.createUniqueCache();
        this.streamPipelineService.createUniqueLookup(cache, adapter);

        return streamPipelineObject;
    }

    private void createAlertRule(AlertRuleStream stream, AlertRuleStream secondStream, String streamIdentifier, String alertTitle, String notificationID, String userName,
                                 String description, String conditionType,
                                 StreamPipelineObject streamPipelineObject,
                                 Map<String, Object> conditionParameters, UserContext userContext) throws ValidationException {
        // Create second stream and pipeline
        String streamID2 = null;
        StreamPipelineObject streamPipelineObject2 = new StreamPipelineObject(null, null, null, null);
        if (conditionType.equals("THEN") || conditionType.equals("AND") || conditionType.equals("OR")) {
            streamPipelineObject2 = this.streamPipelineService.createStreamAndPipeline(secondStream, alertTitle + "#2", userName, stream.getMatchingType());
            streamID2 = streamPipelineObject2.getStream().getId();
        }

        // Create Condition
        EventProcessorConfig configuration = this.alertRuleUtilsService.createCondition(conditionType, conditionParameters, streamIdentifier, streamID2);
        //Create Event
        String eventID = this.alertRuleUtilsService.createEvent(alertTitle, notificationID, configuration, userContext);

        String eventID2 = null;
        //Or Event for Second Stream
        if (conditionType.equals("OR") && streamPipelineObject2.getStream() != null) {
            //Create Condition
            EventProcessorConfig configuration2 = this.alertRuleUtilsService.createAggregationCondition(streamID2, conditionParameters);
            //Create Event
            eventID2 = this.alertRuleUtilsService.createEvent(alertTitle + "#2", notificationID, configuration2, userContext);
        }

        this.clusterEventBus.post(StreamsChangedEvent.create(streamIdentifier));
        this.alertRuleService.create(AlertRule.create(
                alertTitle,
                streamIdentifier,
                eventID,
                notificationID,
                DateTime.now(),
                userName,
                DateTime.now(),
                description,
                conditionType,
                streamID2,
                eventID2,
                streamPipelineObject.getPipelineID(),
                streamPipelineObject.getPipelineRuleID(),
                streamPipelineObject.getListPipelineFieldRule(),
                streamPipelineObject2.getPipelineID(),
                streamPipelineObject2.getPipelineRuleID(),
                streamPipelineObject2.getListPipelineFieldRule()));

        //Update list usage
        for (FieldRule fieldRule: alertRuleUtils.nullSafe(streamPipelineObject.getListPipelineFieldRule())) {
            alertListUtilsService.incrementUsage(fieldRule.getValue());
        }
        for (FieldRule fieldRule: alertRuleUtils.nullSafe(streamPipelineObject2.getListPipelineFieldRule())) {
            alertListUtilsService.incrementUsage(fieldRule.getValue());
        }
    }

    private void importAlertRule(ExportAlertRule alertRule, UserContext userContext) throws ValidationException {
        String title = alertRule.getTitle();
        AlertRuleStream stream = alertRule.getStream();
        AlertRuleStream secondStream = alertRule.getSecondStream();
        String conditionType = alertRule.getConditionType();
        String description = alertRule.getDescription();
        Map<String, Object> conditionParameters = alertRule.conditionParameters();

        // Create stream and pipeline
        StreamPipelineObject streamPipelineObject = this.createStreamAndPipeline(stream, title);
        String streamIdentifier = streamPipelineObject.getStream().getId();

        String userName = getCurrentUser().getName();
        String alertTitle = checkImportPolicyAndGetTitle(title);

        // Create Notification
        String notificationID = this.alertRuleUtilsService.createNotificationFromParameters(alertTitle, alertRule.notificationParameters(), userContext);

        createAlertRule(stream, secondStream, streamIdentifier, alertTitle, notificationID, userName, description, conditionType,
                streamPipelineObject, conditionParameters, userContext);
        LOG.debug("User: " + userName + " successfully import alert rule: " + alertTitle);
    }

    @POST
    @Timed
    @ApiOperation(value = "Create a alert")
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_CREATE)
    @ApiResponses(value = {@ApiResponse(code = 400, message = "The supplied request is not valid.")})
    @AuditEvent(type = AlertWizardAuditEventTypes.WIZARD_ALERTS_RULES_CREATE)
    public Response create(@ApiParam(name = "JSON body", required = true) @Valid @NotNull AlertRuleRequest request, @Context UserContext userContext)
            throws ValidationException, BadRequestException {

        this.alertRuleUtilsService.checkIsValidRequest(request);
        AlertRuleStream stream = request.getStream();
        AlertRuleStream secondStream = request.getSecondStream();
        String title = request.getTitle();
        String conditionType = request.getConditionType();
        String description = request.getDescription();
        Map<String, Object> conditionParameters = request.conditionParameters();

        // Create stream and pipeline
        StreamPipelineObject streamPipelineObject = this.createStreamAndPipeline(stream, title);
        String streamIdentifier = streamPipelineObject.getStream().getId();

        String alertTitle = checkImportPolicyAndGetTitle(title);
        String userName = getCurrentUser().getName();

        // Create Notification
        String notificationID = this.alertRuleUtilsService.createNotification(alertTitle, request.getSeverity(), userContext);

        createAlertRule(stream, secondStream, streamIdentifier, alertTitle, notificationID, userName, description, conditionType,
                streamPipelineObject, conditionParameters, userContext);

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
        Stream stream = streamService.load(oldAlert.getStreamID());
        this.streamPipelineService.updateStream(stream, request.getStream(), alertTitle);

        //update pipeline
        StreamPipelineObject streamPipelineObject = this.streamPipelineService.updatePipeline(alertTitle, oldAlert.getPipelineID(), oldAlert.getPipelineRuleID(), request.getStream().getFieldRules(), stream, request.getStream().getMatchingType());

        // Update stream 2.
        Stream stream2 = this.streamPipelineService.createOrUpdateSecondStream(request.getSecondStream(), alertTitle, userName, request.getConditionType(), oldAlert);
        String streamID2 = null;

        //update pipeline 2
        StreamPipelineObject streamPipelineObject2 = new StreamPipelineObject(null, null, null, null);
        if (stream2 != null) {
            streamID2 = stream2.getId();
            streamPipelineObject2 = this.streamPipelineService.updatePipeline(alertTitle + "#2", oldAlert.getSecondPipelineID(), oldAlert.getSecondPipelineRuleID(), request.getSecondStream().getFieldRules(), stream2, request.getStream().getMatchingType());
        } else if (oldAlert.getSecondStreamID() != null) {
            this.streamPipelineService.deletePipeline(oldAlert.getSecondPipelineID(), oldAlert.getSecondPipelineRuleID());
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
            if (oldAlert.getConditionType().equals("OR")) {
                //Create Condition
                EventProcessorConfig configuration2 = this.alertRuleUtilsService.createAggregationCondition(stream2.getId(), request.conditionParameters());
                // Update Event
                this.alertRuleUtilsService.updateEvent(alertTitle + "#2", eventID2, configuration2);
            } else {
                //Create Condition
                EventProcessorConfig configuration2 = this.alertRuleUtilsService.createAggregationCondition(stream2.getId(), request.conditionParameters());
                //Create Event
                eventID2 = this.alertRuleUtilsService.createEvent(alertTitle + "#2", oldAlert.getNotificationID(), configuration2, userContext);
            }
        } else if (oldAlert.getConditionType().equals("OR")) {
            //Delete Event
            eventDefinitionsResource.delete(eventID2);
        }

        this.alertRuleService.update(java.net.URLDecoder.decode(title, ENCODING),
                AlertRule.create(
                        alertTitle,
                        oldAlert.getStreamID(),
                        oldAlert.getEventID(),
                        oldAlert.getNotificationID(),
                        oldAlert.getCreatedAt(),
                        userName,
                        DateTime.now(),
                        request.getDescription(),
                        request.getConditionType(),
                        streamID2,
                        eventID2,
                        streamPipelineObject.getPipelineID(),
                        streamPipelineObject.getPipelineRuleID(),
                        streamPipelineObject.getListPipelineFieldRule(),
                        streamPipelineObject2.getPipelineID(),
                        streamPipelineObject2.getPipelineRuleID(),
                        streamPipelineObject2.getListPipelineFieldRule()));

        //Decrement list usage
        for (FieldRule fieldRule : alertRuleUtils.nullSafe(oldAlert.getPipelineFieldRules())) {
            alertListUtilsService.decrementUsage(fieldRule.getValue());
        }
        for (FieldRule fieldRule : alertRuleUtils.nullSafe(oldAlert.getSecondPipelineFieldRules())) {
            alertListUtilsService.decrementUsage(fieldRule.getValue());
        }
        //Increment list usage
        for (FieldRule fieldRule : alertRuleUtils.nullSafe(streamPipelineObject.getListPipelineFieldRule())) {
            alertListUtilsService.incrementUsage(fieldRule.getValue());
        }
        for (FieldRule fieldRule : alertRuleUtils.nullSafe(streamPipelineObject2.getListPipelineFieldRule())) {
            alertListUtilsService.incrementUsage(fieldRule.getValue());
        }

        return Response.accepted().build();
    }

    @POST
    @Path("/{title}/Clone")
    @Timed
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_CREATE)
    @ApiOperation(value = "Clone a alert")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "The supplied request is not valid.")})
    @AuditEvent(type = AlertWizardAuditEventTypes.WIZARD_ALERTS_RULES_CREATE)
    public Response clone(@ApiParam(name = TITLE, required = true)
                          @PathParam(TITLE) String title,
                          @ApiParam(name = "JSON body", required = true) @Valid @NotNull CloneAlertRuleRequest request,
                          @Context UserContext userContext
    ) throws NotFoundException, ValidationException {

        AlertRule sourceAlert = this.alertRuleService.load(title);
        String alertTitle = request.getTitle();
        final String creatorUser = getCurrentUser().getName();

        // Create stream.
        Stream sourceFirstStream = streamService.load(sourceAlert.getStreamID());
        Stream firstStream = this.streamPipelineService.cloneStream(sourceFirstStream, alertTitle, creatorUser);

        //create pipeline
        String pipelineID = null;
        String pipelineRuleID = null;
        if (!sourceAlert.getPipelineFieldRules().isEmpty()) {
            RuleDao pipelineRule = this.streamPipelineService.createPipelineRule(alertTitle, sourceAlert.getPipelineFieldRules(), firstStream, null);
            PipelineDao pipeline = this.streamPipelineService.createPipeline(alertTitle, null, sourceFirstStream.getMatchingType().toString());
            pipelineID = pipeline.id();
            pipelineRuleID = pipelineRule.id();
        }


        Stream secondStream = null;
        String secondStreamID = null;
        String pipelineRuleID2 = null;
        String pipelineID2 = null;

        //Create Second Stream and pipeline
        if (sourceAlert.getSecondStreamID() != null && !sourceAlert.getSecondStreamID().isEmpty()) {
            final Stream sourceSecondStream = streamService.load(sourceAlert.getSecondStreamID());
            secondStream = this.streamPipelineService.cloneStream(sourceSecondStream, alertTitle + "#2", creatorUser);
            secondStreamID = secondStream.getId();
            if (!sourceAlert.getSecondPipelineFieldRules().isEmpty()) {
                RuleDao pipelineRule2 = this.streamPipelineService.createPipelineRule(alertTitle + "#2", sourceAlert.getSecondPipelineFieldRules(), secondStream, null);
                PipelineDao pipeline2 = this.streamPipelineService.createPipeline(alertTitle + "#2", null, sourceFirstStream.getMatchingType().toString());
                pipelineID2 = pipeline2.id();
                pipelineRuleID2 = pipelineRule2.id();
            }
        }

        for (Output output : sourceFirstStream.getOutputs()) {
            streamService.addOutput(firstStream, output);
        }
        this.clusterEventBus.post(StreamsChangedEvent.create(firstStream.getId()));

        // Create Notification
        LoggingNotificationConfig loggingNotificationConfig = (LoggingNotificationConfig) eventNotificationsResource.get(sourceAlert.getNotificationID()).config();
        String notificationID = this.alertRuleUtilsService.createNotification(alertTitle, loggingNotificationConfig.severity().getType(), userContext);

        // Create Condition
        EventProcessorConfig eventConfig = eventDefinitionsResource.get(sourceAlert.getEventID()).config();
        Map<String, Object> parametersCondition = alertRuleUtils.getConditionParameters(eventConfig);
        EventProcessorConfig configuration = this.alertRuleUtilsService.createCondition(sourceAlert.getConditionType(), parametersCondition, firstStream.getId(), secondStreamID);

        //Create Event
        String eventID = this.alertRuleUtilsService.createEvent(alertTitle, notificationID, configuration, userContext);

        String eventID2 = null;
        //Or Event for Second Stream
        if (sourceAlert.getConditionType().equals("OR") && secondStream != null) {
            //Create Condition
            EventProcessorConfig configuration2 = this.alertRuleUtilsService.createAggregationCondition(secondStreamID, parametersCondition);
            //Create Event
            eventID2 = this.alertRuleUtilsService.createEvent(alertTitle + "#2", notificationID, configuration2, userContext);
        }

        this.alertRuleService.create(AlertRule.create(
                alertTitle,
                firstStream.getId(),
                eventID,
                notificationID,
                DateTime.now(),
                creatorUser,
                DateTime.now(),
                request.getDescription(),
                sourceAlert.getConditionType(),
                secondStreamID,
                eventID2,
                pipelineID,
                pipelineRuleID,
                sourceAlert.getPipelineFieldRules(),
                pipelineID2,
                pipelineRuleID2,
                sourceAlert.getSecondPipelineFieldRules()));

        //Update list usage
        for (FieldRule fieldRule : alertRuleUtils.nullSafe(sourceAlert.getPipelineFieldRules())) {
            alertListUtilsService.incrementUsage(fieldRule.getValue());
        }
        for (FieldRule fieldRule : alertRuleUtils.nullSafe(sourceAlert.getSecondPipelineFieldRules())) {
            alertListUtilsService.incrementUsage(fieldRule.getValue());
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
            //Delete first Stream
            if (alertRule.getStreamID() != null && !alertRule.getStreamID().isEmpty()) {
                this.streamPipelineService.deleteStreamFromID(alertRule.getStreamID());
            }

            //Delete second Stream
            if (alertRule.getSecondStreamID() != null && !alertRule.getSecondStreamID().isEmpty()) {
                this.streamPipelineService.deleteStreamFromID(alertRule.getSecondStreamID());
            }

            //Delete Event
            if (alertRule.getEventID() != null && !alertRule.getEventID().isEmpty()) {
                eventDefinitionsResource.delete(alertRule.getEventID());
            }
            if (alertRule.getNotificationID() != null && !alertRule.getNotificationID().isEmpty()) {
                eventNotificationsResource.delete(alertRule.getNotificationID());
            }
            if (alertRule.getSecondEventID() != null && !alertRule.getSecondEventID().isEmpty()) {
                eventDefinitionsResource.delete(alertRule.getSecondEventID());
            }

            //Delete Pipeline
            if (alertRule.getPipelineID() != null && alertRule.getPipelineRuleID() != null) {
                this.streamPipelineService.deletePipeline(alertRule.getPipelineID(), alertRule.getPipelineRuleID());
            }

            if (alertRule.getSecondPipelineID() != null && alertRule.getSecondPipelineRuleID() != null) {
                this.streamPipelineService.deletePipeline(alertRule.getSecondPipelineID(), alertRule.getSecondPipelineRuleID());
            }

            //Update list usage
            for (FieldRule fieldRule : alertRuleUtils.nullSafe(alertRule.getPipelineFieldRules())) {
                alertListUtilsService.decrementUsage(fieldRule.getValue());
            }
            for (FieldRule fieldRule : alertRuleUtils.nullSafe(alertRule.getSecondPipelineFieldRules())) {
                alertListUtilsService.decrementUsage(fieldRule.getValue());
            }
        } catch (NotFoundException e) {
            LOG.error("Cannot find alert " + alertTitle, e);
        }

        this.alertRuleService.destroy(alertTitle);
    }

    @POST
    @Path("/export")
    @Timed
    @ApiOperation(value = "Export alert rules")
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_READ)
    @AuditEvent(type = AlertWizardAuditEventTypes.WIZARD_ALERTS_RULES_READ)
    public List<ExportAlertRule> getExportAlertRule(@ApiParam(name = "JSON body", required = true) @Valid @NotNull ExportAlertRuleRequest request) {
        LOG.debug("List titles : " + request.getTitles());
        return alertRuleExporter.export(request.getTitles());
    }

    @PUT
    @Path("/import")
    @Timed
    @ApiOperation(value = "Import a alert")
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_CREATE)
    @ApiResponses(value = {@ApiResponse(code = 400, message = "The supplied request is not valid.")})
    @AuditEvent(type = AlertWizardAuditEventTypes.WIZARD_ALERTS_RULES_CREATE)
    public Response importAlertRules(@ApiParam(name = "JSON body", required = true) @Valid @NotNull List<ExportAlertRule> request, @Context UserContext userContext) {
        Response responses = Response.accepted().build();

        for (ExportAlertRule alertRule : request) {
            if (!alertRuleService.isValidImportRequest(alertRule)) {
                LOG.error("Invalid alert rule:" + alertRule.getTitle());
            } else {
                try {
                    importAlertRule(alertRule, userContext);
                } catch (Exception e) {
                    LOG.error("Cannot create alert " + alertRule.getTitle() + ": ", e.getMessage());
                    responses = Response.serverError().build();
                }
            }
        }

        return responses;
    }
}
