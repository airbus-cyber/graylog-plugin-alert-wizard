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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import org.bson.Document;

@AutoValue
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class FieldRule {

	@JsonProperty("id")
    @Nullable
    public abstract String getID();
	
    @JsonProperty("field")
    @NotNull
    public abstract String getField();

    // TODO this should probably be better as an enum
    @JsonProperty("type")
    public abstract int getType();

    @JsonProperty("value")
    @Nullable
    public abstract String getValue();

    @JsonCreator
    public static FieldRule create(@JsonProperty("id") String id,
                                   @JsonProperty("field") String field,
                                   @JsonProperty("type") int type,
                                   @JsonProperty("value") String value){
        return new AutoValue_FieldRule(id, field, type, value);
    }

    public static FieldRule fromDocument(Document document) {
        return create(
                document.getString("id"),
                document.getString("field"),
                document.getInteger("type"),
                document.getString("value")
        );
    }

}
