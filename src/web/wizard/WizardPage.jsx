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

import React from 'react';
import Reflux from "reflux";
import createReactClass from 'create-react-class';
import {Col, Row} from 'react-bootstrap';
import {IfPermitted, PageHeader, Spinner, DocumentTitle} from 'components/common';
import AlertRuleList from './AlertRuleList';
import ManageSettings from './ManageSettings';
import {addLocaleData, IntlProvider, FormattedMessage} from 'react-intl';
import messages_fr from '../translations/fr.json';
import WizardConfigurationsActions from '../config/WizardConfigurationsActions';
import WizardConfigurationStore from "../config/WizardConfigurationsStore";
import StoreProvider from 'injection/StoreProvider';
import ActionsProvider from 'injection/ActionsProvider';
const PluginsStore = StoreProvider.getStore('Plugins');
const NodesActions = ActionsProvider.getActions('Nodes');

let frLocaleData = require('react-intl/locale-data/fr');
const language = navigator.language.split(/[-_]/)[0];
addLocaleData(frLocaleData);

const messages = {
            'fr': messages_fr
        };

const WizardPage = createReactClass({
    displayName: 'WizardPage',

    mixins: [Reflux.connect(WizardConfigurationStore)],

    getInitialState() {
        return {
            configurations: null,
            version: '',
        };
    },

    componentDidMount() {
        WizardConfigurationsActions.list();
        NodesActions.list().then(nodes => {
            if(nodes.nodes[0]) {
                PluginsStore.list(nodes.nodes[0].node_id).then((plugins) => {
                    for (let i = 0; i < plugins.length; i++) {
                        if (plugins[i].unique_id === "com.airbus-cyber-security.graylog.AlertWizardPlugin") {
                            this.setState({version: plugins[i].version});
                        }
                    }
                });
            }
        });
    },

    _saveConfig(config) {
        WizardConfigurationsActions.update(config);
    },

    _getConfig() {
        if (this.state.configurations) {
            return this.state.configurations;
        }
        return {
            field_order: [{name: 'Severity', enabled: true}, 
                          {name: 'Description', enabled: true}, 
                          {name: 'Created', enabled: true},
                          {name: 'Last Modified', enabled: true},
                          {name: 'User', enabled: true}, 
                          {name: 'Alerts', enabled: true},
                          {name: 'Status', enabled: true},
                          {name: 'Rule', enabled: false}],
            default_values: {
                title: "",
                severity: "",
                matching_type: "",
                threshold_type: "",
                threshold: 0,
                time: 1,
                time_type: 0,
                field: "",
                field_type: 0,
                field_value: "",
                grace: 1,
                backlog: 500
            },
            import_policy: "DONOTHING"
        };
    },
    
    render() {
        if(!this.state.plugins && this.state.nodes){
            this._getPlugins();
        }
        const configWizard = this._getConfig();

        return (
        <IntlProvider locale={language} messages={messages[language]}>         
            <DocumentTitle title="Alert Rules">
                <div>
                    <PageHeader title={<FormattedMessage id="wizard.alertsRule" defaultMessage= "Alert Rules" />}>
                      <span><FormattedMessage id ="wizard.description" 
                            defaultMessage="With the wizard, you can manage the alert rules. An alert rule consists of one or more streams with rules, an alert condition and an alert notification." 
                            />
                      </span>
                      <span>
                            <FormattedMessage id ="wizard.documentation" 
                            defaultMessage="Read more about Wizard alert rules in the documentation" />
                          {this.state.version &&
                          <FormattedMessage id="wizard.version" defaultMessage=" (wizard version : {version})."
                                            values={{version: this.state.version}}/>
                          }
                      </span>
                      <span>
                        <IfPermitted permissions="wizard_alerts_rules:read">
                          <ManageSettings config={configWizard} onSave={this._saveConfig}/>
                        </IfPermitted>
                      </span>
                    </PageHeader>
                    
                    <Row className="content">
                      <Col md={12}>
                        <IfPermitted permissions="wizard_alerts_rules:read">
                          <AlertRuleList config={configWizard}/>
                        </IfPermitted>
                      </Col>
                    </Row>
                </div>
            </DocumentTitle>
        </IntlProvider>
        );
    },
});

export default WizardPage;
