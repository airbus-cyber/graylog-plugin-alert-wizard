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

    def test_set_logging_alert_configuration_should_not_fail(self):
        status_code = self._graylog.update_logging_alert_plugin_configuration()
        # TODO should be 200 instead of 202!!
        self.assertEqual(202, status_code)

    def test_create_alert_rule_with_list_should_not_generate_event_on_substrings_of_elements_in_list__issue49(self):
        list_title = 'list'
        self._graylog.create_list(list_title, ['administrator', 'toto', 'root', 'foobar'])
        rule = {
            'field': 'x',
            'type': 7,
            'value': list_title
        }
        self._graylog.create_alert_rule_count(list_title, rule, _PERIOD)

        with self._graylog.create_gelf_input() as inputs:
            inputs.send({'_x': 'admin'})
            # wait for the period (which is, unfortunately expressed in minutes, so it's quite long a wait)
            # TODO: should improve the API for better testability
            time.sleep(60*_PERIOD)
            inputs.send({'short_message': 'pop'})

            time.sleep(60)
            self.assertEqual(0, self._graylog.get_events_count())

    def test_create_alert_rule_then_should_not_fail(self):
        self._graylog.start_logs_capture()
        self._graylog.create_alert_rule_then('rule_then', '>', _PERIOD)
        logs = self._graylog.extract_logs()
        self.assertNotIn('ERROR', logs)

    def test_create_alert_rule_with_same_name_should_not_fail(self):
        title = 'aaa'
        rule = {
            'field': 'source',
            'type': 1,
            'value': 'toto'
        }
        self._graylog.create_alert_rule_count(title, rule, _PERIOD)
        self._graylog.start_logs_capture()
        self._graylog.create_alert_rule_count(title, rule, _PERIOD)
        logs = self._graylog.extract_logs()
        self.assertNotIn('ERROR', logs)
