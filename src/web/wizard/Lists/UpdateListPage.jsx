import PropTypes from 'prop-types';
import React from 'react';
import Reflux from 'reflux';
import createReactClass from 'create-react-class';
import {Button, Col, Row} from 'components/graylog';
import {LinkContainer} from 'react-router-bootstrap';
import {DocumentTitle, PageHeader, Spinner} from 'components/common';
import CreateListFormInput from './CreateListFormInput';
import AlertListActions from './AlertListActions';
import StoreProvider from 'injection/StoreProvider';
import Routes from 'routing/Routes';
import {addLocaleData, IntlProvider, FormattedMessage} from 'react-intl';
import messages_fr from '../../translations/fr.json';

const CurrentUserStore = StoreProvider.getStore('CurrentUser');
const NodesStore = StoreProvider.getStore('Nodes');

let frLocaleData = require('react-intl/locale-data/fr');
const language = navigator.language.split(/[-_]/)[0];
addLocaleData(frLocaleData);

const messages = {
    'fr': messages_fr
};

const UpdateListPage = createReactClass({
    displayName: 'UpdateListPage',

    mixins: [Reflux.connect(CurrentUserStore), Reflux.connect(NodesStore, 'nodes')],
    propTypes() {
        return {
            params: PropTypes.object.isRequired,
        };
    },
    componentDidMount() {
        AlertListActions.get(this.props.params.alertListTitle).then(list => {
            this.setState({list: list});
        });
    },
    _isLoading() {
        return !this.state.list;
    },

    render() {
        if (this._isLoading()) {
            return <Spinner/>;
        }

        return (
            <IntlProvider locale={language} messages={messages[language]}>
                <DocumentTitle title="Edit list">
                    <div>
                        <PageHeader title={<FormattedMessage id= "wizard.updateList"
                                                             defaultMessage= 'Wizard: Editing list "{title}"'
                                                             values={{title: this.state.list.title }} />} >
                        <span>
                            <FormattedMessage id= "wizard.definelist" defaultMessage= "You can define a list." />
                        </span>
                            <span>
                            <FormattedMessage id="wizard.documentationlist"
                                              defaultMessage= "Read more about Wizard lists in the documentation." />
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
                                <CreateListFormInput create={false} list={this.state.list} nodes={this.state.nodes}/>
                            </Col>
                        </Row>
                    </div>
                </DocumentTitle>
            </IntlProvider>
        );
    },
});

export default UpdateListPage;