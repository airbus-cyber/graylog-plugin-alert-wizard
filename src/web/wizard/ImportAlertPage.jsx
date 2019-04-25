import React from 'react';
import createReactClass from 'create-react-class';
import Routes from 'routing/Routes';
import {addLocaleData, IntlProvider, FormattedMessage} from 'react-intl';
import messages_fr from '../translations/fr.json';
import { Row, Col, Button } from 'react-bootstrap';
import AlertRuleActions from './AlertRuleActions';
import {DocumentTitle, PageHeader, Spinner} from 'components/common';
import {LinkContainer} from 'react-router-bootstrap';

let frLocaleData = require('react-intl/locale-data/fr');
const language = navigator.language.split(/[-_]/)[0];
addLocaleData(frLocaleData);

const messages = {
        'fr': messages_fr
    };

const ImportAlertPage = createReactClass({
    displayName: 'ImportAlertPage',

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
            this.setState({alertRules: JSON.parse(evt.target.result)});
        };
          
        reader.readAsText(this.refs.uploadedFile.files[0]);
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
            <label className="checkbox"><input ref={`alertRules.${alertRule.title}`} type="checkbox" name="alertRules" id={`alertRule_${alertRule.title}`} value={JSON.stringify(alertRule)} />{alertRule.title}</label>
            <span className="help-inline"><FormattedMessage id= "wizard.fieldDescription" defaultMessage= "Description" />: <tt>{alertRule.description}</tt></span>
          </div>
        );
    },
    onSubmitApplyAlertRules(evt){
        evt.preventDefault();
        const request = [];
        
        Object.keys(this.refs).forEach((key, idx) => {
          if (key.indexOf('alertRules') === 0 && this.refs[key].checked === true) {
            request.push(JSON.parse(this.refs[key].value));
          } 
        });
                
        AlertRuleActions.importAlertRules(request);
    },
    
    render() {
        
        return (
            <IntlProvider locale={language} messages={messages[language]}>    
                <DocumentTitle title="Import alert rule">
                    <div>
                        <PageHeader title={<FormattedMessage id= "wizard.importWizardAlertRule" defaultMessage= "Wizard: Import alert rules" />}>
                            <span>
                                <FormattedMessage id= "wizard.importAlertRule" defaultMessage= "You can import an alert rule." />
                            </span>
                            <span>
                                <FormattedMessage id="wizard.documentation" 
                                defaultMessage= "Read more about Wizard alert rules in the documentation." />
                            </span>
                            <span>
                                <LinkContainer to={Routes.pluginRoute('WIZARD')}>
                                    <Button bsStyle="info"><FormattedMessage id= "wizard.back" defaultMessage= "Back to alert rules" /></Button>
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
                                <form className="form-horizontal build-content-pack" onSubmit={this.onSubmitApplyAlertRules}>
                                    <div className="form-group">     
                                        <Col sm={2}>
                                            <label className="control-label" htmlFor="name">
                                                <FormattedMessage id ="wizard.alertsRule" defaultMessage="Alert rules" /> 
                                            </label>
                                        </Col>
                                        <Col sm={10}>
                                            {this.isEmpty(this.state.alertRules) ?
                                                <span className="help-block help-standalone">
                                                    <FormattedMessage id ="wizard.noAlertRulesToExport" defaultMessage="There is no alert rule to import." />
                                                </span>
                                                :
                                                <span>
                                                  <Button className="btn btn-sm btn-link select-all" onClick={this.selectAllAlertRules}>
                                                      <FormattedMessage id ="wizard.selectAll" defaultMessage="Select all" />
                                                  </Button>
                                                  {this.state.alertRules.map(this.formatAlertRule)}
                                                </span>
                                            }
                                        </Col>
                                    </div>
                                    <div className="form-group">
                                        <Col smOffset={2} sm={10}>
                                            <Button bsStyle="success" type="submit">
                                                <FormattedMessage id ="wizard.applyAlertRules" defaultMessage="Apply alert rules" />
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

export default ImportAlertPage;
