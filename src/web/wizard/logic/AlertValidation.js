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

function _isRuleValid(rule) {
    if (rule.field === '') {
        return false;
    }
    if (rule.type === '') {
        return false;
    }
    // note: 5 and -5 correspond to is present, is not present
    if (rule.value === '' && rule.type !== 5 && rule.type !== -5) {
       return false;
    }
    return true;
}

function _isFieldRulesValid(search_query, field_rules) {
    if (search_query === '' && field_rules.length <= 0) {
        return false;
    }
    for (let i = 0; i < field_rules.length; i++) {
        if (!_isRuleValid(field_rules[i])){
            return false;
        }
    }
    return true;
}

export default {
    isAlertValid(alert) {
        if (alert.title === '') {
            return false;
        }
        if (alert.priority === null || alert.priority === undefined) {
            return false;
        }
        if (alert.stream.matching_type === '') {
            return false;
        }
        if (alert.condition_parameters.time === null) {
            return false;
        }
        if (alert.condition_parameters.threshold_type === '') {
            return false;
        }
        if (alert.condition_parameters.threshold === null) {
            return false;
        }
        if (isNaN(alert.condition_parameters.threshold)) {
            return false;
        }
        if (!_isFieldRulesValid(alert.condition_parameters.search_query, alert.stream.field_rule)) {
            return false;
        }
        if (alert.condition_type === 'THEN' && !_isFieldRulesValid(alert.condition_parameters.additional_search_query, alert.second_stream.field_rule)) {
            return false;
        }
        if (alert.condition_type === 'AND' && !_isFieldRulesValid(alert.condition_parameters.additional_search_query, alert.second_stream.field_rule)) {
            return false;
        }
        if (alert.condition_type === 'OR' && !_isFieldRulesValid(alert.condition_parameters.additional_search_query, alert.second_stream.field_rule)) {
            return false;
        }
        return true;
    },

    isAlertCorrupted(alert) {
        if (alert.condition === null || alert.condition_parameters === null || alert.stream === null || alert.notification === null) {
            return true;
        }

        if (alert.condition_type === 'OR' && alert.second_event_definition === null) {
            return true;
        }

        return false;
    }
}
