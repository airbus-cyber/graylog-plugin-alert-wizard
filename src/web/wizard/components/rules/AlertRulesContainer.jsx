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
import { useState, useCallback } from 'react';
import { useIntl, FormattedMessage } from 'react-intl';
import { useQueryClient } from '@tanstack/react-query';
import { toDateObject } from 'util/DateTime';
import StreamsStore from 'stores/streams/StreamsStore';
import { PaginatedEntityTable, Timestamp } from 'components/common';
import ButtonToEventDefinition from 'wizard/components/buttons/ButtonToEventDefinition';
import ButtonToNotification from 'wizard/components/buttons/ButtonToNotification';
import ButtonToSearch from 'wizard/components/buttons/ButtonToSearch';
import ButtonToUpdateRule from 'wizard/components/buttons/ButtonToUpdateRule';
import EventDefinitionResources from 'wizard/resources/EventDefinitionResource';
import AlertRuleActions from 'wizard/actions/AlertRuleActions';
import AlertValidation from 'wizard/logic/AlertValidation';
import { keyFn, fetchAlertRules } from './hooks/useAlertRules';
import AlertRuleBulkActions from './AlertRuleBulkActions';
import AlertRuleText from './AlertRuleText';
import AlertRuleCloneForm from './AlertRuleCloneForm';
import {DEFAULT_LAYOUT} from "./Constants";

