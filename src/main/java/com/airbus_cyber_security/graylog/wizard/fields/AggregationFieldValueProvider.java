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
package com.airbus_cyber_security.graylog.wizard.fields;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import org.graylog.events.event.EventWithContext;
import org.graylog.events.fields.FieldValue;
import org.graylog.events.fields.providers.AbstractFieldValueProvider;
import org.graylog.events.fields.providers.FieldValueProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AggregationFieldValueProvider extends AbstractFieldValueProvider {
    public interface Factory extends AbstractFieldValueProvider.Factory<AggregationFieldValueProvider> {
        @Override
        AggregationFieldValueProvider create(FieldValueProvider.Config config);
    }

    private static final Logger LOG = LoggerFactory.getLogger(AggregationFieldValueProvider.class);

    private final AggregationFieldValueProvider.Config config;

    @Inject
    public AggregationFieldValueProvider(@Assisted FieldValueProvider.Config config) {
        super(config);
        this.config = (AggregationFieldValueProvider.Config) config;
    }

    @Override
    protected FieldValue doGet(String fieldName, EventWithContext eventWithContext) {
        return null;
    }


    @AutoValue
    @JsonTypeName(AggregationFieldValueProvider.Config.TYPE_NAME)
    @JsonDeserialize(builder = AggregationFieldValueProvider.Config.Builder.class)
    public static abstract class Config implements AbstractFieldValueProvider.Config {
        public static final String TYPE_NAME = "aggregation-v1";

        private static final String FIELD_AGGREGATION_TIME_RANGE = "aggregation_time_range";

        @JsonProperty(FIELD_AGGREGATION_TIME_RANGE)
        public abstract Long aggregationTimeRange();

        public static AggregationFieldValueProvider.Config.Builder builder() {
            return AggregationFieldValueProvider.Config.Builder.create();
        }

        public abstract AggregationFieldValueProvider.Config.Builder toBuilder();

        @AutoValue.Builder
        public static abstract class Builder implements FieldValueProvider.Config.Builder<AggregationFieldValueProvider.Config.Builder> {
            @JsonCreator
            public static AggregationFieldValueProvider.Config.Builder create() {
                return new AutoValue_AggregationFieldValueProvider_Config.Builder().type(TYPE_NAME);
            }

            @JsonProperty(FIELD_AGGREGATION_TIME_RANGE)
            public abstract AggregationFieldValueProvider.Config.Builder aggregationTimeRange(Long aggregationTimeRange);

            public abstract AggregationFieldValueProvider.Config build();
        }
    }

}
