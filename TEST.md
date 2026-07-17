# Run tests
## Requirements
```
npx playwright install chromium firefox
```
## Build
```
cd graylog-plugin-alert-wizard
./mvnw package
cp -vf target/graylog-plugin-*.jar "runtime/graylog/plugin"
```
## Execute cgraylog container
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
