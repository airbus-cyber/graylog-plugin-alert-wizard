package com.airbus_cyber_security.graylog.alert.bundles;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
@JsonAutoDetect
public abstract class ExportAlertRuleRequest {

    @JsonProperty("titles")
    @NotNull
    public abstract List<String> getTitles();
    
    @JsonCreator    
    public static ExportAlertRuleRequest create(@JsonProperty("titles") List<String> titles) {
        return new AutoValue_ExportAlertRuleRequest(titles);
    }
}
