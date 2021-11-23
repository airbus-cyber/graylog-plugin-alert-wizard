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

import PropTypes from 'prop-types';
import React from 'react';
import createReactClass from 'create-react-class';
import {Button, Col, Row} from 'components/graylog';
import {LinkContainer} from 'react-router-bootstrap';
import {DocumentTitle, PageHeader, Spinner} from 'components/common';
import CreateAlertInput from '../components/CreateAlertInput';
import {addLocaleData, IntlProvider, FormattedMessage} from 'react-intl';
import messages_fr from '../../translations/fr.json';
import WizardConfigurationsActions from '../actions/WizardConfigurationsActions';
import ROUTES from '../routing/ROUTES';

let frLocaleData = require('react-intl/locale-data/fr');
const language = navigator.language.split(/[-_]/)[0];
addLocaleData(frLocaleData);

const messages = {
            'fr': messages_fr
        };

const NewAlertPage = createReactClass({
    displayName: 'NewAlertPage',

    getInitialState() {
        return {
            configurations: null,
        };
    },
    propTypes: {
        location: PropTypes.object.isRequired,
        params: PropTypes.object.isRequired,
        children: PropTypes.element,
    },

    componentDidMount() {
        WizardConfigurationsActions.list().then(configurations => {
            this.setState({configurations: configurations});
        });
    },

    _isConfigurationLoading() {
        return !this.state.configurations;
    },

    render() {

        let componentCreateAlertInput;
        if (this._isConfigurationLoading()) {
            componentCreateAlertInput = <Spinner/>;
        }else{
            componentCreateAlertInput = <CreateAlertInput create={true} default_values={this.state.configurations.default_values}/>;
        }

        return (
          <IntlProvider locale={language} messages={messages[language]}>
            <DocumentTitle title="New alert rule">
                <div>
                    <PageHeader title={<FormattedMessage id= "wizard.newAlertRule" defaultMessage= "Wizard: New alert rule" />}>
                        <span>
                            <FormattedMessage id= "wizard.define" defaultMessage= "You can define an alert rule." />
                        </span>
                        <span>
                            <FormattedMessage id="wizard.documentation" 
                            defaultMessage= "Read more about Wizard alert rules in the documentation." />
                        </span>
                        <span>
                            <LinkContainer to={ROUTES.WIZARD}>
                                <Button bsStyle="info"><FormattedMessage id= "wizard.back" defaultMessage= "Back to alert rules" /></Button>
                            </LinkContainer>
                            &nbsp;
                        </span>
                    </PageHeader>
                    <Row className="content">
                        <Col md={12}>
                            {componentCreateAlertInput}
                        </Col>
                    </Row>
                </div>
            </DocumentTitle>
          </IntlProvider>
        );
    },
});

export default NewAlertPage;
