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

import React from 'react';
import createReactClass from 'create-react-class';
import { IntlProvider, FormattedMessage } from 'react-intl';
import { Col, Row } from 'react-bootstrap';
import messages_fr from 'translations/fr.json';
import { PageHeader, DocumentTitle } from 'components/common';
import AlertListDisplay from 'wizard/components/lists/AlertListDisplay';

const language = navigator.language.split(/[-_]/)[0];

const messages = {
    'fr': messages_fr
};

const WizardListsPage = createReactClass ({
    displayName: 'WizardListsPage',

    _getConfig() {
        return {
            field_order: [{name: 'Description', enabled: true},
                {name: 'Created', enabled: true},
                {name: 'Last Modified', enabled: true},
                {name: 'User', enabled: true},
                {name: 'Usage', enabled: true},
                {name: 'Lists', enabled: true}],
            default_values: {
            },
            import_policy: "DONOTHING"
        };
    },

    render() {

        const configWizard = this._getConfig();

        return (
            <IntlProvider locale={language} messages={messages[language]}>
                <DocumentTitle title="Lists">
                    <div>
                        <span>
                            <PageHeader title={<FormattedMessage id="wizard.lists" defaultMessage= "Lists" />}>
                                <span>
                                    <FormattedMessage id="wizard.documentationlist"
                                              defaultMessage= "Read more about Wizard lists in the documentation." />
                                </span>
                            </PageHeader>
                        </span>

                        <Row className="content">
                            <Col md={12}>
                                <AlertListDisplay config={configWizard}/>
                            </Col>
                        </Row>
                    </div>
                </DocumentTitle>
            </IntlProvider>
        );
    },
});

export default WizardListsPage;