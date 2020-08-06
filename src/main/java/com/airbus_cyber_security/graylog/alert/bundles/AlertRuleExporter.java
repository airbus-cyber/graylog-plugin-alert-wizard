package com.airbus_cyber_security.graylog.alert.bundles;

import com.airbus_cyber_security.graylog.alert.AlertRule;
import com.airbus_cyber_security.graylog.alert.AlertRuleService;
import com.airbus_cyber_security.graylog.alert.AlertRuleStreamImpl;
import com.airbus_cyber_security.graylog.alert.FieldRuleImpl;
import com.airbus_cyber_security.graylog.alert.utilities.AlertRuleUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.graylog2.alarmcallbacks.AlarmCallbackConfiguration;
import org.graylog2.alarmcallbacks.AlarmCallbackConfigurationService;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.streams.StreamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AlertRuleExporter {
	
	private static final Logger LOG = LoggerFactory.getLogger(AlertRuleExporter.class);
	
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
	
	public List<ExportAlertRule> export(List<String> titles){
		List<ExportAlertRule> listAlertRules = Lists.newArrayListWithCapacity(titles.size()) ;
		
		for (String title : titles) {
			try {
				final AlertRule alert = alertRuleService.load(title);
				
				final String streamID = alert.getStreamID();
		        final Stream stream = streamService.load(streamID);
		        final AlertCondition alertCondition = streamService.getAlertCondition(stream, alert.getEventID());
				List<FieldRuleImpl> fieldRules = new ArrayList<>();
				Optional.ofNullable(alert.getPipelineFieldRules()).ifPresent(fieldRules::addAll);
				Optional.ofNullable(alertRuleUtils.getListFieldRule(stream.getStreamRules())).ifPresent(fieldRules::addAll);
				AlertRuleStreamImpl alertRuleStream = AlertRuleStreamImpl.create(streamID, stream.getMatchingType().toString(), fieldRules);

				AlertRuleStreamImpl alertRuleStream2;
		        if(alert.getSecondStreamID() != null && !alert.getSecondStreamID().isEmpty()) {
		        	final Stream stream2 = streamService.load(alert.getSecondStreamID());
					List<FieldRuleImpl> fieldRules2 = new ArrayList<>();
					Optional.ofNullable(alert.getSecondPipelineFieldRules()).ifPresent(fieldRules2::addAll);
					Optional.ofNullable(alertRuleUtils.getListFieldRule(stream2.getStreamRules())).ifPresent(fieldRules2::addAll);
		        	alertRuleStream2 = AlertRuleStreamImpl.create(alert.getSecondStreamID(), stream2.getMatchingType().toString(), fieldRules2);
		        }else {
		        	alertRuleStream2 = AlertRuleStreamImpl.create("", "", Collections.emptyList());
		        }			
		        
		        final AlarmCallbackConfiguration callbackConfiguration = alarmCallbackConfigurationService.load(alert.getNotificationID());
				Map<String, Object> parametersNotification = Maps.newHashMap();
		        if(callbackConfiguration != null) {
					parametersNotification.putAll(callbackConfiguration.getConfiguration());
		        }
		        
				listAlertRules.add(ExportAlertRule.create(
						title,
						parametersNotification,
						alert.getDescription(), 
						alert.getConditionType(),
						alertCondition.getParameters(),
						alertRuleStream,
						alertRuleStream2));
				
			}catch(Exception e) {
				/* Can't find stream, condition or notification */
				LOG.warn("Can't export alert rule "+ title + ": "+e.getMessage());
			}
		}
		
		return listAlertRules;
	}
}
