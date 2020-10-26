/*
 * graylog-plugin-alert-wizard Source Code
 * Copyright (C) 2018-2020 - Airbus CyberSecurity (SAS) - All rights reserved
 *
 * This file is part of the graylog-plugin-alert-wizard GPL Source Code.
 *
 * graylog-plugin-alert-wizard Source Code is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this code.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.airbus_cyber_security.graylog.alert;

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