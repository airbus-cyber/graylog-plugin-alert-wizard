package com.airbus_cyber_security.graylog.alert.rest;

import com.airbus_cyber_security.graylog.alert.AlertRule;
import com.airbus_cyber_security.graylog.alert.AlertRuleImpl;
import com.airbus_cyber_security.graylog.alert.AlertRuleService;
import com.airbus_cyber_security.graylog.alert.bundles.AlertRuleExporter;
import com.airbus_cyber_security.graylog.alert.bundles.ExportAlertRuleRequest;
import com.airbus_cyber_security.graylog.alert.bundles.ExportAlertRule;
import com.airbus_cyber_security.graylog.alert.rest.models.requests.AlertRuleRequest;
import com.airbus_cyber_security.graylog.alert.rest.models.requests.CloneAlertRuleRequest;
import com.airbus_cyber_security.graylog.alert.rest.models.responses.GetAlertRule;
import com.airbus_cyber_security.graylog.alert.rest.models.responses.GetDataAlertRule;
import com.airbus_cyber_security.graylog.alert.rest.models.responses.GetListAlertRule;
import com.airbus_cyber_security.graylog.alert.rest.models.responses.GetListDataAlertRule;
import com.airbus_cyber_security.graylog.alert.utilities.AlertRuleUtils;
import com.airbus_cyber_security.graylog.alert.utilities.AlertRuleUtilsService;
import com.airbus_cyber_security.graylog.audit.AlertWizardAuditEventTypes;
import com.airbus_cyber_security.graylog.permissions.AlertRuleRestPermissions;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.Maps;
import com.mongodb.MongoException;

