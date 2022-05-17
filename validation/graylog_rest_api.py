import requests
import time
from urllib import parse
from requests.exceptions import ConnectionError

_AUTH = ('admin', 'admin')
_HEADERS = {'X-Requested-By': 'test-program'}


class GraylogRestApi:

    def _build_url(self, path):
        return parse.urljoin('http://127.0.0.1:9000/api/', path)

    def get(self, path):
        url = self._build_url(path)
        response = requests.get(url, auth=_AUTH, headers=_HEADERS)
        print('GET {} => {}'.format(url, response.status_code))
        return response

    def put(self, path, payload):
        url = self._build_url(path)
        response = requests.put(url, json=payload, auth=_AUTH, headers=_HEADERS)
        print('PUT {} {} => {}'.format(url, payload, response.status_code))
        return response

    def post(self, path, payload=None):
        url = self._build_url(path)
        response = requests.post(url, json=payload, auth=_AUTH, headers=_HEADERS)
        print('POST {} {} => {}'.format(url, payload, response.status_code))
        return response

    def wait_until_graylog_has_started(self):
        """
        We wait until the default deflector is up, as it seems to be the last operation done on startup
        This might have to change in the future, if graylog changes its ways...
        :return:
        """
        print('Waiting for graylog to start...')

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
