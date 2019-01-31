package com.airbus_cyber_security.graylog.alert.bundles;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.graylog2.alarmcallbacks.AlarmCallbackConfiguration;
import org.graylog2.alarmcallbacks.AlarmCallbackConfigurationService;
import org.graylog2.database.NotFoundException;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.streams.StreamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.airbus_cyber_security.graylog.alert.AlertRule;
import com.airbus_cyber_security.graylog.alert.AlertRuleService;
import com.airbus_cyber_security.graylog.alert.AlertRuleStream;
import com.airbus_cyber_security.graylog.alert.AlertRuleStreamImpl;
import com.airbus_cyber_security.graylog.alert.rest.AlertRuleResource;
import com.airbus_cyber_security.graylog.alert.rest.models.requests.AlertRuleRequest;
import com.airbus_cyber_security.graylog.alert.utilities.AlertRuleUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class AlertRuleExporter {
	
	private static final Logger LOG = LoggerFactory.getLogger(AlertRuleResource.class);
	
	private final AlertRuleService alertRuleService;
	private final AlarmCallbackConfigurationService alarmCallbackConfigurationService;
    private final StreamService streamService;
    private final AlertRuleUtils alertRuleUtils;
	
	public AlertRuleExporter(AlertRuleService alertRuleService, AlarmCallbackConfigurationService alarmCallbackConfigurationService, 
			StreamService streamService, AlertRuleUtils alertRuleUtils){
		this.alertRuleService = alertRuleService;
		this.alarmCallbackConfigurationService = alarmCallbackConfigurationService;
		this.streamService = streamService;
		this.alertRuleUtils = alertRuleUtils;
	}
	
	public List<AlertRuleRequest> export(List<String> titles) throws NotFoundException{
		List<AlertRuleRequest> listAlertRules = Lists.newArrayListWithCapacity(titles.size()) ;
		
		for (String title : titles) {
			try {
				final AlertRule alert = alertRuleService.load(title);
				
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
		        }else {
		        	alertRuleStream2 = AlertRuleStreamImpl.create("", "", Collections.emptyList());
		        }			
		        
		        final AlarmCallbackConfiguration callbackConfiguration = alarmCallbackConfigurationService.load(alert.getNotificationID());
		        String severity = "";
		        if(callbackConfiguration != null) {
		        	severity = callbackConfiguration.getConfiguration().getOrDefault(AlertRuleUtils.SEVERITY, "").toString();
		        }
		        
				listAlertRules.add(AlertRuleRequest.create(
						title, 
						severity, 
						alert.getDescription(), 
						alert.getConditionType(), 
						parametersCondition, 
						(AlertRuleStreamImpl) alertRuleStream, 
						(AlertRuleStreamImpl) alertRuleStream2));
				
			}catch(Exception e) {
				/* Can't find stream, condition or notification */
				LOG.warn("Can't export alert rule "+ title + ": "+e.getMessage());
			}
		}
		
		return listAlertRules;
	}
}
