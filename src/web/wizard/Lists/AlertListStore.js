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
import AlertListActions from './AlertListActions';

const AlertListStore = Reflux.createStore({
    listenables: [AlertListActions],
    sourceUrl: '/plugins/com.airbus_cyber_security.graylog/lists',
    lists: undefined,

    init() {
      this.trigger({lists: this.lists});
    },

    list() {
        const promise = fetch('GET', URLUtils.qualifyUrl(this.sourceUrl))
            .then(
                response => {
                    this.lists = response.lists;
                    this.trigger({lists: this.lists});
                    return this.lists;
                },
                error => {
                    UserNotification.error(`Fetching alert lists failed with status: ${error}`,
                        'Could not retrieve alerts lists');
                });
        AlertListActions.list.promise(promise);
    },

    get(name) {
        const promise = fetch('GET', URLUtils.qualifyUrl(this.sourceUrl + '/' + encodeURIComponent(name)))
            .then(
                response => {
                    this.lists = response.lists;
                    this.trigger({lists: this.lists});
                    return this.lists;
                },
                error => {
                    UserNotification.error(`Fetching alert list failed with status: ${error}`,
                        'Could not retrieve alert list');
                });
        AlertListActions.get.promise(promise);
    },

    create(newList) {
        const url = URLUtils.qualifyUrl(this.sourceUrl);
        const method = 'POST';

        const request = {
            title: newList.title,
            description: newList.description,
            lists: newList.lists,
        };

        const promise = fetch(method, url, request)
            .then(() => {
                UserNotification.success('Alert list successfully created');
                this.list();
                return true;
            }, (error) => {
                UserNotification.error(`Creating alert list failed with status: ${error.message}`,
                    'Could not create alert list');
            });

        AlertListActions.create.promise(promise);
    },

    update(name, updatedList) {
        const url = URLUtils.qualifyUrl(this.sourceUrl + '/' + encodeURIComponent(name));
        const method = 'PUT';

        const request = {
            title: updatedList.title,
            description: updatedList.description,
            usage: updatedList.usage,
            lists: updatedList.lists,
        };

        const promise = fetch(method, url, request)
            .then(() => {
                UserNotification.success('Alert list successfully updated');
                this.list();
                return true;
            }, (error) => {
                UserNotification.error(`Updating alert list failed with status: ${error.message}`,
                    'Could not update alert list');
            });

        AlertListActions.update.promise(promise);
    },

    deleteByName(listName) {
        const url = URLUtils.qualifyUrl(this.sourceUrl + '/' + encodeURIComponent(listName));
        const method = 'DELETE';

        const promise = fetch(method, url)
            .then(() => {
                UserNotification.success('Alert list successfully deleted');
                this.list();
                return null;
            }, (error) => {
                UserNotification.error(`Deleting alert list failed with status: ${error.message}`,
                    'Could not delete alert list');
            });
        AlertListActions.deleteByName.promise(promise);
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
                UserNotification.success('Alert list successfully clone');
                this.list();
                return true;
            }, (error) => {
                UserNotification.error(`Cloning alert list failed with status: ${error.message}`,
                    'Could not clone alert list');
            });

        AlertListActions.clone.promise(promise);
    },

    exportAlertLists(titles){
        const url = URLUtils.qualifyUrl(this.sourceUrl + '/export');
        const method = 'POST';

        const promise = fetch(method, url, titles)
            .then(
                response => {
                    this.exportAlertLists = JSON.stringify(response);
                    this.trigger({exportAlertLists: this.exportAlertLists});
                    return this.exportAlertLists;
                },
                error => {
                    UserNotification.error(`Export alert lists failed with status: ${error}`,
                        'Could not export alert lists');
                });
        AlertListActions.exportAlertLists.promise(promise);
    },

    importAlertLists(alertLists){
        const url = URLUtils.qualifyUrl(this.sourceUrl + '/import');
        const method = 'PUT';

        const promise = fetch(method, url, alertLists).then(() => {
            UserNotification.success('Alert lists successfully imported');
            return true;
        }, (error) => {
            UserNotification.error(`Importing alert lists failed with status: ${error.message}`,
                'Could not import alert lists');
        });

        AlertListActions.importAlertLists.promise(promise);
    },
});

export default AlertListStore;
