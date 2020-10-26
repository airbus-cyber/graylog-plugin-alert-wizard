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

import Reflux from 'reflux';
import URLUtils from 'util/URLUtils';
import UserNotification from 'util/UserNotification';
import fetch from 'logic/rest/FetchProvider';
import AlertRuleActions from './AlertRuleActions';

const AlertRuleStore = Reflux.createStore({
    listenables: [AlertRuleActions],
    sourceUrl: '/plugins/com.airbus_cyber_security.graylog/alerts',
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
                    this.alert = response.alert;
                    this.trigger({alert: this.alert});
                    return this.alert;
                },
                error => {
                    UserNotification.error(`Fetching alert rule failed with status: ${error}`,
                        'Could not retrieve alert rule');
                });
        AlertRuleActions.get.promise(promise);
    },
    getData(name) {
        const promise = fetch('GET', URLUtils.qualifyUrl(this.sourceUrl + '/' + encodeURIComponent(name) + '/data'))
            .then(
                response => {
                    this.trigger({alertData: response});
                    return response;
                },
                error => {
                    UserNotification.error(`Fetching alert rule data failed with status: ${error}`,
                        'Could not retrieve alert rule data');
                });
        AlertRuleActions.getData.promise(promise);
    },
    create(newAlert) {
        const url = URLUtils.qualifyUrl(this.sourceUrl);
        const method = 'POST';

        const request = {
            title: newAlert.title,
            severity: newAlert.severity,
            description: newAlert.description,
            condition_type: newAlert.condition_type,
            condition_parameters: newAlert.condition_parameters,
            stream: newAlert.stream,
            second_stream: newAlert.second_stream,
        };

        const promise = fetch(method, url, request)
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

    clone(name, title, description) {
        const url = URLUtils.qualifyUrl(this.sourceUrl + '/' + encodeURIComponent(name) + '/Clone');
        const method = 'POST';

        const request = {
            title: title,
            description: description,
        };

        const promise = fetch(method, url, request)
            .then(() => {
                UserNotification.success('Stream successfully clone, Alert condition successfully clone, Alert notification successfully clone');
                this.list();
                return true;
            }, (error) => {
                UserNotification.error(`Cloning alert rule failed with status: ${error.message}`,
                    'Could not clone alert rule');
            });

        AlertRuleActions.clone.promise(promise);
    },

    exportAlertRules(titles){
	const url = URLUtils.qualifyUrl(this.sourceUrl + '/export');
        const method = 'POST';
        
	const promise = fetch(method, url, titles)
        .then(
            response => {
                this.exportAlertRules = JSON.stringify(response);
                this.trigger({exportAlertRules: this.exportAlertRules});
                return this.exportAlertRules;
            },
            error => {
                UserNotification.error(`Export alert rules failed with status: ${error}`,
                    'Could not export alert rules');
            });
	AlertRuleActions.exportAlertRules.promise(promise);
    },
    
    importAlertRules(alertRules){
	const url = URLUtils.qualifyUrl(this.sourceUrl + '/import');
        const method = 'PUT';
        
        const promise = fetch(method, url, alertRules).then(() => {
                    UserNotification.success('Alert rules successfully imported');
                    return true;
                }, (error) => {
                    UserNotification.error(`Importing alert rules failed with status: ${error.message}`,
                        'Could not import alert rules');
                });
        	
	AlertRuleActions.importAlertRules.promise(promise);
    },
});

export default AlertRuleStore;
