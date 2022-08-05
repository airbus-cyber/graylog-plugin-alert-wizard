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

import Reflux from 'reflux';
import URLUtils from 'util/URLUtils';
import UserNotification from 'util/UserNotification';
import fetch from 'logic/rest/FetchProvider';
import AlertRuleActions from 'wizard/actions/AlertRuleActions';
import RestUtils from './RestUtils';

const AlertRuleStore = Reflux.createStore({
    listenables: [AlertRuleActions],
    sourceUrl: RestUtils.buildSourceURL('alerts'),
    alerts: undefined,

    init() {
        this.trigger({alerts: this.alerts});
    },

    list() {
        const promise = fetch('GET', URLUtils.qualifyUrl(this.sourceUrl))
            .then(
                response => {
                    this.alerts = response.alerts;
                    this.trigger({alerts: this.alerts});
                    return this.alerts;
                },
                error => {
                    UserNotification.error(`Fetching alert rules failed with status: ${error}`,
                        'Could not retrieve alerts rules');
                });
        AlertRuleActions.list.promise(promise);
    },
    listWithData() {
        const promise = fetch('GET', URLUtils.qualifyUrl(this.sourceUrl + '/data'))
            .then(
                response => {
                    this.alerts = response.alerts_data;
                    this.trigger({alerts: this.alerts});
                    return this.alerts;
                },
                error => {
                    UserNotification.error(`Fetching alert rules failed with status: ${error}`,
                        'Could not retrieve alert rules with additional data');
                });
        AlertRuleActions.listWithData.promise(promise);
    },
    get(name) {
        const promise = fetch('GET', URLUtils.qualifyUrl(this.sourceUrl + '/' + encodeURIComponent(name)))
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
        const url = URLUtils.qualifyUrl(this.sourceUrl);

        const request = {
            title: newAlert.title,
            severity: newAlert.severity,
            description: newAlert.description,
            condition_type: newAlert.condition_type,
            condition_parameters: newAlert.condition_parameters,
            stream: newAlert.stream,
            second_stream: newAlert.second_stream,
        };

        const promise = fetch('POST', url, request)
            .then(() => {
                UserNotification.success('Stream successfully created, Alert condition successfully created, Alert notification successfully created');
                this.list();
                return true;
            }, (error) => {
                UserNotification.error(`Creating alert rule failed with status: ${error.message}`,
                    'Could not create alert rule');
            });

        AlertRuleActions.create.promise(promise);
    },
    update(name, updatedAlert) {
        const url = URLUtils.qualifyUrl(this.sourceUrl + '/' + encodeURIComponent(name));
        const method = 'PUT';

        const request = {
            title: updatedAlert.title,
            severity: updatedAlert.severity,
            description: updatedAlert.description,
            condition_type: updatedAlert.condition_type,
            condition_parameters: updatedAlert.condition_parameters,
            stream: updatedAlert.stream,
            second_stream: updatedAlert.second_stream,
        };

        const promise = fetch(method, url, request)
            .then(() => {
                UserNotification.success('Alert rule successfully updated');
                this.list();
                return true;
            }, (error) => {
                UserNotification.error(`Updating alert rule failed with status: ${error.message}`,
                    'Could not update alert rule');
            });

        AlertRuleActions.update.promise(promise);
    },

    deleteByName(alertName) {
        const url = URLUtils.qualifyUrl(this.sourceUrl + '/' + encodeURIComponent(alertName));
        const method = 'DELETE';

        const promise = fetch(method, url)
            .then(() => {
                UserNotification.success('Alert rule successfully deleted');
                this.listWithData();
                return null;
            }, (error) => {
                UserNotification.error(`Deleting alert rule failed with status: ${error.message}`,
                    'Could not delete alert rule');
            });
        AlertRuleActions.deleteByName.promise(promise);
    },

    exportAlertRules(titles) {
	    const url = URLUtils.qualifyUrl(this.sourceUrl + '/export');
        const method = 'POST';
        
        const promise = fetch(method, url, titles).then(
            response => {
                return response;
            },
            error => {
                UserNotification.error(`Export alert rules failed with status: ${error}`,
                    'Could not export alert rules');
            });
        AlertRuleActions.exportAlertRules.promise(promise);
    },
});

export default AlertRuleStore;
