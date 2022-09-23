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

// TODO remove Condition suffix, rename namespace ruletype into rule-types

import PropTypes from 'prop-types';
import React from 'react';
import createReactClass from 'create-react-class';
import ObjectUtils from 'util/ObjectUtils';
import { FormattedMessage } from 'react-intl';
import TitleSeverity from 'wizard/components/TitleSeverity';
import FieldsCondition from 'wizard/components/conditions/FieldsCondition';
import NumberCondition from 'wizard/components/conditions/NumberCondition';
import TimeRangeCondition from 'wizard/components/conditions/TimeRangeCondition';
import Description from 'wizard/components/Description';
import { Row, Col } from 'components/bootstrap';

const STREAM = {
        matching_type: '',
        field_rule: [{field: '', type: '', value: ''}],
    };

const OrCondition = createReactClass({
    displayName: 'OrCondition',

    propTypes: {
        onUpdate: PropTypes.func,
    },
    getInitialState() {
        return {
            alert: ObjectUtils.clone(this.props.alert),
        };
    },
    _handleChangeCondition(field, value) {
        let update = ObjectUtils.clone(this.state.alert);
        if (field === "threshold" || field === "additional_threshold" || field === "time") {
            update.condition_parameters[field] = parseInt(value);
        } else {
            update.condition_parameters[field] = value;
        }
        this.setState({alert: update});
        this.props.onUpdate('condition_parameters', update.condition_parameters);
    },

    _handleChangeStream(field, value) {
        let update = ObjectUtils.clone(this.state.alert);
        update.stream[field] = value;
        this.setState({alert: update});
        this.props.onUpdate('stream', update.stream);
    },
    _handleChangeSecondStream(field, value) {
        let update = ObjectUtils.clone(this.state.alert);
        if(update.second_stream === null){
            update.second_stream = STREAM;
        }
        update.second_stream[field] = value;
        this.setState({alert: update});
        this.props.onUpdate('second_stream', update.second_stream);
    },
    
    render() {
        let time;
        let time_type;
        if (this.props.alert.condition_parameters.time >= 1440) {
            time = this.props.alert.condition_parameters.time / 1440;
            time_type = 1440;
        } else if (this.props.alert.condition_parameters.time >= 60) {
            time = this.props.alert.condition_parameters.time / 60;
            time_type = 60;
        } else {
            time = this.props.alert.condition_parameters.time;
            time_type = 1;
        }
        
        return (
            <>
                <div style={{ backgroundColor: '#f6f6f6', padding: '10px' }}>
                    <FieldsCondition stream={this.props.alert.stream} onSaveStream={this._handleChangeStream} message={this.props.message} 
                                    matchData={this.props.matchData} />
                </div>
                <br/>
                <Row style={{ marginBottom: '0px' }}><Col md={2} /><Col md={10}><label><FormattedMessage id= "wizard.or" defaultMessage= "OR" /></label></Col></Row>
                <br/>
                <div style={{ backgroundColor: '#f6f6f6', padding: '10px' }}>
                    <FieldsCondition stream={this.props.alert.second_stream} onSaveStream={this._handleChangeSecondStream} message={this.props.message} />
                </div>
                <br/>
                <NumberCondition onUpdate={this._handleChangeCondition} threshold={this.props.alert.condition_parameters.threshold} 
                                threshold_type={this.props.alert.condition_parameters.threshold_type} />
                <br/>
                <TimeRangeCondition onUpdate={this._handleChangeCondition} time={time.toString()} time_type={time_type.toString()} />
                <br/>
                <Description onUpdate={this.props.onUpdate} description={this.props.alert.description}/>
                <br/>
            </>
        );
        
    },
});

export default OrCondition;
