package com.airbus_cyber_security.graylog.config.rest;

import javax.validation.constraints.NotNull;
import org.graylog2.database.CollectionName;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
@CollectionName("field_wizard")
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
