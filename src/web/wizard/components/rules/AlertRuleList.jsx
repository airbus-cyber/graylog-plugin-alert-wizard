/*
 * Copyright (C) 2018 Airbus CyberSecurity (SAS)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

// sources of inspiration for this code:
// * components/nodes/SystemInformation.jsx

import PropTypes from 'prop-types';
import React from 'react';
import Reflux from 'reflux';
import createReactClass from 'create-react-class';
// TODO should rather use useIntl, but should convert to functional components first...
import { injectIntl, FormattedMessage } from 'react-intl';
import { LinkContainer } from 'react-router-bootstrap';
import moment from 'moment';

import { DataTable, IfPermitted, OverlayElement, Spinner, Timestamp } from 'components/common';
import { Button, Tooltip } from 'components/bootstrap';
import PermissionsMixin from 'util/PermissionsMixin';
import Routes from 'routing/Routes';
import { toDateObject } from 'util/DateTime';
import StreamsStore from 'stores/streams/StreamsStore';

import AlertRuleStore from 'wizard/stores/AlertRuleStore';
import AlertRuleActions from 'wizard/actions/AlertRuleActions';
import EventDefinitionResources from 'wizard/resources/EventDefinitionResource';
import ButtonToEventDefinition from 'wizard/components/buttons/ButtonToEventDefinition';
import ButtonToNotification from 'wizard/components/buttons/ButtonToNotification';
import AlertRuleCloneForm from './AlertRuleCloneForm';
import AlertRuleText from './AlertRuleText'


const AlertRuleList = createReactClass({
    displayName: 'AlertRuleList',

    mixins: [Reflux.connect(AlertRuleStore), PermissionsMixin],

    propTypes: {
        field_order: PropTypes.array.isRequired,
    },

    getInitialState() {
        const { intl } = this.props;
        const fieldsTitle = {
            title: intl.formatMessage({id: "wizard.title", defaultMessage: "Title"}),
            severity: intl.formatMessage({id: "wizard.severity", defaultMessage: "Severity"}),
            description: intl.formatMessage({id: "wizard.fieldDescription", defaultMessage: "Description"}),
            created: intl.formatMessage({id: "wizard.created", defaultMessage: "Created"}),
            lastModified: intl.formatMessage({id: "wizard.lastModified", defaultMessage: "Last Modified"}),
            user: intl.formatMessage({id: "wizard.user", defaultMessage: "User"}),
            status: intl.formatMessage({id: "wizard.status", defaultMessage: "Status"}),
            actions: intl.formatMessage({id: "wizard.actions", defaultMessage: "Actions"}),
            rule: intl.formatMessage({id: "wizard.rule", defaultMessage: "Rule"})
        };
        const messages = {
            infoDelete: intl.formatMessage({id: "wizard.buttonInfoDelete", defaultMessage: "Delete this alert rule"}),
            infoUpdate: intl.formatMessage({id: "wizard.buttonInfoUpdate", defaultMessage: "Edit this alert rule"}),
            infoEnable: intl.formatMessage({id: "wizard.buttonInfoEnable", defaultMessage: "Enable this alert rule"}),
            infoDisable: intl.formatMessage({id: "wizard.buttonInfoDisable", defaultMessage: "Disable this alert rule"}),
            createAlert: intl.formatMessage({id: "wizard.createAlert", defaultMessage: "Create alert rule"}),
            importAlert: intl.formatMessage({id: "wizard.importAlert", defaultMessage: "Import alert rules"}),
            exportAlert: intl.formatMessage({id: "wizard.exportAlert",  defaultMessage :"Export alert rules"}),
            confirmDeletion: intl.formatMessage({id: "wizard.confirmDeletion",  defaultMessage :"Do you really want to delete the alert rule"}),
            confirmDisable: intl.formatMessage({id: "wizard.confirmDisable",  defaultMessage :"Do you really want to disable the alert rule"}),
        };

        return {
            fieldsTitle: fieldsTitle,
            messages: messages
        };
    },

    componentDidMount() {
        this.list();
    },

    list() {
        AlertRuleActions.list().then(newAlerts => {
            this.setState({alerts: newAlerts});
        });
    },

    // TODO rename to _deleteAlertRule
    _deleteAlert(name) {
        if (!window.confirm(`${this.state.messages.confirmDeletion} "${name}" ?`)) {
            return;
        }
        AlertRuleActions.deleteByName(name).then(() => this.list());
    },

    _onResume(eventDefinitionIdentifier, stream, secondEventDefinitionIdentifier, stream2) {
        return async () => {
            const promises = [];
            promises.push(EventDefinitionResources.enable(eventDefinitionIdentifier));
            StreamsStore.resume(stream, response => response).finally(() => this.list());
            if (secondEventDefinitionIdentifier !== null) {
                promises.push(EventDefinitionResources.enable(secondEventDefinitionIdentifier));
            }
            if (stream2 !== null) {
                StreamsStore.resume(stream2, response => response).finally(() => this.list());
            }
            await Promise.all(promises);
        }
    },
    _onPause(name, eventDefinitionIdentifier, stream, secondEventDefinitionIdentifier, secondStream) {
        return async () => {
            if (!window.confirm(`${this.state.messages.confirmDisable} "${name}" ?`)) {
                return;
            }
            const promises = [];
            promises.push(EventDefinitionResources.disable(eventDefinitionIdentifier));
            StreamsStore.pause(stream, response => response).finally(() => this.list());
            if (secondEventDefinitionIdentifier) {
                promises.push(EventDefinitionResources.disable(secondEventDefinitionIdentifier));
            }
            if (secondStream !== null) {
                StreamsStore.pause(secondStream, response => response);
            }
            await Promise.all(promises);
        }
    },

    _headerCellFormatter(header) {
        let formattedHeaderCell;

        if (header == this.state.fieldsTitle.actions) {
            return <th className="actions">{header}</th>;
        }

        return <th>{header}</th>;
    },

    _onCloneSubmit(name, title, description) {
        AlertRuleActions.get(name).then(rule => {
            const newRule = {
                title: title,
                description: description,
                severity: rule.severity,
                condition_type: rule.condition_type,
                condition_parameters: rule.condition_parameters,
                stream: rule.stream,
                second_stream: rule.second_stream
            }
            AlertRuleActions.create(newRule).finally(() => this.list());
        });
    },
    
    _availableSeverityTypes() {
        return [
            {value: 'info', label: <FormattedMessage id="wizard.info" defaultMessage="Info" />},
            {value: 'low', label: <FormattedMessage id="wizard.low" defaultMessage="Low" />},
            {value: 'medium', label: <FormattedMessage id="wizard.medium" defaultMessage="Medium" />},
            {value: 'high', label: <FormattedMessage id="wizard.high" defaultMessage="High" />},
        ];
    },

    _getSeverityType(type) {
        return this._availableSeverityTypes().filter((t) => t.value === type)[0].label;
    },

    // TODO could simplify this code: return a dictionary instead of a list, move into getInitialState
    _availableFieldName() {
        const tooltipUser = (
            <Tooltip id="default-user-tooltip">
                <FormattedMessage id="wizard.tooltipUser" defaultMessage="The last user who modified the alert rule" />
            </Tooltip>
        );
        const userHeader = (
            <OverlayElement overlay={tooltipUser} placement="top" useOverlay={true} trigger={['hover', 'focus']}>
                {this.state.fieldsTitle.user}
            </OverlayElement>
        );
        return [
            {value: 'Severity', label: this.state.fieldsTitle.severity},
            {value: 'Description', label: this.state.fieldsTitle.description},
            {value: 'Created', label: this.state.fieldsTitle.created},
            {value: 'Last Modified', label: this.state.fieldsTitle.lastModified},
            {value: 'User', label: userHeader},
            {value: 'Status', label: this.state.fieldsTitle.status},
            {value: 'Rule', label: this.state.fieldsTitle.rule},
        ];
    },

    _getFieldName(field) {
        return this._availableFieldName().filter((t) => t.value === field)[0].label;
    },

    _alertInfoFormatter(alert) {
        let alertValid = true;
        let textColor = '';
        if (alert.condition_parameters === null || alert.stream === null) {
            alertValid = false;
            textColor = 'text-danger';
        } else if (alert.disabled) {
            textColor = 'text-muted';
        }

        // TODO rename ID into Identifier
        // TODO why is the default case '' for streamID and null for streamId2?
        let streamID = '';
        if (alert.stream) {
            streamID = alert.stream.id;
        }
        let streamId2 = null;
        if (alert.second_stream) {
            streamId2 = alert.second_stream.id;
        }

        // TODO should it be Button starting with an upper case here?
        const deleteAction = (
            <IfPermitted permissions="wizard_alerts_rules:delete">
                <button id="delete-alert" type="button" className="btn btn-primary"
                        title={this.state.messages.infoDelete} onClick={() => this._deleteAlert(alert.title)}>
                    <FormattedMessage id="wizard.delete" defaultMessage="Delete" />
                </button>
            </IfPermitted>
        );

        const updateAlert = (
            <IfPermitted permissions="wizard_alerts_rules:read">
                <LinkContainer to={Routes.pluginRoute('WIZARD_UPDATEALERT_ALERTRULETITLE')(alert.title.replace(/\//g, '%2F'))} disabled={!alertValid}>
                    <Button bsStyle="info" type="submit" title={this.state.messages.infoUpdate} >
                        <FormattedMessage id="wizard.edit" defaultMessage="Edit" />
                    </Button>
                </LinkContainer>
            </IfPermitted>
        );

        let toggleStreamLink;
        if (alert.disabled) {
            toggleStreamLink = (
                <Button bsStyle="success" onClick={this._onResume(alert.condition, streamID, alert.second_event_definition, streamId2)} disabled={!alertValid}
                        title={this.state.messages.infoEnable} style={{whiteSpace: 'pre'}} >
                    <FormattedMessage id="wizard.enable" defaultMessage="Enable " />
                </Button>
            );
        } else {
            toggleStreamLink = (
                <Button bsStyle="primary" onClick={this._onPause(alert.title, alert.condition, streamID, alert.second_event_definition, streamId2)} disabled={!alertValid}
                        title={this.state.messages.infoDisable} >
                    <FormattedMessage id="wizard.disable" defaultMessage="Disable" />
                </Button>
            );
        }

        const cloneAlert = <AlertRuleCloneForm alertTitle={alert.title} disabled={!alertValid} onSubmit={this._onCloneSubmit} />;

        const actions = (
            <div className="pull-left">
                {updateAlert}{' '}
                <ButtonToEventDefinition target={alert.condition} disabled={!alertValid} />{' '}
                <ButtonToNotification target={alert.notification} disabled={!alertValid} />{' '}
                {cloneAlert}{' '}
                {deleteAction}{' '}
                {toggleStreamLink}{' '}
            </div>
        );

        let tabFields = [<td className="limited">{alert.title}</td>];
        this.props.field_order.map((field) => {
            if (field.enabled) {
                switch (field.name) {
                    case 'Severity':
                        tabFields.push(<td className="limited">{alert.severity ? this._getSeverityType(alert.severity) : ''}</td>);
                        break;
                    case 'Description':
                        tabFields.push(<td className="limited"><span style={{whiteSpace: 'pre-line'}}>{alert.description}</span></td>);
                        break;
                    case 'Created':
                        tabFields.push(<td className="limited"><Timestamp dateTime={toDateObject(alert.created_at)} relative/></td>);
                        break;
                    case 'Last Modified':
                        tabFields.push(<td className="limited"><Timestamp dateTime={toDateObject(alert.last_modified)} relative/>
                        </td>);
                        break;
                    case 'User':
                        tabFields.push(<td className="limited">{alert.creator_user_id}</td>);
                        break;
                    case 'Status':
                        if (alertValid) {
                            tabFields.push(<td className="limited">{alert.disabled ? 
                                    <span style={{backgroundColor: 'orange', color: 'white'}}><FormattedMessage id="wizard.disabled" defaultMessage="Disabled" /></span> :
                                    <FormattedMessage id="wizard.enabled" defaultMessage="Enabled" />}</td>);
                        } else {
                            tabFields.push(<td className="limited"><FormattedMessage id="wizard.corrupted" defaultMessage="Corrupted" /></td>);
                        }
                        break;
                    case 'Rule':
                        tabFields.push(<td className="limited"><AlertRuleText alert={alert} /></td>);
                        break;
                    default:
                        break;
                }
            }
        });
        return (
            <tr key={alert.title} className={textColor} >
                {tabFields}
                <td style={{whiteSpace: 'nowrap'}}>{actions}</td>
            </tr>
        );
    },

    render() {
        const filterKeys = ['title', 'severity', 'created_at', 'last_modified', 'creator_user_id'];
        let headers = [this.state.fieldsTitle.title];
        this.props.field_order.map((field) => {
            if (field.enabled) {
                headers.push(this._getFieldName(field.name));
            }
        });
        headers.push(this.state.fieldsTitle.actions);

        if (this.state.alerts) {
            const filterLabel = this.props.intl.formatMessage({
              id: 'wizard.filter',
              defaultMessage: 'Filter alert rules'
            });
            return (
                <div>
                    <div className="pull-right has-bm">
                        <LinkContainer to={Routes.pluginRoute('WIZARD_NEWALERT')}>
                            <Button bsStyle="success" type="submit" title={this.state.messages.createAlert}>
                                <FormattedMessage id="wizard.create" defaultMessage="Create" />
                            </Button>
                        </LinkContainer>
                        {' '}
                        <LinkContainer to={Routes.pluginRoute('WIZARD_IMPORTALERT')}>
                            <Button bsStyle="success" type="submit" title={this.state.messages.importAlert}>
                                <FormattedMessage id="wizard.import" defaultMessage="Import" />
                            </Button>
                        </LinkContainer>
                        {' '}
                        <LinkContainer to={Routes.pluginRoute('WIZARD_EXPORTALERT')}>
                            <Button bsStyle="success" type="submit" title={this.state.messages.exportAlert}>
                                <FormattedMessage id="wizard.export" defaultMessage="Export" />
                            </Button>
                        </LinkContainer>
                    </div>
                    <DataTable id="alert-list"
                               className="table-hover"
                               headers={headers}
                               headerCellFormatter={this._headerCellFormatter}
                               sortByKey={"title"}
                               rows={this.state.alerts}
                               filterBy="title"
                               dataRowFormatter={this._alertInfoFormatter}
                               filterLabel={filterLabel}
                               filterKeys={filterKeys} />
                </div>
            );
        }
        return <Spinner/>
    },
});

export default injectIntl(AlertRuleList);
