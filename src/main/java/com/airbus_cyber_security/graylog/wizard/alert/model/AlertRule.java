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
import org.graylog2.database.BuildableMongoEntity;
import org.joda.time.DateTime;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import org.mongojack.Id;
import org.mongojack.ObjectId;


@AutoValue
@JsonAutoDetect
@JsonDeserialize(builder = AlertRule.Builder.class)
public abstract class AlertRule implements BuildableMongoEntity<AlertRule, AlertRule.Builder> {
    public static final String FIELD_ID = "_id";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_ALERT_TYPE = "alert_type";
    public static final String FIELD_ALERT_PATTERN = "alert_pattern";
    public static final String FIELD_NOTIFICATION = "notification";
    public static final String FIELD_CREATED_AT = "created_at";
    public static final String FIELD_LAST_MODIFIED = "last_modified";
    public static final String FIELD_CREATOR_USER_ID = "creator_user_id";

    @Id
    @ObjectId
    @Nullable
    @JsonProperty("id")
    public abstract String id();

    @JsonProperty(FIELD_TITLE)
    @NotNull
    public abstract String title();

    @JsonProperty(FIELD_ALERT_TYPE)
    @Nullable
    public abstract AlertType alertType();

    @JsonProperty(FIELD_ALERT_PATTERN)
    @NotNull
    public abstract AlertPattern pattern();

    @JsonProperty(FIELD_NOTIFICATION)
    @Nullable
    public abstract String notificationID();

    @JsonProperty(FIELD_CREATED_AT)
    @Nullable
    public abstract DateTime createdAt();

    @JsonProperty(FIELD_CREATOR_USER_ID)
    @Nullable
    public abstract String creatorUserId();

    @JsonProperty(FIELD_LAST_MODIFIED)
    @Nullable
    public abstract DateTime lastModified();

    public abstract Builder toBuilder();

    static Builder builder() {
        return Builder.create();
    }

    @AutoValue.Builder
    public abstract static class Builder implements BuildableMongoEntity.Builder<AlertRule, AlertRule.Builder> {
        @JsonCreator
        public static Builder create() {
            return new AutoValue_AlertRule.Builder();
        }

        @JsonProperty(FIELD_ID)
        public abstract Builder id(String id);
        @JsonProperty(FIELD_TITLE)
        public abstract Builder title(String title);
        @JsonProperty(FIELD_ALERT_TYPE)
        public abstract Builder alertType(AlertType alertType);
        @JsonProperty(FIELD_ALERT_PATTERN)
        public abstract Builder pattern(AlertPattern alertPattern);
        @JsonProperty(FIELD_NOTIFICATION)
        public abstract Builder notificationID(String notificationID);
        @JsonProperty(FIELD_CREATED_AT)
        public abstract Builder createdAt(DateTime  createdAt);
        @JsonProperty(FIELD_CREATOR_USER_ID)
        public abstract Builder creatorUserId(String creatorUserId);
        @JsonProperty(FIELD_LAST_MODIFIED)
        public abstract Builder lastModified(DateTime lastModified);

        public abstract AlertRule autoBuild();

        public AlertRule build() {
            return autoBuild();
        }
    }
}