import com.airbus_cyber_security.graylog.config.rest.AlertWizardConfig;
import com.airbus_cyber_security.graylog.config.rest.ImportPolicyType;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.graylog.plugins.pipelineprocessor.db.*;
import org.graylog2.alarmcallbacks.AlarmCallbackConfiguration;
import org.graylog2.alarmcallbacks.AlarmCallbackConfigurationService;
import org.graylog2.alarmcallbacks.AlarmCallbackFactory;
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
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.database.ValidationException;
import org.graylog2.plugin.rest.PluginRestResource;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.shared.rest.resources.RestResource;
import org.graylog2.streams.StreamRuleService;
import org.graylog2.streams.StreamService;
import org.graylog2.streams.events.StreamsChangedEvent;
import org.graylog2.plugin.streams.Output;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
                             PipelineStreamConnectionsService pipelineStreamConnectionsService) {
        this.alertRuleService = alertRuleService;
        this.streamService = streamService;
        this.clusterEventBus = clusterEventBus;
        this.alarmCallbackConfigurationService = alarmCallbackConfigurationService;
        this.clusterConfigService = clusterConfigService;
        this.ruleService = ruleService;
        this.pipelineService = pipelineService;
        this.alertRuleUtils = new AlertRuleUtils();
        this.alertRuleUtilsService = new AlertRuleUtilsService(alertRuleService, streamService, streamRuleService, clusterEventBus,
                indexSetRegistry.getDefault().getConfig().id(), alertService, alarmCallbackConfigurationService,
                alarmCallbackFactory, clusterConfigService, ruleService, pipelineService, dbDataAdapterService,
                httpConfiguration, dbCacheService, dbTableService, pipelineStreamConnectionsService, alertRuleUtils);
        this.alertRuleExporter = new AlertRuleExporter(alertRuleService, alarmCallbackConfigurationService, streamService, alertRuleUtils);
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
    		throws ValidationException, BadRequestException{

        alertRuleUtilsService.checkIsValidRequest(request);


        List<String> pipelineRuleID = new ArrayList<String>();
        String alertTitle = checkImportPolicyAndGetTitle(request.getTitle());
        String userName = getCurrentUser().getName();
        // Create stream.
        Stream stream = alertRuleUtilsService.createStream(request.getStream(), alertTitle, userName);
        RuleDao pipelineRule = alertRuleUtilsService.createPipelineRule(alertTitle, request.getStream().getFieldRules(), stream, null);
        PipelineDao pipeline = alertRuleUtilsService.createPipeline(alertTitle, null);
        String pipelineID = pipeline.id();
        pipelineRuleID.add(pipelineRule.id());

        //Create unique data adapter
        DataAdapterDto adapter = alertRuleUtilsService.createUniqueDataAdapter(userName);
        CacheDto cache = alertRuleUtilsService.createUniqueCache();
        alertRuleUtilsService.createUniqueLookup(cache, adapter);

        // Create second stream.
        String streamID2 = null;
        Stream stream2 = null;
        List<String> pipelineRuleID2 = new ArrayList<String>();
        String pipelineID2 = null;
        if(request.getConditionType().equals("THEN") || request.getConditionType().equals("AND") || request.getConditionType().equals("OR")) {
        	stream2 = alertRuleUtilsService.createStream(request.getSecondStream(), alertTitle+"#2", userName);
        	streamID2 = stream2.getId();

        	RuleDao pipelineRule2 = alertRuleUtilsService.createPipelineRule(alertTitle+"#2", request.getSecondStream().getFieldRules(), stream2, null);
        	PipelineDao pipeline2 = alertRuleUtilsService.createPipeline(alertTitle+"#2", null);
        	pipelineID2 = pipeline2.id();
        	pipelineRuleID2.add(pipelineRule2.id());
        }

        //Create Condition
        String graylogConditionType = alertRuleUtils.getGraylogConditionType(request.getConditionType());
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

        clusterEventBus.post(StreamsChangedEvent.create(stream.getId()));   
    	alertRuleService.create(AlertRuleImpl.create(
        		alertTitle,
        		stream.getId(),
                alertConditionID,
        		idAlarmCallBack,
				DateTime.now(),
				getCurrentUser().getName(),
				DateTime.now(),
				request.getDescription(),
				request.getConditionType(),
				streamID2,
                pipelineID,
                pipelineRuleID,
                pipelineID2,
                pipelineRuleID2));
        
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
        RuleDao rule = ruleService.load(oldAlert.getPipelineRuleID().get(0));
        PipelineDao pipeline = pipelineService.load(oldAlert.getPipelineID());
        alertRuleUtilsService.updatePipeline(stream, request.getStream().getFieldRules(), pipeline, alertTitle, rule);

        String userName = getCurrentUser().getName();
        // Update stream 2.
        Stream stream2 = alertRuleUtilsService.createOrUpdateSecondStream(request.getSecondStream(), alertTitle, userName, request.getConditionType(), oldAlert);
        String streamID2 = null;
        RuleDao rule2 = null;
        PipelineDao pipeline2 = null;
        if(stream2 != null){
            streamID2 = stream2.getId();

            rule2 = ruleService.load(oldAlert.getSecondPipelineRuleID().get(0));
            pipeline2 = pipelineService.load(oldAlert.getSecondPipelineID());
            alertRuleUtilsService.updatePipeline(stream2, request.getSecondStream().getFieldRules(), pipeline2, alertTitle+"#2", rule2);
            
        } else if (oldAlert.getSecondStreamID() != null) {
            rule2 = ruleService.load(oldAlert.getSecondPipelineRuleID().get(0));
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
                        oldAlert.getSecondPipelineID(),
                        oldAlert.getSecondPipelineRuleID()));

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

        List<String> pipelineRuleID = new ArrayList<String>();

        // Create stream.
        final Stream sourceFirstStream = streamService.load(sourceAlert.getStreamID());
        Stream firstStream = alertRuleUtilsService.cloneStream(sourceFirstStream, alertTitle, creatorUser);

        //create pipeline
        RuleDao pipelineRule = alertRuleUtilsService.clonePipelineRule(firstStream, alertTitle);
        PipelineDao pipeline = alertRuleUtilsService.createPipeline(alertTitle, null);
        String pipelineID = pipeline.id();
        pipelineRuleID.add(pipelineRule.id());

        Stream secondStream = null;
        String secondStreamID = null;
        List<String> pipelineRuleID2 = new ArrayList<String>();
        String pipelineID2 = null;

        //Create Second Stream
        if(sourceAlert.getSecondStreamID() != null && !sourceAlert.getSecondStreamID().isEmpty()) {
            final Stream sourceSecondStream = streamService.load(sourceAlert.getSecondStreamID());
        	secondStream = alertRuleUtilsService.cloneStream(sourceSecondStream, alertTitle+"#2", creatorUser);
        	secondStreamID = secondStream.getId();

            RuleDao pipelineRule2 = alertRuleUtilsService.clonePipelineRule(secondStream, alertTitle+"#2");
            PipelineDao pipeline2 = alertRuleUtilsService.createPipeline(alertTitle+"#2", null);
            pipelineID2 = pipeline2.id();
            pipelineRuleID2.add(pipelineRule2.id());
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
                pipelineID2,
                pipelineRuleID2));

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

            if (alertRule.getPipelineID() != null && alertRule.getPipelineRuleID() != null) {
                RuleDao rule = ruleService.load(alertRule.getPipelineRuleID().get(0));
                PipelineDao pipeline = pipelineService.load(alertRule.getPipelineID());
                alertRuleUtilsService.deletePipeline(pipeline, rule);
            }

            if (alertRule.getSecondPipelineID() != null && alertRule.getSecondPipelineRuleID() != null) {
                RuleDao rule2 = ruleService.load(alertRule.getSecondPipelineRuleID().get(0));
                PipelineDao pipeline2 = pipelineService.load(alertRule.getSecondPipelineID());
                alertRuleUtilsService.deletePipeline(pipeline2, rule2);
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
        List<String> pipelineRuleID = new ArrayList<String>();

        // Create stream.
        Stream stream = alertRuleUtilsService.createStream(alertRule.getStream(), alertTitle, userName);
        RuleDao pipelineRule = alertRuleUtilsService.createPipelineRule(alertTitle, alertRule.getStream().getFieldRules(), stream, null);
        PipelineDao pipeline = alertRuleUtilsService.createPipeline(alertTitle, null);
        String pipelineID = pipeline.id();
        pipelineRuleID.add(pipelineRule.id());

        //Create unique data adapter
        DataAdapterDto adapter = alertRuleUtilsService.createUniqueDataAdapter(userName);
        CacheDto cache = alertRuleUtilsService.createUniqueCache();
        alertRuleUtilsService.createUniqueLookup(cache, adapter);

        List<String> pipelineRuleID2 = new ArrayList<String>();
        String pipelineID2 = null;
        // Create second stream.
        String streamID2 = null;
        Stream stream2 = null;
        if(alertRule.getConditionType().equals("THEN") || alertRule.getConditionType().equals("AND") || alertRule.getConditionType().equals("OR")) {
            stream2 = alertRuleUtilsService.createStream(alertRule.getSecondStream(), alertTitle+"#2", userName);
            streamID2 = stream2.getId();

            RuleDao pipelineRule2 = alertRuleUtilsService.createPipelineRule(alertTitle+"#2", alertRule.getSecondStream().getFieldRules(), stream2, null);
            PipelineDao pipeline2 = alertRuleUtilsService.createPipeline(alertTitle+"#2", null);
            pipelineID2 = pipeline2.id();
            pipelineRuleID2.add(pipelineRule2.id());
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
                pipelineID2,
                pipelineRuleID2));
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
