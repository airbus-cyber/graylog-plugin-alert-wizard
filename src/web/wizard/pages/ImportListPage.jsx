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
import Routes from 'routing/Routes';
import { IntlProvider, FormattedMessage } from 'react-intl';
import { LinkContainer } from 'react-router-bootstrap';
import { Row, Col, Button } from 'components/bootstrap';
import messages_fr from 'translations/fr.json';
import AlertListActions from 'wizard/actions/AlertListActions';
import { DocumentTitle, PageHeader } from 'components/common';

const language = navigator.language.split(/[-_]/)[0];

const messages = {
    'fr': messages_fr
};

const ImportListPage = createReactClass({
    displayName: 'ImportListPage',

    getInitialState() {
        return {};
    },

    onSubmitUploadFile(submitEvent) {
        submitEvent.preventDefault();
        if (!this.refs.uploadedFile.files || !this.refs.uploadedFile.files[0]) {
            return;
        }

        const reader = new FileReader();
        reader.onload = (evt) => {
            this.setState({alertLists: JSON.parse(evt.target.result)});
        };

        reader.readAsText(this.refs.uploadedFile.files[0]);
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
                <label className="checkbox"><input ref={`alertLists.${alertList.title}`} type="checkbox" name="alertLists" id={`alertList_${alertList.title}`} value={JSON.stringify(alertList)} />{alertList.title}</label>
                <span className="help-inline"><FormattedMessage id= "wizard.fieldDescription" defaultMessage= "Description" />: <tt>{alertList.description}</tt></span>
                <span className="help-inline"><FormattedMessage id= "wizard.fieldLists" defaultMessage= "Lists" />: <tt>{alertList.lists}</tt></span>
            </div>
        );
    },
    onSubmitApplyAlertLists(evt){
        evt.preventDefault();
        const request = [];

        Object.keys(this.refs).forEach((key, idx) => {
            if (key.indexOf('alertLists') === 0 && this.refs[key].checked === true) {
                request.push(JSON.parse(this.refs[key].value));
            }
        });

        AlertListActions.importAlertLists(request);
    },

    render() {

        return (
            <IntlProvider locale={language} messages={messages[language]}>
                <DocumentTitle title="Import list">
                    <div>
                        <PageHeader title={<FormattedMessage id= "wizard.importWizardList" defaultMessage= "Wizard: Import lists" />}
                                    actions={(
                                        <LinkContainer to={Routes.pluginRoute('WIZARD_LISTS')}>
                                            <Button bsStyle="info"><FormattedMessage id="wizard.backlist" defaultMessage= "Back to lists" /></Button>
                                        </LinkContainer>
                                    )}>
                            <span>
                                <FormattedMessage id= "wizard.importLists" defaultMessage= "You can import a list." />
                            </span>
                            <span>
                                <FormattedMessage id="wizard.documentationlist"
                                                  defaultMessage= "Read more about Wizard lists in the documentation." />
                            </span>
                        </PageHeader>
                        <Row className="content">
                            <Col md={12}>
                                <form onSubmit={this.onSubmitUploadFile} className="upload" encType="multipart/form-data">
                                    <div className="form-group">
                                        <input ref="uploadedFile" type="file" name="bundle" />
                                    </div>
                                    <button type="submit" className="btn btn-success">
                                        <FormattedMessage id="wizard.upload" defaultMessage= "Upload" />
                                    </button>
                                </form>
                            </Col>
                        </Row>
                        <Row className="content">
                            <Col md={6}>
                                <form className="form-horizontal build-content-pack" onSubmit={this.onSubmitApplyAlertLists}>
                                    <div className="form-group">
                                        <Col sm={2}>
                                            <label className="control-label" htmlFor="name">
                                                <FormattedMessage id ="wizard.alertsList" defaultMessage="Lists" />
                                            </label>
                                        </Col>
                                        <Col sm={10}>
                                            {this.isEmpty(this.state.alertLists) ?
                                                <span className="help-block help-standalone">
                                                    <FormattedMessage id ="wizard.noAlertListsToExport" defaultMessage="There is no list to import." />
                                                </span>
                                                :
                                                <span>
                                                  <Button className="btn btn-sm btn-link select-all" onClick={this.selectAllAlertLists}>
                                                      <FormattedMessage id ="wizard.selectAll" defaultMessage="Select all" />
                                                  </Button>
                                                    {this.state.alertLists.map(this.formatAlertList)}
                                                </span>
                                            }
                                        </Col>
                                    </div>
                                    <div className="form-group">
                                        <Col smOffset={2} sm={10}>
                                            <Button bsStyle="success" type="submit">
                                                <FormattedMessage id ="wizard.applyLists" defaultMessage="Apply lists" />
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

export default ImportListPage;
