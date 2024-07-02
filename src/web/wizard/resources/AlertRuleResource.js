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

import URLUtils from 'util/URLUtils';
import UserNotification from 'util/UserNotification';
import fetch from 'logic/rest/FetchProvider';
import RestUtils from 'wizard/stores/RestUtils';

const SOURCE_URL = RestUtils.buildSourceURL('alerts');

function create(newAlert) {
    const url = URLUtils.qualifyUrl(SOURCE_URL);
    const request = {
        title: newAlert.title,
        priority: newAlert.priority,
        description: newAlert.description,
        condition_type: newAlert.condition_type,
        condition_parameters: newAlert.condition_parameters,
        stream: newAlert.stream,
        second_stream: newAlert.second_stream,
    };
    return fetch('POST', url, request)
        .then(() => {
            UserNotification.success('Stream successfully created, Alert condition successfully created, Alert notification successfully created');
            return true;
        }).catch(error => {
            UserNotification.error(`Creating alert rule failed with status: ${error.message}`,
                'Could not create alert rule');
        });
}

export default {
    create: create
}