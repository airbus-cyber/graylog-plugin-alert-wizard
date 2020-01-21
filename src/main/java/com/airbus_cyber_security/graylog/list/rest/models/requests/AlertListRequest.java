package com.airbus_cyber_security.graylog.list.rest.models.requests;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

@AutoValue
@JsonAutoDetect
public abstract class AlertListRequest {

    @JsonProperty("title")
    @NotNull
    public abstract String getTitle();

    @JsonProperty("description")
    @Nullable
    public abstract String getDescription();

    @JsonProperty("usage")
    @NotNull
    public abstract int getUsage();

    @JsonProperty("lists")
    @Nullable
    public abstract String getLists();

    @JsonCreator
    public static AlertListRequest create(@JsonProperty("title") String title,
                                          @JsonProperty("description") String description,
                                          @JsonProperty("usage") int usage,
                                          @JsonProperty("lists") String lists) {
        return new AutoValue_AlertListRequest(title, description, usage, lists);
    }
}
