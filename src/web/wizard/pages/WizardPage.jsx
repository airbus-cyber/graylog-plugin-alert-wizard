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
import { LinkContainer } from 'react-router-bootstrap';
import Routes from 'routing/Routes';
import { Col, Row, Button } from 'components/bootstrap';
import { IfPermitted, PageHeader, DocumentTitle, Spinner } from 'components/common';
import { IntlProvider, FormattedMessage } from 'react-intl';
import packageJson from '../../../../package.json';
import messages_fr from 'translations/fr.json';
import WizardConfigurationResource from 'wizard/resources/WizardConfigurationResource';
import AlertRulesContainer from "../components/rules/AlertRulesContainer";


const language = navigator.language.split(/[-_]/)[0];
const messages = {
    'fr': messages_fr
};

const WizardPage = () => {
    const [configuration, setConfiguration] = useState(null);

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
                <PageHeader title={<FormattedMessage id="wizard.alertsRule" defaultMessage= "Alert Rules" />} subpage={false} actions={(
                    <IfPermitted permissions="wizard_alerts_rules:read">
                        <div style={{display: 'flex', alignItems: 'flex-end', columnGap: '2px'}}>
                            <LinkContainer to={Routes.pluginRoute('WIZARD_NEWALERT')}>
                                <Button bsStyle="success" type="submit">
                                    <FormattedMessage id="wizard.create" defaultMessage="Create" />
                                </Button>
                            </LinkContainer>
                            <LinkContainer to={Routes.pluginRoute('WIZARD_IMPORTALERT')}>
                                <Button bsStyle="success" type="submit">
                                    <FormattedMessage id="wizard.import" defaultMessage="Import" />
                                </Button>
                            </LinkContainer>
                            <LinkContainer to={Routes.pluginRoute('WIZARD_EXPORTALERT')}>
                                <Button bsStyle="success" type="submit">
                                    <FormattedMessage id="wizard.export" defaultMessage="Export" />
                                </Button>
                            </LinkContainer>
                        </div>
                    </IfPermitted>
                )}>
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
                            <AlertRulesContainer fieldOrder={configuration.field_order} />
                        </IfPermitted>
                    </Col>
                </Row>
            </DocumentTitle>
        </IntlProvider>
    );
}

export default WizardPage;
