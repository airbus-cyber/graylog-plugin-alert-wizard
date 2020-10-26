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
import createReactClass from 'create-react-class';
import {Input} from 'components/bootstrap';
import { Row, Col } from 'components/graylog';
import {FormattedMessage} from 'react-intl';

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
