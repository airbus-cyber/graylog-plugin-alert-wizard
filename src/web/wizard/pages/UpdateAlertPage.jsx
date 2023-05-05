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
import { Button, Col, Row } from 'components/bootstrap';
import { LinkContainer } from 'react-router-bootstrap';
import { DocumentTitle, PageHeader, Spinner } from 'components/common';
import AlertRuleForm from 'wizard/components/rules/AlertRuleForm';
import AlertRuleActions from 'wizard/actions/AlertRuleActions';
import { addLocaleData, IntlProvider, FormattedMessage } from 'react-intl';
import messages_fr from 'translations/fr.json';
import withParams from 'routing/withParams';
import Navigation from 'wizard/routing/Navigation';
import ButtonToEventDefinition from 'wizard/components/buttons/ButtonToEventDefinition';
import ButtonToNotification from 'wizard/components/buttons/ButtonToNotification';

let frLocaleData = require('react-intl/locale-data/fr');
const language = navigator.language.split(/[-_]/)[0];
addLocaleData(frLocaleData);

const messages = {
    'fr': messages_fr
};

const UpdateAlertPage = createReactClass({
    displayName: 'UpdateAlertPage',
    propTypes: {
        params: PropTypes.object.isRequired,
    },

    getInitialState() {
        return {
            alert: null,
            alertData: null,
        };
    },

    componentDidMount() {
        AlertRuleActions.get(this.props.params.alertRuleTitle).then(alert => {
            this.setState({alert: alert});
        });
        AlertRuleActions.get(this.props.params.alertRuleTitle).then(alertData => {
            this.setState({alertData: alertData});
        });
    },

    _isLoading() {
        return !this.state.alert || !this.state.alertData;
    },

    _update(alert) {
        AlertRuleActions.update.triggerPromise(this.state.alertData.title, alert).then((response) => {
            if (response !== true) {
                return;
            }
            Navigation.redirectToWizard();
        });
    },

    render() {
        if (this._isLoading()) {
            return <Spinner/>;
        }

        let navigationToRuleComponents = (
              <div className="alert-actions pull-right">
                  <ButtonToEventDefinition target={this.state.alert.condition} />{' '}
                  <ButtonToNotification target={this.state.alert.notification} />
              </div>
        );

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
                            <LinkContainer to={Navigation.getWizardRoute()}>
                                <Button bsStyle="info"><FormattedMessage id= "wizard.back" defaultMessage= "Back to alert rules" /></Button>
                            </LinkContainer>
                            &nbsp;
                        </span>
                    </PageHeader>
                    <Row className="content">
                        <Col md={12}>
                            <AlertRuleForm alert={this.state.alertData} navigationToRuleComponents={navigationToRuleComponents} onSave={this._update} />
                        </Col>
                    </Row>
                </div>
            </DocumentTitle>
           </IntlProvider>
        );
    },
});

export default withParams(UpdateAlertPage);
