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

import AlertValidation from './AlertValidation'

describe('AlertValidation.isAlertValid', () => {
    it('should not fail', () => {
        const alert = {
            title: 'a',
            condition_parameters: {
                threshold_type: '>',
                threshold: 0,
                time: 1,
            },
            stream: {
                matching_type: 'AND',
                field_rule: [{
                    field: 'a',
                    type: '1',
                    value: 'a'
                }]
            },
            condition_type: 'AND',
            second_stream: null
        };
        const result = AlertValidation.isAlertValid(alert);
        expect(result).toBe(true);
    });

    it('should not validate alert with missing field rule type', () => {
        const alert = {
            title: 'a',
            condition_parameters: {
                threshold_type: '>',
                threshold: 0,
                time: 1,
            },
            stream: {
                matching_type: 'AND',
                field_rule: [{
                    field: 'a',
                    type: '',
                    value: 'a'
                }]
            }
        };
        const result = AlertValidation.isAlertValid(alert);
        expect(result).toBe(false);
    });

    it('should validate second stream rule when condition type is THEN', () => {
        const alert = {
            title: 'a',
            condition_type: 'THEN',
            condition_parameters: {
                threshold_type: '>',
                threshold: 0,
                time: 1,
            },
            stream: {
                matching_type: 'AND',
                field_rule: [{
                    field: 'a',
                    type: '1',
                    value: 'a'
                }]
            },
            second_stream: {
                matching_type: 'AND',
                field_rule: [{
                    field: '',
                    type: '',
                    value: ''
                }]
            },
        };
        const result = AlertValidation.isAlertValid(alert);
        expect(result).toBe(false);
    })
});
