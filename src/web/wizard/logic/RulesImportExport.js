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

export default {
    normalizeImportedRules(exportData) {
        if (exportData.version === undefined) {
            return exportData.map(function (rule) {
                let condition_parameters = { ...rule.condition_parameters };
                if (condition_parameters.type === 'MEAN') {
                    condition_parameters.type = 'AVG';
                }
                if (condition_parameters.threshold_type === 'HIGHER') {
                    condition_parameters.threshold_type = '>';
                }
                if (condition_parameters.threshold_type === 'LOWER') {
                    condition_parameters.threshold_type = '<';
                }
                return { ...rule, condition_parameters: condition_parameters };
            });
        }
        return exportData.rules
    },

    createExportDataFromRules(rules) {
        return {
            version: '1.0.0',
            rules: rules
        }
    }
}