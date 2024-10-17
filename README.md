# Alert Wizard Plugin for Graylog

[![Continuous Integration](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/actions/workflows/ci.yml/badge.svg)](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/license-SSPL-green)](https://www.mongodb.com/licensing/server-side-public-license)
[![GitHub Release](https://img.shields.io/github/v/release/airbus-cyber/graylog-plugin-alert-wizard)](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/releases)

#### Alert Wizard plugin for Graylog to manage the alert rules

An alert wizard for configuring alert rules on Graylog.
 
Perfect for example to configure together and at the same time a stream, an alert condition and a logging alert notification.

**Required Graylog version:** see compatibility table below for required version

**Packaged Graylog plugins:**
* [graylog-plugin-logging-alert](https://github.com/airbus-cyber/graylog-plugin-logging-alert)
* [graylog-plugin-correlation-count](https://github.com/airbus-cyber/graylog-plugin-correlation-count)

## Graylog Version Compatibility

| Wizard Plugin Version | Graylog Version |
|-----------------------|-----------------|
| 6.0.x                 | 6.0.x           |
| 5.2.x                 | 5.1.x<br>(>=5.1.13 for Wizard 5.2.0)<br>(>=5.1.9 for Wizard 5.2.1) |
| 5.1.x                 | 5.1.x           |
| 5.0.x                 | 5.0.x           |
| 4.5.x                 | 4.3.x           |
| 4.4.x                 | 4.3.x           |
| 4.3.x                 | 4.2.x           |
| 4.2.x                 | 4.2.x           |
| 4.1.x                 | 4.2.x           |
| 4.0.x                 | 4.1.x           |
| 3.3.x                 | 3.3.x           |
| 3.2.x                 | 3.2.x           |
| 3.1.x                 | 3.0.x           |
| 3.0.x                 | 3.0.x           |
| 2.0.x                 | 2.5.x           |
| 1.1.x                 | 2.5.x           |
| 1.0.0                 | 2.4.x           |


## Upgrading

### Upgrading to 5.x.x (TODO : FIX Version before delivery)

The 'search query' parameter of alert rules has been added. The following migration procedure must be followed:
1. Export rules from the preceding version,
2. Delete all rules,
3. Install version 5.x.x,
4. Import rules.

### Upgrading to 5.2.0

The internal representation of alert rules has been drastically changed. The following migration procedure must be followed:
1. Export rules from the preceding version,
2. Delete all rules,
3. Install version 5.2.0,
4. Import rules.

### Upgrading to 5.0.0
Column `Alerts` on the page displaying rules. This field should be removed from the configuration.
When updating from a previous version, this field should be removed from the configuration. This can be done with the REST API
(plugins/com.airbus_cyber_security.graylog.wizard/config endpoint)
To give a concrete example, for the following configuration, entry `{ "enabled": true, "name": "Alerts" },` should be removed from the `field_order` values.
```diff
{
  "default_values": {
    "severity": "info",
    "matching_type": "AND",
    "threshold_type": ">",
    "threshold": 0,
    "time": 1,
    "time_type": 1,
    "grace": 1,
    "backlog": 500,
    "title": ""
  },
  "field_order": [
    { "enabled": true, "name": "Severity" },
    { "enabled": true, "name": "Description" },
    { "enabled": true, "name": "Created" },
    { "enabled": true, "name": "Last Modified" },
    { "enabled": true, "name": "User" },
-    { "enabled": true, "name": "Alerts" },
    { "enabled": true, "name": "Status" },
    { "enabled": false, "name": "Rule" }
  ],
  "import_policy": "DONOTHING"
}
```

### Upgrading to 4.5.0

The alert rule description field is now mapped to the Event Definition description field.
To avoid losing the content of the description field when upgrading, you may follow the migration procedure:
1. Export rules from the preceding version,
2. Delete all rules,
3. Install version 4.5.0,
4. Import rules.

Alternatively, for each alert rule, you may, before the upgrade, set the description field of its corresponding
Event Definition with the value of the rule's description. This may be done manually or automatically via the REST API.

### Upgrading to 4.3.0

Follow the migration procedure:
1. Export rules and lists from the preceding version,
2. Delete all rules and lists,
3. Install version 4.3.0,
4. Import rules and lists.
Group/Distinct conditions now accept at most only one distinct field. During import of rules, only the first distinct field will be kept.

> **WARNING**: The REST API for rules has changed. See the [README](https://github.com/airbus-cyber/graylog-plugin-alert-wizard/blob/master/CHANGELOG.md).

### Upgrading to 3.2.0

**Possible issues to Import alert rules from version 3.0.0 or 3.1.0:**
* The field "grace" (Now display in Graylog and the Wizard as "Execute search every") have to be strictly greater than 0
* The Log Body of the notification will not be imported, the default one in the general configuration of the plugin 
[Logging Alert](https://github.com/airbus-cyber/graylog-plugin-logging-alert)
will be use, and have to follow the [Notification format](https://docs.graylog.org/en/latest/pages/alerts.html#notifications) 
(Same as the Email Notification)

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

The field "Name" should be filled by "Authorization"

The field "Value" should be filled by "Basic" followed by "user:password" in base64 for example "Basic TXlVc2Vy0k15UGFzc3dvcmQK" where TXlVc2Vy0k15UGFzc3dvcmQK is the result of "echo -n 'MyUser:MyPassword'|base64"

Instead of a user and its password you can also use a token.
Use the token's value as username and use the word "token" as password.
For example if the token's value is supertoken1234567890:
"echo -n 'supertoken1234567890:token'|base64"

MyUser must be a user with admin rights



## Build

Refer to this [Dockerfile](https://github.com/airbus-cyber/graylog-plugin-logging-alert/blob/master/build_docker/Dockerfile) to use the correct version of Java and Maven.

* Clone this repository.
* Clone [graylog2-server](https://github.com/Graylog2/graylog2-server) repository next to this repository.
* Build Graylog2-server with `mvn compile -DskipTests=true` (in graylog2-server folder)
* Run `mvn package` to build a JAR file (in this project folder).
* Optional: Run `mvn jdeb:jdeb` and `mvn rpm:rpm` to create a DEB and RPM package respectively.
* Copy generated JAR file in target directory to your Graylog plugin directory.
* Restart the Graylog.

## License

This plugin is released under version 1 of the [Server Side Public License (SSPL)](LICENSE).
