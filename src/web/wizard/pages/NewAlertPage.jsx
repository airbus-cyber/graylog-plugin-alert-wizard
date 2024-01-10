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

// sources of inspiration:
// * components/lookup-tables/CacheForm.tsx for useHistory

import React from 'react';
import { useState, useEffect } from 'react';
import { Button, Col, Row } from 'components/bootstrap';
import { LinkContainer } from 'react-router-bootstrap';
import { IntlProvider, FormattedMessage } from 'react-intl';
import messages_fr from 'translations/fr.json';
import { DocumentTitle, PageHeader, Spinner } from 'components/common';
import useHistory from 'routing/useHistory';
import AlertRuleForm from 'wizard/components/rules/AlertRuleForm';
import AlertRuleResource from 'wizard/resources/AlertRuleResource';
import WizardConfigurationResource from 'wizard/resources/WizardConfigurationResource';
import Navigation from 'wizard/routing/Navigation';

const language = navigator.language.split(/[-_]/)[0];

const messages = {
    'fr': messages_fr
};

const NewAlertPage = () => {
    const [configuration, setConfiguration] = useState(null);
    const history = useHistory();

    useEffect(() => {
        WizardConfigurationResource.get().then(configuration => {
            setConfiguration(configuration);
        });
    }, []);

    const _save = (alert) => {
        AlertRuleResource.create(alert).then(response => {
            if (response !== true) {
                return;
            }
            history.push(Navigation.getWizardRoute());
        });
    }

    let componentCreateAlertRule;
    if (configuration) {
        const default_values = configuration.default_values;
        const time = default_values.time;
        const time_type = default_values.time_type;
        const alert = {
            title: default_values.title,
            description: '',
            severity: default_values.severity,
            condition_type: 'COUNT',
            condition_parameters: {
                threshold_type: default_values.threshold_type,
                additional_threshold_type: default_values.threshold_type,
                threshold: default_values.threshold,
                additional_threshold: 0,
                time: time * time_type,
                repeat_notifications: default_values.repeat_notifications,
                grace: default_values.grace,
                backlog: default_values.backlog,
                grouping_fields: [],
                distinct_by: '',
                field: '',
                type: ''
            },
            stream: {
                matching_type: default_values.matching_type,
                field_rule: [{field: '', type: '', value: ''}],
            },
            second_stream: {
                matching_type: default_values.matching_type,
                field_rule: [{field: '', type: '', value: ''}],
            }
        };
        componentCreateAlertRule = <AlertRuleForm alert={alert} onSave={_save} />;
    } else {
        componentCreateAlertRule = <Spinner/>;
    }

    return (
        <IntlProvider locale={language} messages={messages[language]}>
            <DocumentTitle title="New alert rule">
                <PageHeader title={<FormattedMessage id="wizard.newAlertRule" defaultMessage="Wizard: New alert rule" />}
                            actions={(
                                <LinkContainer to={Navigation.getWizardRoute()}>
                                    <Button bsStyle="info"><FormattedMessage id="wizard.back" defaultMessage="Back to alert rules" /></Button>
                                </LinkContainer>
                            )}>
                    <span>
                        <FormattedMessage id="wizard.define" defaultMessage="You can define an alert rule." />
                    </span>
                    <span>
                        <FormattedMessage id="wizard.documentation"
                        defaultMessage="Read more about Wizard alert rules in the documentation." />
                    </span>
                </PageHeader>
                <Row className="content">
                    <Col md={12}>
                        {componentCreateAlertRule}
                    </Col>
                </Row>
            </DocumentTitle>
        </IntlProvider>
    );
}

export default NewAlertPage;
