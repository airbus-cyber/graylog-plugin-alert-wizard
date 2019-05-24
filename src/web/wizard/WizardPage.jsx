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
        };
    },

    componentWillMount() {
        WizardConfigurationsActions.list();
    },

    _saveConfig(config) {
        WizardConfigurationsActions.update(config);
    },

    _isLoading() {
        return !this.state.configurations;
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
                time: 0,
                time_type: 0,
                field: "",
                field_type: 0,
                field_value: "",
                repeat_notifications: false,
                backlog: 1000,
                grace: 0
            },
            import_policy: "DONOTHING"
        };
    },
    
    render() {
        if (this._isLoading()) {
            return <Spinner/>;
        }

        const configWizard = this._getConfig();

        return (
        <IntlProvider locale={language} messages={messages[language]}>         
            <DocumentTitle title="Wizard">
                <div>
                    <PageHeader title="Wizard">
                      <span><FormattedMessage id ="wizard.description" 
                            defaultMessage="With the wizard, you can manage the alert rules. An alert rule consists of one or more streams with rules, an alert condition and an alert notification." 
                            />
                      </span>
                      <span>
                            <FormattedMessage id ="wizard.documentation" 
                            defaultMessage="Read more about Wizard alert rules in the documentation." />
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
