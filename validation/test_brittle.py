# to create and populate the test venv:
# * python3 -m venv venv
# * source venv/bin/activate
# * pip install -r requirements.txt
# to execute these tests:
# * activate venv
#   source ./venv/bin/activate
# * execute tests
#   python -m unittest --verbose
# To execute only one test, suffix with the fully qualified test name. Example:
#   python -m unittest test_brittle.TestBrittle.test_XXX

import shutil
from unittest import TestCase
from unittest import skip
import time
from graylog import Graylog

_PERIOD = 1


class TestBrittle(TestCase):

        def _print_disk_usage(self):
            total, used, free = shutil.disk_usage('/')

            print(f'Total: {total // (2**30)} GiB')
            print(f'Used: {used // (2**30)} GiB')
            print(f'Free: {free // (2**30)} GiB')

        def setUp(self) -> None:
            print('Before start')
            self._print_disk_usage()
            self._graylog = Graylog()
            self._graylog.start()
            print('After start')
            self._print_disk_usage()

        def tearDown(self) -> None:
            print('Before stop')
            self._print_disk_usage()
            self._graylog.stop()
            print('After stop')
            self._print_disk_usage()

