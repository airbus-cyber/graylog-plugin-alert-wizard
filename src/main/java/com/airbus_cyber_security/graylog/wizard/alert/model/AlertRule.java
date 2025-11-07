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
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.auto.value.AutoValue;
import org.bson.Document;
import org.joda.time.DateTime;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import org.mongojack.Id;
import org.mongojack.ObjectId;


@AutoValue
@JsonAutoDetect
public abstract class AlertRule {
    public static final String FIELD_ID = "_id";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_ALERT_TYPE = "alert_type";
    public static final String FIELD_ALERT_PATTERN = "alert_pattern";
    public static final String FIELD_NOTIFICATION = "notification";
    public static final String FIELD_CREATED_AT = "created_at";
    public static final String FIELD_LAST_MODIFIED = "last_modified";
    public static final String FIELD_CREATOR_USER_ID = "creator_user_id";

    @Id
    @ObjectId
    @Nullable
    @JsonProperty("id")
    public abstract String id();

    @JsonProperty(FIELD_TITLE)
    @NotNull
    public abstract String getTitle();

    @JsonProperty(FIELD_ALERT_TYPE)
    @Nullable
    public abstract AlertType getAlertType();

    @JsonProperty(FIELD_ALERT_PATTERN)
    @NotNull
    public abstract AlertPattern pattern();

    // TODO rename into notificationIdentifier
    @JsonProperty(FIELD_NOTIFICATION)
    @Nullable
    public abstract String getNotificationID();

    @JsonProperty(FIELD_CREATED_AT)
    @Nullable
    public abstract DateTime getCreatedAt();

    // TODO rename int creatorUserIdentifier
    @JsonProperty(FIELD_CREATOR_USER_ID)
    @Nullable
    public abstract String getCreatorUserId();

    @JsonProperty(FIELD_LAST_MODIFIED)
    @Nullable
    public abstract DateTime getLastModified();

    // TODO should replace the create functions by a Builder (see EventDefinitionDTO)
    @JsonCreator
    public static AlertRule create(@JsonProperty("_id") String objectId,
                                   @JsonProperty(FIELD_TITLE) String title,
                                   @JsonProperty(FIELD_ALERT_TYPE) AlertType alertType,
                                   @JsonProperty(FIELD_ALERT_PATTERN) AlertPattern pattern,
                                   @JsonProperty(FIELD_NOTIFICATION) String notificationID,
                                   @JsonProperty(FIELD_CREATED_AT) DateTime createdAt,
                                   @JsonProperty(FIELD_CREATOR_USER_ID) String creatorUserId,
                                   @JsonProperty(FIELD_LAST_MODIFIED) DateTime lastModified){
        return new AutoValue_AlertRule(objectId, title, alertType, pattern, notificationID, createdAt, creatorUserId, lastModified);
    }

    public static AlertRule fromDocument(Document document) {
        Document patternDoc = document.get(FIELD_ALERT_PATTERN, Document.class);
        AlertPattern pattern = null;
        if (patternDoc != null) {
            String patternClass = patternDoc.getString(JsonTypeInfo.Id.CLASS.getDefaultPropertyName());
            if (patternClass.equals(AutoValue_AggregationAlertPattern.class.getName())) {
                pattern = AggregationAlertPattern.fromDocument(patternDoc);
            } else if (patternClass.equals(AutoValue_CorrelationAlertPattern.class.getName())) {
                pattern =  CorrelationAlertPattern.fromDocument(patternDoc);
            } else if (patternClass.equals(AutoValue_DisjunctionAlertPattern.class.getName())) {
                pattern = DisjunctionAlertPattern.fromDocument(patternDoc);
            }
        }

        return new AutoValue_AlertRule(
                document.getObjectId(FIELD_ID).toHexString(),
                document.getString(FIELD_TITLE),
                AlertType.valueOf(document.getString(FIELD_ALERT_TYPE)),
                pattern,
                document.getString(FIELD_NOTIFICATION),
                new DateTime(document.getDate(FIELD_CREATED_AT)),
                document.getString(FIELD_CREATOR_USER_ID),
                new DateTime(document.getDate(FIELD_LAST_MODIFIED)));
    }
}
