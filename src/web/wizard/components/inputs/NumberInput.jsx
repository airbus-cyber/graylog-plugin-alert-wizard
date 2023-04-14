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
import {FormattedMessage} from 'react-intl';
import { Input, Row, Col } from 'components/bootstrap';
import { Select } from 'components/common';

const NumberInput = createReactClass({
    displayName: 'NumberInput',

    propTypes: {
        onUpdate: PropTypes.func,
    },   
    getDefaultProps() {
        return {
            threshold_type: '',
            threshold: '',
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
    // TODO should factor constants with ManageSettings
    _availableThresholdTypes() {
        return [
            {value: '>', label: <FormattedMessage id= "wizard.more" defaultMessage= "more than" />},
            {value: '<', label: <FormattedMessage id= "wizard.less" defaultMessage= "less than" />},
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
                    <label className="pull-right"><FormattedMessage id= "wizard.NumberInput" defaultMessage= "Count Condition" /></label>
                </Col>
                <Col md={10}>
                    <label><FormattedMessage id= "wizard.thereMustBe" defaultMessage= "There must be " /></label>
                    <Input ref="threshold_type" id="threshold_type" name="threshold_type" required>
                        <div style={{width:'150px'}}>
                        <Select
                            required
                            clearable={false}
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

export default NumberInput;
