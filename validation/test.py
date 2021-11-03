# to execute these tests:
# * activate venv
#   source ./venv/bin/activate
# * execute tests
#   python -m unittest

from unittest import TestCase
from graylog_server import GraylogServer
import requests

_HEADERS = {"X-Requested-By": "test-program"}

class Test(TestCase):

    #TODO should probably start graylog in a setupClass
    def setUp(self) -> None:
        self._graylog = GraylogServer('../runtime')
        self._graylog.start()
        self._graylog.wait_until_log('Graylog server up and running.', 20)

    def tearDown(self) -> None:
        self._graylog.stop()

    def test_put_config_with_time_default_value_should_modify_time_default_value(self):
        payload = {
            "default_values": {
                "matching_type": "",
                "threshold_type": "",
                "time": "1441"
            },
            "field_order": []
        }
        response = requests.put('http://127.0.0.1:9000/api/plugins/com.airbus_cyber_security.graylog/config', json=payload,
                                auth=('admin', 'admin'), headers=_HEADERS)
        response = requests.get('http://127.0.0.1:9000/api/plugins/com.airbus_cyber_security.graylog/config', auth=('admin', 'admin'), headers=_HEADERS)
        body = response.json()
        self.assertEqual(1441, body["default_values"]["time"])

    # TODO
    #def test_default_time_range_in_configuration_should_propagate_into_notification_time_range__issue47(self):
    #   requests.put('http://127.0.0.1:9000/api/plugins/com.airbus_cyber_security.graylog/config', json=_PAYLOAD, auth=('admin', 'admin'), headers=_HEADERS)
        # create an alert rule
        # check the associated notification aggregation time range should be 1441 (the value set in the put request)
