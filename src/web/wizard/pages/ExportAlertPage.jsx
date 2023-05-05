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
import createReactClass from 'create-react-class';
import { IntlProvider, FormattedMessage } from 'react-intl';
import { LinkContainer } from 'react-router-bootstrap';
import { Row, Col, Button } from 'components/bootstrap';
import { DocumentTitle, PageHeader } from 'components/common';
import UserNotification from 'util/UserNotification';
import DateTime from 'logic/datetimes/DateTime';
import messages_fr from 'translations/fr.json';
import AlertRuleActions from 'wizard/actions/AlertRuleActions';
import FileSaver from 'wizard/logic/FileSaver';
import IconDownload from 'wizard/components/icons/Download';
import Navigation from 'wizard/routing/Navigation';
import AlertRuleSelectionList from 'wizard/components/rules/AlertRuleSelectionList'
import RulesImportExport from 'wizard/logic/RulesImportExport'
import { EventNotificationsActions } from 'stores/event-notifications/EventNotificationsStore';

const language = navigator.language.split(/[-_]/)[0];

const messages = {
    'fr': messages_fr
};

const ExportAlertPage = createReactClass({
    displayName: 'ExportAlertPage',

    getInitialState() {
        return {
            selectedAlertTitles: []
        };
    },

    componentDidMount() {
        AlertRuleActions.list().then(newAlerts => {
            this.setState({alertRules: newAlerts});
        });
    },

    handleRuleSelectionChanged(selection) {
        this.setState({ selectedAlertTitles: Array.from(selection) })
    },

    async onSubmit(evt) {
        evt.preventDefault();

        const alerts = [];
        for (const title of this.state.selectedAlertTitles) {
            const alert = await AlertRuleActions.get(title);
            const notification = await EventNotificationsActions.get(alert.notification);
            // TODO write a test that checks that the notification_parameters are present in the result (using mswjs or selenium?)
            alert.notification_parameters = notification.config;
            alerts.push(alert);
        }

        UserNotification.success('Successfully export alert rules. Starting download...', 'Success!');
        let exportData = RulesImportExport.createExportDataFromRules(alerts);
        let date = DateTime.ignoreTZ(DateTime.now()).toString(DateTime.Formats.DATETIME).replace(/:/g, '').replace(/ /g, '_');
        FileSaver.save(JSON.stringify(exportData), date+'_alert_rules.json', 'application/json', 'utf-8');
    },

    render() {
        const emptyMessage = <FormattedMessage id ="wizard.noAlertRulesToExport" defaultMessage="There are no alert rules to export." />

        // TODO should rather use ControlledTableList (see components/sidecar/administration/CollectorsAdministration)
        return (
            <IntlProvider locale={language} messages={messages[language]}> 
                <DocumentTitle title="Export alert rule">
                    <div>
                        <PageHeader title={<FormattedMessage id= "wizard.exportWizardAlertRule" defaultMessage= "Wizard: Export alert rules" />}>
                            <span>
                                <FormattedMessage id= "wizard.exportAlertRule" defaultMessage= "You can export an alert rule." />
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
                                <AlertRuleSelectionList emptyMessage={emptyMessage}
                                                        alertRules={this.state.alertRules}
                                                        onRuleSelectionChanged={this.handleRuleSelectionChanged}
                                />
                                <Button bsStyle="success" onClick={this.onSubmit}>
                                    <IconDownload/>
                                    <FormattedMessage id="wizard.downloadContentPack" defaultMessage="Download my content pack" />
                                </Button>
                            </Col>
                        </Row>
                    </div>
                </DocumentTitle>
            </IntlProvider>
        );
    },
});

export default ExportAlertPage;
