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

import React from 'react';
import { useState, useCallback, useEffect } from 'react';
import { EntityDataTable, NoSearchResult, Timestamp, IfPermitted } from 'components/common';
import { LinkContainer } from 'react-router-bootstrap';
import { Button } from 'components/bootstrap';
import AlertRuleActions from "../../actions/AlertRuleActions";
import { useIntl, FormattedMessage } from "react-intl";
import AlertRuleBulkActions from "./AlertRuleBulkActions";
import { toDateObject } from 'util/DateTime';
import AlertRuleText from "./AlertRuleText";
import Routes from 'routing/Routes';
import ButtonToEventDefinition from "../buttons/ButtonToEventDefinition";
import ButtonToNotification from "../buttons/ButtonToNotification";
import AlertRuleCloneForm from "./AlertRuleCloneForm";
import EventDefinitionResources from "../../resources/EventDefinitionResource";
import StreamsStore from 'stores/streams/StreamsStore';

function _availablePriorityTypes() {
    return [
        {value: 1, label: <FormattedMessage id="wizard.low" defaultMessage="Low" />},
        {value: 2, label: <FormattedMessage id="wizard.medium" defaultMessage="Normal" />},
        {value: 3, label: <FormattedMessage id="wizard.high" defaultMessage="High" />},
    ];
}

function _getPriorityType(type) {
    return _availablePriorityTypes().filter((t) => t.value === type)[0].label;
}

function _convertAlertToElement(alert) {
    let alertValid = true;
    let textColor = '';
    if (alert.condition === null || alert.condition_parameters === null || alert.stream === null || alert.notification === null) {
        alertValid = false;
        textColor = 'text-danger';
    } else if (alert.disabled) {
        textColor = 'text-muted';
    }
    let streamId = '';
    if (alert.stream) {
        streamId = alert.stream.id;
    }
    let streamId2 = null;
    if (alert.second_stream) {
        streamId2 = alert.second_stream.id;
    }

    return {
        id: alert.title,
        title: alert.title,
        priority: _getPriorityType(alert.priority),
        description: alert.description,
        created: alert.created_at,
        lastModified: alert.last_modified,
        user: alert.creator_user_id,
        status: alert.disabled,
        valid: alertValid,
        textColor: textColor,
        streamId: streamId,
        streamId2: streamId2,
        condition: alert.condition,
        notification: alert.notification,
        secondEventDefinition: alert.second_event_definition
    };
}

function computeVisibleColumn(fieldOrder, fieldsTitle, displayedColumns) {
    const columns = fieldOrder.map((field) => field.name)
        .map((fieldName) => fieldsTitle.find(x => x.config === fieldName).key)
        .filter(columnName => displayedColumns.includes(columnName));

    return displayedColumns.includes('title') ? [...['title'], ...columns] : columns;
}

