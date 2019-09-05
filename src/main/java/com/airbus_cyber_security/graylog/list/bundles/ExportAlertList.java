package com.airbus_cyber_security.graylog.list.bundles;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Map;


@AutoValue
@JsonAutoDetect
public abstract class ExportAlertList {

    @JsonProperty("title")
    @NotNull
    public abstract String getTitle();

    @JsonProperty("description")
    @Nullable
    public abstract String getDescription();

    @JsonCreator
    public static ExportAlertList create(@JsonProperty("title") String title,
                                         @JsonProperty("description") String description) {
        return new AutoValue_ExportAlertList(title, description);
    }
}
