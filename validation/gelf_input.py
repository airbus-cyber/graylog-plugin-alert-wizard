
class GelfInput:

    def __init__(self, api, identifier):
        self._api = api
        self._identifier = identifier

    def is_running(self):
        return self._api.gelf_input_is_running(self._identifier)