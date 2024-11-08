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
        self._api = self._graylog.access_rest_api()
        self._gelf_input_identifier = self._api.create_gelf_input()

    def tearDown(self) -> None:
        self._api.delete_gelf_input(self._gelf_input_identifier)
        self._graylog.stop()

    def test_put_config_with_time_default_value_should_modify_time_default_value(self):
        default_time = 1441
        self._graylog.update_alert_wizard_plugin_configuration(default_time=default_time)
        configuration = self._graylog.get_alert_wizard_plugin_configuration()
        self.assertEqual(default_time, configuration['default_values']['time'])

    def test_set_logging_alert_configuration_should_not_fail(self):
        status_code = self._graylog.update_logging_alert_plugin_configuration()
        # TODO should be 200 instead of 202!!
        self.assertEqual(202, status_code)

    def test_default_time_range_in_configuration_should_propagate_into_notification_time_range__issue47(self):
        self._graylog.update_logging_alert_plugin_configuration()
        title = 'alert_rule_title'
        stream = {
            'field_rule': [{
                'field': 'source',
                'type': 1,
                'value': 'toto'
            }],
            'matching_type': 'AND'
        }
        rule = self._api.create_alert_rule_count(title, _PERIOD, stream=stream)
        notification = self._graylog.get_notification(rule['notification'])
        self.assertEqual(1441, notification['config']['aggregation_time'])

    def test_set_default_backlog_value_should_change_newly_created_event_definition_backlog_value__issue40(self):
        self._graylog.update_alert_wizard_plugin_configuration(backlog_size=1000)
        title = 'aaa'
        stream = {
            'field_rule': [{
                'field': 'source',
                'type': 1,
                'value': 'toto'
            }],
            'matching_type': 'AND'
        }
        alert_rule = self._api.create_alert_rule_count(title, _PERIOD, stream=stream)
        event_definition_identifier = alert_rule['condition']
        event_definition = self._graylog.get_event_definition(event_definition_identifier)
        backlog_size = event_definition['notification_settings']['backlog_size']
        self.assertEqual(1000, backlog_size)

    def test_update_alert_rule_should_set_event_definition_search_query__issue124(self):
        title = 'aaa'
        stream = {
            'field_rule': [{
                'field': 'source',
                'type': 1,
                'value': 'toto'
            }],
            'matching_type': 'AND'
        }
        rule = self._api.create_alert_rule_count(title, _PERIOD, search_query='query1234', stream=stream)
        updated_rule = rule.copy()
        updated_rule['condition_parameters']['search_query'] = 'new_search_query'
        self._graylog.update_alert_rule(title, updated_rule)
        alert_rule = self._graylog.get_alert_rule(title)
        event_definition_identifier = alert_rule['condition']
        event_definition = self._graylog.get_event_definition(event_definition_identifier)
        self.assertEqual('new_search_query', event_definition['config']['query'])

    def test_create_and_alert_rule_with_pipeline_condition_should_not_trigger_event_when_only_field_matches__issue119(self):
        # Create a list (for example the list "users" with 3 users : toto, tata, titi)
        list_title = 'users'
        self._graylog.create_list(list_title, ['toto', 'tata', 'titi'])

        # Create a COUNT rule with 2 conditions linked by an AND
        # 1st condition : field "user" is in list "users"
        # 2st condition: field "source" match exactly "source123"
        stream = {
            'field_rule': [{
                'field': 'x',
                'type': 7,
                'value': list_title
            }, {
                'field': 'source',
                'type': 1,
                'value': 'source123'
            }],
            'matching_type': 'AND'
        }
        alert_rule = self._api.create_alert_rule_count('A', _PERIOD, stream=stream)

        # Send a log with user=toto and source=sourceABC. It will be placed in the Stream because the pipeline function found the user in the list. So the rule will trigger but it is wrong because "source" is not equal to "source123"
        # Send a log with user=xxx and source=source123. It will be placed in the Stream beauce the only Stream rule is field "source" match exactly "source123". So the rule will trigger but it is wrong because "user" is not present in the list
        with self._graylog.access_gelf_input(self._gelf_input_identifier) as inputs:
            inputs.send({'host': 'source123'})
            aggregation_events_count = self._graylog.get_events_count('aggregation-v1')
            print(f'send: {aggregation_events_count}/{self._graylog.get_events_count()}')
            # wait for the period (which is, unfortunately expressed in minutes, so it's quite long a wait)
            # TODO: should improve the API for better testability
            time.sleep(60*_PERIOD)
            aggregation_events_count = self._graylog.get_events_count('aggregation-v1')
            print(f'slept for period: {aggregation_events_count}/{self._graylog.get_events_count()}')

            time.sleep(60)
            aggregation_events_count = self._graylog.get_events_count('aggregation-v1')
            print(f'before assert: {aggregation_events_count}/{self._graylog.get_events_count()}')
            self.assertEqual(0, self._graylog.get_events_count('aggregation-v1'))

    def test_create_alert_rule_with_list_should_generate_event_when_message_field_is_in_list(self):
        title = 'list'
        value = 'a'
        self._graylog.create_list(title, [value])
        stream = {
            'field_rule': [{
                'field': 'x',
                'type': 7,
                'value': title
            }],
            'matching_type': 'AND'
        }
        self._api.create_alert_rule_count(title, _PERIOD, stream=stream)
        with self._graylog.access_gelf_input(self._gelf_input_identifier) as inputs:
            inputs.send({'_x': value})
            # wait for the period (which is, unfortunately expressed in minutes, so it's quite long a wait)
            # TODO: should improve the API for better testability
            print(f'events count before sleep: {self._graylog.get_events_count()}')
            time.sleep(60*_PERIOD)
            print(f'events count after sleep: {self._graylog.get_events_count()}')
            inputs.send({'short_message': 'pop'})

            # wait until the event has propagated through graylog
            # TODO: try to make this code more readable
            for i in range(60):
                events_count = self._graylog.get_events_count('aggregation-v1')
                print(f'events count: {events_count}')
                if events_count == 1:
                    return
                time.sleep(1)
            events = self._graylog.get_events()
            for i in range(events['total_events']):
                print(events['events'][i])
            self.fail('Event not generated within 60 seconds')

    def test_create_alert_rule_with_list_should_not_generate_event_on_substrings_of_elements_in_list__issue49(self):
        print(f'Initially: {self._graylog.get_events_count()}')

        list_title = 'list'
        self._graylog.create_list(list_title, ['administrator', 'toto', 'root', 'foobar'])
        stream = {
            'field_rule': [{
                'field': 'x',
                'type': 7,
                'value': list_title
            }],
            'matching_type': 'AND'
        }
        self._api.create_alert_rule_count(list_title, _PERIOD, stream=stream)

        print(f'Before input: {self._graylog.get_events_count()}')

        starting_events_count = self._graylog.get_events_count('aggregation-v1')
        with self._graylog.access_gelf_input(self._gelf_input_identifier) as inputs:
            inputs.send({'_x': 'admin'})
            print(f'send: {self._graylog.get_events_count()}')
            # wait for the period (which is, unfortunately expressed in minutes, so it's quite long a wait)
            # TODO: should improve the API for better testability
            time.sleep(60*_PERIOD)
            print(f'slept: {self._graylog.get_events_count()}')
            inputs.send({'short_message': 'pop'})
            print(f'pop: {self._graylog.get_events_count()}')

            # TODO should do a wait_until_here...
            time.sleep(60)
            print(f'before assert: {self._graylog.get_events_count()}')
            print(self._graylog.get_events())
            events = self._graylog.get_events()
            for i in range(events['total_events']):
                print(events['events'][i])
            self.assertEqual(starting_events_count, self._graylog.get_events_count('aggregation-v1'))

    # TODO should try to move this method in the fast test suites. I do not know why it sometimes fail there. Should investigate
    def test_create_list_should_create_lookup_table_with_the_list_values(self):
        self._graylog.create_list('test', ['a'])
        response = self._graylog.query_lookup_table('alert-wizard-list-lookup-table-test', 'a')
        result = response.json()['single_value']
        self.assertEqual('a', result)
