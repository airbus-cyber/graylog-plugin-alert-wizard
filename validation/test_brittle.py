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
#   python -m unittest test_brittle.TestBrittle.test_XXX

from unittest import TestCase
from unittest import skip
import time
from graylog import Graylog

_PERIOD = 1


class TestBrittle(TestCase):

        def setUp(self) -> None:
            self._graylog = Graylog()
            self._graylog.start()

        def tearDown(self) -> None:
            self._graylog.stop()

        # TODO try to put this test back (seems to work locally but fails from time to time in continuous integration)
        #@skip
        def test_create_alert_rule_with_list_should_generate_event_when_message_field_is_in_list(self):
            title = 'list'
            value = 'a'
            self._graylog.create_list(title, [value])
            rule = {
                'field': 'x',
                'type': 7,
                'value': title
            }
            self._graylog.create_alert_rule_count(title, _PERIOD, rule=rule)
            with self._graylog.create_gelf_input() as inputs:
                inputs.send({'_x': value})
                # wait for the period (which is, unfortunately expressed in minutes, so it's quite long a wait)
                # TODO: should improve the API for better testability
                time.sleep(60*_PERIOD)
                inputs.send({'short_message': 'pop'})

                # wait until the event has propagated through graylog
                # TODO: try to make this code more readable
                for i in range(60):
                    events_count = self._graylog.get_events_count()
                    if events_count == 1:
                        return
                    time.sleep(1)
                self.fail('Event not generated within 60 seconds')

        # TODO try to put this test back (seems to work locally but not in continuous integration)
        @skip
        def test_create_alert_rule_with_list_should_not_generate_event_on_substrings_of_elements_in_list__issue49(self):
            list_title = 'list'
            self._graylog.create_list(list_title, ['administrator', 'toto', 'root', 'foobar'])
            rule = {
                'field': 'x',
                'type': 7,
                'value': list_title
            }
            self._graylog.create_alert_rule_count(list_title, _PERIOD, rule=rule)

            with self._graylog.create_gelf_input() as inputs:
                inputs.send({'_x': 'admin'})
                # wait for the period (which is, unfortunately expressed in minutes, so it's quite long a wait)
                # TODO: should improve the API for better testability
                time.sleep(60*_PERIOD)
                inputs.send({'short_message': 'pop'})

                time.sleep(60)
                self.assertEqual(0, self._graylog.get_events_count())