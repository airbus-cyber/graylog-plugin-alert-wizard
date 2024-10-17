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

import jakarta.validation.constraints.NotNull;
import java.util.List;

@AutoValue
@JsonAutoDetect
@JsonDeserialize(builder = Pipeline.Builder.class)
public abstract class Pipeline {
    private static final String FIELD_IDENTIFIER = "identifier";
    private static final String FIELD_RULE_IDENTIFIER = "rule";
    private static final String FIELD_FIELD_RULES = "field_rules";

    @JsonProperty(FIELD_IDENTIFIER)
    @NotNull
    public abstract String identifier();

    // TODO is this really necessary? Can't we get it from the pipeline?
    @JsonProperty(FIELD_RULE_IDENTIFIER)
    @NotNull
    public abstract String ruleIdentifier();

    @JsonProperty(FIELD_FIELD_RULES)
    @NotNull
    public abstract List<FieldRule> fieldRules();

    public static Builder builder() {
        return Builder.create();
    }

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public static abstract class Builder {
        @JsonCreator
        public static Builder create() {
            return new AutoValue_Pipeline.Builder();
        }

        @JsonProperty(FIELD_IDENTIFIER)
        public abstract Builder identifier(String identifier);

        @JsonProperty(FIELD_RULE_IDENTIFIER)
        public abstract Builder ruleIdentifier(String ruleIdentifier);

        @JsonProperty(FIELD_FIELD_RULES)
        public abstract Builder fieldRules(List<FieldRule> fieldRules);

        public abstract Pipeline build();
    }
}
