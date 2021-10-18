/*
 * graylog-plugin-alert-wizard Source Code
 * Copyright (C) 2018-2020 - Airbus CyberSecurity (SAS) - All rights reserved
 *
 * This file is part of the graylog-plugin-alert-wizard GPL Source Code.
 *
 * graylog-plugin-alert-wizard Source Code is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this code.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.airbus_cyber_security.graylog.config.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;

@AutoValue
@JsonAutoDetect
public abstract class AlertWizardConfig {

	@JsonProperty("field_order")
	@NotNull
    public abstract List<FieldWizard> accessFieldOrder();
	
	@JsonProperty("default_values")
	@Nullable
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
						FieldWizard.create("Alerts", true),
						FieldWizard.create("Status", true),
						FieldWizard.create("Rule", false)))
				.accessDefaultValues(DefaultValues.create("",
						"",
						"",
						"",
						0,
						1,
						0,
						"",
						0,
						"",
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

