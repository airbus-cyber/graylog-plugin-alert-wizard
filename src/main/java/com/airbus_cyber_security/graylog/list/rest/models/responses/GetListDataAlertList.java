package com.airbus_cyber_security.graylog.list.rest.models.responses;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
@JsonAutoDetect
public abstract class GetListDataAlertList {

    @JsonProperty
    public abstract List<GetDataAlertList> getListsData();

    @JsonCreator
    public static GetListDataAlertList create(@JsonProperty("lists_data") List<GetDataAlertList> lists) {
        return new AutoValue_GetListDataAlertList(lists);
    }

}
