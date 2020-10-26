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
import {Select} from 'components/common';
import { Row, Col } from 'components/graylog';
import {FormattedMessage} from 'react-intl';

const NumberCondition = createReactClass({
    displayName: 'NumberCondition',

    propTypes: {
        onUpdate: PropTypes.func,
    },   
    getDefaultProps() {
        return {
            threshold_type:'',
            threshold:'',
        };
    },
    getInitialState() {
        return {
            threshold_type:this.props.threshold_type,
            threshold:this.props.threshold,
        };
    },
    componentWillReceiveProps(nextProps) {
        if(nextProps.threshold !== this.props.threshold){
            this.setState({threshold: nextProps.threshold});
        }
        if(nextProps.threshold_type !== this.props.threshold_type){
            this.setState({threshold_type: nextProps.threshold_type});
        }
    },
    _availableThresholdTypes() {
        return [
            {value: 'MORE', label: <FormattedMessage id= "wizard.more" defaultMessage= "more than" />},
            {value: 'LESS', label: <FormattedMessage id= "wizard.less" defaultMessage= "less than" />},
        ];
    },
    _onThresholdTypeSelect(id) {
        this.setState({threshold_type: id});
        this.props.onUpdate('threshold_type', id)
    },
    _onThresholdChanged() {
        return e => {
            this.setState({threshold: e.target.value});
            this.props.onUpdate('threshold', e.target.value);
        };
    },
    
    render() {

        return (
            <Row>
                <Col md={2} style={{ marginTop: 5, marginBottom: 0 }}>
                    <label className="pull-right"><FormattedMessage id= "wizard.NumberCondition" defaultMessage= "Count Condition" /></label>
                </Col>
                <Col md={10}>
                    <label><FormattedMessage id= "wizard.thereMustBe" defaultMessage= "There must be " /></label>
                    <Input ref="threshold_type" id="threshold_type" name="threshold_type" required>
                        <div style={{width:'150px'}}>
                        <Select
                            required
                            value={this.state.threshold_type}
                            options={this._availableThresholdTypes()}
                            matchProp="value"
                            onChange={this._onThresholdTypeSelect}
                            placeholder={<FormattedMessage id= "wizard.select" defaultMessage= "Select..." />}
                        />
                        </div>
                    </Input>
                    <Input ref="threshold" id="threshold" name="threshold" type="number" min="0" onChange={this._onThresholdChanged()}
                           value={this.state.threshold}
                           style={{borderTopLeftRadius: '0px', borderBottomLeftRadius: '0px', height:'36px', width:'100px'}} />
                    <label>&nbsp; </label>
                    <label><FormattedMessage id= "wizard.messages" defaultMessage= "messages" /></label>
                    
                </Col>
            </Row>
        );
        
    },
});

export default NumberCondition;
