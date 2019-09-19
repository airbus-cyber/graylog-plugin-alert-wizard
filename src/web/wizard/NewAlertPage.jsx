import PropTypes from 'prop-types';
import React from 'react';
import Reflux from 'reflux';
import createReactClass from 'create-react-class';
import {Button, Col, Row} from 'react-bootstrap';
import {LinkContainer} from 'react-router-bootstrap';
import {DocumentTitle, PageHeader, Spinner} from 'components/common';
import CreateAlertInput from './CreateAlertInput';
import StoreProvider from 'injection/StoreProvider';
import Routes from 'routing/Routes';
import ActionsProvider from 'injection/ActionsProvider';
import {addLocaleData, IntlProvider, FormattedMessage} from 'react-intl';
import messages_fr from '../translations/fr.json';

const CurrentUserStore = StoreProvider.getStore('CurrentUser');
const ConfigurationActions = ActionsProvider.getActions('Configuration');
const ConfigurationsStore = StoreProvider.getStore('Configurations');
const NodesStore = StoreProvider.getStore('Nodes');

let frLocaleData = require('react-intl/locale-data/fr');
const language = navigator.language.split(/[-_]/)[0];
addLocaleData(frLocaleData);

const messages = {
            'fr': messages_fr
        };

const NewAlertPage = createReactClass({
    displayName: 'NewAlertPage',

    mixins: [Reflux.connect(CurrentUserStore), Reflux.connect(ConfigurationsStore), Reflux.connect(NodesStore, 'nodes')],

    WIZARD_CLUSTER_CONFIG: 'com.airbus_cyber_security.graylog.config.rest.AlertWizardConfig',

    propTypes: {
        location: PropTypes.object.isRequired,
        params: PropTypes.object.isRequired,
        children: PropTypes.element,
    },

    componentDidMount() {
        ConfigurationActions.list(this.WIZARD_CLUSTER_CONFIG);
    },

    _getConfig() {
        if (this.state.configuration && this.state.configuration[this.WIZARD_CLUSTER_CONFIG]) {
            return this.state.configuration[this.WIZARD_CLUSTER_CONFIG]
        }
        return {
            default_values: {
                title: '',
                description: '',
                lists: '',
            },
        };
    },

    _isLoading() {
        return !this.state.configuration;
    },

    render() {
        if (this._isLoading()) {
            return <Spinner/>;
        }

        const configWizard = this._getConfig();

        return (
          <IntlProvider locale={language} messages={messages[language]}>
            <DocumentTitle title="New alert rule">
                <div>
                    <PageHeader title={<FormattedMessage id= "wizard.newAlertRule" defaultMessage= "Wizard: New alert rule" />}>
                        <span>
                            <FormattedMessage id= "wizard.define" defaultMessage= "You can define an alert rule." />
                        </span>
                        <span>
                            <FormattedMessage id="wizard.documentation" 
                            defaultMessage= "Read more about Wizard alert rules in the documentation." />
                        </span>
                        <span>
                            <LinkContainer to={Routes.pluginRoute('WIZARD_ALERTRULES')}>
                                <Button bsStyle="info"><FormattedMessage id= "wizard.back" defaultMessage= "Back to alert rules" /></Button>
                            </LinkContainer>
                            &nbsp;
                        </span>
                    </PageHeader>
                    <Row className="content">
                        <Col md={12}>
                            <CreateAlertInput create={true} default_values={configWizard.default_values} nodes={this.state.nodes}/>
                        </Col>
                    </Row>
                </div>
            </DocumentTitle>
          </IntlProvider>
        );
    },
});

export default NewAlertPage;
