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

package com.airbus_cyber_security.graylog.wizard.alert;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog2.database.CollectionName;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;

@AutoValue
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
@CollectionName("wizard_alerts")
public abstract class AlertRuleImpl implements AlertRule {


	@JsonProperty("title")
    @Override
    @NotNull
    public abstract String getTitle();
    
    @JsonProperty("stream")
    @Override
    @Nullable
    public abstract String getStreamID();

    @JsonProperty("event")
    @Override
    @Nullable
    public abstract String getEventID();

    @JsonProperty("notification")
    @Override
    @Nullable
    public abstract String getNotificationID();

    @JsonProperty("created_at")
    @Override
    @Nullable
    public abstract DateTime getCreatedAt();

    @JsonProperty("creator_user_id")
    @Override
    @Nullable
    public abstract String getCreatorUserId();

    @JsonProperty("last_modified")
    @Override
    @Nullable
    public abstract DateTime getLastModified();

    @JsonProperty("description")
    @Override
    @Nullable
    public abstract String getDescription();
    
    @JsonProperty("condition_type")
    @Override
    @Nullable
    public abstract String getConditionType();
    
    @JsonProperty("stream2")
    @Override
    @Nullable
    public abstract String getSecondStreamID();

    @JsonProperty("event2")
    @Override
    @Nullable
    public abstract String getSecondEventID();

    @JsonProperty("pipeline")
    @Override
    @Nullable
    public abstract String getPipelineID();

    @JsonProperty("pipeline_rule")
    @Override
    @Nullable
    public abstract String getPipelineRuleID();

    @JsonProperty("pipeline_field_rules")
    @Override
    @Nullable
    public abstract List<FieldRuleImpl> getPipelineFieldRules();

    @JsonProperty("second pipeline")
    @Override
    @Nullable
    public abstract String getSecondPipelineID();

    @JsonProperty("second pipeline_rule")
    @Override
    @Nullable
    public abstract String getSecondPipelineRuleID();

    @JsonProperty("second_pipeline_field_rules")
    @Override
    @Nullable
    public abstract List<FieldRuleImpl> getSecondPipelineFieldRules();

	@JsonCreator
    public static AlertRuleImpl create(@JsonProperty("_id") String objectId,
                                       @JsonProperty("title") String title,
                                       @JsonProperty("stream") String streamID,
                                       @JsonProperty("event") String eventID,
                                       @JsonProperty("notification") String notificationID,
                                       @JsonProperty("created_at") DateTime createdAt,
                                       @JsonProperty("creator_user_id") String creatorUserId,
                                       @JsonProperty("last_modified") DateTime lastModified,
                                       @JsonProperty("description") String description,
                                       @JsonProperty("condition_type") String conditionType,
                                       @JsonProperty("stream2") String streamID2,
                                       @JsonProperty("event2") String eventID2,
                                       @JsonProperty("pipeline") String pipelineID,
                                       @JsonProperty("pipeline_rule") String pipelineRuleID,
                                       @JsonProperty("pipeline_field_rules")List<FieldRuleImpl> pipelineFieldRules,
                                       @JsonProperty("second pipeline") String pipelineID2,
                                       @JsonProperty("second pipeline_rule") String pipelineRuleID2,
                                       @JsonProperty("second_pipeline_field_rules") List<FieldRuleImpl> pipelineFieldRules2){
        return new AutoValue_AlertRuleImpl(title, streamID, eventID, notificationID, createdAt, creatorUserId, lastModified, description,
                conditionType, streamID2, eventID2, pipelineID, pipelineRuleID, pipelineFieldRules, pipelineID2, pipelineRuleID2, pipelineFieldRules2);
    }
	
	public static AlertRuleImpl create(
            String title,
            String streamID,
            String eventID,
            String notificationID,
            DateTime createdAt,
            String creatorUserId,
            DateTime lastModified,
            String description,
            String conditionType,
            String streamID2,
            String eventID2,
            String pipelineID,
            String pipelineRuleID,
            List<FieldRuleImpl> pipelineFieldRules,
            String pipelineID2,
            String pipelineRuleID2,
            List<FieldRuleImpl> pipelineFieldRules2) {
		return new AutoValue_AlertRuleImpl(title, streamID, eventID, notificationID, createdAt, creatorUserId, lastModified, description,
                conditionType, streamID2, eventID2, pipelineID, pipelineRuleID, pipelineFieldRules, pipelineID2, pipelineRuleID2, pipelineFieldRules2);
	}
}
