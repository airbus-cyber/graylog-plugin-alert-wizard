package com.airbus_cyber_security.graylog.alert.rest;

import com.airbus_cyber_security.graylog.alert.AlertRule;
import com.airbus_cyber_security.graylog.alert.AlertRuleImpl;
import com.airbus_cyber_security.graylog.alert.AlertRuleService;
import com.airbus_cyber_security.graylog.alert.AlertRuleStream;
import com.airbus_cyber_security.graylog.alert.AlertRuleStreamImpl;
import com.airbus_cyber_security.graylog.alert.FieldRule;
import com.airbus_cyber_security.graylog.alert.FieldRuleImpl;
import com.airbus_cyber_security.graylog.alert.bundles.AlertRuleExporter;
import com.airbus_cyber_security.graylog.alert.bundles.ExportAlertRuleRequest;
import com.airbus_cyber_security.graylog.alert.rest.models.requests.AlertRuleRequest;
import com.airbus_cyber_security.graylog.alert.rest.models.requests.CloneAlertRuleRequest;
import com.airbus_cyber_security.graylog.alert.rest.models.responses.GetAlertRule;
import com.airbus_cyber_security.graylog.alert.rest.models.responses.GetDataAlertRule;
import com.airbus_cyber_security.graylog.alert.rest.models.responses.GetListAlertRule;
import com.airbus_cyber_security.graylog.alert.rest.models.responses.GetListDataAlertRule;
import com.airbus_cyber_security.graylog.alert.utilities.AlertRuleUtils;
import com.airbus_cyber_security.graylog.audit.AlertWizardAuditEventTypes;
import com.airbus_cyber_security.graylog.permissions.AlertRuleRestPermissions;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.Maps;
import com.mongodb.MongoException;

import com.airbus_cyber_security.graylog.config.LoggingAlertConfig;
import com.airbus_cyber_security.graylog.config.rest.AlertWizardConfig;
import com.airbus_cyber_security.graylog.config.rest.ImportPolicyType;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.bson.types.ObjectId;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.graylog2.alarmcallbacks.AlarmCallbackConfiguration;
import org.graylog2.alarmcallbacks.AlarmCallbackConfigurationImpl;
import org.graylog2.alarmcallbacks.AlarmCallbackConfigurationService;
import org.graylog2.alarmcallbacks.AlarmCallbackFactory;
import org.graylog2.alerts.Alert;
import org.graylog2.alerts.AlertService;
import org.graylog2.audit.jersey.AuditEvent;
import org.graylog2.database.NotFoundException;
import org.graylog2.events.ClusterEventBus;
import org.graylog2.indexer.IndexSetRegistry;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.database.ValidationException;
import org.graylog2.plugin.rest.PluginRestResource;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.plugin.streams.StreamRule;
import org.graylog2.rest.models.alarmcallbacks.requests.CreateAlarmCallbackRequest;
import org.graylog2.rest.models.streams.alerts.requests.CreateConditionRequest;
import org.graylog2.rest.resources.streams.requests.CreateStreamRequest;
import org.graylog2.shared.rest.resources.RestResource;
import org.graylog2.streams.StreamRuleService;
import org.graylog2.streams.StreamService;
import org.graylog2.streams.events.StreamDeletedEvent;
import org.graylog2.streams.events.StreamsChangedEvent;
import org.graylog2.streams.StreamImpl;
import org.graylog2.streams.StreamRuleImpl;
import org.graylog2.plugin.streams.Output;
import org.graylog2.plugin.Tools;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

