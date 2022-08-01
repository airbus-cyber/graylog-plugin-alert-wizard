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

import RulesImport from './RulesImport'

describe('RulesImport.normalizeImportedRules', () => {
    it('should convert condition type MEAN into AVG', () => {
        const rule = [{
            'notification_parameters': {
                'severity': 'INFO',
                'log_body': 'type: alert\nid: ${logging_alert.id}\nseverity: ${logging_alert.severity}\napp: graylog\nsubject: ${event_definition_title}\nbody: ${event_definition_description}\n${if backlog && backlog[0]} src: ${backlog[0].fields.src_ip}\nsrc_category: ${backlog[0].fields.src_category}\ndest: ${backlog[0].fields.dest_ip}\ndest_category: ${backlog[0].fields.dest_category}\n${end}',
                'split_fields': [],
                'single_notification': false,
                'aggregation_time': 0,
                'alert_tag': 'LoggingAlert'
            },
            'condition_parameters': {
                'distinct_by': 'x',
                'field': 'x',
                'grace': 1,
                'threshold': 0,
                'threshold_type': '>',
                'grouping_fields': [],
                'time': 1,
                'type': 'MEAN'
            },
            'stream': {
                'matching_type': 'AND',
                'field_rule': [{'field': 'b', 'type': 1, 'value': 'b', 'id': '62e7ae768a47ae63221aad48'}],
                'id': '62e7ae768a47ae63221aad46'
            },
            'title': 'b',
            'description': null,
            'second_stream': {'matching_type': '', 'field_rule': [], 'id': ''},
            'condition_type': 'STATISTICAL'
        }];
        const result = RulesImport.normalizeImportedRules(rule)
        expect(result[0].condition_parameters.type).toBe('AVG')
    });
});
