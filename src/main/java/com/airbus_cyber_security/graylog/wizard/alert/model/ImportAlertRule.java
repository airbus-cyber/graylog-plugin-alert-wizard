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

import java.util.Map;

import org.mongojack.Id;
import org.mongojack.ObjectId;

import com.airbus_cyber_security.graylog.wizard.alert.rest.models.AlertRuleStream;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

@AutoValue
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ImportAlertRule {

    public static final String FIELD_ID = "id";
    public static final String FIELD_NOTIFICATION = "notification";

    public static final String FIELD_TITLE = "title";
    public static final String FIELD_PRIORITY = "priority";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_CONDITION_TYPE = "condition_type";
    public static final String FIELD_CONDITION_PARAMETERS = "condition_parameters";
    public static final String FIELD_STREAM = "stream";
    public static final String FIELD_SECOND_STREAM = "second_stream";
    public static final String FIELD_SECOND_EVENT_DEFINITION = "second_event_definition";
    public static final String FIELD_DISABLED = "disabled";
    public static final String FIELD_AGGREGATION_TIME = "aggregation_time";
    public static final String FIELD_NOTIFICATION_PARAMETERS = "notification_parameters";

    //   "id": "69e87c4c9356332e74f327b7",
    //   "last_modified": "2026-04-22T07:44:12.967Z",
    //   "creator_user_id": "admin",
    //   "created_at": "2026-04-22T07:44:12.967Z",
    //   "notification": "69e87c4c9356332e74f327aa",
    //   "title": "teststatdup31",
    //   "description": "",
    //   "priority": 1,
    //   "disabled": false,
    //   "condition_type": "STATISTICAL",
    //   "condition_parameters": {
    //     "distinct_by": "action",
    //     "field": "action",
    //     "grace": 1,
    //     "threshold": 0,
    //     "threshold_type": ">",
    //     "grouping_fields": [],
    //     "time": 1,
    //     "search_query": "*",
    //     "type": "AVG"
    //   },
    //   "stream": {
    //     "matching_type": "AND",
    //     "field_rule": [
    //       {
    //         "field": "alert",
    //         "type": 1,
    //         "value": "fdgdfgfdg",
    //         "id": "69e87c4c9356332e74f327ae"
    //       }
    //     ],
    //     "id": "69e87c4c9356332e74f327ac"
    //   },
    //   "second_stream": null,
    //   "aggregation_time": 0,
    //   "second_event_definition": null,
    //   "condition": "69e87c4c9356332e74f327b4",
    //   "notification_parameters": {
    //     "type": "logging-alert-notification",
    //     "log_body": "type: alert\nid: ${logging_alert.id}\naggregation_id: ${event.fields.aggregation_id}\nseverity: ${logging_alert.severity}\napp: graylog\nsubject: ${event_definition_title}\nbody: ${event_definition_description}\n${if backlog && backlog[0]} src: ${backlog[0].fields.src_ip}\nsrc_category: ${backlog[0].fields.src_category}\ndest: ${backlog[0].fields.dest_ip}\ndest_category: ${backlog[0].fields.dest_category}\n${end}",
    //     "alert_tag": "LoggingAlert",
    //     "single_notification": false
    //   }
    @Id
    @ObjectId
    @Nullable
    @JsonProperty(FIELD_ID)
    public abstract String id();

    // TODO rename into getNotificationIdentifier
    @JsonProperty(FIELD_NOTIFICATION)
    @Nullable
    public abstract String getNotificationID();

    @JsonProperty(FIELD_TITLE)
    @NotNull
    public abstract String getTitle();

    @JsonProperty(FIELD_PRIORITY)
    @Nullable
    public abstract Integer getPriority();

    @JsonProperty(FIELD_DESCRIPTION)
    @Nullable
    public abstract String getDescription();

    @JsonProperty(FIELD_CONDITION_TYPE)
    @Nullable
    public abstract AlertType getConditionType();

    @JsonProperty(FIELD_CONDITION_PARAMETERS)
    @Nullable
    public abstract Map<String, Object> getConditionParameters();

    @JsonProperty(FIELD_STREAM)
    @Nullable
    public abstract AlertRuleStream getStream();

    @JsonProperty(FIELD_SECOND_STREAM)
    @Nullable
    public abstract AlertRuleStream getSecondStream();

    @JsonProperty(FIELD_SECOND_EVENT_DEFINITION)
    @Nullable
    public abstract String secondEventDefinitionIdentifier();

    @JsonProperty(FIELD_DISABLED)
    public abstract boolean isDisabled();

    @JsonProperty(FIELD_AGGREGATION_TIME)
    @Nullable
    public abstract Integer getAggregationTime();

    @JsonProperty(FIELD_NOTIFICATION_PARAMETERS)
    public abstract NotificationParameters getNotificationParameters();

    @JsonCreator
    public static ImportAlertRule create(
            @JsonProperty(FIELD_ID) String id,
            @JsonProperty(FIELD_NOTIFICATION) String notification,
            @JsonProperty(FIELD_TITLE) String title,
            @JsonProperty(FIELD_PRIORITY) Integer priority,
            @JsonProperty(FIELD_DESCRIPTION) String description,
            @JsonProperty(FIELD_CONDITION_TYPE) AlertType conditionType,
            @JsonProperty(FIELD_CONDITION_PARAMETERS) Map<String, Object> conditionParameters,
            @JsonProperty(FIELD_STREAM) AlertRuleStream stream,
            @JsonProperty(FIELD_SECOND_STREAM) AlertRuleStream secondStream,
            @JsonProperty(FIELD_SECOND_EVENT_DEFINITION) String secondEventDefinition,
            @JsonProperty(FIELD_DISABLED) boolean disabled,
            @JsonProperty(FIELD_AGGREGATION_TIME) Integer aggregationTime,
            @JsonProperty(FIELD_NOTIFICATION_PARAMETERS) NotificationParameters notificationParameters
    ) {
        return new AutoValue_ImportAlertRule(
                id,
                notification,
                title,
                priority,
                description,
                conditionType,
                conditionParameters,
                stream,
                secondStream,
                secondEventDefinition,
                disabled,
                aggregationTime,
                notificationParameters
			);
    }

}
