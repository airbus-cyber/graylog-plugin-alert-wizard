package com.airbus_cyber_security.graylog.list.rest.models.responses;

import com.airbus_cyber_security.graylog.list.AlertList;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
@JsonAutoDetect
public abstract class GetAlertList {

    @JsonProperty
    public abstract AlertList getLists();

    @JsonCreator
    public static GetAlertList create(@JsonProperty("list") AlertList list) {
        return new AutoValue_GetAlertList(list);
    }
}
