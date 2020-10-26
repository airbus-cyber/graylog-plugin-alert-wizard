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

import packageJson from '../../package.json';
import {PluginManifest, PluginStore} from 'graylog-web-plugin/plugin';
import WizardPage from 'wizard/WizardPage';
import NewAlertPage from 'wizard/NewAlertPage';
import UpdateAlertPage from 'wizard/UpdateAlertPage';
import ExportAlertPage from 'wizard/ExportAlertPage';
import ImportAlertPage from 'wizard/ImportAlertPage';
import WizardListsPage from './wizard/Lists/WizardListsPage';
import NewAlertListPage from './wizard/Lists/NewAlertListPage';
import UpdateListPage from './wizard/Lists/UpdateListPage';
import ImportListPage from './wizard/Lists/ImportListPage';
import ExportListPage from './wizard/Lists/ExportListPage';


PluginStore.register(new PluginManifest(packageJson, {

    routes: [
        {path: '/wizard/AlertRules', component: WizardPage, permissions: 'WIZARD_ALERTS_RULES_READ'},
        {path: '/wizard/NewAlert', component: NewAlertPage, permissions: 'WIZARD_ALERTS_RULES_CREATE'},
        {path: '/wizard/UpdateAlert/:alertRuleTitle', component: UpdateAlertPage, permissions: 'WIZARD_ALERTS_RULES_UPDATE'},
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
                { path: '/wizard/AlertRules', description: 'Alert Rules' },
                { path: '/wizard/Lists', description: 'Lists' },
            ],
        },
    ],
}));