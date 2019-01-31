package com.airbus_cyber_security.graylog.alert.rest.models.responses;

import com.airbus_cyber_security.graylog.alert.AlertRule;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
@JsonAutoDetect
public abstract class GetListAlertRule {

	@JsonProperty
	public abstract List<AlertRule> getAlerts();

	@JsonCreator
	public static GetListAlertRule create(@JsonProperty("alerts") List<AlertRule> alerts) {
		return new AutoValue_GetListAlertRule(alerts);
	}

}
