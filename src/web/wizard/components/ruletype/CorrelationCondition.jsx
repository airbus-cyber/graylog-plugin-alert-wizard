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
import ObjectUtils from 'util/ObjectUtils';
import { Button } from 'react-bootstrap';
import { FormattedMessage } from 'react-intl';
import TitleSeverity from 'wizard/components/TitleSeverity';
import FieldsCondition from 'wizard/components/conditions/FieldsCondition';
import NumberCondition from 'wizard/components/conditions/NumberCondition';
import TimeRangeCondition from 'wizard/components/conditions/TimeRangeCondition';
import Description from 'wizard/components/Description';
import GroupByCondition from 'wizard/components/conditions/GroupByCondition';
import { Row, Col } from 'components/graylog';
import IconArrowsV from "wizard/components/icons/ArrowsV";

const STREAM = {
        matching_type: '',
        field_rule: [{field: '', type: '', value: ''}],
    };

const CorrelationCondition = createReactClass({
    displayName: 'CorrelationCondition',

    propTypes: {
        onUpdate: PropTypes.func,
        onUpdateAlert: PropTypes.func,
    },
    getInitialState() {
        return {
            alert: ObjectUtils.clone(this.props.alert),
        };
    },
    componentWillReceiveProps(nextProps) {
        if (nextProps.alert !== this.props.alert) {
            this.setState({alert: nextProps.alert});
        }
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
        if(update.stream === null){
            update.stream = STREAM;
        }
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
    _handleChangeAdditionalNbrCond(field, value) {
        if (field === "threshold"){
            this._handleChangeCondition("additional_threshold", value);
        }else if (field === "threshold_type"){
            this._handleChangeCondition("additional_threshold_type", value);
        }else{
            this._handleChangeCondition(field, value);
        }
    },
    _switchStreamNumberCondition(){
        let alert = ObjectUtils.clone(this.state.alert);
        alert.stream = this.state.alert.second_stream ? this.state.alert.second_stream : STREAM;
        alert.second_stream = this.state.alert.stream ? this.state.alert.stream : STREAM;
        alert.condition_parameters.threshold = this.state.alert.condition_parameters.additional_threshold;
        alert.condition_parameters.threshold_type = this.state.alert.condition_parameters.additional_threshold_type;
        alert.condition_parameters.additional_threshold = this.state.alert.condition_parameters.threshold;
        alert.condition_parameters.additional_threshold_type = this.state.alert.condition_parameters.threshold_type;
        this.setState({alert:alert});

        this.props.onUpdateAlert(alert);
    },
    
    render() {
        let time;
        let time_type;
        if (this.state.alert.condition_parameters.time >= 1440) {
            time = this.state.alert.condition_parameters.time / 1440;
            time_type = 1440;
        } else if (this.state.alert.condition_parameters.time >= 60) {
            time = this.state.alert.condition_parameters.time / 60;
            time_type = 60;
        } else {
            time = this.state.alert.condition_parameters.time;
            time_type = 1;
        }
       
        const buttonSwitchStream = <Button onClick={this._switchStreamNumberCondition} title="Switch" bsStyle="info" style={{ fontSize: '18px' }}><IconArrowsV/></Button>;

        let label;
        if (this.props.alert.condition_type === 'THEN') {
            label = <Row style={{ marginBottom: '0px' }}><Col md={2}></Col><Col md={2}><label><FormattedMessage id= "wizard.then" defaultMessage= "THEN" /></label></Col><Col md={8}>{buttonSwitchStream}</Col></Row>;
        } else if (this.props.alert.condition_type === 'AND') {
            label = <Row style={{ marginBottom: '0px' }}><Col md={2}></Col><Col md={10}><label><FormattedMessage id= "wizard.and" defaultMessage= "AND" /></label></Col></Row>;
        }
        
        return (
            <div>
                <TitleSeverity onUpdate={this.props.onUpdate} title={this.state.alert.title} severity={this.state.alert.severity} 
                                isPluginLoggingAlertPresent={this.props.isPluginLoggingAlertPresent}/>
                <br/>
                <div style={{ backgroundColor: '#f6f6f6', padding: '10px' }}>
                    <FieldsCondition stream={this.state.alert.stream} onSaveStream={this._handleChangeStream} message={this.props.message} 
                                    matchData={this.props.matchData} />
                    <br/>
                    <NumberCondition onUpdate={this._handleChangeCondition} threshold={this.state.alert.condition_parameters.threshold} 
                                    threshold_type={this.state.alert.condition_parameters.threshold_type} />
                </div>
                <br/>
                {label}
                <br/>
                <div style={{ backgroundColor: '#f6f6f6', padding: '10px' }}>
                    <FieldsCondition stream={this.state.alert.second_stream} onSaveStream={this._handleChangeSecondStream} message={this.props.message} />
                    <br/>
                    <NumberCondition onUpdate={this._handleChangeAdditionalNbrCond} threshold={this.state.alert.condition_parameters.additional_threshold} 
                                    threshold_type={this.state.alert.condition_parameters.additional_threshold_type} />
                </div>
                <br/>
                <TimeRangeCondition onUpdate={this._handleChangeCondition} time={time.toString()} time_type={time_type.toString()} />
                <br/>
                <GroupByCondition onUpdate={this._handleChangeCondition} grouping_fields={this.state.alert.condition_parameters.grouping_fields} />
                <br/>
                <Description onUpdate={this.props.onUpdate} description={this.state.alert.description}/>
                <br/>
            </div>
        );
        
    },
});

export default CorrelationCondition;
