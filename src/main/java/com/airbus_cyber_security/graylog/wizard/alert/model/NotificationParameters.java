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

package com.airbus_cyber_security.graylog.wizard.alert.model;

import org.bson.Document;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import jakarta.annotation.Nullable;

@AutoValue
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class NotificationParameters {

	public static final String FIELD_TYPE = "type";
	public static final String FIELD_LOG_BODY = "log_body";
	public static final String FIELD_ALERT_TAG = "alert_tag";
	public static final String FIELD_SINGLE_NOTIFICATION = "single_notification";

    //     "type": "logging-alert-notification",
    //     "log_body": "type: alert\nid: ${logging_alert.id}\naggregation_id: ${event.fields.aggregation_id}\nseverity: ${logging_alert.severity}\napp: graylog\nsubject: ${event_definition_title}\nbody: ${event_definition_description}\n${if backlog && backlog[0]} src: ${backlog[0].fields.src_ip}\nsrc_category: ${backlog[0].fields.src_category}\ndest: ${backlog[0].fields.dest_ip}\ndest_category: ${backlog[0].fields.dest_category}\n${end}",
    //     "alert_tag": "LoggingAlert",
    //     "single_notification": false
    
	@JsonProperty(FIELD_TYPE)
	@Nullable
	public abstract String getType();

	@JsonProperty(FIELD_LOG_BODY)
	@Nullable
	public abstract String getLogBody();

	@JsonProperty(FIELD_ALERT_TAG)
	@Nullable
	public abstract String getAlertTag();

	@JsonProperty(FIELD_SINGLE_NOTIFICATION)
	public abstract boolean isSingleNotification();

    @JsonCreator
    public static NotificationParameters create(@JsonProperty(FIELD_TYPE) String type,
                                   @JsonProperty(FIELD_LOG_BODY) String logBody,
                                   @JsonProperty(FIELD_ALERT_TAG) String alertTag,
                                   @JsonProperty(FIELD_SINGLE_NOTIFICATION) boolean singleNotification){
        return new AutoValue_NotificationParameters(type, logBody, alertTag, singleNotification);
    }

    public static NotificationParameters fromDocument(Document document) {
        return create(
                document.getString(FIELD_TYPE),
                document.getString(FIELD_LOG_BODY),
                document.getString(FIELD_ALERT_TAG),
                document.getBoolean(FIELD_SINGLE_NOTIFICATION, false)
        );
    }

}