@Api(value = "Wizard/Alerts", description = "Management of Wizard alerts rules.")
@Path("/alerts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AlertRuleResource extends RestResource implements PluginRestResource {
	private static final Logger LOG = LoggerFactory.getLogger(AlertRuleResource.class);
	
	private static final String ENCODING = "UTF-8";
    private static final String TITLE = "title";
    
    private static final String ERROR_ALARM_CALLBACK_CONFIGURATION = "Invalid alarm callback configuration.";
    private static final String ERROR_ALARM_CALLBACK_TYPE = "Invalid alarm callback type.";
    private static final String ERROR_CLONE = "Unable to clone alert rule ";
    private static final String ERROR_ALARM_CONDITION_CONFIGURATION = "Invalid alarm Condition configuration.";
    	
    private final AlertRuleService alertRuleService;
    private final StreamService streamService;
    private final StreamRuleService streamRuleService;
    private final ClusterEventBus clusterEventBus;
    private final AlertService alertService; 
    private final AlarmCallbackConfigurationService alarmCallbackConfigurationService;
    private final AlarmCallbackFactory alarmCallbackFactory;
    private final ClusterConfigService clusterConfigService;
    private final String indexSetID;
    private final AlertRuleUtils alertRuleUtils;
    private final AlertRuleExporter alertRuleExporter;
    
    @Inject
    public AlertRuleResource(AlertRuleService alertRuleService,
                             StreamService streamService,
                             StreamRuleService streamRuleService,
                             ClusterEventBus clusterEventBus,
                             IndexSetRegistry indexSetRegistry,
                             AlertService alertService,
                             AlarmCallbackConfigurationService alarmCallbackConfigurationService,
                             AlarmCallbackFactory alarmCallbackFactory,
                             ClusterConfigService clusterConfigService) {
        this.alertRuleService = alertRuleService;
        this.streamService = streamService;
        this.streamRuleService = streamRuleService;
        this.clusterEventBus = clusterEventBus;
        this.alertService = alertService;
        this.alarmCallbackConfigurationService = alarmCallbackConfigurationService;
        this.alarmCallbackFactory = alarmCallbackFactory;
        this.clusterConfigService = clusterConfigService;
        this.indexSetID = indexSetRegistry.getDefault().getConfig().id();
        this.alertRuleUtils = new AlertRuleUtils();
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
        return constructDataAlertRule(alert);
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
                alertsData.add(constructDataAlertRule(alert));
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
    
    @PUT
    @Timed    
    @ApiOperation(value = "Create a alert")
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_CREATE)
    @ApiResponses(value = {@ApiResponse(code = 400, message = "The supplied request is not valid.")})
    @AuditEvent(type = AlertWizardAuditEventTypes.WIZARD_ALERTS_RULES_CREATE)
    public Response create(@ApiParam(name = "JSON body", required = true) @Valid @NotNull AlertRuleRequest request)
    		throws ValidationException{

        if(!alertRuleService.isValidRequest(request)){
            LOG.error("Invalid alert rule request");
            throw new BadRequestException("Invalid alert rule request.");
        }

        String alertTitle = request.getTitle();
        if(alertRuleService.isPresent(alertTitle)) {
        	final AlertWizardConfig configGeneral = clusterConfigService.get(AlertWizardConfig.class);
        	if(configGeneral.accessImportPolicy().equals(ImportPolicyType.RENAME)) {
        		String newAlertTitle;
        		Integer i = 1;
        		do{
        			newAlertTitle = alertTitle+"("+i.toString()+")";
        			i++;
        		}while (alertRuleService.isPresent(newAlertTitle));
        		alertTitle = newAlertTitle;
        	}else if(configGeneral.accessImportPolicy().equals(ImportPolicyType.REPLACE)) {
        		try {
					this.delete(alertTitle);
				} catch (MongoException | UnsupportedEncodingException e) {
					LOG.error("Failed to replace alert rule");
		            throw new BadRequestException("Failed to replace alert rule.");
				}
        	}else {
	        	LOG.error("Failed to create alert rule : Alert rule title already exist");
	            throw new BadRequestException("Failed to create alert rule : Alert rule title already exist.");
        	}
        }

        // Create stream.
        Stream stream = createStream(request.getStream(), alertTitle);
           
        // Create second stream.
        String streamID2 = null;
        Stream stream2 = null;
        if(request.getConditionType().equals("THEN") || request.getConditionType().equals("AND") || request.getConditionType().equals("OR")) {
        	stream2 = createStream(request.getSecondStream(), alertTitle+"#2");
        	streamID2 = stream2.getId();
        }
            
        //Create Condition
        String conditionType = alertRuleUtils.getconditionType(request.getConditionType());
        Map<String, Object> parameters = alertRuleUtils.getConditionParameters(streamID2, request.getConditionType(), request.conditionParameters());
        CreateConditionRequest ccr = CreateConditionRequest.create(alertRuleUtils.getconditionType(request.getConditionType()) , alertTitle, parameters);
        AlertCondition alertCondition;
		try {
			alertCondition = alertService.fromRequest(ccr, stream, getCurrentUser().getName());
			streamService.addAlertCondition(stream, alertCondition);
		} catch (ConfigurationException | ValidationException e) {
			cleanAlertRule(stream, stream2);
			LOG.error(ERROR_ALARM_CONDITION_CONFIGURATION, e);
			throw new BadRequestException(e.getMessage(), e);
		}
		
		String idAlarmCallBack = null;
        //Create Notification
		if(alertRuleUtils.isValidSeverity(request.getSeverity())){
	        try {
	        	idAlarmCallBack = createNotification(alertTitle, stream, getParametersNotification(request.getSeverity()));
	        } catch (ValidationException | AlarmCallbackConfigurationException | ConfigurationException e) {
	            LOG.error(ERROR_ALARM_CALLBACK_CONFIGURATION, e);
	        } catch (ClassNotFoundException e) {
	            LOG.error(ERROR_ALARM_CALLBACK_TYPE, e);
	        }
		}
        //Or Condition for Second Stream
        if( request.getConditionType().equals("OR") && stream2 != null) {
        	//Create Condition
        	CreateConditionRequest ccr2 = CreateConditionRequest.create(conditionType , alertTitle, parameters);
            try {
            	final AlertCondition alertCondition2 = alertService.fromRequest(ccr2, stream2, getCurrentUser().getName());
                streamService.addAlertCondition(stream2, alertCondition2);
    		} catch (ConfigurationException | ValidationException e) {
    			cleanAlertRule(stream, stream2);
    			LOG.error(ERROR_ALARM_CONDITION_CONFIGURATION, e);
    			throw new BadRequestException(e.getMessage(), e);
    		}
           
            //Create Notification    
            if(alertRuleUtils.isValidSeverity(request.getSeverity())){
	            try {
	            	createNotification(alertTitle+"#2", stream2, getParametersNotification(request.getSeverity()));
	            } catch (ValidationException | AlarmCallbackConfigurationException | ConfigurationException e) {
	                LOG.error(ERROR_ALARM_CALLBACK_CONFIGURATION, e);
	            } catch (ClassNotFoundException e) {
	                LOG.error(ERROR_ALARM_CALLBACK_TYPE, e);
	            }
            }
        }
        clusterEventBus.post(StreamsChangedEvent.create(stream.getId()));   
    	alertRuleService.create(AlertRuleImpl.create(
        		alertTitle,
        		stream.getId(),
        		alertCondition.getId(),
        		idAlarmCallBack,
				DateTime.now(),
				getCurrentUser().getName(),
				DateTime.now(),
				request.getDescription(),
				request.getConditionType(),
				streamID2));
        
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
        
        if(!alertRuleService.isValidRequest(request)){
            LOG.error("Invalid alert rule request");
            throw new BadRequestException("Invalid alert rule request.");
        }
    	
    	AlertRule oldAlert = alertRuleService.load(title);
        String alertTitle = request.getTitle();

        // Update stream.
        final Stream stream = streamService.load(oldAlert.getStreamID());
        updateStream(stream, request.getStream(), alertTitle);
        
        // Update stream 2.
        String streamID2 = null;
        Stream stream2 = null;
        // Create second stream.
        if(request.getConditionType().equals("THEN") || request.getConditionType().equals("AND") || request.getConditionType().equals("OR")) {
        	if(oldAlert.getSecondStreamID() != null) {
        		stream2 = streamService.load(oldAlert.getSecondStreamID());
                updateStream(stream2, request.getSecondStream(), alertTitle+"#2");
                // If request condition is not "OR" and the old one is "OR" remove stream condition and notification
                if(!request.getConditionType().equals("OR") && oldAlert.getConditionType().equals("OR")) {
                	removeConditionAndNotificationFromStream(stream2);
                }   
        	}else {
        		stream2 = createStream(request.getSecondStream(), alertTitle+"#2");
        	}
        	streamID2=stream2.getId();
        }else {
        	//Delete old stream if one
        	if(oldAlert.getSecondStreamID() != null && !oldAlert.getSecondStreamID().isEmpty()) {
	        	try {
	 		    	final Stream oldStream = streamService.load(oldAlert.getSecondStreamID());
	 		    	streamService.destroy(oldStream);
	 		    	clusterEventBus.post(StreamsChangedEvent.create(oldStream.getId()));
	 		    	clusterEventBus.post(StreamDeletedEvent.create(oldStream.getId()));   
	 		    }catch(NotFoundException e){
	 		        LOG.error("Cannot find the stream for alert " + alertTitle , e);
	 		    }
        	}
        }
        
        //Update Condition   
        AlertCondition alertCondition = streamService.getAlertCondition(stream, oldAlert.getConditionID());
        String alertConditionID = updateCondition(stream, alertCondition, alertTitle, request.getConditionType(), request.conditionParameters(), streamID2);
        
        //Update Notification
        String notificationId = oldAlert.getNotificationID();
        if(alertRuleUtils.isValidSeverity(request.getSeverity())){
	        try {
	        	updateNotification(alertTitle, oldAlert.getNotificationID(), getParametersNotification(request.getSeverity()));
	        } catch (ValidationException | AlarmCallbackConfigurationException | ConfigurationException e) {
	            LOG.error(ERROR_ALARM_CALLBACK_CONFIGURATION, e);
	        } catch (ClassNotFoundException e) {
	            LOG.error(ERROR_ALARM_CALLBACK_TYPE, e);
	       
            	try {
            		notificationId = createNotification(alertTitle, stream, getParametersNotification(request.getSeverity()));
				} catch (ValidationException | AlarmCallbackConfigurationException | ConfigurationException e1) {
		            LOG.error(ERROR_ALARM_CALLBACK_CONFIGURATION, e1);
		        } catch (ClassNotFoundException e1) {
		            LOG.error(ERROR_ALARM_CALLBACK_TYPE, e1);
		        }
	        }
        }
        //Or Condition for Second Stream
        if( request.getConditionType().equals("OR") && stream2 != null) {
        	if(oldAlert.getConditionType().equals("OR")) {
	        	//Update Condition
	        	AlertCondition alertCondition2 = streamService.getAlertConditions(stream2).get(0);
	            updateCondition(stream2, alertCondition2, alertTitle, request.getConditionType(), request.conditionParameters(), null);
          
	            //Remove Notification
	            removeNotificationFromStream(stream2);
        	}else {
        		//Create Condition
            	CreateConditionRequest ccr2 = CreateConditionRequest.create(alertRuleUtils.getconditionType(request.getConditionType()) , 
            			alertTitle, alertRuleUtils.getConditionParameters(null, "OR", request.conditionParameters()) );
            	final AlertCondition alertCondition2 = alertService.fromRequest(ccr2, stream2, getCurrentUser().getName());
                streamService.addAlertCondition(stream2, alertCondition2);
        	}
        	//Create Notification      
            if(alertRuleUtils.isValidSeverity(request.getSeverity())){
                try {
                	createNotification(alertTitle+"#2", stream2, getParametersNotification(request.getSeverity()));
                } catch (ValidationException | AlarmCallbackConfigurationException | ConfigurationException e) {
                    LOG.error(ERROR_ALARM_CALLBACK_CONFIGURATION, e);
                } catch (ClassNotFoundException e) {
                    LOG.error(ERROR_ALARM_CALLBACK_TYPE, e);
                }
            }
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
					streamID2));
        
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
    ) throws UnsupportedEncodingException, NotFoundException, ValidationException {

        AlertRule sourceAlert = alertRuleService.load(title);
        String alertTitle = request.getTitle();
        final String creatorUser = getCurrentUser().getName();

        // Create stream.
        final Stream sourceFirstStream = streamService.load(sourceAlert.getStreamID());
        Stream firstStream = cloneStream(sourceFirstStream, alertTitle);
     
        Stream secondStream = null;
        String secondStreamID = null;
        //Create Second Stream
        if(sourceAlert.getSecondStreamID() != null && !sourceAlert.getSecondStreamID().isEmpty()) {
            final Stream sourceSecondStream = streamService.load(sourceAlert.getSecondStreamID());
        	secondStream = cloneStream(sourceSecondStream, alertTitle+"#2");
        	secondStreamID = secondStream.getId();
        }
        
        //Create Condition
        String alertConditionID=null;
        AlertCondition alertCondition;
        try {
        	alertCondition = streamService.getAlertCondition(sourceFirstStream, sourceAlert.getConditionID());
        	Map<String, Object> conditionParameters = Maps.newHashMap();
        	conditionParameters.putAll(alertCondition.getParameters());
        	if(secondStream != null && alertCondition.getType().equals(AlertRuleUtils.TYPE_CORRELATION)) {
        		conditionParameters.replace(AlertRuleUtils.ADDITIONAL_STREAM, secondStream.getId());
        	}
            final AlertCondition clonedAlertCondition = alertService.fromRequest(
                    CreateConditionRequest.create(alertCondition.getType(), alertTitle, conditionParameters),
                    firstStream,
                    creatorUser
            );
            streamService.addAlertCondition(firstStream, clonedAlertCondition);
            alertConditionID = clonedAlertCondition.getId();
            
        } catch (ConfigurationException | NotFoundException | ValidationException e) {
        	cleanAlertRule(firstStream, secondStream);
            LOG.error(ERROR_CLONE+ title, e);
			throw new BadRequestException(e.getMessage(), e);
        }

        //Create notification
        String alertNotificationID = null;
        AlarmCallbackConfiguration alarmCallbackConfig = null;
        if(sourceAlert.getNotificationID() != null && !sourceAlert.getNotificationID().isEmpty()) {
	        alarmCallbackConfig = alarmCallbackConfigurationService.load(sourceAlert.getNotificationID());
	        final AlarmCallbackConfiguration updatedAlarmCallbackConfig = ((AlarmCallbackConfigurationImpl) alarmCallbackConfig).toBuilder()
	                .setTitle(alertTitle)
	                .build();
	        final CreateAlarmCallbackRequest requestAlarmCallback = CreateAlarmCallbackRequest.create(updatedAlarmCallbackConfig);
	        final AlarmCallbackConfiguration alarmCallback = alarmCallbackConfigurationService.create(firstStream.getId(), requestAlarmCallback, getCurrentUser().getName());
	        
			try {
				alertNotificationID = alarmCallbackConfigurationService.save(alarmCallback);
			} catch (ValidationException e) {
				LOG.error(ERROR_ALARM_CALLBACK_CONFIGURATION, e);
			}
        }

        for (Output output : sourceFirstStream.getOutputs()) {
            streamService.addOutput(firstStream, output);
        }

        clusterEventBus.post(StreamsChangedEvent.create(firstStream.getId()));
        
        //If OR condition
        if(sourceAlert.getConditionType().equals("OR") && secondStream != null) {
        	//Create Condition
        	try {
				AlertCondition clonedAlertCondition2 = alertService.fromRequest(
				        CreateConditionRequest.create(alertCondition.getType(), alertTitle+"#2", alertCondition.getParameters()),
				        secondStream,
				        creatorUser
				);
				streamService.addAlertCondition(secondStream, clonedAlertCondition2);
			} catch (ConfigurationException | ValidationException e) {
				cleanAlertRule(firstStream, secondStream);
	            LOG.error(ERROR_CLONE+ title, e);
				throw new BadRequestException(e.getMessage(), e);
			}
        	 
        	//Create notification
        	if(alarmCallbackConfig != null) {
	        	final AlarmCallbackConfiguration updatedAlarmCallbackConfig2 = ((AlarmCallbackConfigurationImpl) alarmCallbackConfig).toBuilder()
	                    .setTitle(alertTitle+"#2")
	                    .build();
	            final CreateAlarmCallbackRequest requestAlarmCallback2 = CreateAlarmCallbackRequest.create(updatedAlarmCallbackConfig2);
	            final AlarmCallbackConfiguration alarmCallback2 = alarmCallbackConfigurationService.create(secondStream.getId(), requestAlarmCallback2, getCurrentUser().getName());
	            try {
					alarmCallbackConfigurationService.save(alarmCallback2);
				} catch (ValidationException e) {
					LOG.error(ERROR_ALARM_CALLBACK_CONFIGURATION, e);
				}
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
				secondStreamID));

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
            final Stream stream = streamService.load(alertRule.getStreamID());
            streamService.destroy(stream);
            clusterEventBus.post(StreamsChangedEvent.create(stream.getId()));
            clusterEventBus.post(StreamDeletedEvent.create(stream.getId()));
            
            if(alertRule.getSecondStreamID() != null && !alertRule.getSecondStreamID().isEmpty()) {
            	final Stream stream2 = streamService.load(alertRule.getSecondStreamID());
                streamService.destroy(stream2);
                clusterEventBus.post(StreamsChangedEvent.create(stream2.getId()));
                clusterEventBus.post(StreamDeletedEvent.create(stream2.getId()));
            }
        }catch(NotFoundException e){
            LOG.error("Cannot find the stream for alert " + alertTitle , e);
        }
        
        alertRuleService.destroy(alertTitle);
    }
    

    private int countAlerts(String streamID, DateTime since){
        final List<Alert> alerts = alertService.loadRecentOfStream(streamID, since, 999);
        return alerts.size();
    }
    
    private void createStreamRule(List<FieldRuleImpl> listfieldRule, String streamID) throws ValidationException {
    	 for (FieldRule fieldRule:listfieldRule) {
             final Map<String, Object> streamRuleData = Maps.newHashMapWithExpectedSize(6);

             if(fieldRule.getType() >= 0){
                 streamRuleData.put(StreamRuleImpl.FIELD_TYPE, fieldRule.getType());
                 streamRuleData.put(StreamRuleImpl.FIELD_INVERTED, false);
             }else{
                 streamRuleData.put(StreamRuleImpl.FIELD_TYPE, Math.abs(fieldRule.getType()));
                 streamRuleData.put(StreamRuleImpl.FIELD_INVERTED, true);
             }
             streamRuleData.put(StreamRuleImpl.FIELD_FIELD, fieldRule.getField());
             streamRuleData.put(StreamRuleImpl.FIELD_VALUE, fieldRule.getValue());
             streamRuleData.put(StreamRuleImpl.FIELD_STREAM_ID, new ObjectId(streamID));
             streamRuleData.put(StreamRuleImpl.FIELD_DESCRIPTION, AlertRuleUtils.COMMENT_ALERT_WIZARD);

             final StreamRule newStreamRule = streamRuleService.create(streamRuleData);
             streamRuleService.save(newStreamRule);
         }
    }
    
    private Stream createStream(AlertRuleStream alertRuleStream, String title) throws ValidationException {
    	
    	final CreateStreamRequest cr = CreateStreamRequest.create(title, AlertRuleUtils.COMMENT_ALERT_WIZARD,
        		Collections.emptyList(), "", alertRuleStream.getMatchingType(), false, indexSetID);
        final Stream stream = streamService.create(cr, getCurrentUser().getName());
        stream.setDisabled(false);

        if (!stream.getIndexSet().getConfig().isWritable()) {
            throw new BadRequestException("Assigned index set must be writable!");
        }
        final String streamID = streamService.save(stream);

        // Create stream rules.
        createStreamRule(alertRuleStream.getFieldRules(), streamID);
        
        return stream;        
    }
    
    private void updateStream(Stream stream, AlertRuleStream alertRuleStream, String title) throws ValidationException {
    	
        stream.setTitle(title);
        if (alertRuleStream.getMatchingType() != null) {
            try {
                stream.setMatchingType(Stream.MatchingType.valueOf(alertRuleStream.getMatchingType()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid matching type '" + alertRuleStream.getMatchingType()
                        + "' specified. Should be one of: " + Arrays.toString(Stream.MatchingType.values()));
            }
        }
        streamService.save(stream);

        //TODO do it better (don't destroy if update)
        // Destroy existing stream rules
        for (StreamRule streamRule:stream.getStreamRules()) {
            streamRuleService.destroy(streamRule);
        }
        // Create stream rules.
        createStreamRule(alertRuleStream.getFieldRules(), stream.getId());
        
        clusterEventBus.post(StreamsChangedEvent.create(stream.getId()));
    }
    
    
    
    private String updateCondition(Stream stream, AlertCondition oldAlertCondition, String title, String alertRuleCondType, Map<String, Object> alertRuleCondParameters, String streamID2) throws ValidationException {
        String alertConditionID = oldAlertCondition.getId();
        String conditionType = alertRuleUtils.getconditionType(alertRuleCondType);
        Map<String, Object> parameters = alertRuleUtils.getConditionParameters(streamID2, alertRuleCondType, alertRuleCondParameters);
        try {
        	CreateConditionRequest ccr = CreateConditionRequest.create(conditionType , title, parameters);
        	//If same condition type update
        	if(oldAlertCondition.getType().equals(conditionType)) {
        		final AlertCondition updatedCondition = alertService.updateFromRequest(oldAlertCondition, ccr);
                streamService.updateAlertCondition(stream, updatedCondition);
        	}else {
        		streamService.removeAlertCondition(stream, alertConditionID);
        	    final AlertCondition newAlertCondition = alertService.fromRequest(ccr, stream, getCurrentUser().getName());
        	    streamService.addAlertCondition(stream, newAlertCondition);
        	    alertConditionID = newAlertCondition.getId();
        	}     
        } catch (ConfigurationException e) {
            throw new BadRequestException("Invalid alert condition parameters", e);
        }
        return alertConditionID;
    }
    
    private  Stream cloneStream(Stream sourceStream, String newTitle) throws NotFoundException, ValidationException {
    	final String creatorUser = getCurrentUser().getName();

        // Create stream.
        final Map<String, Object> streamData = Maps.newHashMap();
        streamData.put(StreamImpl.FIELD_TITLE, newTitle);
        streamData.put(StreamImpl.FIELD_DESCRIPTION, AlertRuleUtils.COMMENT_ALERT_WIZARD);
        streamData.put(StreamImpl.FIELD_CREATOR_USER_ID, creatorUser);
        streamData.put(StreamImpl.FIELD_CREATED_AT, Tools.nowUTC());
        streamData.put(StreamImpl.FIELD_MATCHING_TYPE, sourceStream.getMatchingType().toString());
        streamData.put(StreamImpl.FIELD_REMOVE_MATCHES_FROM_DEFAULT_STREAM, sourceStream.getRemoveMatchesFromDefaultStream());
        streamData.put(StreamImpl.FIELD_INDEX_SET_ID, indexSetID);

        final Stream stream = streamService.create(streamData);
        stream.setDisabled(false);

        final String streamID = streamService.save(stream);

        final List<StreamRule> sourceStreamRules = streamRuleService.loadForStream(sourceStream);
        for (StreamRule streamRule : sourceStreamRules) {
            final Map<String, Object> streamRuleData = Maps.newHashMapWithExpectedSize(6);

            streamRuleData.put(StreamRuleImpl.FIELD_TYPE, streamRule.getType().toInteger());
            streamRuleData.put(StreamRuleImpl.FIELD_FIELD, streamRule.getField());
            streamRuleData.put(StreamRuleImpl.FIELD_VALUE, streamRule.getValue());
            streamRuleData.put(StreamRuleImpl.FIELD_INVERTED, streamRule.getInverted());
            streamRuleData.put(StreamRuleImpl.FIELD_STREAM_ID, new ObjectId(streamID));
            streamRuleData.put(StreamRuleImpl.FIELD_DESCRIPTION, streamRule.getDescription());

            final StreamRule newStreamRule = streamRuleService.create(streamRuleData);
            streamRuleService.save(newStreamRule);
        }
		return stream;
    }
    
    private void cleanAlertRule(Stream mainStream, Stream secondStream) {
    	try {
	    	if(mainStream != null) {
	    		streamService.destroy(mainStream);
	    		clusterEventBus.post(StreamsChangedEvent.create(mainStream.getId()));
                clusterEventBus.post(StreamDeletedEvent.create(mainStream.getId()));
	    	}
	        if(secondStream != null) {
	        	streamService.destroy(secondStream);
	        	clusterEventBus.post(StreamsChangedEvent.create(secondStream.getId()));
                clusterEventBus.post(StreamDeletedEvent.create(secondStream.getId()));
	        }
    	} catch (NotFoundException e) {
    		 LOG.error("Cannot find the stream ", e);
		}
    }
    
    private GetDataAlertRule constructDataAlertRule(AlertRule alert) throws NotFoundException {
    	final String streamID = alert.getStreamID();

        final Stream stream = streamService.load(streamID);
        final AlertCondition alertCondition = streamService.getAlertCondition(stream, alert.getConditionID()); 
        Map<String, Object> parametersCondition = Maps.newHashMap();
        parametersCondition.putAll(alertCondition.getParameters());
        if(alert.getConditionType().equals("THEN") || alert.getConditionType().equals("AND")) {
        	parametersCondition.put(AlertRuleUtils.THRESHOLD, parametersCondition.remove(AlertRuleUtils.MAIN_THRESHOLD));
        	parametersCondition.put(AlertRuleUtils.THRESHOLD_TYPE, parametersCondition.remove(AlertRuleUtils.MAIN_THRESHOLD_TYPE));
        }

        AlertRuleStream alertRuleStream = AlertRuleStreamImpl.create(streamID, 
    			stream.getMatchingType().toString(), alertRuleUtils.getListFieldRule(stream.getStreamRules()));
        
        AlertRuleStream alertRuleStream2 = null;
        if(alert.getSecondStreamID() != null && !alert.getSecondStreamID().isEmpty()) {
        	final Stream stream2 = streamService.load(alert.getSecondStreamID());
        	alertRuleStream2 = AlertRuleStreamImpl.create(alert.getSecondStreamID(), 
        			stream2.getMatchingType().toString(), alertRuleUtils.getListFieldRule(stream2.getStreamRules()));
        }
        
        final AlarmCallbackConfiguration callbackConfiguration = alarmCallbackConfigurationService.load(alert.getNotificationID());
        String severity = "";
        if(callbackConfiguration != null) {
        	severity = callbackConfiguration.getConfiguration().getOrDefault(AlertRuleUtils.SEVERITY, "").toString();
        }
        
        return GetDataAlertRule.create(alert.getTitle(), alertCondition.getTitle(),
        		severity,
                alert.getConditionID(),
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
    }
    
    private void removeConditionFromStream(Stream stream) {
    	List <AlertCondition> listAlertCondition = streamService.getAlertConditions(stream);
    	if(listAlertCondition != null && !listAlertCondition.isEmpty()) {
    		for (AlertCondition alertCondition : listAlertCondition) {
    			streamService.removeAlertCondition(stream, alertCondition.getId());
			}
    	}
    }
    private void removeNotificationFromStream(Stream stream) {
    	List <AlarmCallbackConfiguration> listAlarmCallbackConfiguration = alarmCallbackConfigurationService.getForStream(stream);
    	if(listAlarmCallbackConfiguration != null && !listAlarmCallbackConfiguration.isEmpty()) {
    		for (AlarmCallbackConfiguration alarmCallbackConfiguration : listAlarmCallbackConfiguration) {
    			alarmCallbackConfigurationService.destroy(alarmCallbackConfiguration);
			}
		}
    }
    private void removeConditionAndNotificationFromStream(Stream stream) {
    	removeConditionFromStream(stream);
    	removeNotificationFromStream(stream);
    }
    
    private Map<String, Object> getParametersNotification(String severity){
        final LoggingAlertConfig configGeneral = clusterConfigService.getOrDefault(LoggingAlertConfig.class,
				LoggingAlertConfig.createDefault());
        
    	 Map<String, Object> parametersNotification = Maps.newHashMap();
         parametersNotification.put(AlertRuleUtils.SEVERITY, severity);
         parametersNotification.put(AlertRuleUtils.CONTENT, configGeneral.accessLogBody());
         parametersNotification.put(AlertRuleUtils.SPLIT_FIELDS, Collections.emptyList());
         parametersNotification.put(AlertRuleUtils.AGGREGATION_TIME, configGeneral.accessAggregationTime());
         parametersNotification.put(AlertRuleUtils.LIMIT_OVERFLOW, configGeneral.accessLimitOverflow());
         parametersNotification.put(AlertRuleUtils.COMMENT, AlertRuleUtils.COMMENT_ALERT_WIZARD);
         
         return parametersNotification;
    }
    
    private String createNotification(String title, Stream stream, Map<String, Object> parameters) throws ClassNotFoundException, ConfigurationException, AlarmCallbackConfigurationException, ValidationException {
        CreateAlarmCallbackRequest cacr = CreateAlarmCallbackRequest.create(AlertRuleUtils.TYPE_LOGGING_ALERT, title, parameters);
        final AlarmCallbackConfiguration alarmCallbackConfiguration = 
        		alarmCallbackConfigurationService.create(stream.getId(), cacr, getCurrentUser().getName());

        alarmCallbackFactory.create(alarmCallbackConfiguration).checkConfiguration();
        return alarmCallbackConfigurationService.save(alarmCallbackConfiguration);
    }
    
    private void updateNotification(String title, String notificationID, Map<String, Object> parameters) throws NotFoundException, ClassNotFoundException, ConfigurationException, AlarmCallbackConfigurationException, ValidationException {
    	final AlarmCallbackConfiguration callbackConfiguration = alarmCallbackConfigurationService.load(notificationID);
        if (callbackConfiguration != null) {
	        final AlarmCallbackConfiguration updatedConfig = ((AlarmCallbackConfigurationImpl) callbackConfiguration).toBuilder()
	                .setTitle(title)
	                .setConfiguration(parameters)
	                .build();
	        
	        alarmCallbackFactory.create(updatedConfig).checkConfiguration();
	        alarmCallbackConfigurationService.save(updatedConfig);
        }
    }
    
    @POST
    @Path("/export")
    @Timed
    @ApiOperation(value = "Export alert rules")
    @RequiresAuthentication
    @RequiresPermissions(AlertRuleRestPermissions.WIZARD_ALERTS_RULES_READ)
    @AuditEvent(type = AlertWizardAuditEventTypes.WIZARD_ALERTS_RULES_READ)
    public List<AlertRuleRequest> getExportAlertRule(@ApiParam(name = "JSON body", required = true) @Valid @NotNull ExportAlertRuleRequest request)
            throws UnsupportedEncodingException, NotFoundException, ValidationException {
    	LOG.info("List titles : " + request.getTitles());
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
    public Response importAlertRules (@ApiParam(name = "JSON body", required = true) @Valid @NotNull List<AlertRuleRequest> request) {
    	Response responses = Response.accepted().build();
    			
    	for (AlertRuleRequest alertRuleRequest : request) {
            if(!alertRuleService.isValidRequest(alertRuleRequest)){
                LOG.error("Invalid alert rule:" + alertRuleRequest.getTitle() );
            }else {
				try {
					create(alertRuleRequest);
				} catch (Exception e) {
					LOG.error("Cannot create alert "+ alertRuleRequest.getTitle() + ": ", e.getMessage());
					responses = Response.serverError().build();
				}
            }
		}
    	
		return responses;
    }
}
