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
import { LinkContainer } from 'react-router-bootstrap';
import { addLocaleData, IntlProvider, FormattedMessage } from 'react-intl';
import { DocumentTitle, PageHeader } from 'components/common';
import { Input } from 'components/bootstrap';
import { Row, Col, Button } from 'components/graylog';
import messages_fr from 'translations/fr.json';
import AlertRuleActions from 'wizard/actions/AlertRuleActions';
import Navigation from 'wizard/routing/Navigation';
import AlertRuleSelectionList from 'wizard/components/AlertRuleSelectionList'

let frLocaleData = require('react-intl/locale-data/fr');
const language = navigator.language.split(/[-_]/)[0];
addLocaleData(frLocaleData);

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

    normalizeImportedRules(rules) {
        const result = rules.map(function (rule) {
            let condition_parameters = { ...rule.condition_parameters };
            if (condition_parameters.type === 'MEAN') {
                condition_parameters.type = 'AVG';
            }
            return { ...rule, condition_parameters: condition_parameters };
        });
        return result;
    },

    onSubmitUploadFile(submitEvent) {
        submitEvent.preventDefault();
        if (!this.state.selectedFile) {
            return;
        }

        const reader = new FileReader();
        reader.onload = (evt) => {
            const importedRules = JSON.parse(evt.target.result);
            const rules = this.normalizeImportedRules(importedRules);
            this.setState({alertRules: rules});
        };
          
        reader.readAsText(this.state.selectedFile);
    },

    handleRuleSelectionChanged(selection) {
        this.setState({ selectedAlertTitles: selection })
    },

    onSubmitApplyAlertRules(evt) {
        evt.preventDefault();
        
        const { alertRules, selectedAlertTitles } = this.state;
        const request = alertRules.filter(alertRule => selectedAlertTitles.has(alertRule.title))

        AlertRuleActions.importAlertRules(request);
    },
    
    render() {
        const emptyMessage = <FormattedMessage id="wizard.noAlertRulesToImport" defaultMessage="There are no alert rules to import." />

        return (
            <IntlProvider locale={language} messages={messages[language]}>    
                <DocumentTitle title="Import alert rule">
                    <div>
                        <PageHeader title={<FormattedMessage id="wizard.importWizardAlertRule" defaultMessage="Wizard: Import alert rules" />}>
                            <span>
                                <FormattedMessage id="wizard.importAlertRule" defaultMessage="You can import an alert rule." />
                            </span>
                            <span>
                                <FormattedMessage id="wizard.documentation" 
                                defaultMessage= "Read more about Wizard alert rules in the documentation." />
                            </span>
                            <span>
                                <LinkContainer to={Navigation.getWizardRoute()}>
                                    <Button bsStyle="info"><FormattedMessage id="wizard.back" defaultMessage="Back to alert rules" /></Button>
                                </LinkContainer>
                                &nbsp;
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
