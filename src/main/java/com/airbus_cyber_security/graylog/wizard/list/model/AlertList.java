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

package com.airbus_cyber_security.graylog.wizard.list.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import org.graylog2.database.BuildableMongoEntity;
import org.joda.time.DateTime;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;


@AutoValue
@JsonAutoDetect
@JsonDeserialize(builder = AlertList.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AlertList implements BuildableMongoEntity<AlertList, AlertList.Builder> {

    public static final String FIELD_ID = "_id";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_USAGE = "usage";
    public static final String FIELD_LISTS = "lists";
    public static final String FIELD_CREATED_AT = "created_at";
    public static final String FIELD_LAST_MODIFIED = "last_modified";
    public static final String FIELD_CREATOR_USER_ID = "creator_user_id";

    @JsonProperty(FIELD_TITLE)
    @NotNull
    public abstract String title();

    @JsonProperty(FIELD_CREATED_AT)
    @Nullable
    public abstract DateTime createdAt();

    @JsonProperty(FIELD_CREATOR_USER_ID)
    @Nullable
    public abstract String creatorUserId();

    @JsonProperty(FIELD_LAST_MODIFIED)
    @Nullable
    public abstract DateTime lastModified();

    @JsonProperty(FIELD_DESCRIPTION)
    @Nullable
    public abstract String description();

    @JsonProperty(FIELD_USAGE)
    @NotNull
    public abstract int usage();

    // TODO not really named adequately should be getValues
    //      also would be better to be a List<String>, or a String[]
    @JsonProperty(FIELD_LISTS)
    // TODO why is this Nullable, should be @NotNull!!!!!
    @Nullable
    public abstract String lists();

    @AutoValue.Builder
    public abstract static class Builder implements BuildableMongoEntity.Builder<AlertList, AlertList.Builder> {
        @JsonCreator
        public static AlertList.Builder create() {
            return new AutoValue_AlertList.Builder();
        }

        @JsonProperty(FIELD_ID)
        public abstract AlertList.Builder id(String id);

        @JsonProperty(FIELD_TITLE)
        public abstract AlertList.Builder title(String title);

        @JsonProperty(FIELD_CREATED_AT)
        public abstract AlertList.Builder createdAt(DateTime createdAt);

        @JsonProperty(FIELD_CREATOR_USER_ID)
        public abstract AlertList.Builder creatorUserId(String creatorUserId);

        @JsonProperty(FIELD_LAST_MODIFIED)
        public abstract AlertList.Builder lastModified(DateTime lastModified);

        @JsonProperty(FIELD_DESCRIPTION)
        public abstract AlertList.Builder description(String description);

        @JsonProperty(FIELD_USAGE)
        public abstract AlertList.Builder usage(int usage);

        @JsonProperty(FIELD_LISTS)
        public abstract AlertList.Builder lists(String lists);

        public abstract AlertList autoBuild();

        public AlertList build() {
            return autoBuild();
        }
    }
}
