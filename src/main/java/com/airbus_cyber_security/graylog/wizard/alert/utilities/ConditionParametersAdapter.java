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
package com.airbus_cyber_security.graylog.wizard.alert.utilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.airbus_cyber_security.graylog.wizard.alert.model.AlertConditionParameters;

public class ConditionParametersAdapter {

	private Map<String, Object> conditionParameters;

	public ConditionParametersAdapter(Map<String, Object> conditionParameters) {
		this.conditionParameters = conditionParameters;
		if (null == conditionParameters) {
			this.conditionParameters = new HashMap<>();
		}
	}

	public Integer getThreshold() {
		return this.getInteger(AlertConditionParameters.THRESHOLD, null);
	}

	public String getThresholdType() {
		return (String) this.conditionParameters.get(AlertConditionParameters.THRESHOLD_TYPE);
	}

	public String getSearchQuery() {
		return (String) this.conditionParameters.get(AlertConditionParameters.SEARCH_QUERY);
	}

	public Integer getAdditionalThreshold() {
		return this.getInteger(AlertConditionParameters.ADDITIONAL_THRESHOLD, null);
	}

	public String getAdditionalThresholdType() {
		return (String) this.conditionParameters.get(AlertConditionParameters.ADDITIONAL_THRESHOLD_TYPE);
	}

	public String getAdditionalSearchQuery() {
		return (String) this.conditionParameters.get(AlertConditionParameters.ADDITIONAL_SEARCH_QUERY);
	}

	public Long getTime() {
		return this.getLong(AlertConditionParameters.TIME, null);
	}

	public Long getGrace() {
		return this.getLong(AlertConditionParameters.GRACE, null);
	}

	public List<String> getGroupingFields() {
		return (List<String>) this.conditionParameters.get(AlertConditionParameters.GROUPING_FIELDS);
	}
	
	public String getDistinctBy() {
		return (String) this.conditionParameters.get(AlertConditionParameters.DISTINCT_BY);
	}

	public String getType() {
		return this.conditionParameters.get(AlertConditionParameters.TYPE).toString();
	}

	public String getField() {
		return this.conditionParameters.get(AlertConditionParameters.FIELD).toString();
	}

	private Integer getInteger(String field, Integer defaultValue) {
		Object raw = this.conditionParameters.get(field);
		if (null == raw) {
			return defaultValue;
		}
		if (raw instanceof Number backlog) {
			return backlog.intValue();
		}
		if (raw instanceof String backlogStr) {
			return Integer.parseInt(backlogStr);
		}
		return defaultValue;
	}

	private Long getLong(String field, Long defaultValue) {
		Object raw = this.conditionParameters.get(field);
		if (null == raw) {
			return defaultValue;
		}
		if (raw instanceof Number number) {
			return number.longValue();
		}
		if (raw instanceof String string) {
			return Long.parseLong(string);
		}
		return defaultValue;
	}

}
