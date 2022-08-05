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

function normalizeThresholdType(threshold_type) {
    if ((threshold_type === 'HIGHER') || (threshold_type === 'MORE')) {
        return '>';
    }
    if ((threshold_type === 'LOWER') || (threshold_type === 'LESS')) {
        return '<';
    }
    return threshold_type;
}

function normalizeConditionParameters(condition_parameters) {
    let result = { ...condition_parameters };
    if (condition_parameters.type === 'MEAN') {
        result.type = 'AVG';
    }
    result.threshold_type = normalizeThresholdType(condition_parameters.threshold_type);
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
        }
    }
    return result;
}

function normalizeImportedRule(rule) {
    let condition_parameters = normalizeConditionParameters(rule.condition_parameters);
    let severity = rule.notification_parameters.severity;
    return { ...rule, severity, condition_parameters };
}

export default {
    normalizeImportedRules(exportData) {
        if (exportData.version === undefined) {
            return exportData.map(normalizeImportedRule);
        }
        // TODO: should remove this part, should only be exportData.rules
        return exportData.rules.map(rule => {
            rule.severity = rule.notification_parameters.severity;
            return rule
        });
    },

    createExportDataFromRules(rules) {
        return {
            version: '1.0.0',
            rules: rules
        }
    }
}
