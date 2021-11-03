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

import com.airbus_cyber_security.graylog.wizard.alert.AlertRuleStreamImpl;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Map;

@AutoValue
@JsonAutoDetect
public abstract class AlertRuleRequest {

    @JsonProperty("title")
    @NotNull
    public abstract String getTitle();

    @JsonProperty("severity")
    @Nullable
    public abstract String getSeverity();

    @JsonProperty("description")
    @Nullable
    public abstract String getDescription();

    @JsonProperty("condition_type")
    @Nullable
    public abstract String getConditionType();
    
    @JsonProperty("condition_parameters")
    public abstract Map<String, Object> conditionParameters();
    
    @JsonProperty("stream")
    public abstract AlertRuleStreamImpl getStream();
    
    @JsonProperty("second_stream")
    @Nullable
    public abstract AlertRuleStreamImpl getSecondStream();

    @JsonCreator    
    public static AlertRuleRequest create(@JsonProperty("title") String title,
                                             @JsonProperty("severity") String severity,
                                             @JsonProperty("description") String description,
                                             @JsonProperty("condition_type") String conditionType,
                                             @JsonProperty("condition_parameters") Map<String, Object> conditionParameters,
                                             @JsonProperty("stream") AlertRuleStreamImpl stream,
                                             @JsonProperty("second_stream") AlertRuleStreamImpl stream2) {
        return new AutoValue_AlertRuleRequest(title, severity, description, conditionType, conditionParameters, stream, stream2);
    }
}