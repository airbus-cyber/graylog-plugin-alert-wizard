import subprocess
from threading import Thread


class GraylogServer:

    def __init__(self, docker_compose_path):
        self._docker_compose_path = docker_compose_path

    def start(self):
        subprocess.run(['docker-compose', 'up', '--detach'], cwd=self._docker_compose_path)

    def _look_for_log(self, expected_message, stream):
        while True:
            try:
                log = stream.readline()
                if log == '':
                    return
                if expected_message in log:
                    return
            except ValueError:
                # is raised when the underlying stream is closed (at the end of the sub-process)
                return

    def wait_until_log(self, expected_message, timeout=10):
        #, '--tail=1'
        with subprocess.Popen(['docker-compose', 'logs', '--no-color', '--follow', 'graylog'],
                              cwd=self._docker_compose_path, stdout=subprocess.PIPE, text=True) as graylog_logs:
            try:
                read_logs = Thread(target=self._look_for_log, args=[expected_message, graylog_logs.stdout])
                read_logs.start()
                read_logs.join(timeout=timeout)
                if read_logs.is_alive():
                    raise AssertionError('\'{}\' not found in logs before timeout {}'.format(expected_message, timeout))
            finally:
                # needed because the context manager is just a wait
                graylog_logs.terminate()

    def stop(self):
        subprocess.run(['docker-compose', 'down'], cwd=self._docker_compose_path)
