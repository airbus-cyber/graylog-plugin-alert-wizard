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
public final class AlertFields {

    private AlertFields() {
    }

    public static final String FIELD_ID = "id";
    public static final String FIELD_NOTIFICATION = "notification";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_PRIORITY = "priority";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_CONDITION_TYPE = "condition_type";
    public static final String FIELD_CONDITION_PARAMETERS = "condition_parameters";
    public static final String FIELD_STREAM = "stream";
    public static final String FIELD_SECOND_STREAM = "second_stream";
    public static final String FIELD_SECOND_EVENT_DEFINITION = "second_event_definition";
    public static final String FIELD_DISABLED = "disabled";
    public static final String FIELD_AGGREGATION_TIME = "aggregation_time";
    public static final String FIELD_NOTIFICATION_PARAMETERS = "notification_parameters";

    public static final String FIELD_ALERT_TYPE = "alert_type";
    public static final String FIELD_ALERT_PATTERN = "alert_pattern";
    public static final String FIELD_CREATED_AT = "created_at";
    public static final String FIELD_LAST_MODIFIED = "last_modified";
    public static final String FIELD_CREATOR_USER_ID = "creator_user_id";
}
