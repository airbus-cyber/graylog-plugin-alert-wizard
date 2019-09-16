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
        {path: '/wizard', component: WizardPage, permissions: 'WIZARD_ALERTS_RULES_READ'},
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
                 { path: '/wizard', description: 'Alert Rules' },
                 { path: '/wizard/Lists', description: 'Lists' },
           ],
        },
     ],
}));
