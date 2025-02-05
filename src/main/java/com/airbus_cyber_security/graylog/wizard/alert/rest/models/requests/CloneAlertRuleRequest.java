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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

@AutoValue
@JsonAutoDetect
public abstract class CloneAlertRuleRequest {

    @JsonProperty("source_title")
    @NotNull
    public abstract String getSourceTitle();

    @JsonProperty("title")
    @NotNull
    public abstract String getTitle();

    @JsonProperty("description")
    @Nullable
    public abstract String getDescription();

    @JsonProperty("clone_notification")
    @NotNull
    public abstract Boolean getCloneNotification();

    @JsonCreator
    public static CloneAlertRuleRequest create(@JsonProperty("source_title") String sourceTitle,
                                               @JsonProperty("title") String title,
                                               @JsonProperty("description") String description,
                                               @JsonProperty("clone_notification") Boolean cloneNotification) {
        return new AutoValue_CloneAlertRuleRequest(sourceTitle, title, description, cloneNotification);
    }
}
