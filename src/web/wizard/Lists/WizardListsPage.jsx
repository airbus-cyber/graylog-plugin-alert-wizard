import React from 'react';
import createReactClass from 'create-react-class';
import messages_fr from "../../translations/fr";
import {PageHeader, DocumentTitle} from 'components/common';
import {addLocaleData, IntlProvider, FormattedMessage} from 'react-intl';
import {Col, Row} from "react-bootstrap";
import AlertListDisplay from './AlertListDisplay';

let frLocaleData = require('react-intl/locale-data/fr');
const language = navigator.language.split(/[-_]/)[0];
addLocaleData(frLocaleData);

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
                            <PageHeader id="wizard.lists" title='Lists'>
                                <span>
                                    <FormattedMessage id="wizard.documentationlist"
                                              defaultMessage= "Read more about Wizard alert list in the documentation." />
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