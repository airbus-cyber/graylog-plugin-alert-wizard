# to execute these tests:
# * activate venv
#   source ./venv/bin/activate
# * execute tests
#   python -m unittest

from unittest import TestCase
from graylog_server import GraylogServer
import requests
from urllib import parse

_AUTH = ('admin', 'admin')
_HEADERS = {"X-Requested-By": "test-program"}
_URL_PREFIX = 'http://127.0.0.1:9000/api/plugins/com.airbys_security.graylog'

class Test(TestCase):

    #TODO should probably start graylog in a setupClass
    def setUp(self) -> None:
        self._graylog = GraylogServer('../runtime')
        self._graylog.start()
        self._graylog.wait_until_log('Graylog server up and running.', 20)

    def tearDown(self) -> None:
        self._graylog.stop()

    def _build_url(self, path):
        return parse.urljoin('http://127.0.0.1:9000/api/plugins/com.airbus_cyber_security.graylog/', path)

    def _get(self, path):
        url = self._build_url(path)
        print(url)
        return requests.get(url, auth=_AUTH, headers=_HEADERS)

    def _put(self, path, payload):
        url = self._build_url(path)
        requests.put(url, json=payload, auth=_AUTH, headers=_HEADERS)

    def test_get_alerts_should_be_found(self):
        response = self._get('alerts/data')
        self.assertEqual(200, response.status_code)

    def test_put_config_with_time_default_value_should_modify_time_default_value(self):
        payload = {
            "default_values": {
                "matching_type": "",
                "threshold_type": "",
                "time": "1441"
            },
            "field_order": []
        }
        self._put('config', payload)
        response = self._get('config')
        body = response.json()
        self.assertEqual(1441, body["default_values"]["time"])

    # TODO
#    def test_default_time_range_in_configuration_should_propagate_into_notification_time_range__issue47(self):
#       requests.put('http://127.0.0.1:9000/api/plugins/com.airbus_cyber_security.graylog/config', json=_PAYLOAD, auth=('admin', 'admin'), headers=_HEADERS)
        # create an alert rule
        # check the associated notification aggregation time range should be 1441 (the value set in the put request)
