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
import org.graylog.events.fields.FieldValueType;
import org.graylog.events.fields.providers.AbstractFieldValueProvider;
import org.graylog.events.fields.providers.FieldValueProvider;
import org.graylog.events.search.EventsSearchFilter;
import org.graylog.events.search.EventsSearchParameters;
import org.graylog.events.search.EventsSearchResult;
import org.graylog.events.search.EventsSearchService;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.indexer.searches.timeranges.RelativeRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

public class AggregationFieldValueProvider extends AbstractFieldValueProvider {
    public interface Factory extends AbstractFieldValueProvider.Factory<AggregationFieldValueProvider> {
        @Override
        AggregationFieldValueProvider create(FieldValueProvider.Config config);
    }

    private static final Logger LOG = LoggerFactory.getLogger(AggregationFieldValueProvider.class);

    private final AggregationFieldValueProvider.Config config;
    private final EventsSearchService searchService;

    @Inject
    public AggregationFieldValueProvider(@Assisted FieldValueProvider.Config config, EventsSearchService searchService) {
        super(config);
        this.config = (AggregationFieldValueProvider.Config) config;
        this.searchService = searchService;
    }

    @Override
    protected FieldValue doGet(String fieldName, EventWithContext eventWithContext) {
        LOG.debug("Start Compute field {}", fieldName);

        // Check if alert already exist in time range
        if (this.config.aggregationTimeRange() > 0) {
            LOG.debug("Notification Aggregation Time Range is defined");

            String eventDefinitionId = eventWithContext.event().getEventDefinitionId();
            int timeRange = this.config.aggregationTimeRange() * 60;

            EventsSearchFilter searchFilter = EventsSearchFilter.builder()
                    .alerts(EventsSearchFilter.Alerts.ONLY)
                    .eventDefinitions(Collections.singleton(eventDefinitionId))
                    .build();

            EventsSearchParameters request = EventsSearchParameters.builder()
                    .filter(searchFilter)
                    .timerange(RelativeRange.create(timeRange))
                    .page(1)
                    // Do not use higher perPage value cause request failed
                    .perPage(5000)
                    .query("")
                    .sortBy(Message.FIELD_TIMESTAMP)
                    .sortDirection(EventsSearchParameters.SortDirection.DESC)
                    .build();

            EventsSearchResult result = this.searchService.search(request, new EmptySubject());

            if (result.totalEvents() > 0) {
                LOG.debug("Found {} events for aggregation", result.totalEvents());

                Optional<EventsSearchResult.Event> existingEvent = result.events().stream().filter(x -> x.event().groupByFields().equals(eventWithContext.event().getGroupByFields())).findFirst();

                if (existingEvent.isPresent()) {
                    String existingId = existingEvent.get().event().fields().getOrDefault(fieldName, UUID.randomUUID().toString());
                    LOG.debug("Find existing Event with aggregation Id {}", existingId);

                    return FieldValue.create(FieldValueType.STRING, existingId);
                }
            }
        }

        return getRandomFieldValue();
    }

    public FieldValue getRandomFieldValue() {
        return FieldValue.create(FieldValueType.STRING, UUID.randomUUID().toString());
    }

    @AutoValue
    @JsonTypeName(AggregationFieldValueProvider.Config.TYPE_NAME)
    @JsonDeserialize(builder = AggregationFieldValueProvider.Config.Builder.class)
    public static abstract class Config implements AbstractFieldValueProvider.Config {
        public static final String TYPE_NAME = "aggregation-v1";

        private static final String FIELD_AGGREGATION_TIME_RANGE = "aggregation_time_range";

        @JsonProperty(FIELD_AGGREGATION_TIME_RANGE)
        public abstract Integer aggregationTimeRange();

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
            public abstract AggregationFieldValueProvider.Config.Builder aggregationTimeRange(Integer aggregationTimeRange);

            public abstract AggregationFieldValueProvider.Config build();
        }
    }

}
