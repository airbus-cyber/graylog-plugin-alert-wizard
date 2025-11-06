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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import org.mongojack.Id;
import org.mongojack.ObjectId;


// TODO rename ID into Identifier everywhere
@AutoValue
@JsonAutoDetect
public abstract class AlertRule {

    @Id
    @ObjectId
    @Nullable
    @JsonProperty("id")
    public abstract String id();

    @JsonProperty("title")
    @NotNull
    public abstract String getTitle();

    @JsonProperty("alert_type")
    @Nullable
    public abstract AlertType getAlertType();

    @JsonProperty("alert_pattern")
    @NotNull
    public abstract AlertPattern pattern();

    // TODO rename into notificationIdentifier
    @JsonProperty("notification")
    @Nullable
    public abstract String getNotificationID();

    @JsonProperty("created_at")
    @Nullable
    public abstract DateTime getCreatedAt();

    // TODO rename int creatorUserIdentifier
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
                                   @JsonProperty("alert_type") AlertType alertType,
                                   @JsonProperty("alert_pattern") AlertPattern pattern,
                                   @JsonProperty("notification") String notificationID,
                                   @JsonProperty("created_at") DateTime createdAt,
                                   @JsonProperty("creator_user_id") String creatorUserId,
                                   @JsonProperty("last_modified") DateTime lastModified){
        return new AutoValue_AlertRule(objectId, title, alertType, pattern, notificationID, createdAt, creatorUserId, lastModified);
    }
	
	public static AlertRule create(
            String title,
            AlertType alertType,
            AlertPattern pattern,
            String notificationID,
            DateTime createdAt,
            String creatorUserId,
            DateTime lastModified) {
		return create(null, title, alertType, pattern, notificationID, createdAt, creatorUserId, lastModified);
	}
}
