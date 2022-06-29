# Change Log

All notable changes to this project will be documented in this file.

## [4.0.1](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/4.0.0...4.0.1) (2022-06-29)
### Bug Fixes
* Updated graylog-plugin-aggregation-count dependency to [4.0.2](https://github.com/airbus-cyber/graylog-plugin-aggregation-count/blob/4.0.2/CHANGELOG.md)

## [4.0.0](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/3.3.0...4.0.0) (2021-12-01)
### Features
* Add compatibility with Graylog 4.1
* Change plugin license to SSPL version 1
* Split "advanced settings" navigation button into two buttons, one to the alert definition, the other to the notification (issue #57)
* Remove blocking dialog box at the end of rule creation (issue #57)
* Add search input to filter rule to export by their title (issue #46)
### Bug Fixes
* The Aggregation Time Range in the configuration of the plugin is now taken into account when creating the notification of a rule (issue #47)
* The input of Fields Conditions is now case sensitive (issue #48)
* Return correct value for the additional_threshold_type when requesting a rule (issue #34)

## [3.3.0](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/3.2.5...3.3.0) (2020-10-27)
### Features
* Add compatibility with Graylog 3.3

## [3.2.5](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/3.2.4...3.2.5) (2020-10-20)
* Update dependencies

## [3.2.4](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/3.2.3...3.2.4) (2020-09-27)
* Update dependencies

## [3.2.3](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/3.2.2...3.2.3) (2020-09-16)
### Bug Fixes
* Fix #28 The log body defined in the Logging Alert Configuration Configuration is not used
* Update dependencies

## [3.2.2](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/3.2.1...3.2.2) (2020-09-07)
* Update dependencies

## [3.2.1](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/3.2.0...3.2.1) (2020-09-01)
### Bug Fixes
* Fix front issue

## [3.2.0](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/3.1.1...3.2.0) (2020-08-13)
### Features
* Add compatibility with Graylog 3.2

## [3.1.1](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/3.1.0...3.1.1) (2020-04-17)
### Features
* Fix #19 Pipelines created with new rule

## [3.1.0](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/3.0.1...3.1.0) (2020-01-21)
### Features
* Add the possibility to create lists and alert rules with condition on these lists

## [3.0.1](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/3.0.0...3.0.1) (2019-08-22)
### Bug Fixes
* Fix Performance problem

## [3.0.0](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/2.0.0...3.0.0) (2019-07-01)
### Features
* Add compatibility with Graylog 3.0

## [2.0.0](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/1.1.3...2.0.0) (2019-06-28)
### Features
* Export and Import all the notification parameters

## [1.1.3](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/1.1.2...1.1.3) (2019-06-12)
### Bug Fixes
* Fix the issue of infinite loading for the Field Rule component

## [1.1.2](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/1.1.1...1.1.2) (2019-04-03)
### Bug Fixes
* Fix the issue of notification reset ([issue #5](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/5))

## [1.1.1](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/1.1.0...1.1.1) (2019-02-19)
### Bug Fixes
* Fix the issue of alert rule title when the title includes a slash ([issue #4](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/4))

## [1.1.0](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/1.0.0...1.1.0) (2019-02-01)
### Features
* Add compatibility with Graylog 2.5

## [1.0.0](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/tree/1.0.0) (2019-01-31)
* First release
