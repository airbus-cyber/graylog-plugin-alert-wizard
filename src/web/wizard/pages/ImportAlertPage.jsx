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

// sources of inspiration for this page: components/event-notifications/event-notification-form/EventNotificationFormContainer.jsx
import React from 'react';
import createReactClass from 'create-react-class';
import { LinkContainer } from 'react-router-bootstrap';
import { IntlProvider, FormattedMessage } from 'react-intl';
import { DocumentTitle, PageHeader } from 'components/common';
import { Input, Row, Col, Button } from 'components/bootstrap';
import messages_fr from 'translations/fr.json';
import AlertRuleActions from 'wizard/actions/AlertRuleActions';
import Navigation from 'wizard/routing/Navigation';
import AlertRuleSelectionList from 'wizard/components/rules/AlertRuleSelectionList'
import RulesImportExport from 'wizard/logic/RulesImportExport'
import { EventNotificationsActions } from 'stores/event-notifications/EventNotificationsStore';

const language = navigator.language.split(/[-_]/)[0];

const messages = {
    'fr': messages_fr
};

// TODO should try to factor import code between this page and ImportListPage
const ImportAlertPage = createReactClass({
    displayName: 'ImportAlertPage',

    getInitialState() {
        return {
            selectedAlertTitles: new Set()
        };
    },

    onSelectUploadFile(event) {
        this.setState({selectedFile: event.target.files[0]})
    },

    onSubmitUploadFile(submitEvent) {
        submitEvent.preventDefault();
        if (!this.state.selectedFile) {
            return;
        }

        const reader = new FileReader();
        reader.onload = (evt) => {
            const importedRules = JSON.parse(evt.target.result);
            const rules = RulesImportExport.normalizeImportedRules(importedRules);
            this.setState({alertRules: rules});
        };

        reader.readAsText(this.state.selectedFile);
    },

    handleRuleSelectionChanged(selection) {
        this.setState({ selectedAlertTitles: selection });
    },

    async onSubmitApplyAlertRules(evt) {
        evt.preventDefault();
        
        const { alertRules, selectedAlertTitles } = this.state;
        const rules = alertRules.filter(alertRule => selectedAlertTitles.has(alertRule.title));

        for (const rule of rules) {
            // TODO should try to add a non-regression test for this quite involved import code
            //      import a rule which has notification with a split fields and check the split fields are present in the system
            //      => set up selenium tests ? :(
            await AlertRuleActions.create(rule);
                // TODO should not need to perform this get: create should return the information of the alert
            const alert = await AlertRuleActions.get(rule.title);
            const notification = {
                'config': {
                    ...rule.notification_parameters,
                    'type': 'logging-alert-notification'
                },
                'description': '',
                'id': alert.notification,
                'title': rule.title
            }
            EventNotificationsActions.update(alert.notification, notification);
        }
    },
    
    render() {
        const emptyMessage = <FormattedMessage id="wizard.noAlertRulesToImport" defaultMessage="There are no alert rules to import." />

        return (
            <IntlProvider locale={language} messages={messages[language]}>    
                <DocumentTitle title="Import alert rule">
                    <div>
                        <PageHeader title={<FormattedMessage id="wizard.importWizardAlertRule" defaultMessage="Wizard: Import alert rules" />}
                                    actions={(
                                        <LinkContainer to={Navigation.getWizardRoute()}>
                                            <Button bsStyle="info"><FormattedMessage id= "wizard.back" defaultMessage= "Back to alert rules" /></Button>
                                        </LinkContainer>
                                    )}>
                            <span>
                                <FormattedMessage id="wizard.importAlertRule" defaultMessage="You can import an alert rule." />
                            </span>
                            <span>
                                <FormattedMessage id="wizard.documentation"
                                                  defaultMessage= "Read more about Wizard alert rules in the documentation." />
                            </span>
                        </PageHeader>
                        <Row className="content"> 
                            <Col md={12}>
                                <form onSubmit={this.onSubmitUploadFile} className="upload" encType="multipart/form-data">
                                    <div className="form-group">
                                        <Input type="file" name="bundle" onChange={this.onSelectUploadFile} />
                                    </div>
                                    <button type="submit" className="btn btn-success">
                                        <FormattedMessage id="wizard.upload" defaultMessage="Upload" />
                                    </button>
                                </form>
                            </Col>
                        </Row>
                        <Row className="content">
                            <Col md={12}>
                                <AlertRuleSelectionList emptyMessage={emptyMessage}
                                                        alertRules={this.state.alertRules}
                                                        onRuleSelectionChanged={this.handleRuleSelectionChanged}
                                />
                                <Button bsStyle="success" onClick={this.onSubmitApplyAlertRules}>
                                    <FormattedMessage id="wizard.applyAlertRules" defaultMessage="Apply alert rules" />
                                </Button>
                            </Col>
                        </Row>        
                    </div>
                </DocumentTitle>   
            </IntlProvider>
        );        
    },
});

export default ImportAlertPage;
