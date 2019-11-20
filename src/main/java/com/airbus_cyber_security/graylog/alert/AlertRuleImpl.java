package com.airbus_cyber_security.graylog.alert;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.graylog.plugins.pipelineprocessor.rest.StageSource;
import org.graylog2.database.CollectionName;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

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

    @JsonProperty("condition")
    @Override
    @Nullable
    public abstract String getConditionID();

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

    @JsonProperty("pipeline")
    @Override
    @Nullable
    public abstract String getPipelineID();

    @JsonProperty("pipeline_rule")
    @Override
    @Nullable
    public abstract List<String> getPipelineRuleID();

    @JsonProperty("second pipeline")
    @Override
    @Nullable
    public abstract String getSecondPipelineID();

    @JsonProperty("second pipeline_rule")
    @Override
    @Nullable
    public abstract List<String> getSecondPipelineRuleID();

	@JsonCreator
    public static AlertRuleImpl create(@JsonProperty("_id") String objectId,
                                       @JsonProperty("title") String title,
                                       @JsonProperty("stream") String streamID,
                                       @JsonProperty("condition") String conditionID,
                                       @JsonProperty("notification") String notificationID,
                                       @JsonProperty("created_at") DateTime createdAt,
                                       @JsonProperty("creator_user_id") String creatorUserId,
                                       @JsonProperty("last_modified") DateTime lastModified,
                                       @JsonProperty("description") String description,
                                       @JsonProperty("condition_type") String conditionType,
                                       @JsonProperty("stream2") String streamID2,
                                       @JsonProperty("pipeline") String pipelineID,
                                       @JsonProperty("pipeline_rule") List<String> pipelineRuleID,
                                        @JsonProperty("second pipeline") String pipelineID2,
                                       @JsonProperty("second pipeline_rule") List<String> pipelineRuleID2){
        return new AutoValue_AlertRuleImpl(title, streamID, conditionID, notificationID, createdAt, creatorUserId,
                lastModified, description, conditionType, streamID2, pipelineID, pipelineRuleID, pipelineID2, pipelineRuleID2);
    }
	
	public static AlertRuleImpl create(
            String title,
            String streamID,
            String conditionID,
            String notificationID,
            DateTime createdAt,
            String creatorUserId,
            DateTime lastModified,
            String description,
            String conditionType,
            String streamID2,
            String pipelineID,
            List<String> pipelineRuleID,
            String pipelineID2,
            List<String> pipelineRuleID2) {
		return new AutoValue_AlertRuleImpl(title, streamID, conditionID, notificationID, createdAt, creatorUserId,
                lastModified, description, conditionType,streamID2, pipelineID, pipelineRuleID, pipelineID2, pipelineRuleID2);
	}
}
