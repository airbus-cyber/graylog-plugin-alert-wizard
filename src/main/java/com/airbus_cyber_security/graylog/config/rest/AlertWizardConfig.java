package com.airbus_cyber_security.graylog.config.rest;

import java.util.List;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

@AutoValue
@JsonAutoDetect
public abstract class AlertWizardConfig {

	@JsonProperty("field_order")
	@NotNull
    public abstract List<FieldWizard> accessFieldOrder();
	
	@JsonProperty("default_values")
	@Nullable
	public abstract  DefaultValues accessDefaultValues();
	
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
						0,
						0,
						"",
						0,
						"",
						false,
						1000,
						0))
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

