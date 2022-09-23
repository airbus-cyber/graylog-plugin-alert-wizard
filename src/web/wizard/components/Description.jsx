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

const Description = createReactClass({
    displayName: 'Description',

    propTypes: {
        onUpdate: PropTypes.func,
    },   
    getDefaultProps() {
        return {
            description:'',
        };
    },
    getInitialState() {
        return {
            description:this.props.description,
        };
    },
    _onValueChanged(field) {
        return e => {
            this.props.onUpdate(field, e.target.value);
        };
    },
    
    render() {
        return (
            <Row>               
                <Col md={2} style={{ marginTop: 5, marginBottom: 0 }}>
                    <label className="pull-right" ><FormattedMessage id= "wizard.titleDescription" defaultMessage= "Description (optional)" /></label>
                </Col>
                <Col md={10}>    
                    <Input style={{minWidth: 600}} ref="description" id="description" name="description" type="textarea"
                                   onChange={this._onValueChanged("description")} defaultValue={this.state.description}/>
                </Col>
            </Row>
        );
    },
});

export default Description;
