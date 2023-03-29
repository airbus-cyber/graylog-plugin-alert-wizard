/*
 * Copyright (C) 2018 Airbus CyberSecurity (SAS)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package com.airbus_cyber_security.graylog.wizard.alert.rest.models.responses;

import com.airbus_cyber_security.graylog.wizard.alert.rest.models.AlertRuleStream;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Map;

// TODO either inherit or reuse AlertRuleRequest to avoid duplication in this class => I believe it should be the same object, with optional fields maybe...
@AutoValue
@JsonAutoDetect
public abstract class GetDataAlertRule {

	@JsonProperty("title")
	@NotNull
	public abstract String getTitle();
	
	@JsonProperty("severity")
	@NotNull
	public abstract String getSeverity();

    @JsonProperty("description")
    @Nullable
    public abstract String getDescription();

    @JsonProperty("condition_type")
    @Nullable
    public abstract String getConditionType();

    @JsonProperty("condition_parameters")
    @Nullable
    public abstract Map<String, Object> conditionParameters();

    @JsonProperty("stream")
    @Nullable
    public abstract AlertRuleStream getStream();

    @JsonProperty("second_stream")
    @Nullable
    public abstract AlertRuleStream getSecondStream();

	// TODO should be named EventDefinitionIdentifier rather than ConditionID
	@JsonProperty("condition")
	@Nullable
	public abstract String getConditionID();

	@JsonProperty("second_event_definition")
	@Nullable
	public abstract String secondEventDefinitionIdentifier();

	// TODO rename into getNotificationIdentifier
	@JsonProperty("notification")
	@Nullable
	public abstract String getNotificationID();

	@JsonProperty("created_at")
	@Nullable
	public abstract DateTime getCreatedAt();

	@JsonProperty("creator_user_id")
	@Nullable
	public abstract String getCreatorUserId();

	@JsonProperty("last_modified")
	@Nullable
	public abstract DateTime getLastModified();

	@JsonProperty("disabled")
	public abstract boolean isDisabled();

	@JsonProperty("alert_count")
	public abstract long getAlertCount();

	@JsonCreator
	public static GetDataAlertRule create(@JsonProperty("title") String title,
                                          @JsonProperty("severity") String severity,
                                          @JsonProperty("condition") String eventDefinitionIdentifier,
										  @JsonProperty("second_event_definition") String secondEventDefinitionIdentifier,
                                          @JsonProperty("notification") String notificationIdentifier,
                                          @JsonProperty("created_at") DateTime createdAt,
                                          @JsonProperty("creator_user_id") String creatorUserIdentifier,
                                          @JsonProperty("created_at") DateTime lastModified,
                                          @JsonProperty("disabled") boolean isDisabled,
                                          @JsonProperty("description") String description,
                                          @JsonProperty("alert_count") long alertCount,
                                          @JsonProperty("condition_type") String conditionType,
                                          @JsonProperty("condition_parameters") Map<String, Object> conditionParameters,
                                          @JsonProperty("stream") AlertRuleStream stream,
                                          @JsonProperty("second_stream") AlertRuleStream stream2) {
		return new AutoValue_GetDataAlertRule(title, severity, description, conditionType, conditionParameters, stream, stream2,
				eventDefinitionIdentifier, secondEventDefinitionIdentifier, notificationIdentifier, createdAt, creatorUserIdentifier,
				lastModified, isDisabled, alertCount);
	}

}
