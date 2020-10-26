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