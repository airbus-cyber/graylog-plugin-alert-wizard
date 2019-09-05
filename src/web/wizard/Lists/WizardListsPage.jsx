import React from 'react';
import createReactClass from 'create-react-class';
import messages_fr from "../../translations/fr";
import {IfPermitted, PageHeader, Spinner, DocumentTitle} from 'components/common';
import {addLocaleData, IntlProvider, FormattedMessage} from 'react-intl';
import {Button, Col, Row} from "react-bootstrap";
import AlertListDisplay from './AlertListDisplay';
import {LinkContainer} from "react-router-bootstrap";

let frLocaleData = require('react-intl/locale-data/fr');
const language = navigator.language.split(/[-_]/)[0];
addLocaleData(frLocaleData);

const messages = {
    'fr': messages_fr
};

const WizardListsPage = createReactClass ({
    displayName: 'WizardListsPage',

    render() {

        return (
            <IntlProvider locale={language} messages={messages[language]}>
                <DocumentTitle title="Lists">
                    <div>
                        <span>
                            <PageHeader id="wizard.lists" title='Lists'>
                                <span>
                                    <FormattedMessage id="wizard.documentation"
                                              defaultMessage= "Read more about Wizard alert list in the documentation." />
                                </span>
                            </PageHeader>
                        </span>

                        <Row className="content">
                            <Col md={12}>
                                <AlertListDisplay/>
                            </Col>
                        </Row>
                    </div>
                </DocumentTitle>
            </IntlProvider>
        );
    },
});

export default WizardListsPage;