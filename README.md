# Alert Wizard Plugin for Graylog

[![Build Status](https://travis-ci.org/airbus-cyber/graylog-plugin-alert-wizard.svg?branch=master)](https://travis-ci.org/airbus-cyber/graylog-plugin-alert-wizard)
[![License](https://img.shields.io/badge/license-GPL--3.0-orange.svg)](https://www.gnu.org/licenses/gpl-3.0.txt)
[![GitHub Release](https://img.shields.io/badge/release-v3.0.1-blue.svg)](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/releases)

#### Alert Wizard plugin for Graylog to manage the alert rules

An alert wizard for configuring alert rules on Graylog.  
 
Perfect for example to configure together and at the same time a stream, an alert condition and a logging alert notification.  

**Required Graylog version:** 3.0.x  

**Required Graylog plugins:**
* [graylog-plugin-logging-alert](https://github.com/airbus-cyber/graylog-plugin-logging-alert)
* [graylog-plugin-aggregation-count](https://github.com/airbus-cyber/graylog-plugin-aggregation-count)
* [graylog-plugin-correlation-count](https://github.com/airbus-cyber/graylog-plugin-correlation-count)

## Graylog and Plugins Version Compatibility

| Wizard Plugin Version | Graylog Version | Logging Alert Plugin Version | Aggregation Count Plugin Version | Correlation Count Plugin Version |
| --------------------- | --------------- | ---------------------------- | -------------------------------- | -------------------------------- |
| 1.0.0                 | 2.4.x           | 1.0.x                        | 1.0.x                            | 1.0.x                            |
| 1.1.x                 | 2.5.x           | 1.0.x                        | 1.0.x                            | 1.0.x                            |
| 2.0.x                 | 2.5.x           | 1.1.x                        | 1.1.x                            | 1.1.x                            |
| 3.0.x                 | 3.0.x           | 1.2.x                        | 1.2.x                            | 1.2.x                            |
| 3.1.x                 | 3.0.x           | 1.2.x                        | 1.2.x                            | 1.2.x                            |

### Upgrading to 3.0.0

> **WARNING**: The REST API for the Wizard Configuration has changed.

### Upgrading to 2.0.0

> **WARNING**: With Wizard plugin in version 2.0.0 and higher you can't import alert rules that have been exported from version 1.X.X.

**Upgrading notice:**
1. Import your alert rules from version 1.X.X
2. Upgrade to version 2.0.0
3. Export your alert rules in the new format


## Installation

[Download the plugin](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/releases)
and place the `.jar` file in your Graylog plugin directory. The plugin directory
is the `plugins/` folder relative from your `graylog-server` directory by default
and can be configured in your `graylog.conf` file.

Restart `graylog-server` and you are done.

## Usage

### Manage the alert rules

![](https://raw.githubusercontent.com/airbus-cyber/graylog-plugin-alert-wizard/master/images/alert_rules.png)

### Create an alert rule

![](https://raw.githubusercontent.com/airbus-cyber/graylog-plugin-alert-wizard/master/images/create_alert_rule.png)

### Use of lists
> **WARNING**: The first time your create a rule with a list, the Wizard automatically create a lookup with cache and data adapter. But you must manually set up the authorization key with your login:password in base 64 for the data adapter.
![](https://raw.githubusercontent.com/airbus-cyber/graylog-plugin-alert-wizard/master/images/Wizard_List4.png)

>The field "Name" should be filled by "Authorization"

>The field "Value" should be filled by "Basic" followed by "user:password" in base64 for example "Basic TXlVc2Vy0k15UGFzc3dvcmQK" where TXlVc2Vy0k15UGFzc3dvcmQK is the result of "echo -n 'MyUser:MyPassword'|base64"

>MyUser must be a user with admin rights



## Build

This project is using Maven 3 and requires Java 8 or higher.

* Clone this repository.
* Run `mvn package` to build a JAR file.
* Optional: Run `mvn jdeb:jdeb` and `mvn rpm:rpm` to create a DEB and RPM package respectively.
* Copy generated JAR file in target directory to your Graylog plugin directory.
* Restart the Graylog.

## License

This plugin is released under version 3.0 of the [GNU General Public License](https://www.gnu.org/licenses/gpl-3.0.txt).
