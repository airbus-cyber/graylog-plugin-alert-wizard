package com.airbus_cyber_security.graylog.alert;

import org.joda.time.DateTime;

import java.util.List;

public interface AlertRule {
	
	public String getTitle();

	public String getStreamID();

    public String getConditionID();

    public String getNotificationID();

    public DateTime getCreatedAt();

    public String getCreatorUserId();

    public DateTime getLastModified();

    public String getDescription();
    
    public String getConditionType();
    
	public String getSecondStreamID();

	public String getPipelineID();

    public String getPipelineRuleID();

    public List<FieldRuleImpl> getPipelineFieldRules();

    public String getSecondPipelineID();

    public String getSecondPipelineRuleID();

    public List<FieldRuleImpl> getSecondPipelineFieldRules();

}