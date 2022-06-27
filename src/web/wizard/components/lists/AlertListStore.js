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
import AlertListActions from './AlertListActions';
import RestUtils from 'wizard/stores/RestUtils';

const AlertListStore = Reflux.createStore({
    listenables: [AlertListActions],
    sourceUrl: RestUtils.buildSourceURL('lists'),
    // TODO is this field needed?
    lists: undefined,

    init() {
      this.trigger({lists: this.lists});
    },

    list() {
        const promise = fetch('GET', URLUtils.qualifyUrl(this.sourceUrl))
            .then(
                response => {
                    this.lists = response.lists;
                    // TODO remove the trigger? And in other methods too
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
                // TODO why is this called after? Remove?
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
