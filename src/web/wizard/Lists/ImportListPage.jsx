import React from 'react';
import createReactClass from 'create-react-class';
import Routes from 'routing/Routes';
import {addLocaleData, IntlProvider, FormattedMessage} from 'react-intl';
import messages_fr from '../../translations/fr.json';
import { Row, Col, Button } from 'react-bootstrap';
import AlertListActions from './AlertListActions';
import {DocumentTitle, PageHeader, Spinner} from 'components/common';
import {LinkContainer} from 'react-router-bootstrap';

let frLocaleData = require('react-intl/locale-data/fr');
const language = navigator.language.split(/[-_]/)[0];
addLocaleData(frLocaleData);

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
                <DocumentTitle title="Import alert list">
                    <div>
                        <PageHeader title={<FormattedMessage id= "wizard.importWizardAlertList" defaultMessage= "Wizard: Import alert lists" />}>
                            <span>
                                <FormattedMessage id= "wizard.importList" defaultMessage= "You can import an alert list." />
                            </span>
                            <span>
                                <FormattedMessage id="wizard.documentationlist"
                                                  defaultMessage= "Read more about Wizard alert lists in the documentation." />
                            </span>
                            <span>
                                <LinkContainer to={Routes.pluginRoute('WIZARD_LISTS')}>
                                    <Button bsStyle="info"><FormattedMessage id= "wizard.backlist" defaultMessage= "Back to alert lists" /></Button>
                                </LinkContainer>
                                &nbsp;
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
                                                <FormattedMessage id ="wizard.alertsList" defaultMessage="Alert lists" />
                                            </label>
                                        </Col>
                                        <Col sm={10}>
                                            {this.isEmpty(this.state.alertLists) ?
                                                <span className="help-block help-standalone">
                                                    <FormattedMessage id ="wizard.noAlertListsToExport" defaultMessage="There is no alert list to import." />
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
                                                <FormattedMessage id ="wizard.applyAlertLists" defaultMessage="Apply alert lists" />
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
