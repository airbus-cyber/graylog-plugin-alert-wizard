/*
 * graylog-plugin-alert-wizard Source Code
 * Copyright (C) 2018-2020 - Airbus CyberSecurity (SAS) - All rights reserved
 *
 * This file is part of the graylog-plugin-alert-wizard GPL Source Code.
 *
 * graylog-plugin-alert-wizard Source Code is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this code.  If not, see <http://www.gnu.org/licenses/>.
 */

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
import {addLocaleData, FormattedMessage, IntlProvider} from 'react-intl';
import messages_fr from '../../translations/fr.json';
import withParams from 'routing/withParams';

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
