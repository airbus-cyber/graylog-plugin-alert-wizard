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

import org.joda.time.DateTime;

import java.util.List;

public interface AlertRule {
	
	public String getTitle();

	public String getStreamID();

    public String getEventID();

    public String getNotificationID();

    public DateTime getCreatedAt();

    public String getCreatorUserId();

    public DateTime getLastModified();

    public String getDescription();
    
    public String getConditionType();
    
	public String getSecondStreamID();

	public String getSecondEventID();

	public String getPipelineID();

    public String getPipelineRuleID();

    public List<FieldRuleImpl> getPipelineFieldRules();

    public String getSecondPipelineID();

    public String getSecondPipelineRuleID();

    public List<FieldRuleImpl> getSecondPipelineFieldRules();

}