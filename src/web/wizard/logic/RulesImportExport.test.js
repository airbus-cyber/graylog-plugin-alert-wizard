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

import RulesImportExport from './RulesImportExport'

describe('RulesImport.normalizeImportedRules', () => {
    it('should convert condition type MEAN to AVG', () => {
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
        const result = RulesImportExport.normalizeImportedRules(rule)
        expect(result[0].condition_parameters.type).toBe('AVG')
    });

    it('should convert threshold type HIGHER to >', () => {
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
                'threshold_type': 'HIGHER',
                'grouping_fields': [],
                'time': 1,
                'type': 'AVG'
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
        const result = RulesImportExport.normalizeImportedRules(rule)
        expect(result[0].condition_parameters.threshold_type).toBe('>')
    });

    it('should convert threshold type LOWER to <', () => {
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
                'threshold_type': 'LOWER',
                'grouping_fields': [],
                'time': 1,
                'type': 'AVG'
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
        const result = RulesImportExport.normalizeImportedRules(rule)
        expect(result[0].condition_parameters.threshold_type).toBe('<')
    });

    it('should handle new format with version number correctly', function() {
        const exportData = {
            'version': '1.0.0',
            'rules': [{
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
                    'type': 'CARD'
                },
                'stream': {
                    'matching_type': 'AND',
                    'field_rule': [{'field': 'a', 'type': 1, 'value': 'a', 'id': '62e8dc0088bd1c707db8e41b'}],
                    'id': '62e8dc0088bd1c707db8e419'
                },
                'title': 'a',
                'description': null,
                'second_stream': {'matching_type': '', 'field_rule': [], 'id': ''},
                'condition_type': 'GROUP_DISTINCT'
            }]
        }
        const result = RulesImportExport.normalizeImportedRules(exportData)
        console.log('TOTO' + result)
        expect(result.length).toBe(1)
    })
});
