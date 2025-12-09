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

package com.airbus_cyber_security.graylog.wizard.config.rest;

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
public abstract class DefaultValues {

    @JsonProperty("title")
    @Nullable
    public abstract String getTitle();
	
    @JsonProperty("priority")
    @Nullable
    public abstract Integer getPriority();

    @JsonProperty("aggregation_time")
    @Nullable
    public abstract Integer getAggregationTime();
	
    @JsonProperty("matching_type")
    @NotNull
    public abstract String getMatchingType();
    
    @JsonProperty("threshold_type")
    @NotNull
    public abstract String getThresholdType();

    @JsonProperty("threshold")
    public abstract int getThreshold();

    @JsonProperty("time")
    public abstract int getTime();
    
    @JsonProperty("time_type")
    public abstract int getTimeType();

    @JsonProperty("grace")
    public abstract int getGrace();
    
    @JsonProperty("backlog")
    public abstract int getBacklog();

    @JsonCreator
    public static DefaultValues create(@JsonProperty("title") String title,
    		@JsonProperty("priority") Integer priority,
            @JsonProperty("aggregation_time") Integer aggregationTime,
    		@JsonProperty("matching_type") String matchingType,
    		@JsonProperty("threshold_type") String thresholdType,
    		@JsonProperty("threshold") int threshold,
    		@JsonProperty("time") int time,
    		@JsonProperty("time_type") int timeType,
            @JsonProperty("grace") int grace,
            @JsonProperty("backlog") int backlog){
        return new AutoValue_DefaultValues(title, priority, aggregationTime, matchingType, thresholdType, threshold, time, timeType,
                grace, backlog);
    }
}
