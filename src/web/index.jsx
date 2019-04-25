import packageJson from '../../package.json';
import {PluginManifest, PluginStore} from 'graylog-web-plugin/plugin';
import WizardPage from 'wizard/WizardPage'
import NewAlertPage from 'wizard/NewAlertPage'
import UpdateAlertPage from 'wizard/UpdateAlertPage'
import ExportAlertPage from 'wizard/ExportAlertPage'
import ImportAlertPage from 'wizard/ImportAlertPage'

PluginStore.register(new PluginManifest(packageJson, {

    routes: [
        {path: '/wizard', component: WizardPage, permissions: 'WIZARD_ALERTS_RULES_READ'},
        {path: '/wizard/NewAlert', component: NewAlertPage, permissions: 'WIZARD_ALERTS_RULES_CREATE'},
        {path: '/wizard/UpdateAlert/:alertRuleTitle', component: UpdateAlertPage, permissions: 'WIZARD_ALERTS_RULES_UPDATE'},
        {path: '/wizard/ExportAlert', component: ExportAlertPage, permissions: 'WIZARD_ALERTS_RULES_READ'},
        {path: '/wizard/ImportAlert', component: ImportAlertPage, permissions: 'WIZARD_ALERTS_RULES_READ'},
    ],

    navigation: [
        {path: '/wizard', description: 'Wizard'}
    ],

}));
