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
// * pages/ConfigurationsPage.tsx
// * pages/NodesPage.jsx
import PropTypes from 'prop-types';
import React from 'react';
import { Button, Col, Row } from 'components/bootstrap';
import { LinkContainer } from 'react-router-bootstrap';
import { DocumentTitle, PageHeader } from 'components/common';
import Routes from 'routing/Routes';
import { IntlProvider, FormattedMessage } from 'react-intl';
import messages_fr from 'translations/fr.json';
import CreateListFormInput from 'wizard/components/lists/CreateListFormInput';
import AlertListActions from 'wizard/actions/AlertListActions';

const language = navigator.language.split(/[-_]/)[0];

const messages = {
    'fr': messages_fr
};

function _save(list) {
    AlertListActions.create(list);
}

const NewAlertListPage = () => {
    return (
        <IntlProvider locale={language} messages={messages[language]}>
            <DocumentTitle title="New list">
                <div>
                    <PageHeader title={<FormattedMessage id="wizard.newList" defaultMessage="Wizard: New list" />}>
                        <span>
                            <FormattedMessage id="wizard.definelist" defaultMessage="You can define a list." />
                        </span>
                        <span>
                            <FormattedMessage id="wizard.documentationlist"
                                              defaultMessage= "Read more about Wizard list in the documentation." />
                        </span>
                        <span>
                            <LinkContainer to={Routes.pluginRoute('WIZARD_LISTS')}>
                                <Button bsStyle="info"><FormattedMessage id="wizard.backlist" defaultMessage= "Back to lists" /></Button>
                            </LinkContainer>
                            &nbsp;
                        </span>
                    </PageHeader>
                    <Row className="content">
                        <Col md={12}>
                           <CreateListFormInput onSave={_save}/>
                        </Col>
                    </Row>
                </div>
            </DocumentTitle>
        </IntlProvider>
    );
};

export default NewAlertListPage;
