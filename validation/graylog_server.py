import subprocess


class GraylogServer:

    def __init__(self, docker_compose_path):
        self._docker_compose_path = docker_compose_path

    def start(self):
        subprocess.run(['docker-compose', 'up', '--detach'], cwd=self._docker_compose_path)

    def stop(self):
        subprocess.run(['docker-compose', 'down'], cwd=self._docker_compose_path)