const AlertRulesContainer = ({ fieldOrder }) => {

    const intl = useIntl();

    const fieldsTitle = [
        {key: 'title', label: intl.formatMessage({id: "wizard.title", defaultMessage: "Title"}), config: 'title'},
        {key: 'priority', label: intl.formatMessage({id: "wizard.priority", defaultMessage: "Priority"}), config: 'Priority'},
        {key: 'description', label: intl.formatMessage({id: "wizard.fieldDescription", defaultMessage: "Description"}), config: 'Description'},
        {key: 'created', label: intl.formatMessage({id: "wizard.created", defaultMessage: "Created"}), config: 'Created'},
        {key: 'lastModified', label: intl.formatMessage({id: "wizard.lastModified", defaultMessage: "Last Modified"}), config: 'Last Modified'},
        {key: 'user', label: intl.formatMessage({id: "wizard.user", defaultMessage: "User"}), config: 'User'},
        {key: 'status', label: intl.formatMessage({id: "wizard.status", defaultMessage: "Status"}), config: 'Status'},
        {key: 'rule', label: intl.formatMessage({id: "wizard.rule", defaultMessage: "Rule"}), config: 'Rule'}
    ];

    const [alerts, setAlerts] = useState([]);
    const [elements, setElements] = useState([]);
    const [visibleColumn, setVisibleColumn] = useState([...['title'], ...fieldOrder.filter(field => field.enabled).map((field) => field.name).map((fieldName) => fieldsTitle.find(x => x.config === fieldName).key)]);

    const columnDefinitions= fieldsTitle.map(field => {return {id: field.key, title: field.label, sortable: false};});
    const columnRenderers = () => ({
        attributes: {
            description: {
                renderCell: (_description) => (<span style={{whiteSpace: 'pre-line'}}>{_description}</span>)
            },
            created: {
                renderCell: (_created) => (<Timestamp dateTime={toDateObject(_created)} relative/>)
            },
            lastModified: {
                renderCell: (_lastModified) => (<Timestamp dateTime={toDateObject(_lastModified)} relative/>)
            },
            status: {
                renderCell: (_status, element) => {
                    if (element.valid) {
                        if(_status) {
                            return <span style={{backgroundColor: 'orange', color: 'white'}}><FormattedMessage id="wizard.disabled" defaultMessage="Disabled"/></span>;
                        } else {
                            return <FormattedMessage id="wizard.enabled" defaultMessage="Enabled" />;
                        }
                    }
                    else {
                        return <FormattedMessage id="wizard.corrupted" defaultMessage="Corrupted" />;
                    }
                }
            },
            rule: {
                renderCell: (_rule, element) => {
                    const alert = alerts.find(x => x.title === element.id);
                    return <AlertRuleText alert={alert} />;
                }
            }
        },
    });
    const onColumnsChange = useCallback((displayedAttributes) => {
        console.log(displayedAttributes);
        const newVisibleColumns = computeVisibleColumn(fieldOrder, fieldsTitle, displayedAttributes);
        setVisibleColumn(newVisibleColumns);
    }, []);
    const renderBulkActions = (
        selectedAlertRuleIds,
        setSelectedAlertRuleIds,
    ) => (
        <AlertRuleBulkActions selectedAlertRuleIds={selectedAlertRuleIds}
                     setSelectedAlertRuleIds={setSelectedAlertRuleIds} deleteAlertRulesFunction={deleteAlertRules}
                              disableAlertRulesFunction={disableAlertRules} enableAlertRulesFunction={enableAlertRules} />
    );
    const onSortChange = useCallback(() => {}, []);
    const renderAlertRuleActions = useCallback((element) => {
        const updateAlert = (
            <IfPermitted permissions="wizard_alerts_rules:read">
                <LinkContainer to={Routes.pluginRoute('WIZARD_UPDATEALERT_ALERTRULETITLE')(element.title.replace(/\//g, '%2F'))} disabled={!element.valid}>
                    <Button bsStyle="info" type="submit">
                        <FormattedMessage id="wizard.edit" defaultMessage="Edit"/>
                    </Button>
                </LinkContainer>
            </IfPermitted>);
        const cloneAlert = <AlertRuleCloneForm alertTitle={element.title} disabled={!element.valid} onSubmit={_onCloneSubmit} />;

        return (<div className="pull-left" style={{display: 'flex', columnGap: '1px'}}>
            {updateAlert}
            <ButtonToEventDefinition target={element.condition} disabled={!element.valid}/>
            <ButtonToNotification target={element.notification} disabled={!element.valid}/>
            {cloneAlert}
        </div>);
    }, []);

    const _loadAlertRules = useCallback(() => {
        AlertRuleActions.list().then(newAlerts => {
            setAlerts(newAlerts);
            setElements(newAlerts.map(_convertAlertToElement));
        });
    }, [alerts, setAlerts, elements, setElements]);

    const deleteAlertRules = (alertRulesTitles) => {
        const promises = alertRulesTitles.map(name => AlertRuleActions.deleteByName(name));
        Promise.all(promises).then(() => _loadAlertRules());
    }

    const disableAlertRules = (alertRulesTitles) => {
        const tempElements = alertRulesTitles.map(name => elements.find(x => x.id === name));

        for(const elt of tempElements) {
            _onPause(elt.title, elt.condition, elt.streamId, elt.secondEventDefinition, elt.streamId2);
        }
    }

    const enableAlertRules = (alertRulesTitles) => {
        const tempElements = alertRulesTitles.map(name => elements.find(x => x.id === name));

        for(const elt of tempElements) {
            _onResume(elt.condition, elt.streamId, elt.secondEventDefinition, elt.streamId2);
        }
    }

    const _onResume = (eventDefinitionIdentifier, stream, secondEventDefinitionIdentifier, stream2) => {
        const promises = [];
        promises.push(EventDefinitionResources.enable(eventDefinitionIdentifier));
        StreamsStore.resume(stream, response => response).finally(() => _loadAlertRules());
        if (secondEventDefinitionIdentifier !== null) {
            promises.push(EventDefinitionResources.enable(secondEventDefinitionIdentifier));
        }
        if (stream2 !== null) {
            StreamsStore.resume(stream2, response => response).finally(() => _loadAlertRules());
        }
        Promise.all(promises).then(() => {});
    };

    const _onPause = (name, eventDefinitionIdentifier, stream, secondEventDefinitionIdentifier, secondStream) => {
        const promises = [];
        promises.push(EventDefinitionResources.disable(eventDefinitionIdentifier));
        StreamsStore.pause(stream, response => response).finally(() => _loadAlertRules());
        if (secondEventDefinitionIdentifier) {
            promises.push(EventDefinitionResources.disable(secondEventDefinitionIdentifier));
        }
        if (secondStream !== null) {
            StreamsStore.pause(secondStream, response => response);
        }
        Promise.all(promises).then(() => {});
    }

    const _onCloneSubmit = (name, title, description) => {
        AlertRuleActions.get(name).then(rule => {
            const newRule = {
                title: title,
                description: description,
                priority: rule.priority,
                condition_type: rule.condition_type,
                condition_parameters: rule.condition_parameters,
                stream: rule.stream,
                second_stream: rule.second_stream
            }
            AlertRuleActions.create(newRule).finally(() => _loadAlertRules());
        });
    }

    useEffect(() => {
        _loadAlertRules();
        }, []);

    return (
            <div>
                {alerts?.length === 0 ? (
                    <NoSearchResult>No Alert Rule has been found</NoSearchResult>
                ) : (
                    <EntityDataTable data={elements}
                        visibleColumns={visibleColumn}
                        columnsOrder={visibleColumn}
                        onColumnsChange={onColumnsChange}
                        onSortChange={onSortChange}
                        bulkActions={renderBulkActions}
                        columnDefinitions={columnDefinitions}
                        columnRenderers={columnRenderers()}
                        actionsCellWidth={500}
                        rowActions={renderAlertRuleActions}
                        entityAttributesAreCamelCase={false} />
                        )}
                    </div>
                );
};

export default AlertRulesContainer;
