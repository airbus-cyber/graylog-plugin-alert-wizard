/*
 * graylog-plugin-alert-wizard Source Code
 * Copyright (C) 2018-2020 - Airbus CyberSecurity (SAS) - All rights reserved
 *
 * This file is part of the graylog-plugin-alert-wizard GPL Source Code.
 *
 * graylog-plugin-alert-wizard Source Code is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this code.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.airbus_cyber_security.graylog.alert.bundles;

import com.airbus_cyber_security.graylog.alert.AlertRule;
import com.airbus_cyber_security.graylog.alert.AlertRuleService;
import com.airbus_cyber_security.graylog.alert.AlertRuleStreamImpl;
import com.airbus_cyber_security.graylog.alert.FieldRuleImpl;
import com.airbus_cyber_security.graylog.alert.utilities.AlertRuleUtils;
import com.airbus_cyber_security.graylog.events.notifications.types.LoggingNotificationConfig;
import com.google.common.collect.Lists;
import org.graylog.events.processor.EventProcessorConfig;
import org.graylog.events.rest.EventDefinitionsResource;
import org.graylog.events.rest.EventNotificationsResource;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.streams.StreamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AlertRuleExporter {
	
	private static final Logger LOG = LoggerFactory.getLogger(AlertRuleExporter.class);
	
	private final AlertRuleService alertRuleService;
    private final StreamService streamService;
    private final AlertRuleUtils alertRuleUtils;
    private final EventDefinitionsResource eventDefinitionsResource;
    private final EventNotificationsResource eventNotificationsResource;
	
	public AlertRuleExporter(AlertRuleService alertRuleService,
							 StreamService streamService,
							 AlertRuleUtils alertRuleUtils,
							 EventDefinitionsResource eventDefinitionsResource,
							 EventNotificationsResource eventNotificationsResource){
		this.alertRuleService = alertRuleService;
		this.streamService = streamService;
		this.alertRuleUtils = alertRuleUtils;
		this.eventDefinitionsResource = eventDefinitionsResource;
		this.eventNotificationsResource = eventNotificationsResource;
	}
	
	public List<ExportAlertRule> export(List<String> titles){
		List<ExportAlertRule> listAlertRules = Lists.newArrayListWithCapacity(titles.size()) ;
		
		for (String title : titles) {
			try {
				final AlertRule alert = alertRuleService.load(title);
				
				final String streamID = alert.getStreamID();
		        final Stream stream = streamService.load(streamID);
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

		        //Get the condition parameters
				EventProcessorConfig eventConfig = eventDefinitionsResource.get(alert.getEventID()).config();
				Map<String, Object> parametersCondition = alertRuleUtils.getConditionParameters(eventConfig);

				//Get the notification parameters
				LoggingNotificationConfig loggingNotificationConfig = (LoggingNotificationConfig) eventNotificationsResource.get(alert.getNotificationID()).config();
				Map<String, Object> parametersNotification = alertRuleUtils.getNotificationParameters(loggingNotificationConfig);

				listAlertRules.add(ExportAlertRule.create(
						title,
						parametersNotification,
						alert.getDescription(), 
						alert.getConditionType(),
						parametersCondition,
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
