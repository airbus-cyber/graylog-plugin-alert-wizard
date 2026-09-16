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

/**
 * Available condition parameters of an alert.
 */
public final class CloneAlertRuleFields {

	private CloneAlertRuleFields() {
	}

	public static final String FIELD_SOURCE_TITLE = "source_title";
	public static final String FIELD_TITLE = "title";
	public static final String FIELD_DESCRIPTION = "description";
	public static final String FIELD_CLONE_NOTIFICATION = "clone_notification";
	public static final String FIELD_CONDITION_TYPE = "condition_type";
	public static final String FIELD_POLICY = "policy";

}
