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
import org.graylog2.plugin.streams.Stream;

/**
 * The filtering stream is the stream which encodes the stream conditions.
 * The pipeline encodes the list conditions.
 * The output stream is the stream on which the event feeds.
 *
 * There are 4 possible constructions:
 * - no stream conditions, no list conditions: filteringStream == null, outputStream == null
 * - stream conditions only: pipeline == null, filteringStream == outputStream
 * - list conditions only: pipeline -> outputStream, filteringStream == null
 * - both stream and list conditions:
 *     If the conditions matching type is OR (at least one): pipeline -> outputStream == filteringStream
 *     If the conditions matching type is AND (all): filteringStream -> pipeline -> outputStream
 */
@AutoValue
@JsonAutoDetect
@JsonDeserialize(builder = TriggeringConditions.Builder.class)
public abstract class TriggeringConditions {
    private static final String FIELD_MATCHING_TYPE = "matching_type";
    private static final String FIELD_FILTERING_STREAM = "filtering_stream";
    private static final String FIELD_OUTPUT_STREAM = "output_stream";
    private static final String FIELD_PIPELINE = "pipeline";

    @JsonProperty(FIELD_MATCHING_TYPE)
    @NotNull
    public abstract Stream.MatchingType matchingType();

    // the stream which carries the conditions to filter the logs
    @JsonProperty(FIELD_FILTERING_STREAM)
    @Nullable
    public abstract String filteringStreamIdentifier();

    // the output stream into which filtered logs are put
    @JsonProperty(FIELD_OUTPUT_STREAM)
    @Nullable
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

        @JsonProperty(FIELD_MATCHING_TYPE)
        public abstract Builder matchingType(Stream.MatchingType matchingType);

        @JsonProperty(FIELD_FILTERING_STREAM)
        public abstract Builder filteringStreamIdentifier(String streamIdentifier);

        @JsonProperty(FIELD_OUTPUT_STREAM)
        public abstract Builder outputStreamIdentifier(String streamIdentifier);

        @JsonProperty(FIELD_PIPELINE)
        public abstract Builder pipeline(Pipeline pipeline);

        public abstract TriggeringConditions build();
    }
}
