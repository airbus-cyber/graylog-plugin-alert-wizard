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

import org.bson.Document;
import org.graylog2.plugin.streams.Stream;

import com.airbus_cyber_security.graylog.wizard.alert.model.CloneAlertRuleFields;
import com.airbus_cyber_security.graylog.wizard.alert.model.TriggeringConditions;
import com.airbus_cyber_security.graylog.wizard.config.rest.ImportPolicyType;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

@AutoValue
@JsonAutoDetect
public abstract class CloneAlertRuleRequest {

    @JsonProperty(CloneAlertRuleFields.FIELD_SOURCE_TITLE)
    @NotNull
    public abstract String getSourceTitle();

    @JsonProperty(CloneAlertRuleFields.FIELD_TITLE)
    @NotNull
    public abstract String getTitle();

    @JsonProperty(CloneAlertRuleFields.FIELD_DESCRIPTION)
    @Nullable
    public abstract String getDescription();

    @JsonProperty(CloneAlertRuleFields.FIELD_CLONE_NOTIFICATION)
    @NotNull
    public abstract Boolean getCloneNotification();

    @JsonProperty(CloneAlertRuleFields.FIELD_CONDITION_TYPE)
    @Nullable
    public abstract String getConditionType();

    @JsonProperty(CloneAlertRuleFields.FIELD_POLICY)
    @Nullable
    public abstract ImportPolicyType getPolicy();

    @JsonCreator
    public static CloneAlertRuleRequest create(
            @JsonProperty(CloneAlertRuleFields.FIELD_SOURCE_TITLE) String sourceTitle,
            @JsonProperty(CloneAlertRuleFields.FIELD_TITLE) String title,
            @JsonProperty(CloneAlertRuleFields.FIELD_DESCRIPTION) String description,
            @JsonProperty(CloneAlertRuleFields.FIELD_CLONE_NOTIFICATION) Boolean cloneNotification,
            @JsonProperty(CloneAlertRuleFields.FIELD_CONDITION_TYPE) String conditionType,
            @JsonProperty(CloneAlertRuleFields.FIELD_POLICY) ImportPolicyType policy) {
        return new AutoValue_CloneAlertRuleRequest(sourceTitle, title, description, cloneNotification, conditionType, policy);
    }
}
