# These tests do not require for the graylog server to be shutdown and started between each of them
# Ideally all tests should be run in this suite, so that the start/shutdown of Graylog is done outside the tests
#
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
#   python -m unittest test.Test.test_create_alert_rule_with_list_should_generate_event_when_message_field_is_in_list

from unittest import TestCase
import time
from graylog import Graylog
from uuid import uuid4

_PERIOD = 1

class TestsFast(TestCase):

    @classmethod
    def setUpClass(cls) -> None:
        cls._graylog = Graylog()
        cls._graylog.start()

    @classmethod
    def tearDownClass(cls) -> None:
        cls._graylog.stop()

    def test_get_alerts_should_be_found(self):
        status_code = self._graylog.get_alert_rules()
        self.assertEqual(200, status_code)

    def test_create_alert_rule_should_not_fail(self):
        rule_title = f'alert_rule_title_{uuid4()}'
        alert_rule = self._graylog.create_alert_rule_count(rule_title, _PERIOD)
        self.assertEqual(rule_title, alert_rule['title'])

    def test_create_alert_rule_statistics_should_not_fail(self):
        rule_title = f'statistics_{uuid4()}'
        alert_rule = self._graylog.create_alert_rule_statistics(rule_title, _PERIOD)
        self.assertEqual(rule_title, alert_rule['title'])

    def test_get_alert_rule_should_return_correct_additional_threshold_type__issue34(self):
        title = f'rule_title_{uuid4()}'
        self._graylog.create_alert_rule_and(title, _PERIOD)
        retrieved_alert_rule = self._graylog.get_alert_rule(title)
        self.assertEqual('<', retrieved_alert_rule['condition_parameters']['additional_threshold_type'])

    def test_get_alert_rule_should_return_correct_additional_threshold__issue69(self):
        title = f'rule_title_{uuid4()}'
        self._graylog.create_alert_rule_and(title, _PERIOD, additional_threshold=1)
        retrieved_alert_rule = self._graylog.get_alert_rule(title)
        self.assertEqual(1, retrieved_alert_rule['condition_parameters']['additional_threshold'])

    def test_create_list_should_create_data_adapter(self):
        self._graylog.create_list('test', ['a'])
        response = self._graylog.query_data_adapter('alert-wizard-list-data-adapter-test', 'a')
        self.assertEqual(200, response.status_code)

    def test_alert_rule_with_no_conditions_should_trigger__issue139(self):
        stream = {
            'field_rule': [],
            'matching_type': 'AND'
        }
        self._graylog.create_alert_rule_count(f'rule_title_{uuid4()}', _PERIOD, stream=stream)
        # TODO should create a gelf_input when instantiating graylog and delete it at the send
        #      so that other tests do not fail
        with self._graylog.create_gelf_input() as inputs:
            inputs.send({})
            # we have to wait for the period before the event triggers, then there might be some more processing time
            self._graylog.wait_until_aggregation_event(2*_PERIOD)
