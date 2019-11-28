package com.airbus_cyber_security.graylog.list.rest.models.requests;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

@AutoValue
@JsonAutoDetect
public abstract class CloneAlertListRequest {

    @JsonProperty("title")
    @NotNull
    public abstract String getTitle();

    @JsonProperty("description")
    @Nullable
    public abstract String getDescription();

    @JsonProperty("lists")
    @Nullable
    public abstract String getLists();

    @JsonCreator
    public static CloneAlertListRequest create(@JsonProperty("title") String title,
                                               @JsonProperty("description") String description,
                                               @JsonProperty("lists") String lists) {
        return new AutoValue_CloneAlertListRequest(title, description, lists);
    }
}
