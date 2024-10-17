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

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

/**
 * There are 3 possible constructions:
 * - if there are no list conditions: filteringStream, filteringStream == outputStream, pipeline == null
 * - if the conditions matching type is OR (at least one), or if there are no stream conditions:
 *   pipeline -> filteringStream, filteringStream == outputStream
 * - if the conditions matching type is AND (all): filteringStream -> pipeline -> outputStream
 */
@AutoValue
@JsonAutoDetect
@JsonDeserialize(builder = TriggeringConditions.Builder.class)
public abstract class TriggeringConditions {
    private static final String FIELD_FILTERING_STREAM = "filtering_stream";
    private static final String FIELD_OUTPUT_STREAM = "output_stream";
    private static final String FIELD_PIPELINE = "pipeline";

    // the stream which carries the conditions to filter the logs
    @JsonProperty(FIELD_FILTERING_STREAM)
    @NotNull
    public abstract String filteringStreamIdentifier();

    // the output stream into which filtered logs are put
    @JsonProperty(FIELD_OUTPUT_STREAM)
    @NotNull
    public abstract String outputStreamIdentifier();

    @JsonProperty(FIELD_PIPELINE)
    @Nullable
    public abstract Pipeline pipeline();

    public static Builder builder() {
        return Builder.create();
    }

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public static abstract class Builder {
        @JsonCreator
        public static Builder create() {
            return new AutoValue_TriggeringConditions.Builder();
        }

        @JsonProperty(FIELD_FILTERING_STREAM)
        public abstract Builder filteringStreamIdentifier(String streamIdentifier);

        @JsonProperty(FIELD_OUTPUT_STREAM)
        public abstract Builder outputStreamIdentifier(String streamIdentifier);

        @JsonProperty(FIELD_PIPELINE)
        public abstract Builder pipeline(Pipeline pipeline);

        public abstract TriggeringConditions build();
    }
}