function _convertAlertToElement(alert) {
    let alertValid = !AlertValidation.isAlertCorrupted(alert);
    let textColor = '';
    if (!alertValid) {
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
    let searchQuery = '';
    if (alert.condition_parameters) {
        searchQuery = alert.condition_parameters.search_query;
    }
    let searchQuery2 = '';
    if (alert.condition_parameters) {
        searchQuery2 = alert.condition_parameters.additional_search_query;
    }
    return {
        id: alert.id,
        title: alert.title,
        priority: alert.priority,
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
        secondEventDefinition: alert.second_event_definition,
        searchQuery: searchQuery,
        searchQuery2: searchQuery2
    };
}

const AlertRulesContainer = ({ fieldOrder }) => {
    const intl = useIntl();
    const queryClient = useQueryClient();

    const _loadAlertRules = () => queryClient.invalidateQueries(keyFn());
    const fieldsTitle = [
        {key: 'title', label: intl.formatMessage({id: 'wizard.title', defaultMessage: 'Title'}), config: 'title', sortable: true},
        {key: 'priority', label: intl.formatMessage({id: 'wizard.priority', defaultMessage: 'Priority'}), config: 'Priority', sortable: true},
        {key: 'description', label: intl.formatMessage({id: 'wizard.fieldDescription', defaultMessage: 'Description'}), config: 'Description', sortable: true},
        {key: 'created', label: intl.formatMessage({id: 'wizard.created', defaultMessage: 'Created'}), config: 'Created', sortable: true},
        {key: 'lastModified', label: intl.formatMessage({id: 'wizard.lastModified', defaultMessage: 'Last Modified'}), config: 'Last Modified', sortable: true},
        {key: 'user', label: intl.formatMessage({id: 'wizard.user', defaultMessage: 'User'}), config: 'User', sortable: true},
        {key: 'status', label: intl.formatMessage({id: 'wizard.status', defaultMessage: 'Status'}), config: 'Status', sortable: false},
        {key: 'rule', label: intl.formatMessage({id: 'wizard.rule', defaultMessage: 'Rule'}), config: 'Rule', sortable: false}
    ];
    const availablePriorityTypes = [
        {value: 1, label: intl.formatMessage({id: 'wizard.low', defaultMessage: 'Low'})},
        {value: 2, label: intl.formatMessage({id: 'wizard.medium', defaultMessage: 'Normal'})},
        {value: 3, label: intl.formatMessage({id: 'wizard.high', defaultMessage: 'High'})}
    ];
    const getPriorityType = (type) => {
        const selectedPriority = availablePriorityTypes.find((t) => t.value === type);

        if (selectedPriority) {
            return selectedPriority.label;
        }

        return '';
    };

    const [elements, setElements] = useState([]);
    const [columnOrder] = useState([...['title'], ...fieldOrder.map((field) => field.name).map((fieldName) => fieldsTitle.find(x => x.config === fieldName).key)]);
    const [additionalAttributes] = useState([...fieldsTitle.map((field) => { return {id: field.key, title: field.label, sortable: field.sortable};})]);

    const renderHeader = (_column) => {
        return (<span>{fieldsTitle.find(x => x.key === _column.id).label}</span>);
    };
    const columnRenderers = () => ({
        attributes: {
            title: {
                renderHeader
            },
            user: {
                renderCell: (_user, alert) => (<span style={{whiteSpace: 'pre-line'}}>{alert.creator_user_id}</span>),
                renderHeader
            },
            priority: {
                renderCell: (_priority) => (<span style={{whiteSpace: 'pre-line'}}>{getPriorityType(_priority)}</span>),
                renderHeader
            },
            description: {
                renderCell: (_description) => (<span style={{whiteSpace: 'pre-line'}}>{_description}</span>),
                renderHeader
            },
            created: {
                renderCell: (_created, alert) => (<Timestamp dateTime={toDateObject(alert.created_at)} relative/>),
                renderHeader
            },
            lastModified: {
                renderCell: (_lastModified, alert) => (<Timestamp dateTime={toDateObject(alert.last_modified)} relative/>),
                renderHeader
            },
            status: {
                renderCell: (_status, alert) => {
                    const element = _convertAlertToElement(alert);
                    if (element.valid) {
                        if(element.status) {
                            return <span style={{backgroundColor: 'orange', color: 'white'}} className={element.textColor}><FormattedMessage id='wizard.disabled' defaultMessage='Disabled'/></span>;
                        } else {
                            return <span><FormattedMessage id='wizard.enabled' defaultMessage='Enabled' /></span>;
                        }
                    }
                    else {
                        return <span className={element.textColor}><FormattedMessage id='wizard.corrupted' defaultMessage='Corrupted' /></span>;
                    }
                },
                renderHeader
            },
            rule: {
                renderCell: (_rule, alert) => <AlertRuleText alert={alert} />,
                renderHeader
            }
        },
    });
    const renderBulkActions = () => (
        <AlertRuleBulkActions deleteAlertRulesFunction={deleteAlertRules}
                              disableAlertRulesFunction={disableAlertRules}
                              enableAlertRulesFunction={enableAlertRules} />
    );
    const renderAlertRuleActions = useCallback((alert) => {
        const element = _convertAlertToElement(alert);

        return (<div className='pull-left' style={{display: 'flex', columnGap: '1px'}}>
            <ButtonToSearch searchQuery1={element.searchQuery} searchQuery2={element.searchQuery2} stream1={element.streamId} stream2={element.streamId2} disabled={!element.valid}/>
            <ButtonToUpdateRule target={element.id} disabled={!element.valid}/>
            <ButtonToEventDefinition target={element.condition} disabled={!element.valid}/>
            <ButtonToNotification target={element.notification} disabled={!element.valid}/>
            <AlertRuleCloneForm alertTitle={element.title} disabled={!element.valid} onSubmit={_onCloneSubmit} />
        </div>);
    }, []);

    const deleteAlertRules = (alertRulesIds) => {
        const promises = alertRulesIds.map(id => AlertRuleActions.delete(id));
        Promise.all(promises).then(() => {}).finally(() => _loadAlertRules());
    }

    const disableAlertRules = async (alertRulesIds) => {
        const tempElements = [];

        for(const id of alertRulesIds) {
            const loadedRule = await AlertRuleActions.get(id);
            tempElements.push(_convertAlertToElement(loadedRule));
        }

        const promises = [];
        for(const elt of tempElements) {
            promises.push(_onPause(elt.title, elt.condition, elt.streamId, elt.secondEventDefinition, elt.streamId2));
        }
        Promise.all(promises).then(() => {}).finally(() => _loadAlertRules());
    }

    const enableAlertRules = async (alertRulesIds) => {
        const tempElements = [];

        for(const id of alertRulesIds) {
            const loadedRule = await AlertRuleActions.get(id);
            tempElements.push(_convertAlertToElement(loadedRule));
        }

        const promises = [];
        for(const elt of tempElements) {
            promises.push(_onResume(elt.condition, elt.streamId, elt.secondEventDefinition, elt.streamId2));
        }
        Promise.all(promises).then(() => {}).finally(() => _loadAlertRules());
    }

    const _onResume = (eventDefinitionIdentifier, stream, secondEventDefinitionIdentifier, stream2) => {
        const promises = [];
        if (eventDefinitionIdentifier !== null) {
            promises.push(EventDefinitionResources.enable(eventDefinitionIdentifier));
        }
        if (stream !== null) {
            StreamsStore.resume(stream, response => response);
        }
        if (secondEventDefinitionIdentifier !== null) {
            promises.push(EventDefinitionResources.enable(secondEventDefinitionIdentifier));
        }
        if (stream2 !== null) {
            StreamsStore.resume(stream2, response => response);
        }
        return Promise.all(promises);
    };

    const _onPause = (name, eventDefinitionIdentifier, stream, secondEventDefinitionIdentifier, secondStream) => {
        const promises = [];
        if (eventDefinitionIdentifier !== null) {
            promises.push(EventDefinitionResources.disable(eventDefinitionIdentifier));
        }
        if (stream !== null) {
            StreamsStore.pause(stream, response => response);
        }
        if (secondEventDefinitionIdentifier !== null) {
            promises.push(EventDefinitionResources.disable(secondEventDefinitionIdentifier));
        }
        if (secondStream !== null) {
            StreamsStore.pause(secondStream, response => response);
        }
        return Promise.all(promises);
    }

    const _onCloneSubmit = (name, title, description, shouldCloneNotification) => {
        AlertRuleActions.clone(name, title, description, shouldCloneNotification)
            .then(() => {}).finally(() => _loadAlertRules());
    }

    return (
        <>
            <PaginatedEntityTable humanName="alert rules"
                                  columnRenderers={columnRenderers()}
                                  columnsOrder={columnOrder}
                                  bulkSelection={{ actions: renderBulkActions() }}
                                  entityActions={renderAlertRuleActions}
                                  entityAttributesAreCamelCase
                                  fetchEntities={fetchAlertRules}
                                  keyFn={keyFn}
                                  additionalAttributes={additionalAttributes}
                                  tableLayout={DEFAULT_LAYOUT}/>
        </>
    );
};

export default AlertRulesContainer;
