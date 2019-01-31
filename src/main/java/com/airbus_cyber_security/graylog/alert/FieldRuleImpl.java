package com.airbus_cyber_security.graylog.alert;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog2.database.CollectionName;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

@AutoValue
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
@CollectionName("field_rule")
public abstract class FieldRuleImpl implements FieldRule{

	@JsonProperty("id")
    @Override
    @Nullable
    public abstract String getID();
	
    @JsonProperty("field")
    @Override
    @NotNull
    public abstract String getField();

    @JsonProperty("type")
    @Override
    public abstract int getType();

    @JsonProperty("value")
    @Override
    @Nullable
    public abstract String getValue();

    @JsonCreator
    public static FieldRuleImpl create(@JsonProperty("id") String id,
    								   @JsonProperty("field") String field,
                                       @JsonProperty("type") int type,
                                       @JsonProperty("value") String value){
        return new AutoValue_FieldRuleImpl(id, field, type, value);
    }

}
