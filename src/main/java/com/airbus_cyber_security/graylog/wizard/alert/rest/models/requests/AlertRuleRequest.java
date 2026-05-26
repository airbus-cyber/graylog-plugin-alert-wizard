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
package com.airbus_cyber_security.graylog.wizard.alert.rest.models.requests;

import java.util.Map;

import com.airbus_cyber_security.graylog.wizard.alert.model.AlertFields;
import com.airbus_cyber_security.graylog.wizard.alert.model.AlertType;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.AlertRuleStream;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

@AutoValue
@JsonAutoDetect
public abstract class AlertRuleRequest {

    @JsonProperty(AlertFields.FIELD_TITLE)
    @NotNull
    public abstract String getTitle();

    @JsonProperty(AlertFields.FIELD_PRIORITY)
    @NotNull
    public abstract Integer getPriority();

    @JsonProperty(AlertFields.FIELD_DESCRIPTION)
    @NotNull
    public abstract String getDescription();

    @JsonProperty(value = "disabled", defaultValue = "false")
    public abstract boolean isDisabled();

    // TODO should be an enum. Possible values: COUNT, GROUP_DISTINCT, STATISTICAL, AND, THEN, OR
    @JsonProperty(AlertFields.FIELD_CONDITION_TYPE)
    @NotNull
    public abstract AlertType getConditionType();

    // TODO this should be typed (its an union of 3 possibilities)...
    //      maybe see how it is done with graylog notifications org.graylog.events.notifications.EventNotificationConfig
    @JsonProperty(AlertFields.FIELD_CONDITION_PARAMETERS)
    public abstract Map<String, Object> conditionParameters();

    @JsonProperty(AlertFields.FIELD_STREAM)
    public abstract AlertRuleStream getStream();

    @JsonProperty(AlertFields.FIELD_SECOND_STREAM)
    @Nullable
    public abstract AlertRuleStream getSecondStream();

    @JsonProperty(AlertFields.FIELD_AGGREGATION_TIME)
    @Nullable
    public abstract Integer getAggregationTime();

    @JsonCreator
    public static AlertRuleRequest create(
            @JsonProperty(AlertFields.FIELD_TITLE) String title,
            @JsonProperty(AlertFields.FIELD_PRIORITY) Integer priority,
            @JsonProperty(AlertFields.FIELD_DESCRIPTION) String description,
            @JsonProperty(AlertFields.FIELD_DISABLED) boolean disabled,
            @JsonProperty(AlertFields.FIELD_CONDITION_TYPE) AlertType alertType,
            @JsonProperty(AlertFields.FIELD_CONDITION_PARAMETERS) Map<String, Object> conditionParameters,
            @JsonProperty(AlertFields.FIELD_STREAM) AlertRuleStream stream,
            @JsonProperty(AlertFields.FIELD_SECOND_STREAM) AlertRuleStream stream2,
            @JsonProperty(AlertFields.FIELD_AGGREGATION_TIME) Integer aggregationTime) {
        return new AutoValue_AlertRuleRequest(title, priority, description, disabled, alertType, conditionParameters, stream, stream2, aggregationTime);
    }
}
