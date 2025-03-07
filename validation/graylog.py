import time
from graylog_server import GraylogServer
from graylog_rest_api import GraylogRestApi
from graylog_inputs import GraylogInputs
from server_timeout_error import ServerTimeoutError

_DEFAULT_STREAM = {
    'field_rule': [{
        'field': 'source',
        'type': 1,
        'value': 'toto'
    }],
    'matching_type': 'AND'
}

class Graylog:

    def __init__(self):
        self._server = GraylogServer('../runtime')
        self._api = GraylogRestApi()

    def _wait(self, condition, attempts, sleep_duration=1):
        count = 0
        while not condition():
            time.sleep(sleep_duration)
            count += 1
            if count > attempts:
                print('Graylog server logs: ', self._server.extract_all_logs())
                raise ServerTimeoutError()

    def _wait_until_graylog_has_started(self):
        """
        We wait until the default deflector is up, as it seems to be the last operation done on startup
        This might have to change in the future, if graylog changes its ways...
        :return:
        """
        print('Waiting for graylog to start...')
        self._wait(self._api.default_deflector_is_up, 180)

    def start(self):
        self._server.start()
        self._wait_until_graylog_has_started()

    def stop(self):
        self._server.stop()

    def start_logs_capture(self):
        self._server.start_logs_capture()
    
    def extract_logs(self):
        return self._server.extract_logs()

    def access_gelf_input(self, gelf_input_identifier):
        gelf_input_is_running = lambda: self._api.gelf_input_is_running(gelf_input_identifier)
        self._wait(gelf_input_is_running, 10, sleep_duration=.1)
        return GraylogInputs()

    def access_rest_api(self):
        return self._api

    def update_logging_alert_plugin_configuration(self):
        return self._api.update_logging_alert_plugin_configuration()
    
    def update_alert_wizard_plugin_configuration(self, default_time=1, backlog_size=500):
        return self._api.update_alert_wizard_plugin_configuration(default_time=default_time, backlog_size=backlog_size)
    
    def get_alert_wizard_plugin_configuration(self):
        return self._api.get_alert_wizard_plugin_configuration()

    def get_alert_rule(self, name):
        return self._api.get_alert_rule(name)
    
    def get_alert_rules(self):
        return self._api.get_alert_rules()

    def create_alert_rule_group_distinct(self, *args):
        return self._api.create_alert_rule_group_distinct(*args)

    def create_alert_rule_statistics(self, *args):
        return self._api.create_alert_rule_statistics(*args)

    def create_alert_rule_then(self, *args, **kwargs):
        return self._api.create_alert_rule_then(*args, **kwargs)

    def create_alert_rule_and(self, *args, **kwargs):
        return self._api.create_alert_rule_and(*args, **kwargs)

    def create_alert_rule_or(self, title, time, description='', group_by_fields=[]):
        return self._api.create_alert_rule_or(title, time, description, group_by_fields)

    def update_alert_rule(self, previousTitle, rule):
        updated_rule = {
            'title': rule['title'],
            'priority': rule['priority'],
            'description': rule['description'],
            'condition_type': rule['condition_type'],
            'condition_parameters': rule['condition_parameters'],
            'stream': rule['stream'],
            'second_stream': rule['second_stream']
        }
        return self._api.update_alert_rule(previousTitle, updated_rule)

    def clone_alert_rule(self, source_title, title, description, clone_notification):
        clone_request = {
            'source_title': source_title,
            'title': title,
            'description': description,
            'clone_notification': clone_notification
        }
        return self._api.clone_alert_rule(clone_request)

    def create_list(self, *args):
        return self._api.create_list(*args)

    def wait_until_data_adapter_unavailable(self, name):
        query_adapter_is_unavailable = lambda: self._api.query_data_adapter(name, 'stopped test key').status_code == 404
        self._wait(query_adapter_is_unavailable, 10, sleep_duration=.1)

    def _wait_until_data_adapter_available(self, name):
        query_adapter_is_ready = lambda: self._api.query_data_adapter(name, 'started test key').status_code != 404
        self._wait(query_adapter_is_ready, 10, sleep_duration=.1)

    # Because it sometimes takes some time before the data_adapter can be queried without returning 404
    def query_data_adapter(self, adapter_name, key):
        self._wait_until_data_adapter_available(adapter_name)
        return self._api.query_data_adapter(adapter_name, key)

    def query_lookup_table(self, table_name, data_adapter_name, key):
        # Because it sometimes takes some time before the data_adapter can be queried without returning 404
        # Here we assume the lookup table and data adapter have the same name
        self._wait_until_data_adapter_available(data_adapter_name)
        return self._api.query_lookup_table(table_name, key)

    def get_event_definition(self, identifier):
        return self._api.get_event_definition(identifier)

    def update_event_definition(self, event_definition):
        self._api.update_event_definition(event_definition)

    def delete_event_definition(self, identifier):
        self._api.delete_event_definition(identifier)

    def get_notification(self, identifier):
        return self._api.get_notification(identifier)

    def update_notification(self, identifier, notification):
        return self._api.update_notification(identifier, notification)

    def delete_notification(self, identifier):
        self._api.delete_notification(identifier)

    def delete_stream(self, identifier):
        self._api.delete_stream(identifier)

    def get_events(self):
        return self._api.get_events()

    def get_events_count(self, event_definition_type=None):
        response = self.get_events()
        total = response['total_events']
        if event_definition_type is None:
            return total
        result = 0
        for i in range(total):
            event = response['events'][i]['event']
            if event['event_definition_type'] == event_definition_type:
                result += 1
        return result

    def _has_aggregation_event(self):
        events_count = self.get_events_count('aggregation-v1')
        return events_count == 1

    def wait_until_new_event(self, initial_event_count, wait_duration):
        has_new_event = lambda: self.get_events_count('aggregation-v1') == initial_event_count + 1
        self._wait(has_new_event, 60*wait_duration)
