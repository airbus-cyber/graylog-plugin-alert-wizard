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
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;

@AutoValue
@JsonAutoDetect
public abstract class TriggeringConditions {

    @JsonProperty("stream")
    @NotNull
    public abstract String streamIdentifier();

    @JsonProperty("pipeline")
    @Nullable
    public abstract String pipelineIdentifier();

    // TODO is this really necessary? Can't we get it from the pipeline?
    @JsonProperty("pipeline_rule")
    @Nullable
    public abstract String pipelineRuleIdentifier();

    @JsonProperty("pipeline_field_rules")
    @Nullable
    public abstract List<FieldRule> pipelineFieldRules();

    @JsonCreator
    public static TriggeringConditions create(@JsonProperty("_id") String objectId,
                                              @JsonProperty("stream") String streamIdentifier,
                                              @JsonProperty("pipeline") String pipelineIdentifier,
                                              @JsonProperty("pipeline_rule") String pipelineRuleIdentifier,
                                              @JsonProperty("pipeline_field_rules") List<FieldRule> pipelineFieldRules) {
        return create(streamIdentifier, pipelineIdentifier, pipelineRuleIdentifier, pipelineFieldRules);
    }

    public static TriggeringConditions create(String streamIdentifier, String pipelineIdentifier, String pipelineRuleIdentifier, List<FieldRule> pipelineFieldRules) {
        return new AutoValue_TriggeringConditions(streamIdentifier, pipelineIdentifier, pipelineRuleIdentifier, pipelineFieldRules);
    }
}
