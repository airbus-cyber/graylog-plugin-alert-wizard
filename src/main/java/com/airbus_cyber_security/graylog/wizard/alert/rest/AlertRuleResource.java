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

import com.airbus_cyber_security.graylog.wizard.alert.business.TriggeringConditionsService;
import com.airbus_cyber_security.graylog.wizard.alert.business.AlertRuleService;
import com.airbus_cyber_security.graylog.wizard.alert.business.EventDefinitionService;
import com.airbus_cyber_security.graylog.wizard.alert.business.NotificationService;
import com.airbus_cyber_security.graylog.wizard.alert.model.TriggeringConditions;
import com.airbus_cyber_security.graylog.wizard.alert.model.AlertPattern;
import com.airbus_cyber_security.graylog.wizard.alert.model.AlertRule;
import com.airbus_cyber_security.graylog.wizard.alert.model.AlertType;
import com.airbus_cyber_security.graylog.wizard.alert.model.AggregationAlertPattern;
import com.airbus_cyber_security.graylog.wizard.alert.model.CorrelationAlertPattern;
import com.airbus_cyber_security.graylog.wizard.alert.model.DisjunctionAlertPattern;
import com.airbus_cyber_security.graylog.wizard.alert.model.FieldRule;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.AlertRuleStream;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.requests.AlertRuleRequest;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.requests.CloneAlertRuleRequest;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.responses.GetDataAlertRule;
import com.airbus_cyber_security.graylog.wizard.audit.AlertWizardAuditEventTypes;
import com.airbus_cyber_security.graylog.wizard.config.rest.AlertWizardConfig;
import com.airbus_cyber_security.graylog.wizard.config.rest.AlertWizardConfigurationService;
import com.airbus_cyber_security.graylog.wizard.config.rest.ImportPolicyType;
import com.airbus_cyber_security.graylog.wizard.permissions.AlertRuleRestPermissions;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import com.mongodb.MongoException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.graylog.events.notifications.NotificationDto;
import org.graylog.events.processor.EventDefinition;
import org.graylog.events.processor.EventDefinitionDto;
import org.graylog.events.processor.EventProcessorConfig;
import org.graylog.events.rest.EventNotificationsResource;
import org.graylog.security.UserContext;
import org.graylog2.audit.jersey.AuditEvent;
import org.graylog2.database.NotFoundException;
import org.graylog2.database.PaginatedList;
import org.graylog2.plugin.database.ValidationException;
import org.graylog2.plugin.rest.PluginRestResource;
import org.graylog2.rest.models.tools.responses.PageListResponse;
import org.graylog2.rest.resources.entities.EntityDefaults;
import org.graylog2.rest.resources.entities.Sorting;
import org.graylog2.search.SearchQuery;
import org.graylog2.search.SearchQueryField;
import org.graylog2.search.SearchQueryParser;
import org.graylog2.shared.rest.resources.RestResource;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Api(value = "Wizard/Alerts", description = "Management of Wizard alerts rules.")
@Path("/alerts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AlertRuleResource extends RestResource implements PluginRestResource {
    private static final Logger LOG = LoggerFactory.getLogger(AlertRuleResource.class);

    private static final String ENCODING = "UTF-8";
    private static final String ID = "id";
    private static final String TITLE = "title";

    private static final String DEFAULT_SORT_FIELD = GetDataAlertRule.FIELD_TITLE;
    private static final String DEFAULT_SORT_DIRECTION = "asc";
    private static final ImmutableMap<String, String> FIELD_MAP = ImmutableMap.<String, String>builder()
            .put("id", "_id")
            .put("title", GetDataAlertRule.FIELD_TITLE)
            .put("description", GetDataAlertRule.FIELD_DESCRIPTION)
            .put("user", GetDataAlertRule.FIELD_CREATOR_USER_ID)
            .put("created", GetDataAlertRule.FIELD_CREATED_AT)
            .put("lastModified", GetDataAlertRule.FIELD_LAST_MODIFIED)
            .put("priority", GetDataAlertRule.FIELD_PRIORITY)
            .build();
    private static final ImmutableMap<String, SearchQueryField> SEARCH_FIELD_MAPPING = ImmutableMap.<String, SearchQueryField>builder()
            .put("id", SearchQueryField.create("_id", SearchQueryField.Type.OBJECT_ID))
            .put("title", SearchQueryField.create(GetDataAlertRule.FIELD_TITLE))
            .put("user", SearchQueryField.create(GetDataAlertRule.FIELD_CREATOR_USER_ID))
            .put("description", SearchQueryField.create(GetDataAlertRule.FIELD_DESCRIPTION))
            .build();
    private static final EntityDefaults settings = EntityDefaults.builder()
            .sort(Sorting.create(DEFAULT_SORT_FIELD, Sorting.Direction.valueOf(DEFAULT_SORT_DIRECTION.toUpperCase(Locale.ROOT))))
            .build();


    // TODO try to remove this field => move it down in business
    private final AlertWizardConfigurationService configurationService;

    // TODO try to remove this field => Use AlertRuleUtilsService
    private final EventNotificationsResource eventNotificationsResource;
    private final EventDefinitionService eventDefinitionService;

    private final AlertRuleService alertRuleService;
    private final Conversions conversions;
    private final TriggeringConditionsService triggeringConditionsService;
    private final NotificationService notificationService;

    private final SearchQueryParser searchQueryParser;

    @Inject
    public AlertRuleResource(AlertRuleService alertRuleService,
                             TriggeringConditionsService triggeringConditionsService,
                             AlertWizardConfigurationService configurationService,
                             EventNotificationsResource eventNotificationsResource,
                             Conversions conversions,
                             EventDefinitionService eventDefinitionService,
                             NotificationService notificationService) {
        // TODO should probably move these fields down into the business namespace
        this.alertRuleService = alertRuleService;
        this.triggeringConditionsService = triggeringConditionsService;
        this.configurationService = configurationService;
        this.eventNotificationsResource = eventNotificationsResource;
        this.eventDefinitionService = eventDefinitionService;

        this.conversions = conversions;
        this.notificationService = notificationService;
        this.searchQueryParser = new SearchQueryParser(GetDataAlertRule.FIELD_TITLE, SEARCH_FIELD_MAPPING);
    }

    private AlertRuleStream constructAlertRuleStream(TriggeringConditions conditions) {
        List<FieldRule> fieldRules = this.triggeringConditionsService.getFieldRules(conditions);
        return AlertRuleStream.create(conditions.filteringStreamIdentifier(), conditions.matchingType(), fieldRules);
    }

    private GetDataAlertRule constructDataAlertRule(AlertRule alert) {
        AlertPattern alertPattern = alert.pattern();
        DateTime lastModified = alert.getLastModified();
        Optional<EventDefinitionDto> event = Optional.empty();
        Optional<EventDefinitionDto> event2 = Optional.empty();
        Map<String, Object> parametersCondition = null;
        Map<String, Object> parametersCondition2 = null;
        boolean isDisabled = false;
        AlertRuleStream alertRuleStream = null;
        AlertRuleStream alertRuleStream2 = null;

        if (alertPattern instanceof CorrelationAlertPattern pattern) {
            event = this.eventDefinitionService.getEventDefinition(pattern.eventIdentifier());
            parametersCondition = getConditionParameters(event);
            TriggeringConditions conditions1 = pattern.conditions1();
            alertRuleStream = this.constructAlertRuleStream(conditions1);
            TriggeringConditions conditions2 = pattern.conditions2();
            alertRuleStream2 = this.constructAlertRuleStream(conditions2);
            isDisabled = this.triggeringConditionsService.isDisabled(conditions1) || this.triggeringConditionsService.isDisabled(conditions2);
        } else if (alertPattern instanceof DisjunctionAlertPattern pattern) {
            TriggeringConditions conditions = pattern.conditions1();
            alertRuleStream = this.constructAlertRuleStream(conditions);
            TriggeringConditions conditions2 = pattern.conditions2();
            alertRuleStream2 = this.constructAlertRuleStream(conditions2);
            isDisabled = this.triggeringConditionsService.isDisabled(conditions) || this.triggeringConditionsService.isDisabled(conditions2);;

            event = this.eventDefinitionService.getEventDefinition(pattern.eventIdentifier1());
            event2 = this.eventDefinitionService.getEventDefinition(pattern.eventIdentifier2());
            parametersCondition = getConditionParameters(event);
            parametersCondition2 = getConditionParameters(event2);
            completeParametersConditionForDisjunction(parametersCondition, parametersCondition2);
        } else if (alertPattern instanceof AggregationAlertPattern pattern) {
            event = this.eventDefinitionService.getEventDefinition(pattern.eventIdentifier());
            parametersCondition = getConditionParameters(event);
            TriggeringConditions conditions = pattern.conditions();
            alertRuleStream = this.constructAlertRuleStream(conditions);
            isDisabled = this.triggeringConditionsService.isDisabled(conditions);
        }
        Optional<NotificationDto> notification = this.notificationService.get(alert.getNotificationID());
        String notificationIdentifier = null;
        if (notification.isPresent()) {
            NotificationDto notificationDto = notification.get();
            notificationIdentifier = notificationDto.id();
        }

        String eventIdentifier = null;
        String description = null;
        Integer priority = null;
        if (event.isPresent()) {
            EventDefinitionDto eventDefinitionDto = event.get();
            eventIdentifier = eventDefinitionDto.id();
            description = eventDefinitionDto.description();
            priority = eventDefinitionDto.priority();
            if (EventDefinition.State.DISABLED.equals(eventDefinitionDto.state())) {
                isDisabled = true;
            }
        }

        String eventIdentifier2 = null;
        if (event2.isPresent()) {
            EventDefinitionDto eventDefinitionDto2 = event2.get();
            eventIdentifier2 = eventDefinitionDto2.id();
            if (EventDefinition.State.DISABLED.equals(eventDefinitionDto2.state())) {
                isDisabled = true;
            }
        }

        return GetDataAlertRule.create(
                alert.id(),
                alert.getTitle(),
                priority,
                eventIdentifier,
                eventIdentifier2,
                notificationIdentifier,
                alert.getCreatedAt(),
                alert.getCreatorUserId(),
                lastModified,
                isDisabled,
                description,
                alert.getAlertType(),
                parametersCondition,
                alertRuleStream,
                alertRuleStream2);
    }

    private Map<String, Object> getConditionParameters(Optional<EventDefinitionDto> event) {
        if (!event.isPresent()) {
            return null;
        }
        return this.conversions.getConditionParameters(event.get().config());
    }

    private void completeParametersConditionForDisjunction(Map<String, Object> configParameters, Map<String, Object> configParameters2) {
        if (configParameters != null && configParameters2 != null) {
            configParameters.put("additional_search_query", configParameters2.get("search_query"));
            configParameters.put("additional_threshold_type", configParameters2.get("threshold_type"));
            configParameters.put("additional_threshold", configParameters2.get("threshold"));
        }
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
    @Path("/{id}")
    @Timed
    @ApiOperation(value = "Get a alert")
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_READ)
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Alert not found."),
    })
    public GetDataAlertRule get(@ApiParam(name = ID, required = true) @PathParam(ID) String id)
            throws UnsupportedEncodingException, NotFoundException {
        String alertId = java.net.URLDecoder.decode(id, ENCODING);
        return getGetDataAlertRule(alertId);
    }

    @GET
    @Path("/title/{title}")
    @Timed
    @ApiOperation(value = "Get a alert by title")
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_READ)
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Alert not found."),
    })
    public GetDataAlertRule getByTitle(@ApiParam(name = TITLE, required = true) @PathParam(TITLE) String title)
            throws UnsupportedEncodingException, NotFoundException {
        String alertTitle = java.net.URLDecoder.decode(title, ENCODING);
        return getGetDataAlertRuleFromTitle(alertTitle);
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
                    AlertRule alert = this.alertRuleService.load(alertTitle);
                    this.delete(alert.id(), userContext);
                } catch (MongoException | UnsupportedEncodingException | NotFoundException e) {
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

        String notificationIdentifier = this.notificationService.createNotification(alertTitle, userContext);
        GetDataAlertRule result = createPatternAndRule(request, userContext, notificationIdentifier, alertTitle, userName, alertType);
        return Response.ok().entity(result).build();
    }

    private GetDataAlertRule createPatternAndRule(AlertRuleRequest request, UserContext userContext, String notificationIdentifier, String alertTitle, String userName, AlertType alertType) throws ValidationException {
        AlertPattern pattern = createAlertPattern(notificationIdentifier, request, alertTitle, userContext, userName);

        AlertRule alertRule = AlertRule.create(
                null,
                alertTitle,
                alertType,
                pattern,
                notificationIdentifier,
                DateTime.now(DateTimeZone.UTC),
                userName,
                DateTime.now(DateTimeZone.UTC));
        alertRule = this.alertRuleService.create(alertRule);

        return this.constructDataAlertRule(alertRule);
    }

    private AlertPattern createAlertPattern(String notificationIdentifier, AlertRuleRequest request, String alertTitle,
                                            UserContext userContext, String userName) throws ValidationException {
        AlertType alertType = request.getConditionType();

        TriggeringConditions conditions = this.triggeringConditionsService.createTriggeringConditions(request.getStream(), alertTitle, userName, request.isDisabled());

        switch (alertType) {
            case THEN:
            case AND:
                return createCorrelationAlertPattern(notificationIdentifier, request, alertTitle, userContext, userName, conditions);
            case OR:
                return createDisjunctionAlertPattern(notificationIdentifier, request, alertTitle, userContext, userName, conditions);
            default:
                String description = request.getDescription();
                Integer priority = request.getPriority();
                Map<String, Object> conditionParameters = request.conditionParameters();
                String streamIdentifier = conditions.outputStreamIdentifier();
                EventProcessorConfig configuration = this.conversions.createEventConfiguration(alertType, conditionParameters, streamIdentifier);

                String eventIdentifier = this.eventDefinitionService.createEvent(alertTitle, description, priority, notificationIdentifier, configuration, userContext, request.isDisabled());

                return AggregationAlertPattern.builder().conditions(conditions).eventIdentifier(eventIdentifier).build();
        }
    }

    private DisjunctionAlertPattern createDisjunctionAlertPattern(String notificationIdentifier, AlertRuleRequest request, String alertTitle, UserContext userContext, String userName, TriggeringConditions conditions) throws ValidationException {
        String description = request.getDescription();
        Integer priority = request.getPriority();
        Map<String, Object> conditionParameters = request.conditionParameters();

        TriggeringConditions conditions2 = this.triggeringConditionsService.createTriggeringConditions(request.getSecondStream(), alertTitle + "#2", userName, request.isDisabled());
        String streamIdentifier = conditions.outputStreamIdentifier();
        EventProcessorConfig configuration = this.conversions.createAggregationCondition(streamIdentifier, conditionParameters);
        String eventIdentifier = this.eventDefinitionService.createEvent(alertTitle, description, priority, notificationIdentifier, configuration, userContext, request.isDisabled());
        String streamIdentifier2 = conditions2.outputStreamIdentifier();
        EventProcessorConfig configuration2 = this.conversions.createAdditionalAggregationCondition(streamIdentifier2, conditionParameters);
        String eventIdentifier2 = this.eventDefinitionService.createEvent(alertTitle + "#2", description, priority, notificationIdentifier, configuration2, userContext, request.isDisabled());

        return DisjunctionAlertPattern.builder()
                .conditions1(conditions).conditions2(conditions2).eventIdentifier1(eventIdentifier).eventIdentifier2(eventIdentifier2)
                .build();
    }

    private CorrelationAlertPattern createCorrelationAlertPattern(String notificationIdentifier, AlertRuleRequest request, String alertTitle, UserContext userContext, String userName, TriggeringConditions conditions) throws ValidationException {
        String description = request.getDescription();
        Integer priority = request.getPriority();
        AlertType alertType = request.getConditionType();
        Map<String, Object> conditionParameters = request.conditionParameters();

        TriggeringConditions conditions2 = this.triggeringConditionsService.createTriggeringConditions(request.getSecondStream(), alertTitle + "#2", userName, request.isDisabled());
        String streamIdentifier = conditions.outputStreamIdentifier();
        String streamIdentifier2 = conditions2.outputStreamIdentifier();
        EventProcessorConfig configuration = this.conversions.createCorrelationCondition(alertType, streamIdentifier, streamIdentifier2, conditionParameters);
        String eventIdentifier = this.eventDefinitionService.createEvent(alertTitle, description, priority, notificationIdentifier, configuration, userContext, request.isDisabled());
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
            TriggeringConditions conditions = this.triggeringConditionsService.updateTriggeringConditions(previousConditions, title, streamConfiguration, userName, request.isDisabled());
            TriggeringConditions previousConditions2 = previousPattern.conditions2();
            TriggeringConditions conditions2 = this.triggeringConditionsService.updateTriggeringConditions(previousConditions2, title2, streamConfiguration2, userName, request.isDisabled());

            String streamIdentifier = conditions.outputStreamIdentifier();
            String streamIdentifier2 = conditions2.outputStreamIdentifier();
            EventProcessorConfig configuration = this.conversions.createCorrelationCondition(alertType, streamIdentifier, streamIdentifier2, request.conditionParameters());
            this.eventDefinitionService.updateEvent(title, request.getDescription(), request.getPriority(), previousPattern.eventIdentifier(), configuration, request.isDisabled());

            return previousPattern.toBuilder().conditions1(conditions).conditions2(conditions2).build();
        } else if (previousAlertPattern instanceof DisjunctionAlertPattern previousPattern) {
            TriggeringConditions previousConditions = previousPattern.conditions1();
            TriggeringConditions conditions = this.triggeringConditionsService.updateTriggeringConditions(previousConditions, title, streamConfiguration, userName, request.isDisabled());
            TriggeringConditions previousConditions2 = previousPattern.conditions2();
            TriggeringConditions conditions2 = this.triggeringConditionsService.updateTriggeringConditions(previousConditions2, title2, streamConfiguration2, userName, request.isDisabled());

            String streamIdentifier = conditions.outputStreamIdentifier();
            EventProcessorConfig configuration = this.conversions.createEventConfiguration(request.getConditionType(), request.conditionParameters(), streamIdentifier);
            this.eventDefinitionService.updateEvent(title, request.getDescription(), request.getPriority(), previousPattern.eventIdentifier1(), configuration, request.isDisabled());

            String streamIdentifier2 = conditions2.outputStreamIdentifier();
            EventProcessorConfig configuration2 = this.conversions.createAdditionalAggregationCondition(streamIdentifier2, request.conditionParameters());
            this.eventDefinitionService.updateEvent(title2, request.getDescription(), request.getPriority(), previousPattern.eventIdentifier2(), configuration2, request.isDisabled());

            return previousPattern.toBuilder().conditions1(conditions).conditions2(conditions2).build();
        } else if (previousAlertPattern instanceof AggregationAlertPattern previousPattern) {
            TriggeringConditions previousConditions = previousPattern.conditions();
            TriggeringConditions conditions = this.triggeringConditionsService.updateTriggeringConditions(previousConditions, title, streamConfiguration, userName, request.isDisabled());
            String streamIdentifier = conditions.outputStreamIdentifier();
            EventProcessorConfig configuration = this.conversions.createEventConfiguration(request.getConditionType(), request.conditionParameters(), streamIdentifier);
            this.eventDefinitionService.updateEvent(title, request.getDescription(), request.getPriority(), previousPattern.eventIdentifier(), configuration, request.isDisabled());

            return previousPattern.toBuilder().conditions(conditions).build();
        }

        throw new RuntimeException("Unreachable code");
    }

    @PUT
    @Path("/{id}")
    @Timed
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_UPDATE)
    @ApiOperation(value = "Update a alert")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "The supplied request is not valid.")})
    @AuditEvent(type = AlertWizardAuditEventTypes.WIZARD_ALERTS_RULES_UPDATE)
    public Response update(@ApiParam(name = ID, required = true)
                           @PathParam(ID) String id,
                           @ApiParam(name = "JSON body", required = true) @Valid @NotNull AlertRuleRequest request,
                           @Context UserContext userContext
    ) throws UnsupportedEncodingException, NotFoundException, ValidationException {

        this.conversions.checkIsValidRequest(request);

        Optional<AlertRule> previousAlertOpt = this.alertRuleService.get(id);
        if (previousAlertOpt.isPresent()) {
            AlertRule previousAlert = previousAlertOpt.get();

            String notificationIdentifier = previousAlert.getNotificationID();
            String userName = getCurrentUser().getName();

            this.notificationService.updateNotification(request.getTitle(), notificationIdentifier);

            AlertType previousAlertType = previousAlert.getAlertType();
            AlertPattern pattern = updateAlertPattern(previousAlert.pattern(), notificationIdentifier, request,
                    previousAlertType, request.getTitle(), userContext, userName);

            AlertRule alertRule = AlertRule.create(
                    id,
                    request.getTitle(),
                    request.getConditionType(),
                    pattern,
                    previousAlert.getNotificationID(),
                    previousAlert.getCreatedAt(),
                    userName,
                    DateTime.now(DateTimeZone.UTC));
            alertRule = this.alertRuleService.update(alertRule);

            GetDataAlertRule result = this.constructDataAlertRule(alertRule);
            return Response.accepted().entity(result).build();
        } else {
            throw new NotFoundException("Alert <" + id + "> not found!");
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
            this.triggeringConditionsService.deleteTriggeringConditions(pattern.conditions1());
            this.triggeringConditionsService.deleteTriggeringConditions(pattern.conditions2());
            deleteEvent(pattern.eventIdentifier());
        } else if (alertPattern instanceof DisjunctionAlertPattern pattern) {
            this.triggeringConditionsService.deleteTriggeringConditions(pattern.conditions1());
            this.triggeringConditionsService.deleteTriggeringConditions(pattern.conditions2());
            deleteEvent(pattern.eventIdentifier1());
            deleteEvent(pattern.eventIdentifier2());
        } else if (alertPattern instanceof AggregationAlertPattern pattern) {
            this.triggeringConditionsService.deleteTriggeringConditions(pattern.conditions());
            deleteEvent(pattern.eventIdentifier());
        }
    }

    @DELETE
    @Path("/{id}")
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_DELETE)
    @ApiOperation(value = "Delete a alert")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Alert not found."),
            @ApiResponse(code = 400, message = "Invalid ObjectId.")
    })
    @AuditEvent(type = AlertWizardAuditEventTypes.WIZARD_ALERTS_RULES_DELETE)
    public void delete(@ApiParam(name = ID, required = true)
                       @PathParam(ID) String id,
                       @Context UserContext userContext
    ) throws MongoException, UnsupportedEncodingException {
        Optional<AlertRule> alertRuleOptional = this.alertRuleService.get(id);

        if (alertRuleOptional.isPresent()) {
            AlertRule alertRule = alertRuleOptional.get();

            deleteAlertPattern(alertRule.pattern());
            if (alertRule.getNotificationID() != null && !alertRule.getNotificationID().isEmpty()) {
                // TODO move this down into AlertRuleUtilsService and remove the use for eventNotificationsResource
                this.eventNotificationsResource.delete(alertRule.getNotificationID(), userContext);
            }

            this.alertRuleService.delete(id);
        } else {
            LOG.error("Cannot find alert {}", id);
        }
    }

    @POST
    @Timed
    @ApiOperation(value = "Clone an alert")
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_CREATE)
    @ApiResponses(value = {@ApiResponse(code = 400, message = "The supplied request is not valid.")})
    @AuditEvent(type = AlertWizardAuditEventTypes.WIZARD_ALERTS_RULES_CREATE)
    @Path("/clone")
    public Response clone(@ApiParam(name = "JSON body", required = true) @Valid @NotNull CloneAlertRuleRequest request, @Context UserContext userContext)
            throws ValidationException, BadRequestException, NotFoundException {
        GetDataAlertRule sourceAlert = getGetDataAlertRuleFromTitle(request.getSourceTitle());
        String userName = getCurrentUser().getName();
        String title = request.getTitle();
        String description = request.getDescription();
        String alertTitle = checkImportPolicyAndGetTitle(title, userContext);
        AlertType alertType = sourceAlert.getConditionType();

        String notificationIdentifier = createNotificationFromCloneRequest(alertTitle, userContext, sourceAlert.getNotificationID(), request.getCloneNotification());
        AlertRuleRequest alertRuleRequest = AlertRuleRequest.create(title, sourceAlert.getPriority(), description, sourceAlert.isDisabled(), sourceAlert.getConditionType(),
                sourceAlert.conditionParameters(), sourceAlert.getStream(), sourceAlert.getSecondStream());

        GetDataAlertRule result = createPatternAndRule(alertRuleRequest, userContext, notificationIdentifier, alertTitle, userName, alertType);
        return Response.ok().entity(result).build();
    }

    private GetDataAlertRule getGetDataAlertRule(String id) throws NotFoundException {
        Optional<AlertRule> loadedAlertOpt = this.alertRuleService.get(id);
        if (loadedAlertOpt.isEmpty()) {
            throw new NotFoundException("Alert <" + id + "> not found!");
        }
        return this.constructDataAlertRule(loadedAlertOpt.get());
    }

    private GetDataAlertRule getGetDataAlertRuleFromTitle(String title) throws NotFoundException {
        AlertRule loadedAlert = this.alertRuleService.load(title);
        if (loadedAlert == null) {
            throw new NotFoundException("Alert <" + title + "> not found!");
        }
        return this.constructDataAlertRule(loadedAlert);
    }

    private String createNotificationFromCloneRequest(String alertTitle, UserContext userContext, String notificationID, Boolean cloneNotification) throws NotFoundException {
        if(cloneNotification) {
            return this.notificationService.cloneNotification(notificationID, alertTitle, userContext);
        } else {
            return this.notificationService.createNotification(alertTitle, userContext);
        }
    }

    // This method is based on method getPage in class org.graylog.events.rest.EventDefinitionsResource
    @GET
    @Timed
    @Path("/paginated")
    @ApiOperation(value = "Get a paginated list of alerts")
    @Produces(MediaType.APPLICATION_JSON)
    public PageListResponse<GetDataAlertRule> getPage(@ApiParam(name = "page") @QueryParam("page") @DefaultValue("1") int page,
                                                      @ApiParam(name = "per_page") @QueryParam("per_page") @DefaultValue("50") int perPage,
                                                      @ApiParam(name = "query") @QueryParam("query") @DefaultValue("") String query,
                                                      @ApiParam(name = "sort",
                                                              value = "The field to sort the result on",
                                                              required = true,
                                                              allowableValues = "title,user,created,lastModified")
                                                      @DefaultValue(DEFAULT_SORT_FIELD) @QueryParam("sort") String sort,
                                                      @ApiParam(name = "order", value = "The sort direction", allowableValues = "asc, desc")
                                                      @DefaultValue(DEFAULT_SORT_DIRECTION) @QueryParam("order") String order) {

        SearchQuery searchQuery;
        try {
            searchQuery = searchQueryParser.parse(query);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid argument in search query: " + e.getMessage());
        }
        String sortAttr = FIELD_MAP.getOrDefault(sort, sort);
        final PaginatedList<AlertRule> result = this.alertRuleService.searchPaginated(
                searchQuery,
                alertRule -> true,
                order,
                sortAttr,
                page,
                perPage);

        PaginatedList<AlertRule> alertRules = new PaginatedList<>(
                result.delegate(), result.pagination().total(), result.pagination().page(), result.pagination().perPage()
        );

        List<GetDataAlertRule> elements = result.delegate().stream().map(this::constructDataAlertRule).toList();

        return PageListResponse.create(query, alertRules.pagination(),
                result.grandTotal().orElse(0L), sort, order, elements, List.of(), settings);
    }
}
