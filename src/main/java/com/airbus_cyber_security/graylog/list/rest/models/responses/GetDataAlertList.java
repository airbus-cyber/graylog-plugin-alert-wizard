package com.airbus_cyber_security.graylog.list.rest.models.responses;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import org.joda.time.DateTime;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

@AutoValue
@JsonAutoDetect
public abstract class GetDataAlertList {

    @JsonProperty("title")
    @NotNull
    public abstract String getTitle();

    @JsonProperty("created_at")
    @Nullable
    public abstract DateTime getCreatedAt();

    @JsonProperty("creator_user_id")
    @Nullable
    public abstract String getCreatorUserId();

    @JsonProperty("last_modified")
    @Nullable
    public abstract DateTime getLastModified();

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
    public static GetDataAlertList create(@JsonProperty("title") String title,
                                          @JsonProperty("created_at") DateTime createdAt,
                                          @JsonProperty("creator_user_id") String creatorUserId,
                                          @JsonProperty("last_modified") DateTime lastModified,
                                          @JsonProperty("description") String description,
                                          @JsonProperty("usage") int usage,
                                          @JsonProperty("lists") String lists){
        return new AutoValue_GetDataAlertList(title, createdAt, creatorUserId,
                lastModified, description, usage, lists);
    }

}
