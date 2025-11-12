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

import com.airbus_cyber_security.graylog.wizard.alert.model.AlertType;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.AlertRuleStream;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import org.mongojack.Id;
import org.mongojack.ObjectId;

import java.util.Map;

// TODO either inherit or reuse AlertRuleRequest to avoid duplication in this class => I believe it should be the same object, with optional fields maybe...
// TODO instead of allowing some Nullable fields, such as condition, stream, condition_parameters, maybe should be either corrupted or contain correct information
@AutoValue
@JsonAutoDetect
public abstract class GetDataAlertRule {
	public static final String FIELD_ID = "id";
	public static final String FIELD_TITLE = "title";
	public static final String FIELD_PRIORITY = "priority";
	public static final String FIELD_DESCRIPTION = "description";
	public static final String FIELD_CONDITION_TYPE = "condition_type";
	public static final String FIELD_CONDITION_PARAMETERS = "condition_parameters";
	public static final String FIELD_STREAM = "stream";
	public static final String FIELD_SECOND_STREAM = "second_stream";
	public static final String FIELD_CONDITION = "condition";
	public static final String FIELD_SECOND_EVENT_DEFINITION = "second_event_definition";
	public static final String FIELD_NOTIFICATION = "notification";
	public static final String FIELD_CREATED_AT = "created_at";
	public static final String FIELD_CREATOR_USER_ID = "creator_user_id";
	public static final String FIELD_LAST_MODIFIED = "last_modified";
	public static final String FIELD_DISABLED = "disabled";

	@Id
	@ObjectId
	@Nullable
	@JsonProperty(FIELD_ID)
	public abstract String id();

	@JsonProperty(FIELD_TITLE)
	@NotNull
	public abstract String getTitle();

	@JsonProperty(FIELD_PRIORITY)
	@Nullable
	public abstract Integer getPriority();

    @JsonProperty(FIELD_DESCRIPTION)
    @Nullable
    public abstract String getDescription();

    @JsonProperty(FIELD_CONDITION_TYPE)
    @Nullable
    public abstract AlertType getConditionType();

    @JsonProperty(FIELD_CONDITION_PARAMETERS)
    @Nullable
    public abstract Map<String, Object> conditionParameters();

    @JsonProperty(FIELD_STREAM)
    @Nullable
    public abstract AlertRuleStream getStream();

    @JsonProperty(FIELD_SECOND_STREAM)
    @Nullable
    public abstract AlertRuleStream getSecondStream();

	// TODO should be named EventDefinitionIdentifier rather than ConditionID
	@JsonProperty(FIELD_CONDITION)
	@Nullable
	public abstract String getConditionID();

	@JsonProperty(FIELD_SECOND_EVENT_DEFINITION)
	@Nullable
	public abstract String secondEventDefinitionIdentifier();

	// TODO rename into getNotificationIdentifier
	@JsonProperty(FIELD_NOTIFICATION)
	@Nullable
	public abstract String getNotificationID();

	@JsonProperty(FIELD_CREATED_AT)
	@Nullable
	public abstract DateTime getCreatedAt();

	@JsonProperty(FIELD_CREATOR_USER_ID)
	@Nullable
	public abstract String getCreatorUserId();

	@JsonProperty(FIELD_LAST_MODIFIED)
	@Nullable
	public abstract DateTime getLastModified();

	@JsonProperty(FIELD_DISABLED)
	public abstract boolean isDisabled();

	// TODO
	//      I guess all annotations on this method create can be removed
	//      since, if I understand well, this @JsonCreator is used to deserialize
	//      json objects to java.
	//      However this object is returns by the API REST, but never fed inside the request
	// TODO try to remove all annotations on this method
	@JsonCreator
	public static GetDataAlertRule create(@JsonProperty(FIELD_ID) String id,
										  @JsonProperty(FIELD_TITLE) String title,
                                          @JsonProperty(FIELD_PRIORITY) Integer priority,
                                          @JsonProperty(FIELD_CONDITION) String eventDefinitionIdentifier,
										  @JsonProperty(FIELD_SECOND_EVENT_DEFINITION) String secondEventDefinitionIdentifier,
                                          @JsonProperty(FIELD_NOTIFICATION) String notificationIdentifier,
                                          @JsonProperty(FIELD_CREATED_AT) DateTime createdAt,
                                          @JsonProperty(FIELD_CREATOR_USER_ID) String creatorUserIdentifier,
                                          @JsonProperty(FIELD_LAST_MODIFIED) DateTime lastModified,
                                          @JsonProperty(FIELD_DISABLED) boolean isDisabled,
                                          @JsonProperty(FIELD_DESCRIPTION) String description,
                                          @JsonProperty(FIELD_CONDITION_TYPE) AlertType alertType,
                                          @JsonProperty(FIELD_CONDITION_PARAMETERS) Map<String, Object> conditionParameters,
                                          @JsonProperty(FIELD_STREAM) AlertRuleStream stream,
                                          @JsonProperty(FIELD_SECOND_STREAM) AlertRuleStream stream2) {
		return new AutoValue_GetDataAlertRule(id, title, priority, description, alertType, conditionParameters, stream, stream2,
				eventDefinitionIdentifier, secondEventDefinitionIdentifier, notificationIdentifier, createdAt, creatorUserIdentifier,
				lastModified, isDisabled);
	}

}
