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

import com.airbus_cyber_security.graylog.wizard.alert.rest.models.FieldRule;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;

@AutoValue
@JsonAutoDetect
@JsonDeserialize(builder = ListOrStreamConditions.Builder.class)
public abstract class ListOrStreamConditions implements TriggeringConditions {
    private static final String FIELD_STREAM = "stream";
    private static final String FIELD_PIPELINE = "pipeline";
    private static final String FIELD_PIPELINE_RULE_IDENTIFIER = "pipeline_rule";
    private static final String FIELD_PIPELINE_FIELD_RULES = "pipeline_field_rules";


    @JsonProperty(FIELD_STREAM)
    @NotNull
    public abstract String streamIdentifier();

    @JsonProperty(FIELD_PIPELINE)
    @Nullable
    public abstract String pipelineIdentifier();

    // TODO is this really necessary? Can't we get it from the pipeline?
    @JsonProperty(FIELD_PIPELINE_RULE_IDENTIFIER)
    @Nullable
    public abstract String pipelineRuleIdentifier();

    @JsonProperty(FIELD_PIPELINE_FIELD_RULES)
    @Nullable
    public abstract List<FieldRule> pipelineFieldRules();

    public static Builder builder() {
        return Builder.create();
    }

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public static abstract class Builder {
        @JsonCreator
        public static Builder create() {
            return new AutoValue_ListOrStreamConditions.Builder();
        }

        // TODO rename into conditions1
        @JsonProperty(FIELD_STREAM)
        public abstract Builder streamIdentifier(String streamIdentifier);

        @JsonProperty(FIELD_PIPELINE)
        public abstract Builder pipelineIdentifier(String pipelineIdentifier);

        @JsonProperty(FIELD_PIPELINE_RULE_IDENTIFIER)
        public abstract Builder pipelineRuleIdentifier(String pipelineRuleIdentifier);

        @JsonProperty(FIELD_PIPELINE_FIELD_RULES)
        public abstract Builder pipelineFieldRules(List<FieldRule> pipelineFieldRules);

        public abstract ListOrStreamConditions build();
    }
}
