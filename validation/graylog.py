import time
from graylog_server import GraylogServer
from graylog_rest_api import GraylogRestApi
from graylog_inputs import GraylogInputs

_DEFAULT_RULE = {
    'field': 'source',
    'type': 1,
    'value': 'toto'
}


class Graylog:

    def __init__(self):
        self._server = GraylogServer('../runtime')
        self._api = GraylogRestApi()

    def _wait_until_graylog_has_started(self):
        """
        We wait until the default deflector is up, as it seems to be the last operation done on startup
        This might have to change in the future, if graylog changes its ways...
        :return:
        """
        print('Waiting for graylog to start...')

        while True:
            if self._api.default_deflector_is_up():
                break
            time.sleep(1)

    def start(self):
        self._server.start()
        self._wait_until_graylog_has_started()

    def stop(self):
        self._server.stop()

    def start_logs_capture(self):
        self._server.start_logs_capture()
    
    def extract_logs(self):
        return self._server.extract_logs()

    def create_gelf_input(self):
        identifier = self._api.create_gelf_input()
        while not self._api.gelf_input_is_running(identifier):
            time.sleep(.1)
        return GraylogInputs()

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

    def create_alert_rule_count(self, title, time, rule=_DEFAULT_RULE, description=''):
        return self._api.create_alert_rule_count(title, rule, time, description)

    def create_alert_rule_group_distinct(self, *args):
        return self._api.create_alert_rule_group_distinct(*args)

    def create_alert_rule_statistics(self, *args):
        return self._api.create_alert_rule_statistics(*args)

    def create_alert_rule_then(self, *args, **kwargs):
        return self._api.create_alert_rule_then(*args, **kwargs)

    def create_alert_rule_and(self, *args, **kwargs):
        return self._api.create_alert_rule_and(*args, **kwargs)

    def create_alert_rule_or(self, title, time, description=''):
        return self._api.create_alert_rule_or(title, time, description)

    def update_alert_rule(self, rule, new_description):
        updated_rule = {
            'title': rule['title'],
            'severity': rule['severity'],
            'description': new_description,
            'condition_type': rule['condition_type'],
            'condition_parameters': rule['condition_parameters'],
            'stream': rule['stream'],
            'second_stream': rule['second_stream']
        }
        return self._api.update_alert_rule(updated_rule)

    def create_list(self, *args):
        self._api.create_list(*args)

    def query_data_adapter(self, adapter_name, key):
        return self._api.query_data_adapter(adapter_name, key)

    def query_lookup_table(self, table_name, key):
        return self._api.query_lookup_table(table_name, key)

    def get_event_definition(self, identifier):
        return self._api.get_event_definition(identifier)

    def update_event_definition(self, event_definition):
        self._api.update_event_definition(event_definition)

    def get_notification_with_title(self, title):
        return self._api.get_notification_with_title(title)

    def delete_stream(self, identifier):
        self._api.delete_stream(identifier)
    def get_events_count(self):
        return self._api.get_events_count()

