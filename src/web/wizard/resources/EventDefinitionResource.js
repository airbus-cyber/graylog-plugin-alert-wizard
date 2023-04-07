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

const SOURCE_URL = '/events/definitions';

function enable(eventDefinitionIdentifier) {
    const url = URLUtils.qualifyUrl(`${SOURCE_URL}/${eventDefinitionIdentifier}/schedule`);
    return fetch('PUT', url)
        .catch(error => {
            UserNotification.error(`Enabling Event Definition "${eventDefinitionIdentifier}" failed with status: ${error}`,
                'Could not enable Event Definition');
        });
}

function disable(eventDefinitionIdentifier) {
    const url = URLUtils.qualifyUrl(`${SOURCE_URL}/${eventDefinitionIdentifier}/unschedule`);
    return fetch('PUT', url)
        .catch(error => {
            UserNotification.error(`Disabling Event Definition "${eventDefinitionIdentifier}" failed with status: ${error}`,
                'Could not disable Event Definition');
        });
}

export default {
    enable,
    disable
};