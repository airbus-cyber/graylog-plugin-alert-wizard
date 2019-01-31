package com.airbus_cyber_security.graylog.alert.rest.models.requests;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

@AutoValue
@JsonAutoDetect
public abstract class CloneAlertRuleRequest {

    @JsonProperty("title")
    @NotNull
    public abstract String getTitle();

    @JsonProperty("description")
    @Nullable
    public abstract String getDescription();


    @JsonCreator    
    public static CloneAlertRuleRequest create(@JsonProperty("title") String title,
                                               @JsonProperty("description") String description) {
        return new AutoValue_CloneAlertRuleRequest(title, description);
    }
}
