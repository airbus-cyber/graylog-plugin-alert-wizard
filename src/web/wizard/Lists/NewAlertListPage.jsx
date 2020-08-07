import PropTypes from 'prop-types';
import React from 'react';
import Reflux from 'reflux';
import createReactClass from 'create-react-class';
import {Button, Col, Row} from 'components/graylog';
import {LinkContainer} from 'react-router-bootstrap';
import {DocumentTitle, PageHeader, Spinner} from 'components/common';
import StoreProvider from 'injection/StoreProvider';
import Routes from 'routing/Routes';
import ActionsProvider from 'injection/ActionsProvider';
import {addLocaleData, IntlProvider, FormattedMessage} from 'react-intl';
import messages_fr from '../../translations/fr.json';
import CreateListFormInput from "./CreateListFormInput";

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

const NewAlertListPage = createReactClass({
    displayName: 'NewAlertListPage',

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
                <DocumentTitle title="New list">
                    <div>
                        <PageHeader title={<FormattedMessage id= "wizard.newList" defaultMessage= "Wizard: New list" />}>
                        <span>
                            <FormattedMessage id= "wizard.definelist" defaultMessage= "You can define a list." />
                        </span>
                            <span>
                            <FormattedMessage id="wizard.documentationlist"
                                              defaultMessage= "Read more about Wizard list in the documentation." />
                        </span>
                            <span>
                            <LinkContainer to={Routes.pluginRoute('WIZARD_LISTS')}>
                                <Button bsStyle="info"><FormattedMessage id= "wizard.backlist" defaultMessage= "Back to lists" /></Button>
                            </LinkContainer>
                                &nbsp;
                        </span>
                        </PageHeader>
                        <Row className="content">
                            <Col md={12}>
                               <CreateListFormInput create={true} default_values={configWizard.default_values} nodes={this.state.nodes}/>
                            </Col>
                        </Row>
                    </div>
                </DocumentTitle>
            </IntlProvider>
        );
    },
});

export default NewAlertListPage;
