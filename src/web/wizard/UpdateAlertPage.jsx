import PropTypes from 'prop-types';
import React from 'react';
import Reflux from 'reflux';
import createReactClass from 'create-react-class';
import {Button, Col, Row} from 'react-bootstrap';
import {LinkContainer} from 'react-router-bootstrap';
import {DocumentTitle, PageHeader, Spinner} from 'components/common';
import CreateAlertInput from './CreateAlertInput';
import AlertRuleActions from './AlertRuleActions';
import StoreProvider from 'injection/StoreProvider';
import Routes from 'routing/Routes';
import {addLocaleData, IntlProvider, FormattedMessage} from 'react-intl';
import messages_fr from '../translations/fr.json';

const CurrentUserStore = StoreProvider.getStore('CurrentUser');
const NodesStore = StoreProvider.getStore('Nodes');

let frLocaleData = require('react-intl/locale-data/fr');
const language = navigator.language.split(/[-_]/)[0];
addLocaleData(frLocaleData);

const messages = {
            'fr': messages_fr
        };

const UpdateAlertPage = createReactClass({
    displayName: 'UpdateAlertPage',

    mixins: [Reflux.connect(CurrentUserStore), Reflux.connect(NodesStore, 'nodes')],
    propTypes() {
        return {
            params: PropTypes.object.isRequired,
        };
    },
    componentDidMount() {
        AlertRuleActions.get(this.props.params.alertRuleTitle).then(alert => {
            this.setState({alert: alert});
        });
        AlertRuleActions.getData(this.props.params.alertRuleTitle).then(alertData => {
            this.setState({alertData: alertData});
        });
    },
    _isLoading() {
        return !this.state.alert || !this.state.alertData;
    },

    render() {
        if (this._isLoading()) {
            return <Spinner/>;
        }

        return (
          <IntlProvider locale={language} messages={messages[language]}>
            <DocumentTitle title="Edit alert rule">
                <div>
                    <PageHeader title={<FormattedMessage id= "wizard.updateAlertRule" 
                            defaultMessage= 'Wizard: Editing alert rule "{title}"' 
                            values={{title: this.state.alert.title }} />} >
                        <span>
                            <FormattedMessage id= "wizard.define" defaultMessage= "You can define an alert rule." />
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
                            <CreateAlertInput create={false} alert={this.state.alertData} nodes={this.state.nodes}/>
                        </Col>
                    </Row>
                </div>
            </DocumentTitle>
           </IntlProvider>
        );
    },
});

export default UpdateAlertPage;
