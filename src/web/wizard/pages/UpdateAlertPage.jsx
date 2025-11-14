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
import { useState, useEffect } from 'react';
import { LinkContainer } from 'react-router-bootstrap';
import { IntlProvider, FormattedMessage } from 'react-intl';

import { Button, Col, Row } from 'components/bootstrap';
import { DocumentTitle, PageHeader, Spinner } from 'components/common';
import withParams from 'routing/withParams';
import useHistory from 'routing/useHistory';

import messages_fr from 'translations/fr.json';
import AlertRuleForm from 'wizard/components/rules/AlertRuleForm';
import AlertRuleActions from 'wizard/actions/AlertRuleActions';
import Navigation from 'wizard/routing/Navigation';
import ButtonToEventDefinition from 'wizard/components/buttons/ButtonToEventDefinition';
import ButtonToNotification from 'wizard/components/buttons/ButtonToNotification';
import generateIdentifier from 'wizard/logic/IdentifierSequence';


const language = navigator.language.split(/[-_]/)[0];

const messages = {
    'fr': messages_fr
};

const UpdateAlertPage = ({params}) => {
    const [alert, setAlert] = useState(null);
    const history = useHistory();

    useEffect(() => {
        AlertRuleActions.get(params.alertId).then(alert => {
            alert.stream.field_rule.forEach(rule => rule.identifier = generateIdentifier());
            if (alert.second_stream) {
                alert.second_stream.field_rule.forEach(rule => rule.identifier = generateIdentifier());
            }
            setAlert(alert);
        });
    }, []);

    const _update = (alert) => {
        // TODO simplify parameters here (only one necessary and the code in AlertRuleStore)
        AlertRuleActions.update(params.alertId, alert).then((response) => {
            if (response !== true) {
                return;
            }
            history.push(Navigation.getWizardRoute());
        });
    }

    if (!alert) {
        return <Spinner/>;
    }

    let navigationToRuleComponents = (
          <div className="pull-right">
              <ButtonToEventDefinition target={alert.condition} />{' '}
              <ButtonToNotification target={alert.notification} />
          </div>
    );

    return (
        <IntlProvider locale={language} messages={messages[language]}>
            <DocumentTitle title="Edit alert rule">
                    <PageHeader title={<FormattedMessage id="wizard.updateAlertRule" defaultMessage='Wizard: Editing alert rule "{title}"' values={{title: alert.title }} />}
                                actions={(
                                    <LinkContainer to={Navigation.getWizardRoute()}>
                                        <Button bsStyle="info"><FormattedMessage id= "wizard.back" defaultMessage= "Back to alert rules" /></Button>
                                    </LinkContainer>
                                )}>
                    <span>
                        <FormattedMessage id= "wizard.define" defaultMessage= "You can define an alert rule." />
                    </span>
                    <span>
                        <FormattedMessage id="wizard.documentation"
                                          defaultMessage= "Read more about Wizard alert rules in the documentation." />
                    </span>
                </PageHeader>
                <Row className="content">
                    <Col md={12}>
                        <AlertRuleForm alert={alert} navigationToRuleComponents={navigationToRuleComponents} onSave={_update} disableNavbar={true} />
                    </Col>
                </Row>
            </DocumentTitle>
        </IntlProvider>
    );
}

export default withParams(UpdateAlertPage);
