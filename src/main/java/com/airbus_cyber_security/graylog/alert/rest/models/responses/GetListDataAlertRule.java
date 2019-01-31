package com.airbus_cyber_security.graylog.alert.rest.models.responses;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
@JsonAutoDetect
public abstract class GetListDataAlertRule {

	@JsonProperty
	public abstract List<GetDataAlertRule> getAlertsData();

	@JsonCreator
	public static GetListDataAlertRule create(@JsonProperty("alerts_data") List<GetDataAlertRule> alerts) {
		return new AutoValue_GetListDataAlertRule(alerts);
	}

}
