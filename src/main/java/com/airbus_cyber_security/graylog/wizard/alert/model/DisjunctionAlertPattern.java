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
import org.bson.Document;

/**
 * To encode Or rule: two separate paths (condition -> aggregation event) which trigger the same notification.
 */
@AutoValue
@JsonAutoDetect
@JsonDeserialize(builder = DisjunctionAlertPattern.Builder.class)
public abstract class DisjunctionAlertPattern implements AlertPattern {
    public static final String FIELD_CONDITIONS1 = "conditions1";
    public static final String FIELD_CONDITIONS2 = "conditions2";
    public static final String FIELD_EVENT_IDENTIFIER1 = "event_identifier1";
    public static final String FIELD_EVENT_IDENTIFIER2 = "event_identifier2";

    @JsonProperty(FIELD_CONDITIONS1)
    @NotNull
    public abstract TriggeringConditions conditions1();

    @JsonProperty(FIELD_CONDITIONS2)
    @NotNull
    public abstract TriggeringConditions conditions2();

    @JsonProperty(FIELD_EVENT_IDENTIFIER1)
    @NotNull
    public abstract String eventIdentifier1();

    @JsonProperty(FIELD_EVENT_IDENTIFIER2)
    @NotNull
    public abstract String eventIdentifier2();

    public static Builder builder() {
        return Builder.create();
    }

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public static abstract class Builder {
        @JsonCreator
        public static Builder create() {
            return new AutoValue_DisjunctionAlertPattern.Builder();
        }

        @JsonProperty(FIELD_CONDITIONS1)
        public abstract Builder conditions1(TriggeringConditions conditions1);

        @JsonProperty(FIELD_CONDITIONS2)
        public abstract Builder conditions2(TriggeringConditions conditions2);

        @JsonProperty(FIELD_EVENT_IDENTIFIER1)
        public abstract Builder eventIdentifier1(String eventIdentifier);

        @JsonProperty(FIELD_EVENT_IDENTIFIER2)
        public abstract Builder eventIdentifier2(String eventIdentifier);

        public abstract DisjunctionAlertPattern build();
    }

    public static DisjunctionAlertPattern fromDocument(Document document) {
        return builder().
                eventIdentifier1(document.getString(FIELD_EVENT_IDENTIFIER1)).
                eventIdentifier2(document.getString(FIELD_EVENT_IDENTIFIER2)).
                conditions1(TriggeringConditions.fromDocument(document.get(FIELD_CONDITIONS1, Document.class))).
                conditions2(TriggeringConditions.fromDocument(document.get(FIELD_CONDITIONS2, Document.class))).
                build();
    }
}
