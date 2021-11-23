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
import {addLocaleData, IntlProvider, FormattedMessage} from 'react-intl';
import messages_fr from '../../translations/fr.json';
import { Row, Col, Button } from 'components/graylog';
import AlertRuleActions from '../actions/AlertRuleActions';
import {DocumentTitle, PageHeader} from 'components/common';
import {LinkContainer} from 'react-router-bootstrap';
import FileSaver from '../logic/FileSaver';
import UserNotification from 'util/UserNotification';
import DateTime from 'logic/datetimes/DateTime';
import IconDownload from '../components/icons/Download';
import ROUTES from '../routing/ROUTES';

let frLocaleData = require('react-intl/locale-data/fr');
const language = navigator.language.split(/[-_]/)[0];
addLocaleData(frLocaleData);

const messages = {
        'fr': messages_fr
    };

const ExportAlertPage = createReactClass({
    displayName: 'ExportAlertPage',

    getInitialState() {
        return {};
    },
    componentDidMount() {
        AlertRuleActions.list().then(newAlerts => {
            this.setState({alertRules: newAlerts});
        });
    },
    isEmpty(obj) {
        return ((obj === undefined) || (typeof obj.count === 'function' ? obj.count() === 0 : obj.length === 0));
    },
    selectAllAlertRules(){
        Object.keys(this.refs).forEach((key) => {
            if (key.indexOf('alertRules') === 0) {
              this.refs[key].checked = true;
            }
          });
    },
    formatAlertRule(alertRule) {
        return (
          <div className="checkbox" key={`alertRule_checkbox-${alertRule.title}`}>
            <label className="checkbox"><input ref={`alertRules.${alertRule.title}`} type="checkbox" name="alertRules" id={`alertRule_${alertRule.title}`} value={alertRule.title} />{alertRule.title}</label>
            <span className="help-inline"><FormattedMessage id= "wizard.fieldDescription" defaultMessage= "Description" />: <tt>{alertRule.description}</tt></span>
          </div>
        );
    },
    onSubmit(evt) {
        evt.preventDefault();
        const request = {
          titles: [],
        };
        Object.keys(this.refs).forEach((key) => {
          if (key.indexOf('alertRules') === 0 && this.refs[key].checked === true) {
            request['titles'].push(this.refs[key].value);
          } 
        });
        
        AlertRuleActions.exportAlertRules(request).then((response) => {           
            UserNotification.success('Successfully export alert rules. Starting download...', 'Success!');  
            let date = DateTime.ignoreTZ(DateTime.now()).toString(DateTime.Formats.DATETIME).replace(/:/g, '').replace(/ /g, '_')
            FileSaver.save(response, date+'_alert_rules.json', 'application/json', 'utf-8');
        });
    },

    
    render() {

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
                                <LinkContainer to={ROUTES.WIZARD}>
                                    <Button bsStyle="info"><FormattedMessage id= "wizard.back" defaultMessage= "Back to alert rules" /></Button>
                                </LinkContainer>
                                &nbsp;
                            </span>
                        </PageHeader>
                        <Row className="content">
                            <Col md={6}>
                                <form className="form-horizontal build-content-pack" onSubmit={this.onSubmit}>
                                    <div className="form-group">     
                                        <Col sm={2}>
                                            <label className="control-label" htmlFor="name">
                                                <FormattedMessage id ="wizard.alertsRule" defaultMessage="Alert rules" /> 
                                            </label>
                                        </Col>
                                        <Col sm={10}>
                                            {this.isEmpty(this.state.alertRules) ?
                                                <span className="help-block help-standalone">
                                                    <FormattedMessage id ="wizard.noAlertRulesToExport" defaultMessage="There is no alert rule to export." />
                                                </span>
                                                :
                                                <span>
                                                  <Button className="btn btn-sm btn-link select-all" onClick={this.selectAllAlertRules}>
                                                      <FormattedMessage id ="wizard.selectAll" defaultMessage="Select all" />
                                                  </Button>
                                                  {this.state.alertRules.sort((i1, i2) => { return i1.title.localeCompare(i2.title); }).map(this.formatAlertRule)}
                                                </span>
                                            }
                                        </Col>
                                    </div>
                                    <div className="form-group">
                                        <Col smOffset={2} sm={10}>
                                            <Button bsStyle="success" type="submit">
                                                <IconDownload/>
                                                <FormattedMessage id ="wizard.downloadContentPack" defaultMessage="Download my content pack" />
                                            </Button>
                                        </Col>
                                    </div>
                                </form>
                            </Col>
                        </Row>
                    </div>
                </DocumentTitle>
            </IntlProvider>
        );
    },
});

export default ExportAlertPage;
