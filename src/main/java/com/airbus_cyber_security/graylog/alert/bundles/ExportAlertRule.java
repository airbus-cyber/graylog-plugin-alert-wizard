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

package com.airbus_cyber_security.graylog.alert.bundles;

import com.airbus_cyber_security.graylog.alert.AlertRuleStreamImpl;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Map;

@AutoValue
@JsonAutoDetect
public abstract class ExportAlertRule {

    @JsonProperty("title")
    @NotNull
    public abstract String getTitle();

    @JsonProperty("notification_parameters")
    public abstract Map<String, Object> notificationParameters();

    @JsonProperty("description")
    @Nullable
    public abstract String getDescription();

    @JsonProperty("condition_type")
    @Nullable
    public abstract String getConditionType();
    
    @JsonProperty("condition_parameters")
    public abstract Map<String, Object> conditionParameters();
    
    @JsonProperty("stream")
    public abstract AlertRuleStreamImpl getStream();
    
    @JsonProperty("second_stream")
    @Nullable
    public abstract AlertRuleStreamImpl getSecondStream();

    @JsonCreator    
    public static ExportAlertRule create(@JsonProperty("title") String title,
                                                @JsonProperty("notification_parameters") Map<String, Object> notificationParameters,
                                                @JsonProperty("description") String description,
                                                @JsonProperty("condition_type") String conditionType,
                                                @JsonProperty("condition_parameters") Map<String, Object> conditionParameters,
                                                @JsonProperty("stream") AlertRuleStreamImpl stream,
                                                @JsonProperty("second_stream") AlertRuleStreamImpl stream2) {
        return new AutoValue_ExportAlertRule(title, notificationParameters, description, conditionType, conditionParameters, stream, stream2);
    }
}
