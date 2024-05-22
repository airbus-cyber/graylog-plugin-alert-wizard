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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;

import javax.validation.constraints.NotNull;

/**
 * To encode most simple rules: a single path (condition -> aggregation event) which trigger the same notification.
 */
@AutoValue
@JsonAutoDetect
@JsonDeserialize(builder = AggregationAlertPattern.Builder.class)
public abstract class AggregationAlertPattern implements AlertPattern {
    public static final String FIELD_CONDITIONS = "conditions";

    @JsonProperty(FIELD_CONDITIONS)
    @NotNull
    public abstract TriggeringConditions conditions();

    public static Builder builder() {
        return Builder.create();
    }

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public static abstract class Builder {
        @JsonCreator
        public static Builder create() {
            return new AutoValue_AggregationAlertPattern.Builder();
        }

        @JsonProperty(FIELD_CONDITIONS)
        public abstract Builder conditions(TriggeringConditions conditions);

        public abstract AggregationAlertPattern build();
    }
}
