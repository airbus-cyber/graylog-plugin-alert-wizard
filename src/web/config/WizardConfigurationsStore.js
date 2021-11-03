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
import WizardConfigurationsActions from './WizardConfigurationsActions';
import RestUtils from '../wizard/rest/RestUtils';

// TODO try to move this into wizard/configuration
const WizardConfigurationStore = Reflux.createStore({
    listenables: [WizardConfigurationsActions],
    sourceUrl: RestUtils.buildSourceURL('config'),
    configurations: undefined,

    init() {
        this.trigger({configurations: this.configurations});
    },

    list() {
        const promise = fetch('GET', URLUtils.qualifyUrl(this.sourceUrl))
            .then(
                response => {
            this.configurations = response;
            this.trigger({configurations: this.configurations});
            return this.configurations;
        },
        error => {
            UserNotification.error(`Fetching wizard configurations failed with status: ${error}`,
                'Could not retrieve configurations');
        });
        WizardConfigurationsActions.list.promise(promise);
    },

    update(config) {
        const request = {field_order: config.field_order,
                        default_values: config.default_values,
                        import_policy: config.import_policy,
        };

        const promise = fetch('PUT', URLUtils.qualifyUrl(this.sourceUrl), request)
            .then(() => {
            UserNotification.success('Wizard configurations successfully updated');
            this.list();
            return true;
        }, (error) => {
            UserNotification.error(`Updating wizard configurations failed with status: ${error.message}`,
                'Could not update configurations');
        });

        WizardConfigurationsActions.update.promise(promise);
    },

});

export default WizardConfigurationStore;
