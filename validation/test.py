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
from graylog_rest_api import GraylogRestApi

_AUTH = ('admin', 'admin')
_HEADERS = {"X-Requested-By": "test-program"}


class Test(TestCase):

    #TODO should probably start graylog in a setupClass
    def setUp(self) -> None:
        self._graylog = GraylogServer('../runtime')
        self._graylog.start()
        self._graylog_rest_api = GraylogRestApi()
        self._graylog_rest_api.wait_until_graylog_has_started()

    def tearDown(self) -> None:
        self._graylog.stop()

    def test_get_alerts_should_be_found(self):
        response = self._graylog_rest_api.get('plugins/com.airbus_cyber_security.graylog.wizard/alerts/data')
        self.assertEqual(200, response.status_code)

    def test_put_config_with_time_default_value_should_modify_time_default_value(self):
        default_time = 1441
        self._graylog_rest_api.update_alert_rules_settings(default_time)
        response = self._graylog_rest_api.get('plugins/com.airbus_cyber_security.graylog.wizard/config')
        body = response.json()
        self.assertEqual(default_time, body['default_values']['time'])

    def test_create_alert_rule_should_not_fail(self):
        status_code = self._graylog_rest_api.create_alert_rule_count('alert_rule_title')
        self.assertEqual(200, status_code)

    def test_set_logging_alert_configuration_should_not_fail(self):
        status_code = self._graylog_rest_api.update_logging_alert_plugin_configuration()
        # TODO should be 200 instead of 202!!
        self.assertEqual(202, status_code)

    def test_default_time_range_in_configuration_should_propagate_into_notification_time_range__issue47(self):
        self._graylog_rest_api.update_logging_alert_plugin_configuration()
        title = 'alert_rule_title'
        self._graylog_rest_api.create_alert_rule_count(title)
        notifications = self._graylog_rest_api.get('events/notifications')
        associated_notification = None
        for notification in notifications.json()['notifications']:
            if notification['title'] == title:
                associated_notification = notification
        self.assertEqual(1441, associated_notification['config']['aggregation_time'])

    def test_get_alert_rule_should_return_correct_additional_threshold_type__issue34(self):
        title = 'rule_title'
        self._graylog_rest_api.create_alert_rule_and(title)
        retrieved_alert_rule = self._graylog_rest_api.get_alert_rule(title)
        self.assertEqual('LESS', retrieved_alert_rule['condition_parameters']['additional_threshold_type'])

    def test_export_alert_rule_should_return_correct_additional_threshold_type__issue34(self):
        title = 'rule_title'
        self._graylog_rest_api.create_alert_rule_and(title)
        exported_alert_rule = self._graylog_rest_api.create_alert_rules_export([title])
        self.assertEqual('LESS', exported_alert_rule[0]['condition_parameters']['additional_threshold_type'])

    def test_get_alert_rule_should_return_correct_additional_threshold__issue69(self):
        title = 'rule_title'
        self._graylog_rest_api.create_alert_rule_and(title, additional_threshold=1)
        retrieved_alert_rule = self._graylog_rest_api.get_alert_rule(title)
        self.assertEqual(1, retrieved_alert_rule['condition_parameters']['additional_threshold'])

    def test_create_list_should_not_fail(self):
        self._graylog_rest_api.create_list()

