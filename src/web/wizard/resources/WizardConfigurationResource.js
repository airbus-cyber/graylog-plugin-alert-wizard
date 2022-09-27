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

// TODO should combine methods into buildSourceURL?
const SOURCE_URL = URLUtils.qualifyUrl(RestUtils.buildSourceURL('config'));

function get() {
    return fetch('GET', SOURCE_URL)
        .then(response => {
            return response;
        })
        .catch(error => {
            UserNotification.error(`Fetching wizard configurations failed with status: ${error}`,
                'Could not retrieve configurations');
        });
}

function update(configuration) {
    const request = {
        field_order: configuration.field_order,
        default_values: configuration.default_values,
        import_policy: configuration.import_policy,
    };

    return fetch('PUT', SOURCE_URL, request)
        .then(response => {
            UserNotification.success('Wizard configurations successfully updated');
            return response;
        }).catch((error) => {
            UserNotification.error(`Updating wizard configurations failed with status: ${error.message}`,
                'Could not update configurations');
        });
}

export default {
    get,
    update
};