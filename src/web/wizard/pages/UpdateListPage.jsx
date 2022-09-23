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

import PropTypes from 'prop-types';
import React from 'react';
import Reflux from 'reflux';
import createReactClass from 'create-react-class';
import { Button, Col, Row } from 'components/bootstrap';
import { LinkContainer } from 'react-router-bootstrap';
import { DocumentTitle, PageHeader, Spinner } from 'components/common';
import CreateListFormInput from 'wizard/components/lists/CreateListFormInput';
import AlertListActions from 'wizard/actions/AlertListActions';
import Routes from 'routing/Routes';
import { addLocaleData, FormattedMessage, IntlProvider } from 'react-intl';
import messages_fr from 'translations/fr.json';
import withParams from 'routing/withParams';
import CurrentUserStore from 'stores/users/CurrentUserStore';
import NodesStore from 'stores/nodes/NodesStore';

let frLocaleData = require('react-intl/locale-data/fr');
const language = navigator.language.split(/[-_]/)[0];
addLocaleData(frLocaleData);

const messages = {
    'fr': messages_fr
};

const UpdateListPage = createReactClass({
    displayName: 'UpdateListPage',

    mixins: [Reflux.connect(CurrentUserStore), Reflux.connect(NodesStore, 'nodes')],
    propTypes: {
        params: PropTypes.object.isRequired,
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
                        <PageHeader title={<FormattedMessage id="wizard.updateList"
                                                             defaultMessage='Wizard: Editing list "{title}"'
                                                             values={{title: this.state.list.title}}/>}>
                        <span>
                            <FormattedMessage id="wizard.definelist" defaultMessage="You can define a list."/>
                        </span>
                            <span>
                            <FormattedMessage id="wizard.documentationlist"
                                              defaultMessage="Read more about Wizard lists in the documentation."/>
                        </span>
                            <span>
                            <LinkContainer to={Routes.pluginRoute('WIZARD_LISTS')}>
                                <Button bsStyle="info"><FormattedMessage id="wizard.backlist"
                                                                         defaultMessage="Back to lists"/></Button>
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

export default withParams(UpdateListPage);
