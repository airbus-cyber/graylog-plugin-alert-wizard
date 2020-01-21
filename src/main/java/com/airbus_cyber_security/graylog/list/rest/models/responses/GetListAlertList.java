package com.airbus_cyber_security.graylog.list.rest.models.responses;

import com.airbus_cyber_security.graylog.list.AlertList;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
@JsonAutoDetect
public abstract class GetListAlertList {

    @JsonProperty
    public abstract List<AlertList> getLists();

    @JsonCreator
    public static GetListAlertList create(@JsonProperty("lists") List<AlertList> lists) {
        return new AutoValue_GetListAlertList(lists);
    }
}
