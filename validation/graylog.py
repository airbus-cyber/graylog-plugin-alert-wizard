import time
from graylog_server import GraylogServer
from graylog_rest_api import GraylogRestApi
from graylog_inputs import GraylogInputs


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

    def create_gelf_input(self):
        identifier = self._api.create_gelf_input()
        while not self._api.gelf_input_is_running(identifier):
            time.sleep(.1)
        return GraylogInputs()

    def update_logging_alert_plugin_configuration(self):
        return self._api.update_logging_alert_plugin_configuration()
    
    def update_alert_rules_settings(self, default_time):
        return self._api.update_alert_rules_settings(default_time)
    
    def get_alert_wizard_plugin_configuration(self):
        return self._api.get_alert_wizard_plugin_configuration()

    def get_alert_rule(self, name):
        return self._api.get_alert_rule(name)
    
    def get_alert_rules(self):
        return self._api.get_alert_rules()

    def create_alert_rule_count(self, *args):
        return self._api.create_alert_rule_count(*args)

    def create_alert_rule_group_distinct(self, *args):
        return self._api.create_alert_rule_group_distinct(*args)

    def create_alert_rule_and(self, *args, **kwargs):
        return self._api.create_alert_rule_and(*args, **kwargs)

    # TODO shouldn't it rather be named export_alert_rules?
    def create_alert_rules_export(self, alert_rule_titles):
        return self._api.create_alert_rules_export(alert_rule_titles)
    
    def create_list(self, *args):
        self._api.create_list(*args)

    def get_notification_with_title(self, title):
        return self._api.get_notification_with_title(title)

    def get_events_count(self):
        return self._api.get_events_count()

