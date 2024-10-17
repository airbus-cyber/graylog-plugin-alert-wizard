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

package com.airbus_cyber_security.graylog.wizard.alert.rest.models;

import com.airbus_cyber_security.graylog.wizard.alert.model.FieldRule;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog2.plugin.streams.Stream;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.List;

// TODO could this class be removed by factoring with some class in model? (maybe even at the expense of changing the REST API)
@AutoValue
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AlertRuleStream {

    // TODO rename into getIdentifier
    // TODO is this fied really needed?
    @JsonProperty("id")
    @Nullable
    public abstract String getID();

    // the way field rules are to be combined: either "AND" or "OR"
    @JsonProperty("matching_type")
    @NotNull
    public abstract Stream.MatchingType getMatchingType();
    
    @JsonProperty("field_rule")
    @Nullable
    // TODO should be NotNull (empty list)!!!
    public abstract List<FieldRule> getFieldRules();

    @JsonCreator
    public static AlertRuleStream create(@JsonProperty("id") String id,
                                         @JsonProperty("matching_type") Stream.MatchingType matchingType,
                                         @JsonProperty("field_rule") List<FieldRule> fieldRules){
        return new AutoValue_AlertRuleStream(id, matchingType, fieldRules);
    }

}
