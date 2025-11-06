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

// TODO should remove all Actions and Stores
// TODO should remove Reflux and AlertRuleActions (use store directly), see the code below and AlertRuleResource
/*
import URLUtils from 'util/URLUtils';
import UserNotification from 'util/UserNotification';
import fetch from 'logic/rest/FetchProvider';
import RestUtils from './RestUtils';

const SOURCE_URL = RestUtils.buildSourceURL('alerts')

export default {
    get(name) {
        const promise = fetch('GET', URLUtils.qualifyUrl(SOURCE_URL + '/' + encodeURIComponent(name)))
            .then(
                response => {
                    return response;
                },
                error => {
                    UserNotification.error(`Fetching alert rule failed with status: ${error}`,
                        'Could not retrieve alert rule');
                });
        return promise;
    }
};
*/

import Reflux from 'reflux';
import URLUtils from 'util/URLUtils';
// TODO should not depend on a graphical element!!!!
import UserNotification from 'util/UserNotification';
import fetch from 'logic/rest/FetchProvider';
import AlertRuleActions from 'wizard/actions/AlertRuleActions';
import RestUtils from './RestUtils';
import PaginationURL from 'util/PaginationURL';

const SOURCE_URL = RestUtils.buildSourceURL('alerts');

const AlertRuleStore = Reflux.createStore({
    listenables: [AlertRuleActions],
    // TODO try to remove this field
    alerts: undefined,

    init() {
        // TODO try to remove
        this.trigger({alerts: this.alerts});
    },

    list() {
        const promise = fetch('GET', URLUtils.qualifyUrl(SOURCE_URL))
            .then(
                response => {
                    return response;
                },
                error => {
                    UserNotification.error(`Fetching alert rules failed with status: ${error}`,
                        'Could not retrieve alert rules');
                });
        AlertRuleActions.list.promise(promise);
    },

    searchPaginated(newPage, newPerPage, newQuery, additional) {
        const url = PaginationURL(`${SOURCE_URL}/paginated`, newPage, newPerPage, newQuery, additional);

        const promise = fetch('GET', URLUtils.qualifyUrl(url))
            .then((response) => {
                const {
                    elements,
                    query,
                    attributes,
                    pagination: {
                        count,
                        total,
                        page,
                        per_page: perPage,
                    },
                } = response;

                return {
                    elements,
                    attributes,
                    pagination: {
                        count,
                        total,
                        page,
                        perPage,
                        query,
                    },
                };
            });

        AlertRuleActions.searchPaginated.promise(promise);

        return promise;
    },

    get(name) {
        const promise = fetch('GET', URLUtils.qualifyUrl(SOURCE_URL + '/' + encodeURIComponent(name)))
            .then(
                response => {
                    this.trigger({alert: response});
                    return response;
                },
                error => {
                    UserNotification.error(`Fetching alert rule failed with status: ${error}`,
                        'Could not retrieve alert rule');
                });
        AlertRuleActions.get.promise(promise);
    },

    create(newAlert) {
        const url = URLUtils.qualifyUrl(SOURCE_URL);

        const request = {
            title: newAlert.title,
            priority: newAlert.priority,
            description: newAlert.description,
            condition_type: newAlert.condition_type,
            condition_parameters: newAlert.condition_parameters,
            stream: newAlert.stream,
            second_stream: newAlert.second_stream,
            disabled: newAlert.disabled
        };

        const promise = fetch('POST', url, request)
            .then(() => {
                UserNotification.success('Stream successfully created, Alert condition successfully created, Alert notification successfully created');
                return true;
            }, error => {
                UserNotification.error(`Creating alert rule failed with status: ${error.message}`,
                    'Could not create alert rule');
            });

        AlertRuleActions.create.promise(promise);
    },

    clone(source_title, title, description, shouldCloneNotification) {
        const url = URLUtils.qualifyUrl(SOURCE_URL + '/clone');

        const request = {
            source_title: source_title,
            title: title,
            description: description,
            clone_notification: shouldCloneNotification
        };

        const promise = fetch('POST', url, request)
            .then(() => {
                UserNotification.success('Stream successfully created, Alert condition successfully created, Alert notification successfully created');
                return true;
            }, error => {
                UserNotification.error(`Cloning alert rule failed with status: ${error.message}`,
                    'Could not clone alert rule');
            });

        AlertRuleActions.clone.promise(promise);
    },

    update(name, updatedAlert) {
        const url = URLUtils.qualifyUrl(SOURCE_URL + '/' + encodeURIComponent(name));
        const method = 'PUT';

        const request = {
            title: updatedAlert.title,
            priority: updatedAlert.priority,
            description: updatedAlert.description,
            condition_type: updatedAlert.condition_type,
            condition_parameters: updatedAlert.condition_parameters,
            stream: updatedAlert.stream,
            second_stream: updatedAlert.second_stream,
            disabled: updatedAlert.disabled
        };

        const promise = fetch(method, url, request)
            .then(() => {
                UserNotification.success('Alert rule successfully updated');
                return true;
            }, (error) => {
                UserNotification.error(`Updating alert rule failed with status: ${error.message}`,
                    'Could not update alert rule');
            });

        AlertRuleActions.update.promise(promise);
    },

    deleteByName(alertName) {
        const url = URLUtils.qualifyUrl(SOURCE_URL + '/' + encodeURIComponent(alertName));
        const method = 'DELETE';

        const promise = fetch(method, url)
            .then(() => {
                UserNotification.success('Alert rule successfully deleted');
                return null;
            }, (error) => {
                UserNotification.error(`Deleting alert rule failed with status: ${error.message}`,
                    'Could not delete alert rule');
            });
        AlertRuleActions.deleteByName.promise(promise);
    },
});

export default AlertRuleStore;
