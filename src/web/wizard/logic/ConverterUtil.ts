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

import AlertValidation from 'wizard/logic/AlertValidation';
import { AlertRule } from 'wizard/model/AlertRule';


export function convertAlertToElement(alert: AlertRule) {
    let alertValid = !AlertValidation.isAlertCorrupted(alert);
    let textColor = '';
    if (!alertValid) {
        textColor = 'text-danger';
    } else if (alert.disabled) {
        textColor = 'text-muted';
    }
    let streamId = '';
    if (alert.stream) {
        streamId = alert.stream.id;
    }
    let streamId2 = null;
    if (alert.second_stream) {
        streamId2 = alert.second_stream.id;
    }
    let searchQuery = '';
    if (alert.condition_parameters) {
        searchQuery = alert.condition_parameters.search_query;
    }
    let searchQuery2 = '';
    if (alert.condition_parameters) {
        searchQuery2 = alert.condition_parameters.additional_search_query;
    }
    return {
        id: alert.id,
        title: alert.title,
        priority: alert.priority,
        description: alert.description,
        created: alert.created_at,
        lastModified: alert.last_modified,
        user: alert.creator_user_id,
        status: alert.disabled,
        valid: alertValid,
        textColor: textColor,
        streamId: streamId,
        streamId2: streamId2,
        condition: alert.condition,
        notification: alert.notification,
        secondEventDefinition: alert.second_event_definition,
        searchQuery: searchQuery,
        searchQuery2: searchQuery2
    };
}