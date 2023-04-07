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
import { useState, useEffect } from 'react';
import { Col, Row } from 'react-bootstrap';
import { IfPermitted, PageHeader, DocumentTitle, Spinner } from 'components/common';
import AlertRuleList from 'wizard/components/AlertRuleList';
import ManageSettings from 'wizard/components/ManageSettings';
import { addLocaleData, IntlProvider, FormattedMessage } from 'react-intl';
import messages_fr from 'translations/fr.json';
import WizardConfigurationResource from 'wizard/resources/WizardConfigurationResource';
import { PluginsStore } from 'stores/plugins/PluginsStore';
import { NodesActions } from 'stores/nodes/NodesStore';

let frLocaleData = require('react-intl/locale-data/fr');
const language = navigator.language.split(/[-_]/)[0];
addLocaleData(frLocaleData);

const messages = {
    'fr': messages_fr
};

const WizardPage = () => {
    const [configuration, setConfiguration] = useState(null);
    const [version, setVersion] = useState('');

    const _saveConfiguration = configuration => {
        WizardConfigurationResource.update(configuration).then(() => setConfiguration(configuration));
    };

    useEffect(() => {
        WizardConfigurationResource.get().then(configuration => {
            setConfiguration(configuration);
        });

        // TODO this is an inefficient way to get the plugin version => would be better to inject it at build-time by retrieving it from package.json
        NodesActions.list().then(nodes => {
            if (!nodes.nodes[0]) {
                return
            }
            PluginsStore.list(nodes.nodes[0].node_id).then((plugins) => {
                for (let i = 0; i < plugins.length; i++) {
                    if (plugins[i].unique_id === "com.airbus-cyber-security.graylog.AlertWizardPlugin") {
                        setVersion(plugins[i].version);
                    }
                }
            });
        });
    }, []);

    if (!configuration) {
        return <Spinner text="Loading, please wait..." />;
    }

    // TODO write a test if <AlertRuleList config={configuration.field_order} /> instead of <AlertRuleList field_order={configuration.field_order} />
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
                          {version &&
                          <FormattedMessage id="wizard.version" defaultMessage=" (wizard version : {version})."
                                            values={{version: version}}/>
                          }
                      </span>
                      <span>
                        <IfPermitted permissions="wizard_alerts_rules:read">
                          <ManageSettings config={configuration} onSave={_saveConfiguration}/>
                        </IfPermitted>
                      </span>
                    </PageHeader>

                    <Row className="content">
                      <Col md={12}>
                        <IfPermitted permissions="wizard_alerts_rules:read">
                          <AlertRuleList field_order={configuration.field_order} />
                        </IfPermitted>
                      </Col>
                    </Row>
                </div>
            </DocumentTitle>
        </IntlProvider>
    );


}

export default WizardPage;
