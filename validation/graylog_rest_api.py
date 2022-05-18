import requests
import time
from urllib import parse
from requests.exceptions import ConnectionError

_AUTH = ('admin', 'admin')
_HEADERS = {'X-Requested-By': 'test-program'}


class GraylogRestApi:

    def _print(self, message):
        print(message, flush=True)

    def _build_url(self, path):
        return parse.urljoin('http://127.0.0.1:9000/api/', path)

    def get(self, path):
        url = self._build_url(path)
        response = requests.get(url, auth=_AUTH, headers=_HEADERS)
        self._print('GET {} => {}'.format(url, response.status_code))
        return response

    def _put(self, path, payload):
        url = self._build_url(path)
        response = requests.put(url, json=payload, auth=_AUTH, headers=_HEADERS)
        self._print('PUT {} {} => {}'.format(url, payload, response.status_code))
        return response

    def post(self, path, payload=None):
        url = self._build_url(path)
        response = requests.post(url, json=payload, auth=_AUTH, headers=_HEADERS)
        self._print('POST {} {} => {}'.format(url, payload, response.status_code))
        return response

    def wait_until_graylog_has_started(self):
        """
        We wait until the default deflector is up, as it seems to be the last operation done on startup
        This might have to change in the future, if graylog changes its ways...
        :return:
        """
        self._print('Waiting for graylog to start...')

        # TODO move as a method in _graylog_rest_api
        #only for 60s maximum
        while True:
            try:
                response = self.get('system/deflector')
                body = response.json()
                if body['is_up']:
                    break
            except ConnectionError:
                pass
            time.sleep(1)

    def _create_alert_rule(self, title, condition_type, additional_threshold_type='', additional_threshold=0, second_stream=None):
        alert_rule = {
            'condition_parameters': {
                'additional_threshold': additional_threshold,
                'additional_threshold_type': additional_threshold_type,
                'backlog': 500,
                'distinction_fields': [],
                'field': '',
                'grace': 1,
                'grouping_fields': [],
                'threshold': 0,
                'threshold_type': 'MORE',
                'time': 1,
                'type': ''
            },
            condition_type: condition_type,
            'severity': 'info',
            'stream': {
                'field_rule': [
                    {
                        'field': 'source',
                        'type': 1,
                        'value': 'toto'
                    }
                ],
                'matching_type': 'AND'
            },
            'title': title
        }
        if second_stream:
            alert_rule.update({
                'second_stream': second_stream
            })
        response = self._post('plugins/com.airbus_cyber_security.graylog.wizard/alerts', alert_rule)
        return response.status_code

    def create_alert_rule_count(self, title):
        return self._create_alert_rule(title, 'COUNT')

    def create_alert_rule_and(self, title, additional_threshold=0):
        second_stream = {
            'field_rule': [
                {
                    'field': 'b',
                    'type': 1,
                    'value': 'titi'
                }
            ],
            'matching_type': 'AND'
        }
        return self._create_alert_rule(title, 'AND', additional_threshold_type='LESS', additional_threshold=additional_threshold, second_stream=second_stream)

    def create_alert_rules_export(self, alert_rule_titles):
        export_selection = {
            'titles': alert_rule_titles
        }
        response = self._post('plugins/com.airbus_cyber_security.graylog.wizard/alerts/export', export_selection)
        return response.json()

    def get_alert_rule(self, name):
        response = self.get('plugins/com.airbus_cyber_security.graylog.wizard/alerts/' + name)
        return response.json()

    def update_logging_alert_plugin_configuration(self):
        configuration = {
            'aggregation_time': '1441',
            'alert_tag': 'LoggingAlert',
            'field_alert_id': 'id',
            'log_body': 'type: alert\nid: ${logging_alert.id}\nseverity: ${logging_alert.severity}\napp: graylog\nsubject: ${event_definition_title}\nbody: ${event_definition_description}\n${if backlog && backlog[0]} src: ${backlog[0].fields.src_ip}\nsrc_category: ${backlog[0].fields.src_category}\ndest: ${backlog[0].fields.dest_ip}\ndest_category: ${backlog[0].fields.dest_category}\n${end}',
            'overflow_tag': 'LoggingOverflow',
            'separator': ' | ',
            'severity': 'LOW'
        }
        response = self._put('system/cluster_config/com.airbus_cyber_security.graylog.events.config.LoggingAlertConfig', configuration)
        return response.status_code

    def update_alert_rules_settings(self, default_time):
        configuration = {
            'default_values': {
                'matching_type': '',
                'threshold_type': '',
                'time': default_time
            },
            'field_order': []
        }
        self._put('plugins/com.airbus_cyber_security.graylog.wizard/config', configuration)
