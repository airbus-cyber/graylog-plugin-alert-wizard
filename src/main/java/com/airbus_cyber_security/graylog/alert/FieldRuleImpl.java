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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.graylog2.database.CollectionName;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

@AutoValue
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
@CollectionName("field_rule")
public abstract class FieldRuleImpl implements FieldRule{

	@JsonProperty("id")
    @Override
    @Nullable
    public abstract String getID();
	
    @JsonProperty("field")
    @Override
    @NotNull
    public abstract String getField();

    @JsonProperty("type")
    @Override
    public abstract int getType();

    @JsonProperty("value")
    @Override
    @Nullable
    public abstract String getValue();

    @JsonCreator
    public static FieldRuleImpl create(@JsonProperty("id") String id,
    								   @JsonProperty("field") String field,
                                       @JsonProperty("type") int type,
                                       @JsonProperty("value") String value){
        return new AutoValue_FieldRuleImpl(id, field, type, value);
    }

}
