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
import { addLocaleData, IntlProvider, FormattedMessage } from 'react-intl';
import { LinkContainer } from 'react-router-bootstrap';
import { Row, Col, Button } from 'components/graylog';
import { DocumentTitle, PageHeader } from 'components/common';
import UserNotification from 'util/UserNotification';
import DateTime from 'logic/datetimes/DateTime';
import messages_fr from '../../translations/fr.json';
import AlertRuleActions from '../actions/AlertRuleActions';
import FileSaver from '../logic/FileSaver';
import IconDownload from '../components/icons/Download';
import Navigation from '../routing/Navigation';
import AlertRuleSelectionList from '../components/AlertRuleSelectionList'

let frLocaleData = require('react-intl/locale-data/fr');
const language = navigator.language.split(/[-_]/)[0];
addLocaleData(frLocaleData);

const messages = {
    'fr': messages_fr
};

const ExportAlertPage = createReactClass({
    displayName: 'ExportAlertPage',

    getInitialState() {
        return {
            selectedAlertTitles: new Set()
        };
    },
    componentDidMount() {
        AlertRuleActions.list().then(newAlerts => {
            this.setState({alertRules: newAlerts});
        });
    },
    isEmpty(obj) {
        return ((obj === undefined) || (typeof obj.count === 'function' ? obj.count() === 0 : obj.length === 0));
    },
    selectAllAlertRules() {
        const { alertRules } = this.state;

        const newSelection = new Set(alertRules.map(rule => rule.title));

        this.setState({ selectedAlertTitles: newSelection });
    },
    handleRuleSelect(event, title) {
        const { selectedAlertTitles } = this.state;
        if (event.target.checked) {
            selectedAlertTitles.add(title);
        } else {
            selectedAlertTitles.delete(title);
        }
        this.setState({ selectedAlertTitles: selectedAlertTitles });
    },
    onSubmit(evt) {
        evt.preventDefault();
        const request = {
          titles: Array.from(this.state.selectedAlertTitles),
        };
        AlertRuleActions.exportAlertRules(request).then((response) => {           
            UserNotification.success('Successfully export alert rules. Starting download...', 'Success!');  
            let date = DateTime.ignoreTZ(DateTime.now()).toString(DateTime.Formats.DATETIME).replace(/:/g, '').replace(/ /g, '_')
            FileSaver.save(response, date+'_alert_rules.json', 'application/json', 'utf-8');
        });
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
                                                        selectedAlertTitles={this.state.selectedAlertTitles}
                                                        handleRuleSelect={this.handleRuleSelect}
                                                        selectAllAlertRules={this.selectAllAlertRules}
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
