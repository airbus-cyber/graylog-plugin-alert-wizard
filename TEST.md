# Run tests UI
## Requirements
* nodejs >=20
* playwright
```
npx playwright install chromium firefox
```
## Build
```
cd graylog-plugin-alert-wizard
./mvnw package
cp -vf target/graylog-plugin-*.jar "runtime/graylog/plugin"
```
## Execute graylog container
```
cd graylog-plugin-alert-wizard/runtime;docker compose up
```
## Execute tests
### All
```
npx playwright test -c playwright.config.js
```
### One in debug + UI
```
npx playwright test -c playwright.config.js --ui --debug tests/conflicted-rules.spec.js
```
# Run tests backend
## Requirements
* Python
```
apt install python3-dev
```
## Build
```
cd validation
python3 -m venv venv
source venv/bin/activate
pip3 install -r requirements.txt
docker compose --project-directory ../runtime pull
```
## Execute tests
### All
```
PYTHONUNBUFFERED=true python -m unittest test_brittle --verbose
```
### One
```
python -m unittest test_fasts.TestsFast.test_update_alert_rule_should_not_raise_exception_when_removing_conditions
```

