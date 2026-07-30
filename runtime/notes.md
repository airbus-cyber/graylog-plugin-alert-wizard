# Manual test environment
## Description
* Global test env with yggdrasil
docker-compose-httpd.yml
* Simple test env
docker-compose.yml


## First time
```
docker compose -f docker-compose-httpd.yml up  --build
```

## Manually patch graylog
* Build graylog
```
cd graylog2-server
./mvnw clean; ./mvnw -X package -DskipTests=true
cd ..
```
* Extract jar in runtime directory

```
tar -xf graylog2-server/target/assembly/graylog-7.1.5-20260729072402-graylog-server-tarball.tar graylog.jar
mv -vf graylog.jar graylog-plugin-alert-wizard/runtime/graylog/build/graylog.jar
```
* Copy jar in docker container
```
docker cp graylog-plugin-alert-wizard/runtime/graylog/build/graylog.jar graylog:/usr/share/graylog/graylog.jar
docker restart graylog
```

## To start the test environment

* copy the built jar in directory ./graylog/plugin
* docker compose up
```
docker compose -f docker-compose-httpd.yml up
```
* visit http://127.0.0.1:9000/
* log in with admin/admin

## To stop the test environment
```
docker compose stop
```

## To restart the test environment
```
docker compose start && docker-compose logs --follow graylog
```

## To stop and clean the test environment
```
docker compose down
```
