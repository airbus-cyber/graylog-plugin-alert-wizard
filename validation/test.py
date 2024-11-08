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

    def tearDown(self) -> None:
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

    def test_get_all_rules_should_not_fail_when_a_stream_is_deleted__issue105(self):
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
        self._graylog.delete_stream(alert_rule['stream']['id'])
        response = self._graylog.get_alert_rules()
        self.assertEqual(200, response.status_code)

    def test_get_all_rules_should_not_fail_when_an_event_definition_is_deleted__issue117(self):
        stream = {
            'field_rule': [{
                'field': 'source',
                'type': 1,
                'value': 'toto'
            }],
            'matching_type': 'AND'
        }
        alert_rule = self._api.create_alert_rule_count('alert_rule_title', _PERIOD, stream=stream)
        self._graylog.delete_event_definition(alert_rule['condition'])
        response = self._graylog.get_alert_rules()
        self.assertEqual(200, response.status_code)

    def test_get_all_rules_should_not_fail_when_a_notification_is_deleted__issue116(self):
        stream = {
            'field_rule': [{
                'field': 'source',
                'type': 1,
                'value': 'toto'
            }],
            'matching_type': 'AND'
        }
        alert_rule = self._api.create_alert_rule_count('alert_rule_title', _PERIOD, stream=stream)
        self._graylog.delete_notification(alert_rule['notification'])
        response = self._graylog.get_alert_rules()
        self.assertEqual(200, response.status_code)

    def test_get_all_rules_should_not_fail_after_rule_with_field_rule_without_type_is_created__issue120(self):
        stream = {
            'field_rule': [{
                'field': 'source',
                'type': '',
                'value': 'toto'
            }],
            'matching_type': 'AND'
        }
        self._api.create_alert_rule_count('alert_rule_title', _PERIOD, stream=stream)
        response = self._graylog.get_alert_rules()
        self.assertEqual(200, response.status_code)

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

    def test_create_alert_should_return_the_second_event_definition_identifier(self):
        alert_rule = self._graylog.create_alert_rule_or('aaa', _PERIOD)
        self.assertIn('second_event_definition', alert_rule)

    def test_create_alert_rule_or_should_set_second_event_definition_description__issue102(self):
        title = 'aaa'
        alert_rule = self._graylog.create_alert_rule_or(title, _PERIOD, description='second rule description')
        second_event_definition_identifier = alert_rule['second_event_definition']
        second_event_definition = self._graylog.get_event_definition(second_event_definition_identifier)
        self.assertEqual('second rule description', second_event_definition['description'])

    def test_update_alert_rule_or_should_update_second_event_definition_description__issue102(self):
        title = 'aaa'
        alert_rule = self._graylog.create_alert_rule_or(title, _PERIOD, description='description')
        self._graylog.update_alert_rule(title, {**alert_rule, 'description': 'new description'})
        second_event_definition_identifier = alert_rule['second_event_definition']
        second_event_definition = self._graylog.get_event_definition(second_event_definition_identifier)
        self.assertEqual('new description', second_event_definition['description'])

    def test_create_alert_rule_should_have_an_int_threshold(self):
        title = 'aaa'
        stream = {
            'field_rule': [{
                'field': 'source',
                'type': 1,
                'value': 'toto'
            }],
            'matching_type': 'AND'
        }
        rule = self._api.create_alert_rule_count(title, _PERIOD, stream=stream)
        self.assertIsInstance(rule['condition_parameters']['threshold'], int)

    def test_update_alert_rule_count_to_or_should_update_second_event_definition_description__issue102(self):
        title = 'aaa'
        stream = {
            'field_rule': [{
                'field': 'source',
                'type': 1,
                'value': 'toto'
            }],
            'matching_type': 'AND'
        }
        alert_rule = self._api.create_alert_rule_count(title, _PERIOD, stream=stream, description='description')
        alert_rule['condition_type'] = 'OR'
        alert_rule['second_stream'] = {
            'field_rule': [
                {
                    'field': 'b',
                    'type': 1,
                    'value': 'titi'
                }
            ],
            'matching_type': 'AND'
        }
        alert_rule['condition_parameters']['additional_search_query'] = ''
        alert_rule = self._graylog.update_alert_rule(title, {**alert_rule, 'description': 'new description'}).json()
        second_event_definition_identifier = alert_rule['second_event_definition']
        second_event_definition = self._graylog.get_event_definition(second_event_definition_identifier)
        self.assertEqual('new description', second_event_definition['description'])

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
        with self._graylog.create_gelf_input() as inputs:
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
        with self._graylog.create_gelf_input() as inputs:
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

        with self._graylog.create_gelf_input() as inputs:
            inputs.send({'_x': 'admin'})
            print(f'send: {self._graylog.get_events_count()}')
            # wait for the period (which is, unfortunately expressed in minutes, so it's quite long a wait)
            # TODO: should improve the API for better testability
            time.sleep(60*_PERIOD)
            print(f'slept: {self._graylog.get_events_count()}')
            inputs.send({'short_message': 'pop'})
            print(f'pop: {self._graylog.get_events_count()}')

            time.sleep(60)
            print(f'before assert: {self._graylog.get_events_count()}')
            print(self._graylog.get_events())
            events = self._graylog.get_events()
            for i in range(events['total_events']):
                print(events['events'][i])
            self.assertEqual(0, self._graylog.get_events_count('aggregation-v1'))

    def test_create_alert_rule_should_set_event_definition_search_query__issue124(self):
        title = 'aaa'
        stream = {
            'field_rule': [{
                'field': 'source',
                'type': 1,
                'value': 'toto'
            }],
            'matching_type': 'AND'
        }
        alert_rule = self._api.create_alert_rule_count(title, _PERIOD, search_query='query1234', stream=stream)
        event_definition_identifier = alert_rule['condition']
        event_definition = self._graylog.get_event_definition(event_definition_identifier)
        self.assertEqual('query1234', event_definition['config']['query'])

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
