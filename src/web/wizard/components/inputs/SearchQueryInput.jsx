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
import createReactClass from 'create-react-class';
import { Input, Row, Col } from 'components/bootstrap';
import { FormattedMessage } from 'react-intl';

const SearchQueryInput = createReactClass({
    displayName: 'SearchQuery',

    propTypes: {
        onUpdate: PropTypes.func,
    },
    getDefaultProps() {
        return {
            search_query:'',
        };
    },
    getInitialState() {
        return {
            search_query:this.props.search_query,
        };
    },
    _onValueChanged(field) {
        return e => {
            this.props.onUpdate(field, e.target.value);
        };
    },

    render() {
        return (
            <Row style={{ display: 'flex', alignItems: 'center' }}>
                <Col md={2} style={{ marginTop: 5, marginBottom: 0 }}>
                    <label className="pull-right text-right" ><FormattedMessage id= "wizard.titleSearchQuery" defaultMessage= "Search Query" /></label>
                </Col>
                <Col md={10}>
                    <Input style={{borderTopRightRadius: '0px', borderBottomRightRadius: '0px', height:'36px', width:'600px'}}
                           id="search_query" name="search_query" type="text"
                           onChange={this._onValueChanged("search_query")}
                           defaultValue={this.state.search_query}/>
                </Col>
            </Row>
        );
    },
});

export default SearchQueryInput;
