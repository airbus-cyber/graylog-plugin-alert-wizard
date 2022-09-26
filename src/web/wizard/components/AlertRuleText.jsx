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
import { Spinner } from 'components/common';
import { FormattedMessage } from 'react-intl';

const AlertRuleText = createReactClass({
    displayName: 'AlertRuleText',

    propTypes: {
        alert: PropTypes.object,
    },
    _isLoading() {
        return !(this.props.alert);
    },
    _availableMatchingTypes() {
        return [
            {value: 'AND', label: <FormattedMessage id= "wizard.all" defaultMessage= "all" />},
            {value: 'OR', label: <FormattedMessage id= "wizard.atLeastOne" defaultMessage= "at least one" />},
        ];
    },
    _getMatchingType(type) {
        return this._availableMatchingTypes().filter((t) => t.value === type)[0].label;
    },

    // TODO should split this in two methods: there is a different meaning to > and < (also would be easier if it were a dictionary)
    _availableThresholdTypes() {
        return [
            {value: '>', label: <FormattedMessage id= "wizard.more" defaultMessage= "more than" />},
            {value: '<', label: <FormattedMessage id= "wizard.less" defaultMessage= "less than" />},
            {value: '>', label: <FormattedMessage id= "wizard.higher" defaultMessage= "higher than" />},
            {value: '>=', label: <FormattedMessage id= "wizard.higherEqual" defaultMessage= "higher or equal than" />},
            {value: '<', label: <FormattedMessage id= "wizard.lower" defaultMessage= "lower than" />},
            {value: '<=', label: <FormattedMessage id= "wizard.lowerEqual" defaultMessage= "lower or equal than" />},
            {value: '==', label: <FormattedMessage id= "wizard.equal" defaultMessage= "equal" />},
        ];
    },
    _getThresholdType(type) {
        return this._availableThresholdTypes().filter((t) => t.value === type)[0].label;
    },
    
    _availableAggregationTypes() {
        return [
            {value: 'AVG', label: <FormattedMessage id= "wizard.meanValue" defaultMessage= "average value" />},
            {value: 'STDDEV', label: <FormattedMessage id= "wizard.standardDeviation" defaultMessage= "standard deviation" />},
            {value: 'MIN', label: <FormattedMessage id= "wizard.minValue" defaultMessage= "min value" />},
            {value: 'MAX', label: <FormattedMessage id= "wizard.maxValue" defaultMessage= "max value" />},
            {value: 'SUM', label: <FormattedMessage id= "wizard.sum" defaultMessage= "sum" />},
            {value: 'CARD', label: <FormattedMessage id= "wizard.card" defaultMessage= "cardinality" />},
            {value: 'COUNT', label: <FormattedMessage id= "wizard.count" defaultMessage= "count" />},
            {value: 'SUMOFSQUARES', label: <FormattedMessage id= "wizard.sum" defaultMessage= "sum of squares" />},
            {value: 'VARIANCE', label: <FormattedMessage id= "wizard.variance" defaultMessage= "variance" />},
        ];
    },
    _getAggregationType(type) {
        return this._availableAggregationTypes().filter((t) => t.value === type)[0].label;
    },

    // TODO try to factor this code with the one in FieldRule.jsx and ManageSettings
    _availableRuleType() {
        return [
            {value: 1, label: <FormattedMessage id= "wizard.matchesExactly" defaultMessage= "matches exactly" />},
            {value: -1, label: <FormattedMessage id= "wizard.notMatchesExactly" defaultMessage= "does not match exactly" />},
            {value: 2, label: <FormattedMessage id= "wizard.matchesRegularExpression" defaultMessage= "matches regular expression" />},
            {value: -2, label: <FormattedMessage id= "wizard.notMatchRegularExpression" defaultMessage= "does not match regular expression" />},
            {value: 3, label: <FormattedMessage id= "wizard.greaterThan" defaultMessage= "is greater than" />},
            {value: -3, label: <FormattedMessage id= "wizard.notGreaterThan" defaultMessage= "is not greater than" />},
            {value: 4, label: <FormattedMessage id= "wizard.smallerThan" defaultMessage= "is smaller than" />},
            {value: -4, label: <FormattedMessage id= "wizard.notSmallerThan" defaultMessage= "is not smaller than" />},
            {value: 5, label: <FormattedMessage id= "wizard.present" defaultMessage= "is present" />},
            {value: -5, label: <FormattedMessage id= "wizard.notPresent" defaultMessage= "is not present" />},
            {value: 6, label: <FormattedMessage id= "wizard.contains" defaultMessage= "contains" />},
            {value: -6, label: <FormattedMessage id= "wizard.notContain" defaultMessage= "does not contain" />},
            {value: 7, label: <FormattedMessage id= "wizard.listpresent" defaultMessage= "is present in list" />},
            {value: -7, label: <FormattedMessage id= "wizard.listnotpresent" defaultMessage= "is not present in list" />},
        ];
    },
    _getRuleType(type) {
        return this._availableRuleType().filter((t) => t.value === type)[0].label;
    },
    
    _getTextFieldRule(stream){
        let textFieldRule = [];
        if(stream !== null){
            for (let index = 0; index < stream.field_rule.length; index++) {
                textFieldRule[index] = (<FormattedMessage id ="wizard.FieldRule" 
                        defaultMessage={" {field} {type} {value} \n"}
                        values={{field: <strong>{stream.field_rule[index].field}</strong>, 
                                type: <strong>{this._getRuleType(stream.field_rule[index].type)}</strong>, 
                                value: <strong>{stream.field_rule[index].value}</strong>}}/> );
            }  
        }
        return textFieldRule;
    },
    _getTriggerRule(){
        return (<FormattedMessage id ="wizard.TriggerRule" 
                defaultMessage={"Trigger an alert when there are {threshold_type} {threshold} messages in the last {time} minutes "}
                values={{threshold_type: <strong>{this._getThresholdType(this.props.alert.condition_parameters.threshold_type)}</strong>, 
                    threshold: <strong>{this.props.alert.condition_parameters.threshold}</strong>, 
                    time: <strong>{this.props.alert.condition_parameters.time}</strong> }}/> );
    },
    _getMatchRule(matching_type){
        return (<FormattedMessage id ="wizard.MatchRule" 
                defaultMessage={"matching {matching_type} of the following rules: \n"}
                values={{matching_type: <strong>{this._getMatchingType(matching_type)}</strong> }}/> );
    },
    _getGroupByRule(){
        return (<FormattedMessage id ="wizard.GroupByRule" 
                defaultMessage={"with the same value of {groupBy}, "}
                values={{groupBy: <strong>{Array.isArray(this.props.alert.condition_parameters.grouping_fields) ? 
                        this.props.alert.condition_parameters.grouping_fields.join(' ') : this.props.alert.condition_parameters.grouping_fields}</strong> }}/> );
    },
    _getDistinctByRule(){
        return (<FormattedMessage id ="wizard.DistinctByRule" 
                defaultMessage={"with distinct value of {distinctBy}, "}
                values={{distinctBy: <strong>{this.props.alert.condition_parameters.distinct_by}</strong> }}/> );
    },
    
    render() {
        if (this._isLoading()) {
            return (
                <div>
                    <Spinner/>
                </div>
            );
        }
        try {
            let textRule = [];
            if (this.props.alert.condition_parameters !== null && this.props.alert.stream !== null) {
                if (this.props.alert.condition_type === "STATISTICAL") {                    
                    textRule.push(<FormattedMessage id ="wizard.StatisticalRule" 
                        defaultMessage= {"Trigger an alert when the {type} of {field} is {threshold_type} {threshold} in the last {time} minutes "}
                        values={{type: <strong>{this._getAggregationType(this.props.alert.condition_parameters.type)}</strong> , 
                            field: <strong>{this.props.alert.condition_parameters.field}</strong>, 
                            threshold_type: <strong>{this._getThresholdType(this.props.alert.condition_parameters.threshold_type)}</strong>, 
                            threshold: <strong>{this.props.alert.condition_parameters.threshold}</strong>, 
                            time: <strong>{this.props.alert.condition_parameters.time}</strong>}}/> );     
                    textRule.push(this._getMatchRule(this.props.alert.stream.matching_type));
                    textRule.push(this._getTextFieldRule(this.props.alert.stream));
                } else if (this.props.alert.condition_type === "GROUP_DISTINCT") {
                    textRule.push(this._getTriggerRule());
                    if (this.props.alert.condition_parameters.grouping_fields && this.props.alert.condition_parameters.grouping_fields.length > 0) {
                        textRule.push(this._getGroupByRule());
                    }
                    if (this.props.alert.condition_parameters.distinct_by !== '') {
                        textRule.push(this._getDistinctByRule());
                    }
                    textRule.push(this._getMatchRule(this.props.alert.stream.matching_type));
                    textRule.push(this._getTextFieldRule(this.props.alert.stream));
                } else if (this.props.alert.condition_type === "THEN") {
                    textRule.push(this._getTriggerRule());
                    if (this.props.alert.condition_parameters.grouping_fields && this.props.alert.condition_parameters.grouping_fields.length > 0) {
                         textRule.push(this._getGroupByRule());
                    }
                    textRule.push(this._getMatchRule(this.props.alert.stream.matching_type));
                    textRule.push(this._getTextFieldRule(this.props.alert.stream));
                    
                    textRule.push(<FormattedMessage id ="wizard.ThenRule" 
                            defaultMessage={"followed by {threshold_type} {threshold} messages "}
                            values={{threshold_type: <strong>{this._getThresholdType(this.props.alert.condition_parameters.additional_threshold_type)}</strong>, 
                                    threshold: <strong>{this.props.alert.condition_parameters.additional_threshold}</strong> }}/>);
                    if (this.props.alert.condition_parameters.grouping_fields && this.props.alert.condition_parameters.grouping_fields.length > 0) {
                        textRule.push(this._getGroupByRule());
                    }
                    textRule.push(this._getMatchRule(this.props.alert.second_stream.matching_type));
                    textRule.push(this._getTextFieldRule(this.props.alert.second_stream));
                    
                } else if (this.props.alert.condition_type === "AND"){
                    textRule.push(this._getTriggerRule());
                    if (this.props.alert.condition_parameters.grouping_fields && this.props.alert.condition_parameters.grouping_fields.length > 0) {
                         textRule.push(this._getGroupByRule());
                    }
                    textRule.push(this._getMatchRule(this.props.alert.stream.matching_type));
                    textRule.push(this._getTextFieldRule(this.props.alert.stream));
                    
                    textRule.push(<FormattedMessage id ="wizard.AndRule" 
                            defaultMessage={"AND when there are {threshold_type} {threshold} messages "}
                            values={{threshold_type: <strong>{this._getThresholdType(this.props.alert.condition_parameters.additional_threshold_type)}</strong>, 
                                    threshold: <strong>{this.props.alert.condition_parameters.additional_threshold}</strong> }}/>);
                    if (this.props.alert.condition_parameters.grouping_fields && this.props.alert.condition_parameters.grouping_fields.length > 0) {
                        textRule.push(this._getGroupByRule());
                    }
                    textRule.push(this._getMatchRule(this.props.alert.second_stream.matching_type));
                    textRule.push(this._getTextFieldRule(this.props.alert.second_stream));
                    
                } else if (this.props.alert.condition_type === "OR"){
                    textRule.push(this._getTriggerRule());
                    textRule.push(this._getMatchRule(this.props.alert.stream.matching_type));
                    textRule.push(this._getTextFieldRule(this.props.alert.stream));
                    textRule.push(<FormattedMessage id ="wizard.or" defaultMessage={"OR "}/>);
                    textRule.push(this._getMatchRule(this.props.alert.second_stream.matching_type));
                    textRule.push(this._getTextFieldRule(this.props.alert.second_stream));
                } else {
                    textRule.push(this._getTriggerRule());
                    textRule.push(this._getMatchRule(this.props.alert.stream.matching_type));
                    textRule.push(this._getTextFieldRule(this.props.alert.stream));
                }
            }
            
            return (
                    <span style={{whiteSpace: 'pre-line'}}>{textRule}</span>
            );
        } catch(e) {
            return (
                    <span></span>
            );
        }
    },
});

export default AlertRuleText;
