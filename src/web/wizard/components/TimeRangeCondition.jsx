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
import {Input} from 'components/bootstrap';
import {Select} from 'components/common';
import { Row, Col } from 'components/graylog';
import {FormattedMessage} from 'react-intl';

const TimeRangeCondition = createReactClass({
    displayName: 'TimeRangeCondition',

    propTypes: {
        onUpdate: PropTypes.func,
    },   
    getDefaultProps() {
        return {
            time: 1,
            time_type: 1,
        };
    },
    getInitialState() {       
        return {
            time: this.props.time,
            time_type: this.props.time_type,
        };
    },
    _availableTimeTypes() {
        return [
            {value: '1', label: <FormattedMessage id= "wizard.minutes" defaultMessage= "minutes" />},
            {value: '60', label: <FormattedMessage id= "wizard.hours" defaultMessage= "hours" />},
            {value: '1440', label: <FormattedMessage id= "wizard.days" defaultMessage= "days" />},
        ];
    },
    _onTimeSelect(value) {
        this.setState({time_type: parseInt(value)});
        this.props.onUpdate("time", parseInt(value) * this.state.time);
    },
    _onTimeChanged() {
        return e => {
            this.setState({time: e.target.value});
            this.props.onUpdate("time", e.target.value * this.state.time_type);
        };
    },

    render() {
        
        return (
            <Row>
                <Col md={2} style={{ marginTop: 5, marginBottom: 0 }}>
                    <label className="pull-right"><FormattedMessage id= "wizard.timeRangeCondition" defaultMessage= "Time Range Condition" /></label>
                </Col>
                <Col md={10}>
                    <label><FormattedMessage id= "wizard.messagesCome" defaultMessage= "Messages must come in the last" /></label>
                    <Input ref="time" id="time" name="time" type="number" min="1" onChange={this._onTimeChanged()} value={this.state.time}
                           style={{borderTopRightRadius: '0px', borderBottomRightRadius: '0px', height:'36px' , width:'100px'}} />
                    <Input ref="time_type" id="time_type" name="time_type" required className="form-control">
                        <div style={{width:'150px'}}>
                        <Select
                            required
                            value={this.state.time_type.toString()}
                            options={this._availableTimeTypes()}
                            matchProp="value"
                            onChange={this._onTimeSelect}
                            placeholder={<FormattedMessage id= "wizard.select" defaultMessage= "Select..." />}
                        />
                        </div>
                    </Input>
                </Col>
            </Row>
        );
        
    },
});

export default TimeRangeCondition;
