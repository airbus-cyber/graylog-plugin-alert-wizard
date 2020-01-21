package com.airbus_cyber_security.graylog.alert;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog2.database.CollectionName;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;

@AutoValue
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
@CollectionName("stream")
public abstract class AlertRuleStreamImpl implements AlertRuleStream{
    
    @JsonProperty("id")
    @Override
    @Nullable
    public abstract String getID();
    
    @JsonProperty("matching_type")
    @Override
    @NotNull
    public abstract String getMatchingType();
    
    @JsonProperty("field_rule")
    @Override
    @Nullable
    public abstract List<FieldRuleImpl> getFieldRules();

    @JsonCreator
    public static AlertRuleStreamImpl create(@JsonProperty("id") String id,
    									@JsonProperty("matching_type") String matchingType,
    									@JsonProperty("field_rule") List<FieldRuleImpl> fieldRules){
        return new AutoValue_AlertRuleStreamImpl(id, matchingType, fieldRules);
    }

}
