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
import { Button, Col, Row } from 'components/bootstrap';
import { LinkContainer } from 'react-router-bootstrap';
import { DocumentTitle, PageHeader, Spinner } from 'components/common';
import AlertRuleForm from 'wizard/components/rules/AlertRuleForm';
import AlertRuleActions from 'wizard/actions/AlertRuleActions';
import { IntlProvider, FormattedMessage } from 'react-intl';
import messages_fr from 'translations/fr.json';
import withParams from 'routing/withParams';
import useHistory from 'routing/useHistory';
import Navigation from 'wizard/routing/Navigation';
import ButtonToEventDefinition from 'wizard/components/buttons/ButtonToEventDefinition';
import ButtonToNotification from 'wizard/components/buttons/ButtonToNotification';

const language = navigator.language.split(/[-_]/)[0];

const messages = {
    'fr': messages_fr
};

const UpdateAlertPage = ({params}) => {
    const [alert, setAlert] = useState(null);
    const history = useHistory();

    useEffect(() => {
        AlertRuleActions.get(params.alertRuleTitle).then(alert => {
            setAlert(alert);
        });
    }, []);

    const _update = (alert) => {
        // TODO simplify parameters here (only one necessary and the code in AlertRuleStore)
        AlertRuleActions.update(alert.title, alert).then((response) => {
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
                <div>
                    <PageHeader title={<FormattedMessage id= "wizard.updateAlertRule"
                            defaultMessage= 'Wizard: Editing alert rule "{title}"'
                            values={{title: alert.title }} />} >
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
                            <AlertRuleForm alert={alert} navigationToRuleComponents={navigationToRuleComponents} onSave={_update} />
                        </Col>
                    </Row>
                </div>
            </DocumentTitle>
        </IntlProvider>
    );
}

export default withParams(UpdateAlertPage);
