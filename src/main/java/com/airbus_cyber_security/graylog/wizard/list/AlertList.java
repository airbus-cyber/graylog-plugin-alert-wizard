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

package com.airbus_cyber_security.graylog.wizard.list;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog2.database.CollectionName;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;


@AutoValue
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
@CollectionName("wizard_lists")
public abstract class AlertList {


    @JsonProperty("title")
    @NotNull
    public abstract String getTitle();

    @JsonProperty("created_at")
    @Nullable
    public abstract DateTime getCreatedAt();

    @JsonProperty("creator_user_id")
    @Nullable
    public abstract String getCreatorUserId();

    @JsonProperty("last_modified")
    @Nullable
    public abstract DateTime getLastModified();

    @JsonProperty("description")
    @Nullable
    public abstract String getDescription();

    @JsonProperty("usage")
    @NotNull
    public abstract int getUsage();

    @JsonProperty("lists")
    @Nullable
    public abstract String getLists();

    @JsonCreator
    public static AlertList create(@JsonProperty("_id") String objectId,
                                       @JsonProperty("title") String title,
                                       @JsonProperty("created_at") DateTime createdAt,
                                       @JsonProperty("creator_user_id") String creatorUserId,
                                       @JsonProperty("last_modified") DateTime lastModified,
                                       @JsonProperty("description") String description,
                                       @JsonProperty("usage") int usage,
                                       @JsonProperty("lists") String lists){
        return new AutoValue_AlertList(title, createdAt, creatorUserId,
                lastModified, description, usage, lists);
    }

    public static AlertList create(
            String title,
            DateTime createdAt,
            String creatorUserId,
            DateTime lastModified,
            String description,
            int usage,
            String lists) {
        return new AutoValue_AlertList(title, createdAt, creatorUserId,
                lastModified, description, usage, lists);
    }
}
