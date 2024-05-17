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

package com.airbus_cyber_security.graylog.wizard.alert.model;

import com.airbus_cyber_security.graylog.wizard.alert.rest.models.FieldRule;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;

// TODO rename ID into Identifier everywhere
@AutoValue
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AlertRule {

    // TODO should add an identifier and work with it (see Notification or EventDefinition)

    @JsonProperty("title")
    @NotNull
    public abstract String getTitle();

    @JsonProperty("alert_type")
    @Nullable
    public abstract String getAlertType();

    @JsonProperty("conditions1")
    @NotNull
    public abstract TriggeringConditions conditions1();

    @JsonProperty("conditions2")
    @Nullable
    public abstract TriggeringConditions conditions2();

    // TODO can't this be always NotNull?
    @JsonProperty("event")
    @Nullable
    public abstract String event1();

    @JsonProperty("event2")
    @Nullable
    public abstract String event2();

    // TODO rename into notification
    @JsonProperty("notification")
    @Nullable
    public abstract String getNotificationID();

    @JsonProperty("created_at")
    @Nullable
    public abstract DateTime getCreatedAt();

    // TODO rename int getCreatorUserIdentifier
    @JsonProperty("creator_user_id")
    @Nullable
    public abstract String getCreatorUserId();

    @JsonProperty("last_modified")
    @Nullable
    public abstract DateTime getLastModified();

    // TODO should replace the create functions by a Builder (see EventDefinitionDTO)
    @JsonCreator
    public static AlertRule create(@JsonProperty("_id") String objectId,
                                   @JsonProperty("title") String title,
                                   @JsonProperty("alert_type") String alertType,
                                   @JsonProperty("conditions1") TriggeringConditions conditions1,
                                   @JsonProperty("conditions2") TriggeringConditions conditions2,
                                   @JsonProperty("event") String eventID,
                                   @JsonProperty("event2") String eventID2,
                                   @JsonProperty("notification") String notificationID,
                                   @JsonProperty("created_at") DateTime createdAt,
                                   @JsonProperty("creator_user_id") String creatorUserId,
                                   @JsonProperty("last_modified") DateTime lastModified){
        return create(title, alertType, conditions1, conditions2, eventID, eventID2, notificationID, createdAt,
                creatorUserId, lastModified);
    }
	
	public static AlertRule create(
            String title,
            String alertType,
            TriggeringConditions conditions1,
            TriggeringConditions conditions2,
            String eventID,
            String eventIdentifier2,
            String notificationID,
            DateTime createdAt,
            String creatorUserId,
            DateTime lastModified) {
		return new AutoValue_AlertRule(title, alertType, conditions1, conditions2, eventID, eventIdentifier2, notificationID, createdAt,
                creatorUserId, lastModified);
	}
}
