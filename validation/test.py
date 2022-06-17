# to create and populate the test venv:
# * python3 -m venv venv
# * source venv/bin/activate
# * pip install -r requirements.txt 
# to execute these tests:
# * activate venv
#   source ./venv/bin/activate
# * execute tests
#   python -m unittest --verbose
# To execute only one test, suffix with the fully qualified test name. Example:
#   python -m unittest test.Test.test_default_time_range_in_configuration_should_propagate_into_notification_time_range__issue47

from unittest import TestCase
import time
from graylog import Graylog

_PERIOD = 1


class Test(TestCase):

    #TODO should probably start graylog in a setupClass
    #     would require each test to remove everything it created in graylog
    #     but would be much faster
    def setUp(self) -> None:
        self._graylog = Graylog()
        self._graylog.start()

    def tearDown(self) -> None:
        self._graylog.stop()

    def test_get_alerts_should_be_found(self):
        status_code = self._graylog.get_alert_rules()
        self.assertEqual(200, status_code)

    def test_put_config_with_time_default_value_should_modify_time_default_value(self):
        default_time = 1441
        self._graylog.update_alert_rules_settings(default_time)
        configuration = self._graylog.get_alert_wizard_plugin_configuration()
        self.assertEqual(default_time, configuration['default_values']['time'])

    def test_create_alert_rule_should_not_fail(self):
        rule = {
            'field': 'source',
            'type': 1,
            'value': 'toto'
        }
        status_code = self._graylog.create_alert_rule_count('alert_rule_title', rule, _PERIOD)
        self.assertEqual(200, status_code)

    def test_set_logging_alert_configuration_should_not_fail(self):
        status_code = self._graylog.update_logging_alert_plugin_configuration()
        # TODO should be 200 instead of 202!!
        self.assertEqual(202, status_code)

    def test_default_time_range_in_configuration_should_propagate_into_notification_time_range__issue47(self):
        self._graylog.update_logging_alert_plugin_configuration()
        title = 'alert_rule_title'
        rule = {
            'field': 'source',
            'type': 1,
            'value': 'toto'
        }
        self._graylog.create_alert_rule_count(title, rule, _PERIOD)
        notification = self._graylog.get_notification_with_title(title)
        self.assertEqual(1441, notification['config']['aggregation_time'])

    def test_get_alert_rule_should_return_correct_additional_threshold_type__issue34(self):
        title = 'rule_title'
        self._graylog.create_alert_rule_and(title, _PERIOD)
        retrieved_alert_rule = self._graylog.get_alert_rule(title)
        self.assertEqual('LESS', retrieved_alert_rule['condition_parameters']['additional_threshold_type'])

    def test_export_alert_rule_should_return_correct_additional_threshold_type__issue34(self):
        title = 'rule_title'
        self._graylog.create_alert_rule_and(title, _PERIOD)
        exported_alert_rule = self._graylog.create_alert_rules_export([title])
        self.assertEqual('LESS', exported_alert_rule[0]['condition_parameters']['additional_threshold_type'])

    def test_get_alert_rule_should_return_correct_additional_threshold__issue69(self):
        title = 'rule_title'
        self._graylog.create_alert_rule_and(title, _PERIOD, additional_threshold=1)
        retrieved_alert_rule = self._graylog.get_alert_rule(title)
        self.assertEqual(1, retrieved_alert_rule['condition_parameters']['additional_threshold'])

    def test_create_alert_rule_with_list_should_generate_event_when_message_field_is_in_list(self):
        title = 'list'
        value = 'a'
        self._graylog.create_list(title, [value])
        rule = {
            'field': 'x',
            'type': 7,
            'value': title
        }
        self._graylog.create_alert_rule_count(title, rule, _PERIOD)
        with self._graylog.create_gelf_input() as inputs:
            inputs.send({'_x': value})
            # wait for the period (which is, unfortunately expressed in minutes, so it's quite long a wait)
            # TODO: should improve the API for better testability
            time.sleep(60*_PERIOD)
            inputs.send({'short_message': 'pop'})
            events_count = self._graylog.get_events_count()
            self.assertEqual(1, events_count)

