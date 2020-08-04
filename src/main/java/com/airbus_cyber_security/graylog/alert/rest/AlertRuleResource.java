package com.airbus_cyber_security.graylog.alert.rest;

import com.airbus_cyber_security.graylog.alert.*;
import com.airbus_cyber_security.graylog.alert.bundles.AlertRuleExporter;
import com.airbus_cyber_security.graylog.alert.bundles.ExportAlertRule;
import com.airbus_cyber_security.graylog.alert.bundles.ExportAlertRuleRequest;
import com.airbus_cyber_security.graylog.alert.rest.models.requests.AlertRuleRequest;
import com.airbus_cyber_security.graylog.alert.rest.models.requests.CloneAlertRuleRequest;
import com.airbus_cyber_security.graylog.alert.rest.models.responses.GetAlertRule;
import com.airbus_cyber_security.graylog.alert.rest.models.responses.GetDataAlertRule;
import com.airbus_cyber_security.graylog.alert.rest.models.responses.GetListAlertRule;
import com.airbus_cyber_security.graylog.alert.rest.models.responses.GetListDataAlertRule;
import com.airbus_cyber_security.graylog.alert.utilities.AlertRuleUtils;
import com.airbus_cyber_security.graylog.alert.utilities.AlertRuleUtilsService;
import com.airbus_cyber_security.graylog.audit.AlertWizardAuditEventTypes;
import com.airbus_cyber_security.graylog.config.LoggingAlertConfig;
import com.airbus_cyber_security.graylog.config.LoggingNotificationConfig;
import com.airbus_cyber_security.graylog.config.SeverityType;
import com.airbus_cyber_security.graylog.config.rest.AlertWizardConfig;
import com.airbus_cyber_security.graylog.config.rest.ImportPolicyType;
import com.airbus_cyber_security.graylog.events.processor.aggregation.AggregationCountProcessorConfig;
import com.airbus_cyber_security.graylog.events.processor.correlation.CorrelationCountProcessorConfig;
import com.airbus_cyber_security.graylog.list.AlertListService;
import com.airbus_cyber_security.graylog.list.utilities.AlertListUtilsService;
import com.airbus_cyber_security.graylog.permissions.AlertRuleRestPermissions;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mongodb.MongoException;
import io.swagger.annotations.*;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
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
import org.graylog.plugins.pipelineprocessor.db.*;
import org.graylog2.alarmcallbacks.AlarmCallbackConfiguration;
import org.graylog2.alarmcallbacks.AlarmCallbackConfigurationService;
import org.graylog2.alarmcallbacks.AlarmCallbackFactory;
import org.graylog2.alerts.AlertService;
import org.graylog2.audit.jersey.AuditEvent;
import org.graylog2.configuration.HttpConfiguration;
import org.graylog2.contentpacks.model.entities.references.ValueReference;
import org.graylog2.database.NotFoundException;
import org.graylog2.events.ClusterEventBus;
import org.graylog2.indexer.IndexSetRegistry;
import org.graylog2.lookup.db.DBCacheService;
import org.graylog2.lookup.db.DBDataAdapterService;
import org.graylog2.lookup.db.DBLookupTableService;
import org.graylog2.lookup.dto.CacheDto;
import org.graylog2.lookup.dto.DataAdapterDto;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.graylog2.plugin.configuration.ConfigurationException;
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

    private final AlertRuleService alertRuleService;
    private final StreamService streamService;
    private final ClusterEventBus clusterEventBus;
    private final AlarmCallbackConfigurationService alarmCallbackConfigurationService;
    private final ClusterConfigService clusterConfigService;
    private final AlertRuleUtils alertRuleUtils;
    private final AlertRuleExporter alertRuleExporter;
    private final AlertRuleUtilsService alertRuleUtilsService;
    private final RuleService ruleService;
    private final PipelineService pipelineService;
    private final AlertListUtilsService alertListUtilsService;
    private final EventDefinitionHandler eventDefinitionHandler;
    private final EventDefinitionsResource eventDefinitionsResource;
    private final NotificationResourceHandler notificationResourceHandler;
    private final EventNotificationsResource eventNotificationsResource;

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
                             AlarmCallbackConfigurationService alarmCallbackConfigurationService,
                             AlarmCallbackFactory alarmCallbackFactory,
                             ClusterConfigService clusterConfigService,
                             PipelineStreamConnectionsService pipelineStreamConnectionsService,
                             AlertListService alertListService,
                             EventDefinitionHandler eventDefinitionHandler,
                             EventDefinitionsResource eventDefinitionsResource,
                             NotificationResourceHandler notificationResourceHandler,
                             EventNotificationsResource eventNotificationsResource) {
        this.alertRuleService = alertRuleService;
        this.streamService = streamService;
        this.clusterEventBus = clusterEventBus;
        this.alarmCallbackConfigurationService = alarmCallbackConfigurationService;
        this.clusterConfigService = clusterConfigService;
        this.ruleService = ruleService;
        this.pipelineService = pipelineService;
        this.alertRuleUtils = new AlertRuleUtils();
        this.alertRuleExporter = new AlertRuleExporter(alertRuleService, alarmCallbackConfigurationService, streamService, alertRuleUtils);
        this.alertListUtilsService = new AlertListUtilsService(alertListService);
        this.eventDefinitionHandler = eventDefinitionHandler;
        this.eventDefinitionsResource = eventDefinitionsResource;
        this.notificationResourceHandler = notificationResourceHandler;
        this.eventNotificationsResource = eventNotificationsResource;
        this.alertRuleUtilsService = new AlertRuleUtilsService(alertRuleService, streamService, streamRuleService, clusterEventBus,
                indexSetRegistry.getDefault().getConfig().id(), alertService, alarmCallbackConfigurationService,
                alarmCallbackFactory, clusterConfigService, ruleService, pipelineService, dbDataAdapterService,
                httpConfiguration, dbCacheService, dbTableService, pipelineStreamConnectionsService, alertRuleUtils, eventDefinitionsResource, eventNotificationsResource);
    }

    @GET
    @Timed
    @ApiOperation(value = "Lists all existing alerts")
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_READ)
    public GetListAlertRule list() {
        final List<AlertRule> alerts = alertRuleService.all();
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
    public GetAlertRule get(@ApiParam(name = TITLE, required = true) @PathParam(TITLE) String title)
    		throws UnsupportedEncodingException, NotFoundException {
    	String alertTitle = java.net.URLDecoder.decode(title, ENCODING);
    	
        final AlertRule alert = alertRuleService.load(alertTitle);
        if(alert == null) {
        	throw new NotFoundException("Alert <" + alertTitle + "> not found!");
        }
        return GetAlertRule.create(alert);
    }

    @GET
    @Path("/{title}/data")
    @Timed
    @ApiOperation(value = "Get a alert with additional data")
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_READ)
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Alert not found."),
        })
    public GetDataAlertRule getData(@ApiParam(name = TITLE, required = true) @PathParam(TITLE) String title)
            throws UnsupportedEncodingException, NotFoundException {
        final String alertTitle = java.net.URLDecoder.decode(title, ENCODING);
        final AlertRule alert = alertRuleService.load(alertTitle);
        if(alert == null) {
        	throw new NotFoundException("Alert <" + alertTitle + "> not found!");
        }
        return alertRuleUtilsService.constructDataAlertRule(alert);
    }

    @GET
    @Path("/data")
    @Timed
    @ApiOperation(value = "Lists all existing alerts with additional data")
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_READ)
    public GetListDataAlertRule listWithData() {
        final List<AlertRule> alerts = alertRuleService.all();

        List<GetDataAlertRule> alertsData = new ArrayList<>();
        for(AlertRule alert : alerts) {
            try{
                alertsData.add(alertRuleUtilsService.constructDataAlertRule(alert));
            }catch(NotFoundException e){
                LOG.warn("Alert " + alert.getTitle() + " is broken: " + e.getMessage());
                alertsData.add(GetDataAlertRule.create(alert.getTitle(), alert.getTitle(),
                		"",
                        alert.getConditionID(),
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

    private String checkImportPolicyAndGetTitle(String title){
        String alertTitle = title;
        if(alertRuleService.isPresent(alertTitle)) {
            final AlertWizardConfig configGeneral = clusterConfigService.get(AlertWizardConfig.class);
            ImportPolicyType importPolicy = configGeneral.accessImportPolicy();
            if(importPolicy != null && importPolicy.equals(ImportPolicyType.RENAME)) {
                String newAlertTitle;
                int i = 1;
                do{
                    newAlertTitle = alertTitle+"("+i+")";
                    i++;
                }while (alertRuleService.isPresent(newAlertTitle));
                alertTitle = newAlertTitle;
            }else if(importPolicy != null && importPolicy.equals(ImportPolicyType.REPLACE)) {
                try {
                    this.delete(alertTitle);
                } catch (MongoException | UnsupportedEncodingException e) {
                    LOG.error("Failed to replace alert rule");
                    throw new BadRequestException("Failed to replace alert rule.");
                }
            }else{
                LOG.error("Failed to create alert rule : Alert rule title already exist");
                throw new BadRequestException("Failed to create alert rule : Alert rule title already exist.");
            }
        }
        return alertTitle;
    }

    @PUT
    @Timed    
    @ApiOperation(value = "Create a alert")
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_CREATE)
    @ApiResponses(value = {@ApiResponse(code = 400, message = "The supplied request is not valid.")})
    @AuditEvent(type = AlertWizardAuditEventTypes.WIZARD_ALERTS_RULES_CREATE)
    public Response create(@ApiParam(name = "JSON body", required = true) @Valid @NotNull AlertRuleRequest request)
    		throws ValidationException, BadRequestException {

        alertRuleUtilsService.checkIsValidRequest(request);

        String alertTitle = checkImportPolicyAndGetTitle(request.getTitle());
        String userName = getCurrentUser().getName();
        // Create stream.
        Stream stream = alertRuleUtilsService.createStream(request.getStream(), alertTitle, userName);
        List<FieldRuleImpl> listPipelineFieldRule = alertRuleUtilsService.extractPipelineFieldRules(request.getStream().getFieldRules());
        RuleDao pipelineRule = alertRuleUtilsService.createPipelineRule(alertTitle, listPipelineFieldRule, stream, null);
        PipelineDao pipeline = alertRuleUtilsService.createPipeline(alertTitle, null, request.getStream().getMatchingType());
        String pipelineID = pipeline.id();
        String pipelineRuleID = pipelineRule.id();

        //Create unique data adapter
        DataAdapterDto adapter = alertRuleUtilsService.createUniqueDataAdapter(userName);
        CacheDto cache = alertRuleUtilsService.createUniqueCache();
        alertRuleUtilsService.createUniqueLookup(cache, adapter);

        // Create second stream.
        String streamID2 = null;
        Stream stream2 = null;
        String pipelineRuleID2 = null;
        String pipelineID2 = null;
        List<FieldRuleImpl> listPipelineFieldRule2 = null;
        if(request.getConditionType().equals("THEN") || request.getConditionType().equals("AND") || request.getConditionType().equals("OR")) {
        	stream2 = alertRuleUtilsService.createStream(request.getSecondStream(), alertTitle+"#2", userName);
        	streamID2 = stream2.getId();

            listPipelineFieldRule2 = alertRuleUtilsService.extractPipelineFieldRules(request.getSecondStream().getFieldRules());
            RuleDao pipelineRule2 = alertRuleUtilsService.createPipelineRule(alertTitle+"#2", listPipelineFieldRule2, stream2, null);
        	PipelineDao pipeline2 = alertRuleUtilsService.createPipeline(alertTitle+"#2", null, request.getStream().getMatchingType());
        	pipelineID2 = pipeline2.id();
        	pipelineRuleID2 = pipelineRule2.id();
        }

        Map<String, Object> conditionParameter = request.conditionParameters();

        LOG.info("Create condition type:" + request.getConditionType());
        //TODO
        EventProcessorConfig configuration;
        if(request.getConditionType().equals("THEN") || request.getConditionType().equals("AND")){
            String messsageOrder;
            if(request.getConditionType().equals("THEN")){
                messsageOrder = "AFTER";
            }else{
                messsageOrder = "ANY";
            }

            configuration = CorrelationCountProcessorConfig.builder()
                    .stream(stream.getId())
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

        } else if (request.getConditionType().equals("STATISTICAL")){
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
                case "HIGHER":
                    expression = Expr.Greater.create(left, right);
                    break;
                case "LOWER":
                    expression = Expr.Lesser.create(left, right);
                    break;
                default:
                    throw new BadRequestException();
            }

            configuration = AggregationEventProcessorConfig.builder()
                    .query("")
                    .streams(new HashSet<String> (Collections.singleton(stream.getId())))
                    .series(ImmutableList.of(serie))
                    .groupBy(ImmutableList.of())
                    .conditions(AggregationConditions.builder()
                            .expression(expression)
                            .build())
                    .executeEveryMs(((int) conditionParameter.get("grace")) * 60 * 1000)
                    .searchWithinMs(((int) conditionParameter.get("time")) * 60 * 1000)
                    .build();

        } else {
             configuration = AggregationCountProcessorConfig.builder()
                    .stream(stream.getId())
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



        LOG.info("After create Condition");

        LoggingNotificationConfig loggingNotificationConfig = LoggingNotificationConfig.builder()
                .singleMessage(false)
                .severity(SeverityType.valueOf(request.getSeverity().toUpperCase()))
                .logBody("Test")
                .build();
        NotificationDto notification = NotificationDto.builder()
                .config(loggingNotificationConfig)
                .title(alertTitle)
                .description(AlertRuleUtils.COMMENT_ALERT_WIZARD)
                .build();
        notification = this.notificationResourceHandler.create(notification);
        LOG.info("notification ID: "+notification.id());

        EventNotificationHandler.Config notificationConfiguration = EventNotificationHandler.Config.builder()
                .notificationId(notification.id())
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

        LOG.info("Event ID: " + eventDefinition.id());


        //Create Condition
     /*   String graylogConditionType = alertRuleUtils.getGraylogConditionType(request.getConditionType());
        Map<String, Object> parameters = alertRuleUtils.getConditionParameters(streamID2, request.getConditionType(), request.conditionParameters());
        String alertConditionID = alertRuleUtilsService.createCondition(graylogConditionType, alertTitle, parameters, stream, stream2, userName);
        //Create Notification
        String idAlarmCallBack = alertRuleUtilsService.createDefaultNotification(alertTitle, stream, request.getSeverity(), userName);

        //Or Condition for Second Stream
        if( request.getConditionType().equals("OR") && stream2 != null) {
        	//Create Condition
            alertRuleUtilsService.createCondition(graylogConditionType, alertTitle, parameters, stream2, stream, userName);
            //Create Notification
            alertRuleUtilsService.createDefaultNotification(alertTitle+"#2", stream2, request.getSeverity(), userName);
        }
*/

        clusterEventBus.post(StreamsChangedEvent.create(stream.getId()));
    	alertRuleService.create(AlertRuleImpl.create(
        		alertTitle,
        		stream.getId(),
                eventDefinition.id(),
                notification.id(),
				DateTime.now(),
				getCurrentUser().getName(),
				DateTime.now(),
				request.getDescription(),
				request.getConditionType(),
				streamID2,
                pipelineID,
                pipelineRuleID,
                listPipelineFieldRule,
                pipelineID2,
                pipelineRuleID2,
                listPipelineFieldRule2));

        LOG.info("After create Alert Rule");

        //Update list usage
        for (FieldRule fieldRule:alertRuleUtils.nullSafe(listPipelineFieldRule)) {
            alertListUtilsService.incrementUsage(fieldRule.getValue());
        }
        for (FieldRule fieldRule:alertRuleUtils.nullSafe(listPipelineFieldRule2)) {
            alertListUtilsService.incrementUsage(fieldRule.getValue());
        }
        
        return Response.accepted().build();
    }

    @POST
    @Path("/{title}")
    @Timed
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_UPDATE)
    @ApiOperation(value = "Update a alert")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "The supplied request is not valid.")})
    @AuditEvent(type = AlertWizardAuditEventTypes.WIZARD_ALERTS_RULES_UPDATE)
    public Response update(@ApiParam(name = TITLE, required = true)
    					   @PathParam(TITLE) String title,
                           @ApiParam(name = "JSON body", required = true) @Valid @NotNull AlertRuleRequest request
                             ) throws UnsupportedEncodingException, NotFoundException, ValidationException, ConfigurationException {

        alertRuleUtilsService.checkIsValidRequest(request);
    	
    	AlertRule oldAlert = alertRuleService.load(title);
        String alertTitle = request.getTitle();

        // Update stream.
        final Stream stream = streamService.load(oldAlert.getStreamID());
        alertRuleUtilsService.updateStream(stream, request.getStream(), alertTitle);

        //update pipeline
        RuleDao rule = ruleService.load(oldAlert.getPipelineRuleID());
        PipelineDao pipeline = pipelineService.load(oldAlert.getPipelineID());
        List<FieldRuleImpl> listPipelineFieldRule = alertRuleUtilsService.extractPipelineFieldRules(request.getStream().getFieldRules());
        alertRuleUtilsService.updatePipeline(stream, listPipelineFieldRule, pipeline, alertTitle, rule);

        String userName = getCurrentUser().getName();
        // Update stream 2.
        Stream stream2 = alertRuleUtilsService.createOrUpdateSecondStream(request.getSecondStream(), alertTitle, userName, request.getConditionType(), oldAlert);
        String streamID2 = null;
        RuleDao rule2 = null;
        PipelineDao pipeline2 = null;
        List<FieldRuleImpl> listPipelineFieldRule2 = null;
        if(stream2 != null){
            streamID2 = stream2.getId();
            rule2 = ruleService.load(oldAlert.getSecondPipelineRuleID());
            pipeline2 = pipelineService.load(oldAlert.getSecondPipelineID());
            listPipelineFieldRule2 = alertRuleUtilsService.extractPipelineFieldRules(request.getSecondStream().getFieldRules());
            alertRuleUtilsService.updatePipeline(stream2, listPipelineFieldRule2, pipeline2, alertTitle+"#2", rule2);
        } else if (oldAlert.getSecondStreamID() != null) {
            rule2 = ruleService.load(oldAlert.getSecondPipelineRuleID());
            pipeline2 = pipelineService.load(oldAlert.getSecondPipelineID());
            alertRuleUtilsService.deletePipeline(pipeline2, rule2);
        }

        //Update Condition   
        AlertCondition alertCondition = streamService.getAlertCondition(stream, oldAlert.getConditionID());
        String alertConditionID = alertRuleUtilsService.updateCondition(stream, alertCondition, alertTitle, request.getConditionType(), request.conditionParameters(), streamID2, userName);
        
        //Update Notification
        String notificationId = oldAlert.getNotificationID();
        if(!alertRuleUtilsService.updateNotification(alertTitle, notificationId, request.getSeverity())){
            notificationId = alertRuleUtilsService.createDefaultNotification(alertTitle, stream, request.getSeverity(), userName);
        }

        //Or Condition for Second Stream
        if( request.getConditionType().equals("OR") && stream2 != null) {
        	if(oldAlert.getConditionType().equals("OR")) {
	        	//Update Condition
	        	AlertCondition alertCondition2 = streamService.getAlertConditions(stream2).get(0);
                alertRuleUtilsService.updateCondition(stream2, alertCondition2, alertTitle, request.getConditionType(), request.conditionParameters(), null, userName);
          
	            //Remove Notification
                alertRuleUtilsService.removeNotificationFromStream(stream2);
        	}else {
                //Create Condition
                alertRuleUtilsService.createCondition(alertRuleUtils.getGraylogConditionType(request.getConditionType()), alertTitle,
                        alertRuleUtils.getConditionParameters(null, "OR", request.conditionParameters()), stream2, null, userName);
        	}
        	//Create Notification
            alertRuleUtilsService.createDefaultNotification(alertTitle+"#2", stream2, request.getSeverity(), userName);
        }

        alertRuleService.update(java.net.URLDecoder.decode(title, ENCODING),
                AlertRuleImpl.create(
                        alertTitle,
                        oldAlert.getStreamID(),
                        alertConditionID,
                        notificationId,
                        oldAlert.getCreatedAt(),
                        getCurrentUser().getName(),
                        DateTime.now(),
                        request.getDescription(),
                        request.getConditionType(),
                        streamID2,
                        oldAlert.getPipelineID(),
                        oldAlert.getPipelineRuleID(),
                        listPipelineFieldRule,
                        oldAlert.getSecondPipelineID(),
                        oldAlert.getSecondPipelineRuleID(),
                        listPipelineFieldRule2));

        //Decrement list usage
        for (FieldRule fieldRule:alertRuleUtils.nullSafe(oldAlert.getPipelineFieldRules())) {
            alertListUtilsService.decrementUsage(fieldRule.getValue());
        }
        for (FieldRule fieldRule:alertRuleUtils.nullSafe(oldAlert.getSecondPipelineFieldRules())) {
            alertListUtilsService.decrementUsage(fieldRule.getValue());
        }
        //Increment list usage
        for (FieldRule fieldRule:alertRuleUtils.nullSafe(listPipelineFieldRule)) {
            alertListUtilsService.incrementUsage(fieldRule.getValue());
        }
        for (FieldRule fieldRule:alertRuleUtils.nullSafe(listPipelineFieldRule2)) {
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
                           @ApiParam(name = "JSON body", required = true) @Valid @NotNull CloneAlertRuleRequest request
    ) throws NotFoundException, ValidationException {

        AlertRule sourceAlert = alertRuleService.load(title);
        String alertTitle = request.getTitle();
        final String creatorUser = getCurrentUser().getName();

        // Create stream.
        final Stream sourceFirstStream = streamService.load(sourceAlert.getStreamID());
        Stream firstStream = alertRuleUtilsService.cloneStream(sourceFirstStream, alertTitle, creatorUser);

        //create pipeline
        RuleDao pipelineRule = alertRuleUtilsService.createPipelineRule(alertTitle, sourceAlert.getPipelineFieldRules(), firstStream, null);
        PipelineDao pipeline = alertRuleUtilsService.createPipeline(alertTitle, null, sourceFirstStream.getMatchingType().toString());
        String pipelineID = pipeline.id();
        String pipelineRuleID = pipelineRule.id();

        Stream secondStream = null;
        String secondStreamID = null;
        String pipelineRuleID2 = null;
        String pipelineID2 = null;

        //Create Second Stream
        if(sourceAlert.getSecondStreamID() != null && !sourceAlert.getSecondStreamID().isEmpty()) {
            final Stream sourceSecondStream = streamService.load(sourceAlert.getSecondStreamID());
        	secondStream = alertRuleUtilsService.cloneStream(sourceSecondStream, alertTitle+"#2", creatorUser);
        	secondStreamID = secondStream.getId();

            RuleDao pipelineRule2 = alertRuleUtilsService.createPipelineRule(alertTitle+"#2", sourceAlert.getSecondPipelineFieldRules(), secondStream, null);
            PipelineDao pipeline2 = alertRuleUtilsService.createPipeline(alertTitle+"#2", null, sourceFirstStream.getMatchingType().toString());
            pipelineID2 = pipeline2.id();
            pipelineRuleID2 = pipelineRule2.id();
        }

        //Create Condition
        AlertCondition alertCondition = streamService.getAlertCondition(sourceFirstStream, sourceAlert.getConditionID());
        Map<String, Object> conditionParameters = Maps.newHashMap();
        conditionParameters.putAll(alertCondition.getParameters());
        if(secondStream != null && alertCondition.getType().equals(AlertRuleUtils.TYPE_CORRELATION)) {
            conditionParameters.replace(AlertRuleUtils.ADDITIONAL_STREAM, secondStream.getId());
        }
        String alertConditionID = alertRuleUtilsService.createCondition(alertCondition.getType(), alertTitle, conditionParameters, firstStream, secondStream, creatorUser);

        //Create notification
        String alertNotificationID = null;
        AlarmCallbackConfiguration alarmCallbackConfig = null;
        if(sourceAlert.getNotificationID() != null && !sourceAlert.getNotificationID().isEmpty()) {
	        alarmCallbackConfig = alarmCallbackConfigurationService.load(sourceAlert.getNotificationID());
            alertNotificationID = alertRuleUtilsService.createNotificationFromConfiguration(alertTitle, firstStream, alarmCallbackConfig, creatorUser);
        }

        for (Output output : sourceFirstStream.getOutputs()) {
            streamService.addOutput(firstStream, output);
        }
        clusterEventBus.post(StreamsChangedEvent.create(firstStream.getId()));
        
        //If OR condition
        if(sourceAlert.getConditionType().equals("OR") && secondStream != null) {
        	//Create Condition
            alertRuleUtilsService.createCondition(alertCondition.getType(), alertTitle+"#2", alertCondition.getParameters(), secondStream, firstStream, creatorUser);
        	 
        	//Create notification
        	if(alarmCallbackConfig != null) {
                alertNotificationID = alertRuleUtilsService.createNotificationFromConfiguration(alertTitle+"#2", secondStream, alarmCallbackConfig, creatorUser);
        	}
            clusterEventBus.post(StreamsChangedEvent.create(secondStream.getId()));
        }

        alertRuleService.create(AlertRuleImpl.create(
        		alertTitle,
        		firstStream.getId(),
                alertConditionID,
        		alertNotificationID,
				DateTime.now(),
				getCurrentUser().getName(),
				DateTime.now(),
				request.getDescription(),
				sourceAlert.getConditionType(),
				secondStreamID,
                pipelineID,
                pipelineRuleID,
                sourceAlert.getPipelineFieldRules(),
                pipelineID2,
                pipelineRuleID2,
                sourceAlert.getSecondPipelineFieldRules()));

        //Update list usage
        for (FieldRule fieldRule:alertRuleUtils.nullSafe(sourceAlert.getPipelineFieldRules())) {
            alertListUtilsService.incrementUsage(fieldRule.getValue());
        }
        for (FieldRule fieldRule:alertRuleUtils.nullSafe(sourceAlert.getSecondPipelineFieldRules())) {
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

        try{
        	AlertRule alertRule = alertRuleService.load(alertTitle);
        	//Delete first Stream
            alertRuleUtilsService.deleteStreamFromID(alertRule.getStreamID());

            //Delete second Stream
            if(alertRule.getSecondStreamID() != null && !alertRule.getSecondStreamID().isEmpty()) {
                alertRuleUtilsService.deleteStreamFromID(alertRule.getSecondStreamID());
            }

            eventDefinitionsResource.delete(alertRule.getConditionID());
            eventNotificationsResource.delete(alertRule.getNotificationID());

            if (alertRule.getPipelineID() != null && alertRule.getPipelineRuleID() != null) {
                RuleDao rule = ruleService.load(alertRule.getPipelineRuleID());
                PipelineDao pipeline = pipelineService.load(alertRule.getPipelineID());
                alertRuleUtilsService.deletePipeline(pipeline, rule);
            }

            if (alertRule.getSecondPipelineID() != null && alertRule.getSecondPipelineRuleID() != null) {
                RuleDao rule2 = ruleService.load(alertRule.getSecondPipelineRuleID());
                PipelineDao pipeline2 = pipelineService.load(alertRule.getSecondPipelineID());
                alertRuleUtilsService.deletePipeline(pipeline2, rule2);
            }

            //Update list usage
            for (FieldRule fieldRule:alertRuleUtils.nullSafe(alertRule.getPipelineFieldRules())) {
                alertListUtilsService.decrementUsage(fieldRule.getValue());
            }
            for (FieldRule fieldRule:alertRuleUtils.nullSafe(alertRule.getSecondPipelineFieldRules())) {
                alertListUtilsService.decrementUsage(fieldRule.getValue());
            }
        }catch(NotFoundException e){
            LOG.error("Cannot find alert " + alertTitle , e);
        }
        
        alertRuleService.destroy(alertTitle);
    }
    
    @POST
    @Path("/export")
    @Timed
    @ApiOperation(value = "Export alert rules")
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_READ)
    @AuditEvent(type = AlertWizardAuditEventTypes.WIZARD_ALERTS_RULES_READ)
    public List<ExportAlertRule> getExportAlertRule(@ApiParam(name = "JSON body", required = true) @Valid @NotNull ExportAlertRuleRequest request) {
    	LOG.info("List titles : " + request.getTitles());
        return alertRuleExporter.export(request.getTitles());
    }


    public void importAlertRule(ExportAlertRule alertRule)
            throws ValidationException, BadRequestException{
        String alertTitle = checkImportPolicyAndGetTitle(alertRule.getTitle());
        String userName = getCurrentUser().getName();

        // Create stream.
        Stream stream = alertRuleUtilsService.createStream(alertRule.getStream(), alertTitle, userName);
        List<FieldRuleImpl> listPipelineFieldRule = alertRuleUtilsService.extractPipelineFieldRules(alertRule.getStream().getFieldRules());
        RuleDao pipelineRule = alertRuleUtilsService.createPipelineRule(alertTitle, listPipelineFieldRule, stream, null);
        PipelineDao pipeline = alertRuleUtilsService.createPipeline(alertTitle, null, alertRule.getStream().getMatchingType());
        String pipelineID = pipeline.id();
        String pipelineRuleID = pipelineRule.id();

        //Create unique data adapter
        DataAdapterDto adapter = alertRuleUtilsService.createUniqueDataAdapter(userName);
        CacheDto cache = alertRuleUtilsService.createUniqueCache();
        alertRuleUtilsService.createUniqueLookup(cache, adapter);

        String pipelineRuleID2 = null;
        String pipelineID2 = null;
        // Create second stream.
        String streamID2 = null;
        Stream stream2 = null;
        List<FieldRuleImpl> listPipelineFieldRule2 = null;
        if(alertRule.getConditionType().equals("THEN") || alertRule.getConditionType().equals("AND") || alertRule.getConditionType().equals("OR")) {
            stream2 = alertRuleUtilsService.createStream(alertRule.getSecondStream(), alertTitle+"#2", userName);
            streamID2 = stream2.getId();

            listPipelineFieldRule2 = alertRuleUtilsService.extractPipelineFieldRules(alertRule.getSecondStream().getFieldRules());
            RuleDao pipelineRule2 = alertRuleUtilsService.createPipelineRule(alertTitle+"#2", listPipelineFieldRule2, stream2, null);
            PipelineDao pipeline2 = alertRuleUtilsService.createPipeline(alertTitle+"#2", null, alertRule.getStream().getMatchingType());
            pipelineID2 = pipeline2.id();
            pipelineRuleID2 = pipelineRule2.id();
        }

        //Create Condition
        String graylogConditionType = alertRuleUtils.getGraylogConditionType(alertRule.getConditionType());
        Map<String, Object> conditionParameters = alertRule.conditionParameters();
        if(graylogConditionType.equals(AlertRuleUtils.TYPE_CORRELATION)){
            conditionParameters.replace(AlertRuleUtils.ADDITIONAL_STREAM, streamID2);
        }
        String alertConditionID = alertRuleUtilsService.createCondition(graylogConditionType, alertTitle, conditionParameters, stream, stream2, userName);
        //Create Notification
        String idAlarmCallBack = null;
        if(alertRule.notificationParameters() != null) {
            idAlarmCallBack = alertRuleUtilsService.createNotificationFromParameters(alertTitle, stream, alertRule.notificationParameters(), userName);
        }

        //Or Condition for Second Stream
        if( alertRule.getConditionType().equals("OR") && stream2 != null) {
            //Create Condition
            alertRuleUtilsService.createCondition(graylogConditionType, alertTitle, alertRule.conditionParameters(), stream2, stream, userName);
            //Create Notification
            if(alertRule.notificationParameters() != null) {
                alertRuleUtilsService.createNotificationFromParameters(alertTitle+"#2", stream2, alertRule.notificationParameters(), userName);
            }
        }

        clusterEventBus.post(StreamsChangedEvent.create(stream.getId()));
        alertRuleService.create(AlertRuleImpl.create(
                alertTitle,
                stream.getId(),
                alertConditionID,
                idAlarmCallBack,
                DateTime.now(),
                getCurrentUser().getName(),
                DateTime.now(),
                alertRule.getDescription(),
                alertRule.getConditionType(),
                streamID2,
                pipelineID,
                pipelineRuleID,
                listPipelineFieldRule,
                pipelineID2,
                pipelineRuleID2,
                listPipelineFieldRule2));

        //Update list usage
        for (FieldRule fieldRule:alertRuleUtils.nullSafe(listPipelineFieldRule)) {
            alertListUtilsService.incrementUsage(fieldRule.getValue());
        }
        for (FieldRule fieldRule:alertRuleUtils.nullSafe(listPipelineFieldRule2)) {
            alertListUtilsService.incrementUsage(fieldRule.getValue());
        }
    }

    @PUT
    @Path("/import")
    @Timed    
    @ApiOperation(value = "Import a alert")
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_CREATE)
    @ApiResponses(value = {@ApiResponse(code = 400, message = "The supplied request is not valid.")})
    @AuditEvent(type = AlertWizardAuditEventTypes.WIZARD_ALERTS_RULES_CREATE)
    public Response importAlertRules (@ApiParam(name = "JSON body", required = true) @Valid @NotNull List<ExportAlertRule> request) {
    	Response responses = Response.accepted().build();
    			
    	for (ExportAlertRule alertRule : request) {
            if(!alertRuleService.isValidImportRequest(alertRule)){
                LOG.error("Invalid alert rule:" + alertRule.getTitle() );
            }else {
				try {
                    importAlertRule(alertRule);
				} catch (Exception e) {
					LOG.error("Cannot create alert "+ alertRule.getTitle() + ": ", e.getMessage());
					responses = Response.serverError().build();
				}
            }
		}
    	
		return responses;
    }
}
