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
import { Col, Row } from 'components/bootstrap';
import { IfPermitted, PageHeader, DocumentTitle, Spinner } from 'components/common';
import AlertRuleList from 'wizard/components/rules/AlertRuleList';
import ManageSettings from 'wizard/components/ManageSettings';
import { IntlProvider, FormattedMessage } from 'react-intl';
import packageJson from '../../../../package.json';
import messages_fr from 'translations/fr.json';
import WizardConfigurationResource from 'wizard/resources/WizardConfigurationResource';

const language = navigator.language.split(/[-_]/)[0];

const messages = {
    'fr': messages_fr
};

const WizardPage = () => {
    const [configuration, setConfiguration] = useState(null);

    const _saveConfiguration = configuration => {
        WizardConfigurationResource.update(configuration).then(() => setConfiguration(configuration));
    };

    useEffect(() => {
        WizardConfigurationResource.get().then(configuration => {
            setConfiguration(configuration);
        });
    }, []);

    if (!configuration) {
        return <Spinner text="Loading, please wait..." />;
    }

    return (
        <IntlProvider locale={language} messages={messages[language]}>
            <DocumentTitle title="Alert Rules">
                <PageHeader title={<FormattedMessage id="wizard.alertsRule" defaultMessage= "Alert Rules" />}>
                    <span>
                    <FormattedMessage id ="wizard.description"
                        defaultMessage="With the wizard, you can manage the alert rules. An alert rule consists of one or more streams with rules, an alert condition and an alert notification. "
                        />
                    </span>
                    <span>
                        <FormattedMessage id ="wizard.documentation"
                          defaultMessage="Read more about Wizard alert rules in the documentation" />
                        <FormattedMessage id="wizard.version" defaultMessage=" (wizard version : {version})."
                                          values={{version: packageJson.version}}/>
                    </span>
                </PageHeader>

                <Row className="content">
                    <Col md={12}>
                        <IfPermitted permissions="wizard_alerts_rules:read">
                            <AlertRuleList field_order={configuration.field_order} />
                        </IfPermitted>
                    </Col>
                </Row>
            </DocumentTitle>
        </IntlProvider>
    );
}

export default WizardPage;
