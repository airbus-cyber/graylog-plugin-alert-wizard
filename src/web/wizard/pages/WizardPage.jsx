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

// sources of inspiration for this code: 
// * pages/ShowNodePage.jsx
// * pages/ShowMessagePage.tsx
import React from 'react';
import Reflux from 'reflux';
import createReactClass from 'create-react-class';
import {Col, Row} from 'react-bootstrap';
import {IfPermitted, PageHeader, DocumentTitle} from 'components/common';
import AlertRuleList from 'wizard/components/AlertRuleList';
import ManageSettings from 'wizard/components/ManageSettings';
import {addLocaleData, IntlProvider, FormattedMessage} from 'react-intl';
import messages_fr from 'translations/fr.json';
import WizardConfigurationsActions from 'wizard/actions/WizardConfigurationsActions';
import WizardConfigurationStore from 'wizard/stores/WizardConfigurationsStore';
import PluginsStore from 'stores/plugins/PluginsStore';
import { NodesActions } from 'stores/nodes/NodesStore';

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
