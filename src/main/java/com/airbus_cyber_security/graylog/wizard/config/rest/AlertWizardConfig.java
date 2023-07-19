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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;

// TODO rename into AlertWizardConfiguration
@AutoValue
@JsonAutoDetect
public abstract class AlertWizardConfig {

	@JsonProperty("field_order")
	@NotNull
    public abstract List<FieldWizard> accessFieldOrder();
	
	@JsonProperty("default_values")
	public abstract DefaultValues accessDefaultValues();
	
	@JsonProperty("import_policy")
	@Nullable
	public abstract ImportPolicyType accessImportPolicy();
	
	@JsonCreator
    public static AlertWizardConfig create(@JsonProperty("field_order") List<FieldWizard> fieldOrder,
    		@JsonProperty("default_values") DefaultValues defaultValues, @JsonProperty("import_policy") ImportPolicyType importPolicy ){
        return builder()
                .accessFieldOrder(fieldOrder)
                .accessDefaultValues(defaultValues)
                .accessImportPolicy(importPolicy)
                .build();
    }


	public static AlertWizardConfig defaultConfig() {
		return builder()
				.accessFieldOrder(ImmutableList.of(
						FieldWizard.create("Severity", true),
						FieldWizard.create("Description", true),
						FieldWizard.create("Created", true),
						FieldWizard.create("Last Modified", true),
						FieldWizard.create("User", true),
						FieldWizard.create("Status", true),
						FieldWizard.create("Rule", false)))
				.accessDefaultValues(DefaultValues.create("",
						"info",
						"AND",
						">",
						0,
						1,
						1,
						1,
						500))
				.accessImportPolicy(ImportPolicyType.DONOTHING)
				.build();
	}
	
    public static Builder builder() {
        return new AutoValue_AlertWizardConfig.Builder();
    }

    public abstract Builder toBuilder();
    
	@AutoValue.Builder
	public abstract static class Builder {
	 	public abstract Builder accessFieldOrder(List<FieldWizard> fieldOrder);
	 	public abstract Builder accessDefaultValues(DefaultValues defaultValues);
	 	public abstract Builder accessImportPolicy(ImportPolicyType importPolicy);
	 	public abstract AlertWizardConfig build();
	}
}

