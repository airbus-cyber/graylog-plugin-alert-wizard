# Change Log

All notable changes to this project will be documented in this file.

## [4.3.0](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/4.3.0...4.4.0)
### Changes
* When getting rest resources plugins/com.airbus_cyber_security.graylog.wizard/alerts/data and plugins/com.airbus_cyber_security.graylog.wizard/alerts/{title}: field title_condition is removed

## [4.3.0](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/4.2.2...4.3.0)
### Features
* Native aggregation is used instead of [graylog-plugin-aggregation-count](https://github.com/airbus-cyber/graylog-plugin-aggregation-count) ([issue #71](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/71))
### Bug Fixes
* The pipeline rule created with lists now matches exact values of the list rather than substrings ([issue #49](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/49))
* The default value of the severity in the wizard configuration is set to "Info" ([issue #61](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/61))
* The default value of the time range unit in the wizard configuration is set to "minutes" ([issue #62](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/62))
* The default value of the threshold type is set to "more than" ([issue #63](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/63))
* The default value of the matching type is set to "all"
* The default value of the grace is set to 1 ([issue #35](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/35))
* Updated graylog-plugin-correlation-count dependency to [4.1.2](https://github.com/airbus-cyber/graylog-plugin-correlation-count/blob/4.1.2/CHANGELOG.md)
### Changes
* Group/Distinct condition now accepts at most only one distinct field. During import of old rules, only the first distinct field is kept
* Rest resources plugins/com.airbus_cyber_security.graylog.wizard/alerts and plugins/com.airbus_cyber_security.graylog.wizard/alerts/{title}: condition_parameters.distinction_fields renamed into distinct_by. It now accepts only one value
* Rest resources plugins/com.airbus_cyber_security.graylog.wizard/alerts and plugins/com.airbus_cyber_security.graylog.wizard/alerts/{title}: condition_parameters.threshold_type and condition_parameters.additional_threshold_type values MORE/HIGHER and LESS/LOWER are replaced by > and <
* Removed rest resource plugins/com.airbus_cyber_security.graylog.wizard/alerts/import, imports are now implemented using regular create POSTs
* Removed rest resource plugins/com.airbus_cyber_security.graylog.wizard/alerts/export, exports are now implemented using regular GETs

## [4.2.2](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/4.2.1...4.2.2)
### Bug Fixes
* Updated graylog-plugin-correlation-count dependency to [4.1.1](https://github.com/airbus-cyber/graylog-plugin-correlation-count/blob/4.1.1/CHANGELOG.md)

## [4.2.1](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/4.2.0...4.2.1)
### Bug Fixes
* Updated graylog-plugin-aggregation-count dependency to [4.1.1](https://github.com/airbus-cyber/graylog-plugin-aggregation-count/blob/4.1.1/CHANGELOG.md)

## [4.2.0](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/4.1.0...4.2.0)
### Features
* Rules import: added search input to filter rules by their title ([issue #46](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/46))
* Rules import&export: replaced button "Select all" by checkbox to select/deselect all rules ([issue #64](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/64))
* Updated graylog-plugin-logging-alert dependency to [4.1.1](https://github.com/airbus-cyber/graylog-plugin-logging-alert/blob/4.1.1/CHANGELOG.md#411)
### Bug Fixes
* The correct value of the additional threshold is now displayed when editing an alert rule ([issue #69](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/69))
### Changes
* Rest resource plugins/com.airbus_cyber_security.graylog.wizard/alerts/{alert_name} now returns the same response as plugins/com.airbus_cyber_security.graylog.wizard/alerts/{alert_name}/data
* Removed rest resource plugins/com.airbus_cyber_security.graylog.wizard/alerts/{alert_name}/data

## [4.1.0](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/4.0.0...4.1.0)
### Features
* Add compatibility with Graylog 4.2

## [4.0.0](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/3.3.0...4.0.0)
### Features
* Add compatibility with Graylog 4.1
* Change plugin license to SSPL version 1
* Split "advanced settings" navigation button into two buttons, one to the alert definition, the other to the notification ([issue #57](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/57))
* Remove blocking dialog box at the end of rule creation ([issue #57](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/57))
* Add search input to filter rule to export by their title ([issue #46](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/46))
### Bug Fixes
* The Aggregation Time Range in the configuration of the plugin is now taken into account when creating the notification of a rule ([issue #47](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/47))
* The input of Fields Conditions is now case sensitive ([issue #48](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/48))
* Return correct value for the additional_threshold_type when requesting a rule ([issue #34](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/34))

## [3.3.0](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/3.2.5...3.3.0)
### Features
* Add compatibility with Graylog 3.3

## [3.2.5](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/3.2.4...3.2.5)
* Update dependencies

## [3.2.4](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/3.2.3...3.2.4)
* Update dependencies

## [3.2.3](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/3.2.2...3.2.3)
### Bug Fixes
* Fix #28 The log body defined in the Logging Alert Configuration Configuration is not used
* Update dependencies

## [3.2.2](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/3.2.1...3.2.2)
* Update dependencies

## [3.2.1](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/3.2.0...3.2.1)
### Bug Fixes
* Fix front issue

## [3.2.0](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/3.1.1...3.2.0)
### Features
* Add compatibility with Graylog 3.2

## [3.1.1](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/3.1.0...3.1.1)
### Features
* Fix #19 Pipelines created with new rule

## [3.1.0](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/3.0.1...3.1.0)
### Features
* Add the possibility to create lists and alert rules with condition on these lists

## [3.0.1](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/3.0.0...3.0.1)
### Bug Fixes
* Fix Performance problem

## [3.0.0](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/2.0.0...3.0.0)
### Features
* Add compatibility with Graylog 3.0

## [2.0.0](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/1.1.3...2.0.0)
### Features
* Export and Import all the notification parameters

## [1.1.3](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/1.1.2...1.1.3)
### Bug Fixes
* Fix the issue of infinite loading for the Field Rule component

## [1.1.2](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/1.1.1...1.1.2)
### Bug Fixes
* Fix the issue of notification reset ([issue #5](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/5))

## [1.1.1](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/1.1.0...1.1.1)
### Bug Fixes
* Fix the issue of alert rule title when the title includes a slash ([issue #4](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/4))

## [1.1.0](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/1.0.0...1.1.0)
### Features
* Add compatibility with Graylog 2.5

## [1.0.0](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/tree/1.0.0)
* First release
