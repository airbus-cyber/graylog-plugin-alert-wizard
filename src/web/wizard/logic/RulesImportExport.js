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

function normalizeConditionParametersType(type) {
    if (type === 'MEAN') {
        return 'AVG';
    }
    return type;
}

function normalizeGroupingFields(rule) {
    const groupingFields = rule.condition_parameters.grouping_fields;
    const splitFields = rule.notification_parameters.split_fields;
    if (splitFields.length === 0) {
        return groupingFields;
    }
    if (rule.condition_type === 'COUNT' || rule.condition_type === 'GROUP_DISTINCT') {
        return [...new Set([...groupingFields, ...splitFields])];
    }
    UserNotification.warning(`The notification of rule ${rule.title} had split fields, they will be ignored`);
    return groupingFields;
}

function normalizeDistinctBy(condition_parameters, title) {
    const distinction_fields = condition_parameters.distinction_fields;
    if (!distinction_fields) {
        return condition_parameters.distinct_by;
    }
    if (distinction_fields.length === 0) {
        return '';
    }
    const result = condition_parameters.distinction_fields[0];
    UserNotification.warning(`The rule ${title} has multiple distinction_fields, only the first one will be kept ("${result}")`);
    return result;
}

function normalizeSearchQuery(additional_search_query) {
    if (!additional_search_query) {
        return '*';
    }

    return additional_search_query;
}

function normalizeConditionParameters(rule) {
    const condition_parameters = rule.condition_parameters;
    const type = normalizeConditionParametersType(condition_parameters.type);
    const threshold_type = normalizeThresholdType(condition_parameters.threshold_type);
    const grouping_fields = normalizeGroupingFields(rule);
    const distinct_by = normalizeDistinctBy(condition_parameters, rule.title);
    const search_query = normalizeSearchQuery(condition_parameters.search_query);
    const result = { ...condition_parameters, type, search_query, threshold_type, grouping_fields, distinct_by };
    if (['COUNT', 'GROUP_DISTINCT', 'STATISTICAL'].includes(rule.condition_type)) {
        return result;
    }
    const additional_search_query = normalizeSearchQuery(condition_parameters.additional_search_query);
    if (rule.condition_type === 'OR') {
        return { ...result, additional_search_query };
    }
    const additional_threshold_type = normalizeThresholdType(condition_parameters.additional_threshold_type);
    return { ...result, additional_search_query, additional_threshold_type };
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
    return {
        priority, description, condition_type, condition_parameters, notification_parameters,
        title: rule.title, stream: rule.stream, second_stream: rule.second_stream
    };
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
