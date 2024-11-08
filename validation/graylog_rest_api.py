import requests
from urllib import parse
from requests.exceptions import ConnectionError
from gelf_input import GelfInput

_AUTH = ('admin', 'admin')
_HEADERS = {'X-Requested-By': 'test-program'}


class GraylogRestApi:

    def _build_url(self, path):
        return parse.urljoin('http://127.0.0.1:9000/api/', path)

    def _get(self, path, params=None):
        url = self._build_url(path)
        response = requests.get(url, params, auth=_AUTH, headers=_HEADERS)
        print(f'GET {url} => {response.status_code}')
        return response

    def _put(self, path, payload):
        url = self._build_url(path)
        response = requests.put(url, json=payload, auth=_AUTH, headers=_HEADERS)
        print(f'PUT {url} {payload} => {response.status_code}')
        return response

    def _post(self, path, payload=None):
        url = self._build_url(path)
        response = requests.post(url, json=payload, auth=_AUTH, headers=_HEADERS)
        print(f'POST {url} {payload} => {response.status_code}')
        return response

    def _delete(self, path):
        url = self._build_url(path)
        response = requests.delete(url, auth=_AUTH, headers=_HEADERS)
        print(f'DEL {url}')
        return response

    def default_deflector_is_up(self):
        try:
            response = self._get('system/deflector')
            body = response.json()
            if body['is_up']:
                return True
            return False
        except ConnectionError:
            return False

    def gelf_input_is_running(self, identifier):
        response = self._get('system/inputstates/')
        body = response.json()
        for state in body['states']:
            if state['id'] != identifier:
                continue
            return state['state'] == 'RUNNING'
        return False

    def create_gelf_input(self):
        payload = {
            'configuration': {
                'bind_address': '0.0.0.0',
                'decompress_size_limit': 8388608,
                'max_message_size': 2097152,
                'number_worker_threads': 8,
                'override_source': None,
                'port': 12201,
                'recv_buffer_size': 1048576,
                'tcp_keepalive': False,
                'tls_cert_file': '',
                'tls_client_auth': 'disabled',
                'tls_client_auth_cert_file': '',
                'tls_enable': False,
                'tls_key_file': 'admin',
                'tls_key_password': 'admin',
                'use_null_delimiter': True
            },
            'global': True,
            'title': 'Inputs',
            'type': 'org.graylog2.inputs.gelf.tcp.GELFTCPInput'
        }
        response = self._post('system/inputs', payload)
        identifier = response.json()['id']
        return GelfInput(self, identifier)

    def _create_alert_rule(self, title, stream, condition_type, time,
                           threshold_type='>', additional_threshold_type='', additional_threshold=0, second_stream=None,
                           group_by_fields=[], distinct_by='', field='', statistics_function='', description='',
                           search_query='', additional_search_query=''):
        alert_rule = {
            'condition_parameters': {
                'search_query': search_query,
                'additional_search_query': additional_search_query,
                'additional_threshold': additional_threshold,
                'additional_threshold_type': additional_threshold_type,
                'backlog': 500,
                'distinct_by': distinct_by,
                'field': field,
                'grace': 1,
                'grouping_fields': group_by_fields,
                'threshold': 0,
                'threshold_type': threshold_type,
                'time': time,
                'type': statistics_function
            },
            'condition_type': condition_type,
            'description': description,
            'priority': 1,
            'stream': stream,
            'title': title
        }
        if second_stream:
            alert_rule.update({
                'second_stream': second_stream
            })
        response = self._post('plugins/com.airbus_cyber_security.graylog.wizard/alerts', alert_rule)
        return response.json()

    def update_alert_rule(self, previousTitle, rule):
        return self._put(f'plugins/com.airbus_cyber_security.graylog.wizard/alerts/{previousTitle}', rule)

    # TODO have a default value for rule
    def create_alert_rule_count(self, title, stream, time, description, search_query):
        return self._create_alert_rule(title, stream, 'COUNT', time, description=description, search_query=search_query)

    def create_alert_rule_group_distinct(self, title, rule, group_by_fields, distinct_by, time):
        stream = {
            'field_rule': [rule],
            'matching_type': 'AND'
        }
        return self._create_alert_rule(title, stream, 'GROUP_DISTINCT', time, group_by_fields=group_by_fields, distinct_by=distinct_by)

    def create_alert_rule_statistics(self, title, time):
        rule = {
            'field': 'a',
            'type': 1,
            'value': 'b'
        }
        stream = {
            'field_rule': [rule],
            'matching_type': 'AND'
        }
        return self._create_alert_rule(title, stream, 'STATISTICAL', time, field='x', statistics_function='AVG')

    def create_alert_rule_then(self, title, threshold_type, time):
        rule = {
            'field': 'a',
            'type': 1,
            'value': 'b'
        }
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
        stream = {
            'field_rule': [rule],
            'matching_type': 'AND'
        }
        return self._create_alert_rule(title, stream, 'THEN', time, threshold_type=threshold_type,
                                       additional_threshold_type='>', additional_threshold=0, second_stream=second_stream)

    def create_alert_rule_and(self, title, time, additional_threshold=0):
        rule = {
            'field': 'a',
            'type': 1,
            'value': 'b'
        }
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
        stream = {
            'field_rule': [rule],
            'matching_type': 'AND'
        }
        return self._create_alert_rule(title, stream, 'AND', time, additional_threshold_type='<', additional_threshold=additional_threshold, second_stream=second_stream)

    def create_alert_rule_or(self, title, time, description):
        rule = {
            'field': 'a',
            'type': 1,
            'value': 'b'
        }
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
        stream = {
            'field_rule': [rule],
            'matching_type': 'AND'
        }
        return self._create_alert_rule(title, stream, 'OR', time, description=description, additional_threshold_type='<', second_stream=second_stream)

    def get_alert_rule(self, name):
        response = self._get(f'plugins/com.airbus_cyber_security.graylog.wizard/alerts/{name}')
        return response.json()

    def delete_alert_rule(self, name):
        self._delete(f'plugins/com.airbus_cyber_security.graylog.wizard/alerts/{name}')

    def get_alert_rules(self):
        return self._get('plugins/com.airbus_cyber_security.graylog.wizard/alerts')

    def update_logging_alert_plugin_configuration(self):
        configuration = {
            'aggregation_time': '1441',
            'alert_tag': 'LoggingAlert',
            'field_alert_id': 'id',
            'log_body': 'type: alert\nid: ${logging_alert.id}\nseverity: ${logging_alert.severity}\napp: graylog\nsubject: ${event_definition_title}\nbody: ${event_definition_description}\n${if backlog && backlog[0]} src: ${backlog[0].fields.src_ip}\nsrc_category: ${backlog[0].fields.src_category}\ndest: ${backlog[0].fields.dest_ip}\ndest_category: ${backlog[0].fields.dest_category}\n${end}',
            'overflow_tag': 'LoggingOverflow',
            'separator': ' | '
        }
        response = self._put('system/cluster_config/com.airbus_cyber_security.graylog.events.config.LoggingAlertConfig', configuration)
        return response.status_code

    def get_alert_wizard_plugin_configuration(self):
        response = self._get('plugins/com.airbus_cyber_security.graylog.wizard/config')
        return response.json()

    def update_alert_wizard_plugin_configuration(self, default_time=1, backlog_size=500):
        configuration = {
            'default_values': {
                'matching_type': '',
                'threshold_type': '',
                'time': default_time,
                'backlog': backlog_size
            },
            'field_order': []
        }
        self._put('plugins/com.airbus_cyber_security.graylog.wizard/config', configuration)

    def create_list(self, title, values):
        payload = {
            'description': '',
            # TODO: improve API => it should accept a list directly here...
            'lists': ';'.join(values),
            'title': title
        }
        self._post('plugins/com.airbus_cyber_security.graylog.wizard/lists', payload)

    def get_lists(self):
        return self._get('plugins/com.airbus_cyber_security.graylog.wizard/lists').json()

    def delete_list(self, name):
        self._delete(f'plugins/com.airbus_cyber_security.graylog.wizard/lists/{name}')

    def query_data_adapter(self, adapter_name, key):
        params = {
            'key': key
        }
        return self._get(f'system/lookup/adapters/{adapter_name}/query?key={key}', params=params)

    def query_lookup_table(self, table_name, key):
        params = {
            'key': key
        }
        return self._get(f'system/lookup/tables/{table_name}/query?key={key}', params=params)

    def get_notification(self, identifier):
        response = self._get(f'events/notifications/{identifier}')
        return response.json()

    def delete_notification(self, identifier):
        self._delete(f'events/notifications/{identifier}')

    def get_event_definition(self, identifier):
        response = self._get(f'events/definitions/{identifier}')
        return response.json()

    def delete_event_definition(self, identifier):
        self._delete(f'events/definitions/{identifier}')

    def update_event_definition(self, event_definition):
        identifier = event_definition['id']
        self._put(f'events/definitions/{identifier}', event_definition)

    def get_stream(self, identifier):
        return self._get(f'streams/{identifier}')

    def delete_stream(self, identifier):
        self._delete(f'streams/{identifier}')

    def get_events(self):
        response = self._post('events/search', {})
        return response.json()
