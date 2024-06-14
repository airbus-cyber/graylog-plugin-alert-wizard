# Change Log

All notable changes to this project will be documented in this file.

## [5.2.0](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/5.1.3...5.2.0)
### Bug Fixes
* Rules where both is in list conditions and other field conditions should match, did not behave correctly ([issue #119](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/119))
* The list of alert rules could not be displayed after the deletion of a notification associated to a rule ([issue #116](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/116))
* The list of alert rules could not be displayed after the deletion of an event definition associated to a rule ([issue #117](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/117))
* The list of alert rules could not be displayed after an alert with a field condition without the type is created ([issue #120](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/120))
* The save button did not enable when changing the rule type ([issue #114](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/114))
* The save button was enabled without the second block of conditions being filled for rules of type THEN, AND and OR ([issue #86](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/86))
* In rule export, the selected rules count is reset when another set of rules is imported ([issue #88](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/88))
* Switching the theme did not change the style of the typeahead field input ([issue #118](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/118))
* Update graylog-plugin-logging-alert dependency to [5.1.3](https://github.com/airbus-cyber/graylog-plugin-logging-alert/blob/5.1.3/CHANGELOG.md)
### Changes
* POST plugins/com.airbus_cyber_security.graylog.wizard/alerts and PUT plugins/com.airbus_cyber_security.graylog.wizard/alerts/{title} field condition_type should be not null
* When an alert is corrupted (for instance when the associated notification has been manually deleted), the severity field returned by the REST routes may be null

## [5.1.3](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/5.1.2...5.1.3)
### Bug Fixes
* Update graylog-plugin-logging-alert dependency to [5.1.2](https://github.com/airbus-cyber/graylog-plugin-logging-alert/blob/5.1.2/CHANGELOG.md)

## [5.1.2](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/5.1.1...5.1.2)
### Bug Fixes
* Fixed layout of buttons in several pages ([issue #121](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/121))
* Clicking the delete button of a condition was always deleting the last condition ([issue #74](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/74))
* Disabling/enabling a AND rule was throwing an exception ([issue #129](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/129))
* Clone button was not working ([issue #131](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/131))
* Update graylog-plugin-correlation-count dependency to [5.1.1](https://github.com/airbus-cyber/graylog-plugin-correlation-count/blob/5.1.1/CHANGELOG.md)
* Update graylog-plugin-logging-alert dependency to [5.1.1](https://github.com/airbus-cyber/graylog-plugin-logging-alert/blob/5.1.1/CHANGELOG.md)

## [5.1.1](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/5.1.0...5.1.1)
### Bug Fixes
* Stack-trace when hovering over the user in the table of alert rules. The tooltip is now shown on the header only
* Incorrect field being updated when a condition of type "is present in list" is selected during rule creation
* Fixed highlighting of AND, THEN and OR conditions in dark mode

## [5.1.0](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/5.0.0...5.1.0)
### Features
* Add compatibility with [Graylog 5.1](https://www.graylog.org/post/announcing-graylog-v5-1/)

## [5.0.0](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/4.5.0...5.0.0)
### Features
* Add compatibility with [Graylog 5.0](https://www.graylog.org/post/announcing-graylog-v5-0-8/)
### Changes
* Removed column `Alerts` on the page displaying rules.
  When upgrading from a previous version, this field should be removed from the configuration. This can be done with the REST API
  (plugins/com.airbus_cyber_security.graylog.wizard/config endpoint)
* GET plugins/com.airbus_cyber_security.graylog.wizard/alerts/data has been removed
* GET plugins/com.airbus_cyber_security.graylog.wizard/alerts now returns the full information of each alert rules (what endpoint plugins/com.airbus_cyber_security.graylog.wizard/alerts/data used to return)
* GET plugins/com.airbus_cyber_security.graylog.wizard/alerts plugins/com.airbus_cyber_security.graylog.wizard/alerts/{title} do not return field `alert_count` in their results anymore

## [4.5.0](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/4.4.1...4.5.0)
### Features
* The rule description is now mapped to the event definition description ([issue #102](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/102))
* POST plugins/com.airbus_cyber_security.graylog.wizard/alerts now returns data about the newly created alert rule
* Resource plugins/com.airbus_cyber_security.graylog.wizard/alerts returns the second event definition identifier (if any) as field `second_event_definition`
* Disabling a wizard rule now disables the associated event definitions ([issue #58](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/58))
### Bug Fixes
* After deleting the stream associated with a rule, the list of rules would not load anymore ([issue #105](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/105))
* The backlog size value set in the configuration is now taken into account in the event definition ([issue #40](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/40))
* Several display bugs in dark mode fixed ([issue #59](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/59))
* French translation of statistical condition 'sum' was incorrect ([issue #112](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/112))
* Update graylog-plugin-logging-alert dependency to [4.4.1](https://github.com/airbus-cyber/graylog-plugin-logging-alert/blob/4.4.1/CHANGELOG.md)
### Changes
* POST plugins/com.airbus_cyber_security.graylog.wizard/alerts, field `description` should be not null (use an empty string, if there is no description)

## [4.4.1](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/4.4.0...4.4.1)
### Bug Fixes
* Rules export was not exporting field notification_parameters anymore ([issue #97](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/97))
* Rules import was not restoring notification split fields ([issue #97](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/97))

## [4.4.0](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/compare/4.3.0...4.4.0)
### Features
* Add compatibility with [Graylog 4.3](https://www.graylog.org/post/announcing-graylog-v4-3-graylog-operations-graylog-security) ([issue #75](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/75))
* Update graylog-plugin-correlation-count dependency to [4.2.0](https://github.com/airbus-cyber/graylog-plugin-correlation-count/blob/4.2.0/CHANGELOG.md) ([issue #90](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/90))
* Update graylog-plugin-logging-alert dependency to [4.2.0](https://github.com/airbus-cyber/graylog-plugin-logging-alert/blob/4.2.0/CHANGELOG.md) ([issue #89](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/89))
### Bug Fixes
* The default value of the matching type is set to "all" for the second stream too ([issue #85](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/85))
* Creation a rule with the same name as a previously created rule does not raise an exception anymore ([issue #96](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues96))
### Changes
* When getting rest resources plugins/com.airbus_cyber_security.graylog.wizard/alerts/data and plugins/com.airbus_cyber_security.graylog.wizard/alerts/{title}: field title_condition is removed
* Removed the possibility to configure field, field type and field value ([issue #94](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/issues/94))

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
