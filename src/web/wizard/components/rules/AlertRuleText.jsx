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

import React from 'react';
import { Spinner } from 'components/common';
import { FormattedMessage } from 'react-intl';
import { AVAILABLE_AGGREGATION_TYPES } from 'wizard/components/inputs/StatisticalInput';

const AlertRuleText = ({alert}) => {

    const _availableMatchingTypes = [
        {value: 'AND', label: <FormattedMessage id= "wizard.all" defaultMessage= "all" />},
        {value: 'OR', label: <FormattedMessage id= "wizard.atLeastOne" defaultMessage= "at least one" />},
    ];

    const _availableThresholdTypes = [
        {value: '>', label: <FormattedMessage id= "wizard.more" defaultMessage= "more than" />},
        {value: '<', label: <FormattedMessage id= "wizard.less" defaultMessage= "less than" />},
        {value: '>=', label: <FormattedMessage id= "wizard.higherEqual" defaultMessage= "higher or equal than" />},
        {value: '<=', label: <FormattedMessage id= "wizard.lowerEqual" defaultMessage= "lower or equal than" />},
        {value: '==', label: <FormattedMessage id= "wizard.equal" defaultMessage= "equal" />},
    ];
    
    const _isLoading = () => {
        return !alert;
    };
    
    const _getMatchingType = (type) => {
        return _availableMatchingTypes.filter((t) => t.value === type)[0].label;
    };

    const _getThresholdType = (type) => {
        return _availableThresholdTypes.filter((t) => t.value === type)[0].label;
    };

    const _getAggregationType = (type) => {
        return AVAILABLE_AGGREGATION_TYPES.filter((t) => t.value === type)[0].label;
    };

    // TODO try to factor this code with the one in FieldRule.jsx and ManageSettings
    const _availableRuleType = [
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

    const _getRuleType = (type) => {
        return _availableRuleType.filter((t) => t.value === type)[0].label;
    }
    
    const _getTextFieldRule = (stream) => {
        let textFieldRule = [];
        if(stream !== null){
            for (let index = 0; index < stream.field_rule.length; index++) {
                textFieldRule[index] = (<FormattedMessage id ="wizard.FieldRule" 
                        defaultMessage={" {field} {type} {value} \n"}
                        values={{field: <strong>{stream.field_rule[index].field}</strong>, 
                                type: <strong>{_getRuleType(stream.field_rule[index].type)}</strong>, 
                                value: <strong>{stream.field_rule[index].value}</strong>}}/> );
            }  
        }
        return textFieldRule;
    };

    const _getTriggerRule = () => {
        return (<FormattedMessage id ="wizard.TriggerRule" 
                defaultMessage={"Trigger an alert when there are {threshold_type} {threshold} messages in the last {time} minutes "}
                values={{threshold_type: <strong>{_getThresholdType(alert.condition_parameters.threshold_type)}</strong>, 
                    threshold: <strong>{alert.condition_parameters.threshold}</strong>, 
                    time: <strong>{alert.condition_parameters.time}</strong> }}/> );
    };

    const _getMatchRule = (matching_type) => {
        return (<FormattedMessage id ="wizard.MatchRule" 
                defaultMessage={"matching {matching_type} of the following rules: \n"}
                values={{matching_type: <strong>{_getMatchingType(matching_type)}</strong> }}/> );
    };

    const _getGroupByRule = () => {
        return (<FormattedMessage id ="wizard.GroupByRule" 
                defaultMessage={"with the same value of {groupBy}, "}
                values={{groupBy: <strong>{Array.isArray(alert.condition_parameters.grouping_fields) ? 
                        alert.condition_parameters.grouping_fields.join(' ') : alert.condition_parameters.grouping_fields}</strong> }}/> );
    };

    const _getDistinctByRule = () => {
        return (<FormattedMessage id ="wizard.DistinctByRule" 
                defaultMessage={"with distinct value of {distinctBy}, "}
                values={{distinctBy: <strong>{alert.condition_parameters.distinct_by}</strong> }}/> );
    };
    
    if (_isLoading()) {
        return (
            <div>
                <Spinner/>
            </div>
        );
    }
    try {
        let textRule = [];
        if (alert.condition_parameters !== null && alert.stream !== null) {
            if (alert.condition_type === "STATISTICAL") {                    
                textRule.push(<FormattedMessage id ="wizard.StatisticalRule" 
                    defaultMessage= {"Trigger an alert when the {type} of {field} is {threshold_type} {threshold} in the last {time} minutes "}
                    values={{type: <strong>{_getAggregationType(alert.condition_parameters.type)}</strong> ,
                        field: <strong>{alert.condition_parameters.field}</strong>, 
                        threshold_type: <strong>{_getThresholdType(alert.condition_parameters.threshold_type)}</strong>,
                        threshold: <strong>{alert.condition_parameters.threshold}</strong>, 
                        time: <strong>{alert.condition_parameters.time}</strong>}}/> );     
                textRule.push(_getMatchRule(alert.stream.matching_type));
                textRule.push(_getTextFieldRule(alert.stream));
            } else if (alert.condition_type === "GROUP_DISTINCT") {
                textRule.push(_getTriggerRule());
                if (alert.condition_parameters.grouping_fields && alert.condition_parameters.grouping_fields.length > 0) {
                    textRule.push(_getGroupByRule());
                }
                if (alert.condition_parameters.distinct_by !== '') {
                    textRule.push(_getDistinctByRule());
                }
                textRule.push(_getMatchRule(alert.stream.matching_type));
                textRule.push(_getTextFieldRule(alert.stream));
            } else if (alert.condition_type === "THEN") {
                textRule.push(_getTriggerRule());
                if (alert.condition_parameters.grouping_fields && alert.condition_parameters.grouping_fields.length > 0) {
                     textRule.push(_getGroupByRule());
                }
                textRule.push(_getMatchRule(alert.stream.matching_type));
                textRule.push(_getTextFieldRule(alert.stream));
                
                textRule.push(<FormattedMessage id ="wizard.ThenRule" 
                        defaultMessage={"followed by {threshold_type} {threshold} messages "}
                        values={{threshold_type: <strong>{_getThresholdType(alert.condition_parameters.additional_threshold_type)}</strong>,
                                threshold: <strong>{alert.condition_parameters.additional_threshold}</strong> }}/>);
                if (alert.condition_parameters.grouping_fields && alert.condition_parameters.grouping_fields.length > 0) {
                    textRule.push(_getGroupByRule());
                }
                textRule.push(_getMatchRule(alert.second_stream.matching_type));
                textRule.push(_getTextFieldRule(alert.second_stream));
                
            } else if (alert.condition_type === "AND"){
                textRule.push(_getTriggerRule());
                if (alert.condition_parameters.grouping_fields && alert.condition_parameters.grouping_fields.length > 0) {
                     textRule.push(_getGroupByRule());
                }
                textRule.push(_getMatchRule(alert.stream.matching_type));
                textRule.push(_getTextFieldRule(alert.stream));
                
                textRule.push(<FormattedMessage id ="wizard.AndRule" 
                        defaultMessage={"AND when there are {threshold_type} {threshold} messages "}
                        values={{threshold_type: <strong>{_getThresholdType(alert.condition_parameters.additional_threshold_type)}</strong>,
                                threshold: <strong>{alert.condition_parameters.additional_threshold}</strong> }}/>);
                if (alert.condition_parameters.grouping_fields && alert.condition_parameters.grouping_fields.length > 0) {
                    textRule.push(_getGroupByRule());
                }
                textRule.push(_getMatchRule(alert.second_stream.matching_type));
                textRule.push(_getTextFieldRule(alert.second_stream));
                
            } else if (alert.condition_type === "OR"){
                textRule.push(_getTriggerRule());
                textRule.push(_getMatchRule(alert.stream.matching_type));
                textRule.push(_getTextFieldRule(alert.stream));
                textRule.push(<FormattedMessage id ="wizard.or" defaultMessage={"OR "}/>);
                textRule.push(_getMatchRule(alert.second_stream.matching_type));
                textRule.push(_getTextFieldRule(alert.second_stream));
            } else {
                textRule.push(_getTriggerRule());
                textRule.push(_getMatchRule(alert.stream.matching_type));
                textRule.push(_getTextFieldRule(alert.stream));
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
}

export default AlertRuleText;
