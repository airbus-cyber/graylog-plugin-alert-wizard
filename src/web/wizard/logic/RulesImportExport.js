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

import UserNotification from 'util/UserNotification';

function normalizeThresholdType(threshold_type) {
    if ((threshold_type === 'HIGHER') || (threshold_type === 'MORE')) {
        return '>';
    }
    if ((threshold_type === 'LOWER') || (threshold_type === 'LESS')) {
        return '<';
    }
    return threshold_type;
}

function normalizeSearchQueryParameters(alertRule, condition_parameters) {
    let result = { ...condition_parameters };
    if (!result.search_query) {
        result.search_query = '*';
    }

    if (['THEN', 'AND', 'OR'].includes(alertRule.condition_type) && !result.additional_search_query) {
        result.additional_search_query = '*';
    }

    return result;
}

function normalizeConditionParametersType(type) {
    if (type === 'MEAN') {
        return 'AVG';
    }
    return type;
}

function normalizeConditionParameters(rule) {
    const condition_parameters = rule.condition_parameters;
    const type = normalizeConditionParametersType(condition_parameters.type);
    const threshold_type = normalizeThresholdType(condition_parameters.threshold_type);
    let result = { ...condition_parameters, type, threshold_type };
    const additional_threshold_type = condition_parameters.additional_threshold_type;
    if (additional_threshold_type) {
        result.additional_threshold_type = normalizeThresholdType(additional_threshold_type);
    }
    const distinction_fields = condition_parameters.distinction_fields;
    if (distinction_fields !== undefined) {
        if (distinction_fields.length === 0) {
            result.distinct_by = '';
        } else {
            result.distinct_by = condition_parameters.distinction_fields[0];
            UserNotification.warning(`The rule ${rule.title} has multiple distinction_fields, only the first one will be kept ("${result.distinct_by}")`);
        }
    }

    const split_fields = rule.notification_parameters.split_fields;
    if (split_fields.length !== 0) {
        if (rule.condition_type === 'COUNT' || rule.condition_type === 'GROUP_DISTINCT') {
            result.grouping_fields = [...new Set([...result.grouping_fields, ...split_fields])];
        } else {
            UserNotification.warning(`The notification of rule ${rule.title} had split fields, they will be ignored`);
        }
    }

    return normalizeSearchQueryParameters(rule, result);
}

function normalizeNotificationParameters(notification_parameters) {
    const { split_fields, severity, ...result } = notification_parameters;
    return result;
}

function convertSeverityToPriority(severity) {
    switch (severity.toUpperCase()) {
        case 'INFO' | 'LOW':
            return 1;
        case 'MEDIUM':
            return 2;
        case 'HIGH':
            return 3;
        default:
            return 1;
    }
}

function normalizePriority(alertRule) {
    const severity = alertRule.notification_parameters.severity;
    if (severity) {
        return convertSeverityToPriority(severity);
    }
    return alertRule.priority;
}

function normalizeDescription(description) {
    if (description === null || description === undefined) {
        return '';
    }

    return description;
}

function normalizeConditionType(rule) {
    const split_fields = rule.notification_parameters.split_fields;
    if (split_fields.length !== 0 && rule.condition_type === 'COUNT') {
        UserNotification.warning(`The rule ${rule.title} of type COUNT has been converted to type DISTINCT/GROUP, because notification had split fields`);
        return 'GROUP_DISTINCT';
    }
    return rule.condition_type;
}

function normalizeImportedRule(rule) {
    const condition_parameters = normalizeConditionParameters(rule);
    const notification_parameters = normalizeNotificationParameters(rule.notification_parameters);
    const condition_type = normalizeConditionType(rule);
    const priority = normalizePriority(rule);
    const description = normalizeDescription(rule.description);
    const {severity, ...ruleWithoutSeverity} = rule;
    return { ...ruleWithoutSeverity, priority, description, condition_type, condition_parameters, notification_parameters };
}

export default {
    normalizeImportedRules(exportData) {
        if (exportData.version === undefined) {
            return exportData.map(normalizeImportedRule);
        } else {
            return exportData.rules.map(normalizeImportedRule);
        }
    },

    createExportDataFromRules(rules) {
        return {
            version: '1.0.2',
            rules: rules
        }
    }
}
