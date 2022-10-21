# Manual test environment

## To start the test environment

* copy the built jar in directory ./graylog/plugin
* docker-compose up
* visit http://127.0.0.1:9000/
* log in with admin/admin

## To stop the test environment

* docker-compose stop

## To restart the test environment

* docker-compose start && docker-compose logs --follow graylog

## To stop and clean the test environment

* execute docker-compose down
