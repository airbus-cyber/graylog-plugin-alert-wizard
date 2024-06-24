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
import { FormattedMessage } from 'react-intl';
import FieldsInput from 'wizard/components/inputs/FieldsInput';
import NumberInput from 'wizard/components/inputs/NumberInput';
import TimeRangeInput from 'wizard/components/inputs/TimeRangeInput';
import Description from 'wizard/components/inputs/Description';
import { Row, Col } from 'components/bootstrap';
import HighlightedDiv from 'wizard/components/containers/HighlightedDiv';
import SearchQueryInput from "wizard/components/inputs/SearchQueryInput";

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
                <HighlightedDiv>
                    <SearchQueryInput onUpdate={this._handleChangeCondition} search_query={this.props.alert.condition_parameters.search_query} />
                    <br/>
                    <FieldsInput stream={this.props.alert.stream} onSaveStream={this._handleChangeStream} message={this.props.message}
                                    matchData={this.props.matchData} />
                </HighlightedDiv>
                <br/>
                <Row style={{ marginBottom: '0px' }}><Col md={2} /><Col md={10}><label><FormattedMessage id= "wizard.or" defaultMessage= "OR" /></label></Col></Row>
                <br/>
                <HighlightedDiv>
                    <SearchQueryInput onUpdate={this._handleChangeCondition} search_query={this.props.alert.condition_parameters.additional_search_query} fieldName='additional_search_query' />
                    <br/>
                    <FieldsInput stream={this.props.alert.second_stream} onSaveStream={this._handleChangeSecondStream} message={this.props.message} />
                </HighlightedDiv>
                <br/>
                <NumberInput onUpdate={this._handleChangeCondition} threshold={this.props.alert.condition_parameters.threshold}
                                threshold_type={this.props.alert.condition_parameters.threshold_type} />
                <br/>
                <TimeRangeInput onUpdate={this._handleChangeCondition} time={time.toString()} time_type={time_type.toString()} />
                <br/>
                <Description onUpdate={this.props.onUpdate} description={this.props.alert.description}/>
                <br/>
            </>
        );
        
    },
});

export default OrCondition;
