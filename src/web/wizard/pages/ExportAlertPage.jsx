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
import messages_fr from '../../translations/fr.json';
import { Row, Col, Button } from 'components/graylog';
import { DocumentTitle, PageHeader, SearchForm } from 'components/common';
import { Input } from 'components/bootstrap';
import AlertRuleActions from '../actions/AlertRuleActions';
import FileSaver from '../logic/FileSaver';
import UserNotification from 'util/UserNotification';
import DateTime from 'logic/datetimes/DateTime';
import IconDownload from '../components/icons/Download';
import Navigation from '../routing/Navigation';
import ControlledTableList from 'components/common/ControlledTableList';

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
            alertTitlesFilter: '',
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
    formatAlertRule(alertRule) {
        const { selectedAlertTitles } = this.state;
        return (
            <ControlledTableList.Item key={`alertRule_${alertRule.title}`}>
                <Input id={`alertRule_${alertRule.title}`}
                       type="checkbox"
                       checked={selectedAlertTitles.has(alertRule.title)}
                       onChange={event => this.handleRuleSelect(event, alertRule.title)}
                       label={alertRule.title} />
                <span className="description" style={{'margin-left': '20px'}}>{alertRule.description}</span>
            </ControlledTableList.Item>
        );
    },
    formatAlertRules() {
      return this.state.alertRules
                 .sort((rule1, rule2) => rule1.title.localeCompare(rule2.title))
                 .filter(rule => rule.title.includes(this.state.alertTitlesFilter))
                 .map(this.formatAlertRule);
    },
    onSearch(filter) {
        this.setState({ alertTitlesFilter: filter });
    },
    onReset() {
        this.setState({ alertTitlesFilter: '' });
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
                                <SearchForm onSearch={this.onSearch}
                                            onReset={this.onReset}
                                            searchButtonLabel="Filter"
                                            placeholder="Filter alert rules by title..."
                                            queryWidth={400}
                                            resetButtonLabel="Reset"
                                            searchBsStyle="info"
                                            topMargin={0} />

                                {this.isEmpty(this.state.alertRules) ?
                                    <span className="help-block help-standalone">
                                            <FormattedMessage id ="wizard.noAlertRulesToExport" defaultMessage="There are no alert rules to export." />
                                        </span>
                                    :
                                    <ControlledTableList>
                                        <ControlledTableList.Header>
                                            <Button className="btn btn-sm btn-link select-all" onClick={this.selectAllAlertRules}>
                                                <FormattedMessage id ="wizard.selectAll" defaultMessage="Select all" />
                                            </Button>
                                        </ControlledTableList.Header>
                                        {this.formatAlertRules()}
                                    </ControlledTableList>
                                }

                                <Col sm={10}>
                                    <Button bsStyle="success" onClick={this.onSubmit}>
                                        <IconDownload/>
                                        <FormattedMessage id ="wizard.downloadContentPack" defaultMessage="Download my content pack" />
                                    </Button>
                                </Col>
                            </Col>
                        </Row>
                    </div>
                </DocumentTitle>
            </IntlProvider>
        );
    },
});

export default ExportAlertPage;
