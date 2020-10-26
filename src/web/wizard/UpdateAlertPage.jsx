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

import PropTypes from 'prop-types';
import React from 'react';
import createReactClass from 'create-react-class';
import {Button, Col, Row} from 'components/graylog';
import {LinkContainer} from 'react-router-bootstrap';
import {DocumentTitle, PageHeader, Spinner} from 'components/common';
import CreateAlertInput from './CreateAlertInput';
import AlertRuleActions from './AlertRuleActions';
import Routes from 'routing/Routes';
import {addLocaleData, IntlProvider, FormattedMessage} from 'react-intl';
import messages_fr from '../translations/fr.json';

let frLocaleData = require('react-intl/locale-data/fr');
const language = navigator.language.split(/[-_]/)[0];
addLocaleData(frLocaleData);

const messages = {
            'fr': messages_fr
        };

const UpdateAlertPage = createReactClass({
    displayName: 'UpdateAlertPage',

    getInitialState() {
        return {
            alert: null,
            alertData: null,
        };
    },
    propTypes() {
        return {
            params: PropTypes.object.isRequired,
        };
    },
    componentDidMount() {
        AlertRuleActions.get(this.props.params.alertRuleTitle).then(alert => {
            this.setState({alert: alert});
        });
        AlertRuleActions.getData(this.props.params.alertRuleTitle).then(alertData => {
            this.setState({alertData: alertData});
        });
    },
    _isLoading() {
        return !this.state.alert || !this.state.alertData;
    },

    render() {
        if (this._isLoading()) {
            return <Spinner/>;
        }

        return (
          <IntlProvider locale={language} messages={messages[language]}>
            <DocumentTitle title="Edit alert rule">
                <div>
                    <PageHeader title={<FormattedMessage id= "wizard.updateAlertRule" 
                            defaultMessage= 'Wizard: Editing alert rule "{title}"' 
                            values={{title: this.state.alert.title }} />} >
                        <span>
                            <FormattedMessage id= "wizard.define" defaultMessage= "You can define an alert rule." />
                        </span>
                        <span>
                            <FormattedMessage id="wizard.documentation" 
                            defaultMessage= "Read more about Wizard alert rules in the documentation." />
                        </span>
                        <span>
                            <LinkContainer to={Routes.pluginRoute('WIZARD_ALERTRULES')}>
                                <Button bsStyle="info"><FormattedMessage id= "wizard.back" defaultMessage= "Back to alert rules" /></Button>
                            </LinkContainer>
                            &nbsp;
                        </span>
                    </PageHeader>
                    <Row className="content">
                        <Col md={12}>
                            <CreateAlertInput create={false} alert={this.state.alertData}/>
                        </Col>
                    </Row>
                </div>
            </DocumentTitle>
           </IntlProvider>
        );
    },
});

export default UpdateAlertPage;
