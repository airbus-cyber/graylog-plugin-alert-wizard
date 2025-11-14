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

_PERIOD = 1


class TestsFast(TestCase):

    @classmethod
    def setUpClass(cls) -> None:
        cls._graylog = Graylog()
        cls._graylog.start()

    @classmethod
    def tearDownClass(cls) -> None:
        cls._graylog.stop()

    def setUp(self):
        self._api = self._graylog.access_rest_api()
        self._gelf_input_identifier = self._api.create_gelf_input()

    def tearDown(self):
        rules = self._api.get_alert_rules().json()
        for rule in rules:
            self._api.delete_alert_rule(rule['id'])
        lists = self._api.get_lists()
        for list in lists['lists']:
            self._api.delete_list(list['title'])
            # Because it takes some time before the query adapter to be stopped
            # (that's not a problem in our code, already the case with Graylog data adapter's queries)
            self._graylog.wait_until_data_adapter_unavailable(list['title'])

        self._api.delete_gelf_input(self._gelf_input_identifier)

    def test_get_config_should_have_a_default_priority_info(self):
        configuration = self._graylog.get_alert_wizard_plugin_configuration()
        self.assertEqual(1, configuration['default_values']['priority'])

    def test_get_config_should_have_a_default_time_range_unit_of_minutes__issue62(self):
        configuration = self._graylog.get_alert_wizard_plugin_configuration()
        self.assertEqual(1, configuration['default_values']['time_type'])

    def test_get_alerts_should_be_found(self):
        response = self._graylog.get_alert_rules()
        self.assertEqual(200, response.status_code)

    def test_create_alert_rule_should_not_fail(self):
        stream = {
            'field_rule': [{
                'field': 'source',
                'type': 1,
                'value': 'toto'
            }],
            'matching_type': 'AND'
        }
        rule_title = 'alert_rule_title'
        alert_rule = self._api.create_alert_rule_count(rule_title, _PERIOD, stream=stream)
        self.assertEqual(rule_title, alert_rule['title'])

    def test_create_alert_rule_statistics_should_not_fail(self):
        rule_title = 'statistics'
        alert_rule = self._graylog.create_alert_rule_statistics(rule_title, _PERIOD)
        self.assertEqual(rule_title, alert_rule['title'])

    def test_get_alert_rule_should_return_correct_additional_threshold_type__issue34(self):
        title = 'rule_title'
        rule = self._graylog.create_alert_rule_and(title, _PERIOD)
        retrieved_alert_rule = self._graylog.get_alert_rule(rule['id'])
        self.assertEqual('<', retrieved_alert_rule['condition_parameters']['additional_threshold_type'])

    def test_get_alert_rule_should_return_correct_additional_threshold__issue69(self):
        title = 'rule_title'
        rule = self._graylog.create_alert_rule_and(title, _PERIOD, additional_threshold=1)
        retrieved_alert_rule = self._graylog.get_alert_rule(rule['id'])
        self.assertEqual(1, retrieved_alert_rule['condition_parameters']['additional_threshold'])

    def test_get_alert_rule_should_return_correct_field__issue143(self):
        title = 'rule_title'
        rule = self._api.create_alert_rule_statistics(title, _PERIOD)
        retrieved_alert_rule = self._graylog.get_alert_rule(rule['id'])
        self.assertEqual('x', retrieved_alert_rule['condition_parameters']['field'])

    def test_delete_alert_rule_with_no_conditions_should_not_delete_default_stream(self):
        title = 'rule_title'
        rule = self._api.create_alert_rule_count(title, _PERIOD)
        self._api.delete_alert_rule(rule['id'])
        default_stream = self._api.get_stream('000000000000000000000001')
        self.assertEqual(200, default_stream.status_code)

    def test_update_alert_rule_with_no_conditions_should_not_fail(self):
        title = 'rule_title'
        rule = self._api.create_alert_rule_count(title, _PERIOD)
        response = self._graylog.update_alert_rule(rule['id'], {**rule, 'description': 'new description'})
        self.assertEqual(202, response.status_code)

    def test_update_alert_rule_should_not_raise_exception_when_removing_conditions(self):
        title = 'rule_title'
        stream = {
            'field_rule': [{
                'field': 'source',
                'type': 1,
                'value': 'toto'
            }],
            'matching_type': 'AND'
        }
        rule = self._api.create_alert_rule_count(title, _PERIOD, stream=stream)
        stream = {
            'field_rule': [],
            'matching_type': 'AND'
        }
        self._graylog.start_logs_capture()
        self._graylog.update_alert_rule(rule['id'], {**rule, 'stream': stream})
        logs = self._graylog.extract_logs()
        self.assertNotIn('Exception', logs)

    def test_update_alert_rule_should_delete_stream_when_removing_conditions(self):
        title = 'rule_title'
        stream = {
            'field_rule': [{
                'field': 'source',
                'type': 1,
                'value': 'toto'
            }],
            'matching_type': 'AND'
        }
        rule = self._api.create_alert_rule_count(title, _PERIOD, stream=stream)
        stream = {
            'field_rule': [],
            'matching_type': 'AND'
        }
        self._graylog.update_alert_rule(rule['id'], {**rule, 'stream': stream})
        response = self._api.get_stream(rule['stream']['id'])
        self.assertEqual(404, response.status_code)

    def test_update_alert_rule_should_delete_stream_when_removing_stream_conditions_from_a_rule_with_list(self):
        list_title = 'users'
        self._graylog.create_list(list_title, ['toto', 'tata', 'titi'])
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
        title = 'A'
        rule = self._api.create_alert_rule_count(title, _PERIOD, stream=stream)
        stream = {
            'field_rule': [{
                'field': 'x',
                'type': 7,
                'value': list_title
            }],
            'matching_type': 'AND'
        }
        self._graylog.update_alert_rule(rule['id'], {**rule, 'stream': stream})
        response = self._api.get_stream(rule['stream']['id'])
        self.assertEqual(404, response.status_code)

    def test_create_alert_rule_should_set_event_definition_description__issue102(self):
        title = 'aaa'
        stream = {
            'field_rule': [{
                'field': 'source',
                'type': 1,
                'value': 'toto'
            }],
            'matching_type': 'AND'
        }
        alert_rule = self._api.create_alert_rule_count(title, _PERIOD, stream=stream, description='rule_description')
        event_definition_identifier = alert_rule['condition']
        event_definition = self._graylog.get_event_definition(event_definition_identifier)
        self.assertEqual('rule_description', event_definition['description'])

    def test_get_alert_rule_should_return_the_description_of_the_event_definition__issue102(self):
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
        event_definition = self._graylog.get_event_definition(alert_rule['condition'])
        event_definition['description'] = 'new_description'
        self._graylog.update_event_definition(event_definition)
        alert_rule = self._graylog.get_alert_rule(alert_rule['id'])
        self.assertEqual('new_description', alert_rule['description'])

    def test_update_alert_should_change_the_alert_description__issue102(self):
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
        self._graylog.update_alert_rule(rule['id'], {**rule, 'description': 'new_description'})
        alert_rule = self._graylog.get_alert_rule(rule['id'])
        self.assertEqual('new_description', alert_rule['description'])

    def test_get_alert_with_group_by_fields_should_contain_the_group_by_fields(self):
        title = 'rule_distinct'
        rule = {
            'field': 'source',
            'type': 1,
            'value': 'toto'
        }
        rule = self._graylog.create_alert_rule_group_distinct(title, rule, ['x'], '', _PERIOD)
        alert_rule = self._graylog.get_alert_rule(rule['id'])
        self.assertEqual(1, len(alert_rule['condition_parameters']['grouping_fields']))

    def test_get_alert_with_distinct_by_should_contain_the_distinct_by_field(self):
        title = 'rule_distinct'
        rule = {
            'field': 'source',
            'type': 1,
            'value': 'toto'
        }
        distinct_by = 'x'
        rule = self._graylog.create_alert_rule_group_distinct(title, rule, [], distinct_by, _PERIOD)
        alert_rule = self._graylog.get_alert_rule(rule['id'])
        self.assertEqual(distinct_by, alert_rule['condition_parameters']['distinct_by'])

    def test_get_alert_with_no_distinct_by_should_contain_an_empty_distinct_by_field(self):
        title = 'rule_count'
        stream = {
            'field_rule': [{
                'field': 'source',
                'type': 1,
                'value': 'toto'
            }],
            'matching_type': 'AND'
        }
        rule = self._api.create_alert_rule_count(title, _PERIOD, stream=stream)
        alert_rule = self._graylog.get_alert_rule(rule['id'])
        self.assertEqual('', alert_rule['condition_parameters']['distinct_by'])

    def test_create_alert_rule_then_should_not_fail(self):
        self._graylog.start_logs_capture()
        self._graylog.create_alert_rule_then('rule_then', '>', _PERIOD)
        logs = self._graylog.extract_logs()
        self.assertNotIn('ERROR', logs)

    def test_get_alert_rule_then_should_have_correct_threshold_type(self):
        title = 'rule_then'
        rule = self._graylog.create_alert_rule_then(title, '>', _PERIOD)
        alert_rule = self._graylog.get_alert_rule(rule['id'])
        self.assertEqual('>', alert_rule['condition_parameters']['threshold_type'])

    def test_create_alert_rule_with_same_name_should_not_fail(self):
        title = 'aaa'
        stream = {
            'field_rule': [{
                'field': 'source',
                'type': 1,
                'value': 'toto'
            }],
            'matching_type': 'AND'
        }
        self._api.create_alert_rule_count(title, _PERIOD, stream=stream)
        self._graylog.start_logs_capture()
        self._api.create_alert_rule_count(title, _PERIOD, stream=stream)
        logs = self._graylog.extract_logs()
        self.assertNotIn('ERROR', logs)

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
        self._graylog.update_alert_rule(alert_rule['id'], {**alert_rule, 'description': 'new description'})
        second_event_definition_identifier = alert_rule['second_event_definition']
        second_event_definition = self._graylog.get_event_definition(second_event_definition_identifier)
        self.assertEqual('new description', second_event_definition['description'])

    def test_create_alert_rule_or_should_set__event_definition_group_by__issue149(self):
        title = 'aaa'
        alert_rule = self._graylog.create_alert_rule_or(title, _PERIOD, group_by_fields=['groupField'])
        event_definition_identifier = alert_rule['condition']
        event_definition = self._graylog.get_event_definition(event_definition_identifier)
        self.assertEqual('groupField', event_definition['config']['group_by'][0])

    def test_create_alert_rule_or_should_set_second_event_definition_group_by__issue149(self):
        title = 'aaa'
        alert_rule = self._graylog.create_alert_rule_or(title, _PERIOD, group_by_fields=['groupField'])
        second_event_definition_identifier = alert_rule['second_event_definition']
        second_event_definition = self._graylog.get_event_definition(second_event_definition_identifier)
        self.assertEqual('groupField', second_event_definition['config']['group_by'][0])

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

    def test_update_alert_rule_should_not_disable_event_definition__issue140(self):
        title = 'aaa'
        alert_rule = self._api.create_alert_rule_count(title, _PERIOD)
        alert_rule = self._graylog.update_alert_rule(alert_rule['id'], {**alert_rule, 'description': 'new description'}).json()
        event_definition_identifier = alert_rule['condition']
        event_definition = self._graylog.get_event_definition(event_definition_identifier)
        print(event_definition)
        self.assertEqual('ENABLED', event_definition['state'])

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
        self._graylog.update_alert_rule(rule['id'], updated_rule)
        alert_rule = self._graylog.get_alert_rule(rule['id'])
        event_definition_identifier = alert_rule['condition']
        event_definition = self._graylog.get_event_definition(event_definition_identifier)
        self.assertEqual('new_search_query', event_definition['config']['query'])

    def test_create_list_should_create_data_adapter(self):
        self._graylog.create_list('test', ['a'])
        response = self._graylog.query_data_adapter('alert-wizard-list-data-adapter-test', 'a')
        self.assertEqual(200, response.status_code)

    # careful with the tests with lookup tables, because it sometimes take some time before the query adapter
    # (and thus the lookup table) can be queried
    def test_create_list_should_create_lookup_table_with_the_list_values(self):
        self._graylog.create_list('test', ['a'])
        response = self._graylog.query_lookup_table('alert-wizard-list-lookup-table-test', 'alert-wizard-list-data-adapter-test', 'a')
        result = response.json()['single_value']
        self.assertEqual('a', result)

    def test_alert_rule_with_no_conditions_should_trigger__issue139(self):
        self._api.create_alert_rule_count('rule_title', _PERIOD)
        starting_events_count = self._graylog.get_events_count()
        # TODO should create a gelf_input when instantiating graylog and delete it at the send
        #      so that other tests do not fail
        with self._graylog.access_gelf_input(self._gelf_input_identifier) as inputs:
            inputs.send({})
            # we have to wait for the period before the event triggers, then there might be some more processing time
            self._graylog.wait_until_new_event(starting_events_count, 2*_PERIOD)

    def test_clone_alert_rule_and_not_notification(self):
        rule = self._init_rule_with_updated_notification()
        response_clone = self._graylog.clone_alert_rule(rule['title'], 'cloneTitle', 'cloneDescription', False)
        cloned_rule = self._check_clone(response_clone)

        cloned_notification_id = cloned_rule['notification']
        cloned_notification = self._graylog.get_notification(cloned_notification_id)

        self.assertEqual(False, cloned_notification['config']['single_notification'])

    def test_clone_alert_rule_and_notification(self):
        rule = self._init_rule_with_updated_notification()
        response_clone = self._graylog.clone_alert_rule(rule['title'], 'cloneTitle', 'cloneDescription', True)
        cloned_rule = self._check_clone(response_clone)

        cloned_notification_id = cloned_rule['notification']
        cloned_notification = self._graylog.get_notification(cloned_notification_id)

        self.assertEqual(True, cloned_notification['config']['single_notification'])

    def _init_rule_with_updated_notification(self):
        created_rule = self._graylog.create_alert_rule_then('rule_then', '>', _PERIOD)
        notification_id = created_rule['notification']
        created_notification = self._graylog.get_notification(notification_id)
        updated_notification = created_notification.copy()
        updated_notification['config']['single_notification'] = True
        self._graylog.update_notification(notification_id, updated_notification)
        return created_rule

    def _check_clone(self, response_clone):
        cloned_rule = response_clone.json()
        self.assertEqual(200, response_clone.status_code)
        self.assertEqual('cloneTitle', cloned_rule['title'])
        self.assertEqual('cloneDescription', cloned_rule['description'])
        return cloned_rule

    def test_get_alert_rule_should_return_type_in_upper_case(self):
        title = 'rule_title'
        rule = self._api.create_alert_rule_statistics(title, _PERIOD)
        response = self._graylog.get_alert_rule(rule['id'])
        print(response)
        self.assertEqual('AVG', response['condition_parameters']['type'])

    def test_create_alert_with_disable_state_should_be_disabled__issue138(self):
        title = 'rule_title'
        rule = self._api.create_alert_rule_count(title, _PERIOD, disabled=True)

        response = self._api.get_alert_rule(rule['id'])
        self.assertEqual(True, response['disabled'])

    def test_create_alert_with_disable_state_should_have_disabled_event_definition__issue138(self):
        title = 'rule_title'
        create_response = self._api.create_alert_rule_count(title, _PERIOD, disabled=True)

        response = self._api.get_event_definition(create_response['condition'])
        self.assertEqual('DISABLED', response['state'])

    def test_create_alert_with_disable_state_should_have_disabled_stream__issue138(self):
        title = 'rule_title'
        stream = {
            'field_rule': [{
                'field': 'source',
                'type': 1,
                'value': 'toto'
            }],
            'matching_type': 'AND'
        }
        create_response = self._api.create_alert_rule_count(title, _PERIOD, stream=stream, disabled=True)

        response = self._api.get_stream(create_response['stream']['id'])
        self.assertEqual(True, response.json()['disabled'])

    def test_update_alert_rule_title_should_not_fail__issue128(self):
        title = 'title_to_update'
        rule = self._api.create_alert_rule_count(title, _PERIOD)
        response = self._graylog.update_alert_rule(rule['id'], {**rule, 'title': 'updated_title'}).json()

        self.assertEqual('updated_title', response['title'])

    def test_update_and_or_then_rule_should_set_stream__issue159(self):
        search_query_1 = 'src: x'
        search_query_2 = 'user: x'

        title = 'or_without_stream'
        stream_1 = {
            'field_rule': [{
                'field': 'src',
                'type': 1,
                'value': 'x'
            }],
            'matching_type': 'AND',
            'id': None
        }
        stream_2 = {
            'field_rule': [{
                'field': 'user',
                'type': 1,
                'value': 'x'
            }],
            'matching_type': 'AND',
            'id': None
        }
        alert_rule = self._graylog.create_alert_rule_or_without_stream(title, _PERIOD, search_query_1, search_query_2)
        updated_rule = alert_rule.copy()
        updated_rule['condition_parameters']['search_query'] = ''
        updated_rule['condition_parameters']['additional_search_query'] = ''
        updated_rule['stream'] = stream_1
        updated_rule['second_stream'] = stream_2

        self._graylog.update_alert_rule(alert_rule['id'], updated_rule)

        alert_rule = self._graylog.get_alert_rule(alert_rule['id'])

        updated_stream_1 = alert_rule['stream']
        self.assertIsNotNone(updated_stream_1['id'])
        self.assertEqual('src', updated_stream_1['field_rule'][0]['field'])

        updated_stream_2 = alert_rule['second_stream']
        self.assertIsNotNone(updated_stream_2['id'])
        self.assertEqual('user', updated_stream_2['field_rule'][0]['field'])

