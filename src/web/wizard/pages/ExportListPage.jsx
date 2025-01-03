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

// sources of inspiration for this code: 
// * views/components/views/MissingRequirements.tsx
import React from 'react';
import createReactClass from 'create-react-class';
import { IntlProvider, FormattedMessage } from 'react-intl';
import { LinkContainer } from 'react-router-bootstrap';
import messages_fr from 'translations/fr.json';
import { Row, Col, Button } from 'components/bootstrap';
import { DocumentTitle, PageHeader} from 'components/common';
import UserNotification from 'util/UserNotification';
import { adjustFormat } from 'util/DateTime';
import Routes from 'routing/Routes';
import AlertListActions from 'wizard/actions/AlertListActions';
import FileSaver from 'wizard/logic/FileSaver';
import IconDownload from 'wizard/components/icons/Download';

const language = navigator.language.split(/[-_]/)[0];

const messages = {
    'fr': messages_fr
};

const ExportListPage = createReactClass({
    displayName: 'ExportListPage',

    getInitialState() {
        return {};
    },
    componentDidMount() {
        AlertListActions.list().then(newLists => {
            this.setState({alertLists: newLists});
        });
    },
    isEmpty(obj) {
        return ((obj === undefined) || (typeof obj.count === 'function' ? obj.count() === 0 : obj.length === 0));
    },
    selectAllAlertLists(){
        Object.keys(this.refs).forEach((key) => {
            if (key.indexOf('alertLists') === 0) {
                this.refs[key].checked = true;
            }
        });
    },
    formatAlertList(alertList) {
        return (
            <div className="checkbox" key={`alertList_checkbox-${alertList.title}`}>
                <label className="checkbox"><input ref={`alertLists.${alertList.title}`} type="checkbox" name="alertLists" id={`alertList_${alertList.title}`} value={alertList.title} />{alertList.title}</label>
                <span className="help-inline"><FormattedMessage id= "wizard.fieldDescription" defaultMessage= "Description" />: <tt>{alertList.description}</tt></span>
                <span className="help-inline"><FormattedMessage id= "wizard.fieldLists" defaultMessage= "Lists" />: <tt>{alertList.lists}</tt></span>
            </div>
        );
    },
    onSubmit(evt) {
        evt.preventDefault();
        const request = {
            titles: [],
        };
        Object.keys(this.refs).forEach((key) => {
            if (key.indexOf('alertLists') === 0 && this.refs[key].checked === true) {
                request['titles'].push(this.refs[key].value);
            }
        });

        AlertListActions.exportAlertLists(request).then((response) => {
            UserNotification.success('Successfully export alert lists. Starting download...', 'Success!');
            // TODO factor with ExportAlertPage?
            let date = adjustFormat(new Date()).replace(/:/g, '').replace(/ /g, '_')
            FileSaver.save(response, date+'_alert_lists.json', 'application/json', 'utf-8');
        });
    },


    render() {

        return (
            <IntlProvider locale={language} messages={messages[language]}>
                <DocumentTitle title="Export list">
                    <div>
                        <PageHeader title={<FormattedMessage id= "wizard.exportWizardList" defaultMessage= "Wizard: Export lists" />}
                                    actions={(
                                        <LinkContainer to={Routes.pluginRoute('WIZARD_LISTS')}>
                                            <Button bsStyle="info"><FormattedMessage id="wizard.backlist" defaultMessage= "Back to lists" /></Button>
                                        </LinkContainer>
                                    )}>
                            <span>
                                <FormattedMessage id= "wizard.exportAlertList" defaultMessage= "You can export a list." />
                            </span>
                            <span>
                                <FormattedMessage id="wizard.documentationlist"
                                                  defaultMessage= "Read more about Wizard lists in the documentation." />
                            </span>
                        </PageHeader>
                        <Row className="content">
                            <Col md={6}>
                                <form className="form-horizontal build-content-pack" onSubmit={this.onSubmit}>
                                    <div className="form-group">
                                        <Col sm={2}>
                                            <label className="control-label" htmlFor="name">
                                                <FormattedMessage id ="wizard.alertsLists" defaultMessage="Lists" />
                                            </label>
                                        </Col>
                                        <Col sm={10}>
                                            {this.isEmpty(this.state.alertLists) ?
                                                <span className="help-block help-standalone">
                                                    <FormattedMessage id ="wizard.noListsToExport" defaultMessage="There is no lists to export." />
                                                </span>
                                                :
                                                <span>
                                                  <Button className="btn btn-sm btn-link select-all" onClick={this.selectAllAlertLists}>
                                                      <FormattedMessage id ="wizard.selectAll" defaultMessage="Select all" />
                                                  </Button>
                                                    {this.state.alertLists.sort((i1, i2) => { return i1.title.localeCompare(i2.title); }).map(this.formatAlertList)}
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

export default ExportListPage;
