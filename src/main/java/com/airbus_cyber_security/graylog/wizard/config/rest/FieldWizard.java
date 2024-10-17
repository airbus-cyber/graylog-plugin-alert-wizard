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

import jakarta.validation.constraints.NotNull;

@AutoValue
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class FieldWizard {
    
	@JsonProperty("name")
    @NotNull
    public abstract String getName();

    @JsonProperty("enabled")
    public abstract boolean getEnabled();
    
    @JsonCreator
    public static FieldWizard create(@JsonProperty("name") String name,
                                       @JsonProperty("enabled") boolean enabled){
        return new AutoValue_FieldWizard(name, enabled);
    }
}
