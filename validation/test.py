# to create and populate the test venv:
# * python3 -m venv venv
# * source venv/bin/activate
# * pip install -r requirements.txt 
# to execute these tests:
# * activate venv
#   source ./venv/bin/activate
# * execute tests
#   python -m unittest
# To execute only one test, suffix with the fully qualified test name. Example:
#   python -m unittest test.Test.test_default_time_range_in_configuration_should_propagate_into_notification_time_range__issue47

from unittest import TestCase
from graylog_server import GraylogServer
import requests
from urllib import parse

_AUTH = ('admin', 'admin')
_HEADERS = {"X-Requested-By": "test-program"}


class Test(TestCase):

    #TODO should probably start graylog in a setupClass
    def setUp(self) -> None:
        self._graylog = GraylogServer('../runtime')
        self._graylog.start()
        self._graylog.wait_until_log('Graylog server up and running.', 60)

    def tearDown(self) -> None:
        self._graylog.stop()

    def _build_url(self, path):
        return parse.urljoin('http://127.0.0.1:9000/api/', path)

    def _get(self, path):
        url = self._build_url(path)
        print('GET {}'.format(url))
        return requests.get(url, auth=_AUTH, headers=_HEADERS)

    def _put(self, path, payload):
        url = self._build_url(path)
        print('PUT {} {}'.format(url, payload))
        return requests.put(url, json=payload, auth=_AUTH, headers=_HEADERS)

    def _post(self, path, payload):
        url = self._build_url(path)
        print('POST {} {}'.format(url, payload))
        return requests.post(url, json=payload, auth=_AUTH, headers=_HEADERS)

    def test_get_alerts_should_be_found(self):
        response = self._get('plugins/com.airbus_cyber_security.graylog.wizard/alerts/data')
        self.assertEqual(200, response.status_code)

    def test_put_config_with_time_default_value_should_modify_time_default_value(self):
        configuration = {
            'default_values': {
                'matching_type': '',
                'threshold_type': '',
                'time': '1441'
            },
            'field_order': []
        }
        self._put('plugins/com.airbus_cyber_security.graylog.wizard/config', configuration)
        response = self._get('plugins/com.airbus_cyber_security.graylog.wizard/config')
        body = response.json()
        self.assertEqual(1441, body['default_values']['time'])

    def test_create_alert_rule_should_not_fail(self):
        alert_rule = {
            'condition_parameters': {
                'additional_threshold': 0,
                'additional_threshold_type': '',
                'backlog': 500,
                'distinction_fields': [],
                'field': '',
                'grace': 1,
                'grouping_fields': [],
                'threshold': 0,
                'threshold_type': 'MORE',
                'time': 1,
                'type': ''
            },
            'condition_type': 'COUNT',
            'severity': 'info',
            'stream': {
                'field_rule': [
                    {
                        'field': 'source',
                        'type': 1,
                        'value': 'toto'
                    }
                ],
                'matching_type': 'AND'
            },
            'title': 'a'
        }
        response = self._post('plugins/com.airbus_cyber_security.graylog.wizard/alerts', alert_rule)
        self.assertEqual(200, response.status_code)

    def test_set_logging_alert_configuration_should_not_fail(self):
        configuration = {
            'aggregation_time': '1441',
            'alert_tag': 'LoggingAlert',
            'field_alert_id': 'id',
            'log_body': 'type: alert\nid: ${logging_alert.id}\nseverity: ${logging_alert.severity}\napp: graylog\nsubject: ${event_definition_title}\nbody: ${event_definition_description}\n${if backlog && backlog[0]} src: ${backlog[0].fields.src_ip}\nsrc_category: ${backlog[0].fields.src_category}\ndest: ${backlog[0].fields.dest_ip}\ndest_category: ${backlog[0].fields.dest_category}\n${end}',
            'overflow_tag': 'LoggingOverflow',
            'separator': ' | ',
            'severity': 'LOW'
        }
        response = self._put('system/cluster_config/com.airbus_cyber_security.graylog.events.config.LoggingAlertConfig', configuration)
        # TODO should be 200 instead of 202!!
        self.assertEqual(202, response.status_code)

    def test_default_time_range_in_configuration_should_propagate_into_notification_time_range__issue47(self):
        configuration = {
            'aggregation_time': '1441',
            'alert_tag': 'LoggingAlert',
            'field_alert_id': 'id',
            'log_body': 'type: alert\nid: ${logging_alert.id}\nseverity: ${logging_alert.severity}\napp: graylog\nsubject: ${event_definition_title}\nbody: ${event_definition_description}\n${if backlog && backlog[0]} src: ${backlog[0].fields.src_ip}\nsrc_category: ${backlog[0].fields.src_category}\ndest: ${backlog[0].fields.dest_ip}\ndest_category: ${backlog[0].fields.dest_category}\n${end}',
            'overflow_tag': 'LoggingOverflow',
            'separator': ' | ',
            'severity': 'LOW'
        }
        self._put('system/cluster_config/com.airbus_cyber_security.graylog.events.config.LoggingAlertConfig', configuration)
        alert_rule = {
            'condition_parameters': {
                'additional_threshold': 0,
                'additional_threshold_type': '',
                'backlog': 500,
                'distinction_fields': [],
                'field': '',
                'grace': 1,
                'grouping_fields': [],
                'threshold': 0,
                'threshold_type': 'MORE',
                'time': 1,
                'type': ''
            },
            'condition_type': 'COUNT',
            'severity': 'info',
            'stream': {
                'field_rule': [
                    {
                        'field': 'source',
                        'type': 1,
                        'value': 'toto'
                    }
                ],
                'matching_type': 'AND'
            },
            'title': 'a'
        }
        self._post('plugins/com.airbus_cyber_security.graylog.wizard/alerts', alert_rule)
        notifications = self._get('events/notifications')
        associated_notification = None
        for notification in notifications.json()['notifications']:
            print(notification)
            if notification['title'] == 'a':
                associated_notification = notification
        self.assertEqual(1441, associated_notification['config']['aggregation_time'])

    def test_get_alert_rule_should_return_correct_additional_threshold_type__issue34(self):
        alert_rule = {
            'condition_parameters': {
                'additional_threshold': 0,
                'additional_threshold_type': 'LESS',
                'backlog': 500,
                'distinction_fields': [],
                'field': '',
                'grace': 1,
                'grouping_fields': [],
                'threshold': 0,
                'threshold_type': 'MORE',
                'time': 1,
                'type': ''
            },
            'condition_type': 'AND',
            'second_stream': {
                'field_rule': [
                    {
                        'field': 'b',
                        'type': 1,
                        'value': 'titi'
                    }
                ],
                'matching_type': 'AND'
            },
            'severity': 'info',
            'stream': {
                'field_rule': [
                    {
                        'field': 'a',
                        'type': 1,
                        'value': 'toto'
                    }
                ],
                'matching_type': 'AND'
            },
            'title': 'b'
        }
        self._post('plugins/com.airbus_cyber_security.graylog.wizard/alerts', alert_rule)
        response = self._get('plugins/com.airbus_cyber_security.graylog.wizard/alerts/b/data')
        retrieved_alert_rule = response.json()
        self.assertEqual('LESS', retrieved_alert_rule['condition_parameters']['additional_threshold_type'])

    def test_export_alert_rule_should_return_correct_additional_threshold_type__issue34(self):
        alert_rule = {
            'condition_parameters': {
                'additional_threshold': 0,
                'additional_threshold_type': 'LESS',
                'backlog': 500,
                'distinction_fields': [],
                'field': '',
                'grace': 1,
                'grouping_fields': [],
                'threshold': 0,
                'threshold_type': 'MORE',
                'time': 1,
                'type': ''
            },
            'condition_type': 'AND',
            'second_stream': {
                'field_rule': [
                    {
                        'field': 'b',
                        'type': 1,
                        'value': 'titi'
                    }
                ],
                'matching_type': 'AND'
            },
            'severity': 'info',
            'stream': {
                'field_rule': [
                    {
                        'field': 'a',
                        'type': 1,
                        'value': 'toto'
                    }
                ],
                'matching_type': 'AND'
            },
            'title': 'b'
        }
        self._post('plugins/com.airbus_cyber_security.graylog.wizard/alerts', alert_rule)
        export_selection = {
            'titles': ['b']
        }
        response = self._post('plugins/com.airbus_cyber_security.graylog.wizard/alerts/export', export_selection)
        exported_alert_rule = response.json()
        self.assertEqual('LESS', exported_alert_rule[0]['condition_parameters']['additional_threshold_type'])
