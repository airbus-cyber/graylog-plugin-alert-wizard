import PropTypes from 'prop-types';
import React from 'react';
import Reflux from 'reflux';
import createReactClass from 'create-react-class';
import {Button, Col, Row} from 'react-bootstrap';
import {LinkContainer} from 'react-router-bootstrap';
import {DocumentTitle, PageHeader, Spinner} from 'components/common';
import StoreProvider from 'injection/StoreProvider';
import Routes from 'routing/Routes';
import ActionsProvider from 'injection/ActionsProvider';
import {addLocaleData, IntlProvider, FormattedMessage} from 'react-intl';
import messages_fr from '../../translations/fr.json';
import {Input} from 'components/bootstrap';
import AlertListForm from "./AlertListForm";
import WizardStyle from "../WizardStyle.css";
import CreateAlertFormInput from "./CreateAlertFormInput";

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
        // affiche la page pour créer les nouvelles listes d'alertes
      /*  if (this.state.configuration && this.state.configuration[this.WIZARD_CLUSTER_CONFIG]) {
            return this.state.configuration[this.WIZARD_CLUSTER_CONFIG]
        }
        return {
            default_values: {
                title: '',
                severity: '',
                matching_type: '',
                threshold_type: '',
                threshold: 0,
                time: 0,
                time_type: 1,
                field: '',
                field_type: '',
                field_value: ''
            },
        }; */
    },

    _isLoading() {
        return !this.state.configuration;
    },

    render() {

        return (
            <IntlProvider locale={language} messages={messages[language]}>
                <DocumentTitle title="New alert rule">
                    <div>
                        <PageHeader title={<FormattedMessage id= "wizard.newAlertList" defaultMessage= "Wizard: New alert list" />}>
                        <span>
                            <FormattedMessage id= "wizard.define" defaultMessage= "You can define an alert list." />
                        </span>
                            <span>
                            <FormattedMessage id="wizard.documentation"
                                              defaultMessage= "Read more about Wizard alert list in the documentation." />
                        </span>
                            <span>
                            <LinkContainer to={Routes.pluginRoute('WIZARD_LISTS')}>
                                <Button bsStyle="info"><FormattedMessage id= "wizard.back" defaultMessage= "Back to alert lists" /></Button>
                            </LinkContainer>
                                &nbsp;
                        </span>
                        </PageHeader>
                        <Row className="content">
                            <Col md={12}>
                               <CreateAlertFormInput/>
                            </Col>
                        </Row>
                    </div>
                </DocumentTitle>
            </IntlProvider>
        );
    },
});

export default NewAlertListPage;
