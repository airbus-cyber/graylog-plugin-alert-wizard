package com.airbus_cyber_security.graylog.alert.rest.models.responses;

import com.airbus_cyber_security.graylog.alert.AlertRule;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
@JsonAutoDetect
public abstract class GetAlertRule {

	@JsonProperty
	public abstract AlertRule getAlert();

	@JsonCreator
	public static GetAlertRule create(@JsonProperty("alert") AlertRule alert) {
		return new AutoValue_GetAlertRule(alert);
	}

}
