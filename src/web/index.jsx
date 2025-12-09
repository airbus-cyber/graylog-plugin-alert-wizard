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

import packageJson from '../../package.json';
import { PluginManifest, PluginStore } from 'graylog-web-plugin/plugin';
import { appPrefixed } from 'util/URLUtils';
import WizardPage from './wizard/pages/WizardPage';
import NewAlertPage from './wizard/pages/NewAlertPage';
import UpdateAlertPage from './wizard/pages/UpdateAlertPage';
import ExportAlertPage from './wizard/pages/ExportAlertPage';
import ImportAlertPage from './wizard/pages/ImportAlertPage';
import WizardListsPage from './wizard/pages/WizardListsPage';
import NewAlertListPage from './wizard/pages/NewAlertListPage';
import UpdateListPage from './wizard/pages/UpdateListPage';
import ImportListPage from './wizard/pages/ImportListPage';
import ExportListPage from './wizard/pages/ExportListPage';
import WizardAlertConfig from './wizard/components/configuration/WizardAlertConfig';
import CreateRuleSearchAction from './wizard/searchActions/CreateRuleSearchAction';
import AggregationFieldValueProviderForm from './wizard/aggregationField/AggregationFieldValueProviderForm';
import { AGGREGATION_TYPE, AGGREGATION_DEFAULT_CONFIG, AGGREGATION_REQUIRED_FIELDS } from './wizard/aggregationField/AggregationFieldValueProviderForm';
import AggregationFieldValueProviderSummary from './wizard/aggregationField/AggregationFieldValueProviderSummary';

// TODO: think about it, but it seems alerts and lists are two entirely different "realms". If so, split their code in two distinct namespace
PluginStore.register(new PluginManifest(packageJson, {

    routes: [
        {path: '/wizard/AlertRules', component: WizardPage, permissions: 'WIZARD_ALERTS_RULES_READ'},
        {path: '/wizard/NewAlert', component: NewAlertPage, permissions: 'WIZARD_ALERTS_RULES_CREATE'},
        {path: '/wizard/UpdateAlert/:alertId', component: UpdateAlertPage, permissions: 'WIZARD_ALERTS_RULES_UPDATE'},
        {path: '/wizard/ExportAlert', component: ExportAlertPage, permissions: 'WIZARD_ALERTS_RULES_READ'},
        {path: '/wizard/ImportAlert', component: ImportAlertPage, permissions: 'WIZARD_ALERTS_RULES_READ'},
        {path: '/wizard/Lists', component: WizardListsPage},
        {path: '/wizard/NewList', component: NewAlertListPage},
        {path: '/wizard/UpdateList/:alertListTitle', component: UpdateListPage},
        {path: '/wizard/ImportList', component: ImportListPage},
        {path: '/wizard/ExportList', component: ExportListPage},
    ],

    navigation: [
        {
            description: 'Wizard',
            children: [
                { path: appPrefixed('/wizard/AlertRules'), description: 'Alert Rules' },
                { path: appPrefixed('/wizard/Lists'), description: 'Lists' },
            ],
        },
    ],

    systemConfigurations: [
        {
            component: WizardAlertConfig,
            displayName: 'Alert Wizard',
            configType: 'com.airbus_cyber_security.graylog.wizard.config.rest.AlertWizardConfig'
        },
    ],

    fieldValueProviders: [
        {
            type: AGGREGATION_TYPE,
            displayName: 'Aggregation Id Field',
            formComponent: AggregationFieldValueProviderForm,
            summaryComponent: AggregationFieldValueProviderSummary,
            defaultConfig: AGGREGATION_DEFAULT_CONFIG,
            requiredFields: AGGREGATION_REQUIRED_FIELDS,
        }
    ],

    'views.components.searchActions': [
        {
            component: CreateRuleSearchAction,
            key: 'com.airbus_cyber_security.graylog.wizard.create.rule.search.action'
        }
    ]
}));
