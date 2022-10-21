import socket
import json

_GRAYLOG_INPUT_ADDRESS = ('127.0.0.1', 12201)


class GraylogInputs:

    def __init__(self):
        self._socket = socket.create_connection(_GRAYLOG_INPUT_ADDRESS)

    def send(self, args):
        data = dict({'version': '1.1', 'host': 'host', 'short_message': 'short_message'}, **args)
        print('Sending {}'.format(data))
        message = '{}\0'.format(json.dumps(data))
        self._socket.send(message.encode())

    def close(self):
        self._socket.close()

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_value, exc_traceback):
        self.close()
