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

    @JsonProperty("stream")
    @NotNull
    public abstract String getStreamIdentifier();

    // TODO rename to getEventIdentifier
    // TODO can't this be always NotNull?
    @JsonProperty("event")
    @Nullable
    public abstract String getEventID();

    // TODO rename into getNotificationIdentifier
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

    // TODO rename into getSecondStreamIdentifier
    @JsonProperty("stream2")
    @Nullable
    public abstract String getSecondStreamID();

    // TODO rename into getSecondEventIdentifier
    @JsonProperty("event2")
    @Nullable
    public abstract String getSecondEventID();

    // TODO rename into getPipelineIdentifier
    @JsonProperty("pipeline")
    @Nullable
    public abstract String getPipelineID();

    // TODO rename into getPipelineRuleIdentifier
    @JsonProperty("pipeline_rule")
    @Nullable
    public abstract String getPipelineRuleID();

    @JsonProperty("pipeline_field_rules")
    @Nullable
    public abstract List<FieldRule> getPipelineFieldRules();

    // TODO rename into getSecondPipelineIdentifier
    @JsonProperty("second pipeline")
    @Nullable
    public abstract String getSecondPipelineID();

    // TODO rename into getSecondPipelineRuleIdentifier
    @JsonProperty("second pipeline_rule")
    @Nullable
    public abstract String getSecondPipelineRuleID();

    @JsonProperty("second_pipeline_field_rules")
    @Nullable
    public abstract List<FieldRule> getSecondPipelineFieldRules();

    @JsonCreator
    public static AlertRule create(@JsonProperty("_id") String objectId,
                                   @JsonProperty("title") String title,
                                   @JsonProperty("alert_type") String alertType,
                                   @JsonProperty("conditions1") TriggeringConditions conditions1,
                                   @JsonProperty("stream") String streamID,
                                   @JsonProperty("event") String eventID,
                                   @JsonProperty("notification") String notificationID,
                                   @JsonProperty("created_at") DateTime createdAt,
                                   @JsonProperty("creator_user_id") String creatorUserId,
                                   @JsonProperty("last_modified") DateTime lastModified,
                                   @JsonProperty("stream2") String streamID2,
                                   @JsonProperty("event2") String eventID2,
                                   @JsonProperty("pipeline") String pipelineID,
                                   @JsonProperty("pipeline_rule") String pipelineRuleID,
                                   @JsonProperty("pipeline_field_rules")List<FieldRule> pipelineFieldRules,
                                   @JsonProperty("second pipeline") String pipelineID2,
                                   @JsonProperty("second pipeline_rule") String pipelineRuleID2,
                                   @JsonProperty("second_pipeline_field_rules") List<FieldRule> pipelineFieldRules2){
        return new AutoValue_AlertRule(title, alertType, conditions1, streamID, eventID, notificationID, createdAt,
                creatorUserId, lastModified, streamID2, eventID2, pipelineID, pipelineRuleID, pipelineFieldRules,
                pipelineID2, pipelineRuleID2, pipelineFieldRules2);
    }
	
	public static AlertRule create(
            String title,
            String alertType,
            TriggeringConditions conditions1,
            String streamID,
            String eventID,
            String notificationID,
            DateTime createdAt,
            String creatorUserId,
            DateTime lastModified,
            String streamIdentifier2,
            String eventIdentifier2,
            String pipelineIdentifier,
            String pipelineRuleIdentifier,
            List<FieldRule> pipelineFieldRules,
            String pipelineIdentifier2,
            String pipelineRuleIdentifier2,
            List<FieldRule> pipelineFieldRules2) {
		return new AutoValue_AlertRule(title, alertType, conditions1, streamID, eventID, notificationID, createdAt,
                creatorUserId, lastModified, streamIdentifier2, eventIdentifier2, pipelineIdentifier,
                pipelineRuleIdentifier, pipelineFieldRules, pipelineIdentifier2, pipelineRuleIdentifier2,
                pipelineFieldRules2);
	}
}
