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
import Reflux from 'reflux';
import { useState, useCallback, useEffect } from 'react';
import { EntityDataTable, NoSearchResult, Timestamp, SearchForm } from 'components/common';
import AlertRuleActions from 'wizard/actions/AlertRuleActions';
import AlertRuleStore from 'wizard/stores/AlertRuleStore';
import { useIntl, FormattedMessage } from 'react-intl';
import AlertRuleBulkActions from './AlertRuleBulkActions';
import { toDateObject, DATE_TIME_FORMATS } from 'util/DateTime';
import AlertRuleText from './AlertRuleText';
import ButtonToEventDefinition from '../buttons/ButtonToEventDefinition';
import ButtonToNotification from '../buttons/ButtonToNotification';
import AlertRuleCloneForm from './AlertRuleCloneForm';
import EventDefinitionResources from '../../resources/EventDefinitionResource';
import StreamsStore from 'stores/streams/StreamsStore';
import ButtonToSearch from '../buttons/ButtonToSearch';
import AlertValidation from '../../logic/AlertValidation';
import ButtonToUpdateRule from '../buttons/ButtonToUpdateRule';

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
        id: alert.title,
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

    const fieldsTitle = [
        {key: 'title', label: intl.formatMessage({id: 'wizard.title', defaultMessage: 'Title'}), config: 'title'},
        {key: 'priority', label: intl.formatMessage({id: 'wizard.priority', defaultMessage: 'Priority'}), config: 'Priority'},
        {key: 'description', label: intl.formatMessage({id: 'wizard.fieldDescription', defaultMessage: 'Description'}), config: 'Description'},
        {key: 'created', label: intl.formatMessage({id: 'wizard.created', defaultMessage: 'Created'}), config: 'Created'},
        {key: 'lastModified', label: intl.formatMessage({id: 'wizard.lastModified', defaultMessage: 'Last Modified'}), config: 'Last Modified'},
        {key: 'user', label: intl.formatMessage({id: 'wizard.user', defaultMessage: 'User'}), config: 'User'},
        {key: 'status', label: intl.formatMessage({id: 'wizard.status', defaultMessage: 'Status'}), config: 'Status'},
        {key: 'rule', label: intl.formatMessage({id: 'wizard.rule', defaultMessage: 'Rule'}), config: 'Rule'}
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

    const [alerts, setAlerts] = useState([]);
    const [filterElements, setFilterElements] = useState([]);
    const [elements, setElements] = useState([]);
    const [query, setQuery] = useState('');
    const [visibleColumn, setVisibleColumn] = useState([...['title'], ...fieldOrder.filter(field => field.enabled).map((field) => field.name).map((fieldName) => fieldsTitle.find(x => x.config === fieldName).key)]);
    const [columnOrder, setColumnOrder] = useState([...['title'], ...fieldOrder.map((field) => field.name).map((fieldName) => fieldsTitle.find(x => x.config === fieldName).key)]);

    const columnDefinitions= fieldsTitle.map(field => {return {id: field.key, title: field.label, sortable: false};});
    const columnRenderers = () => ({
        attributes: {
            priority: {
                renderCell: (_priority) => (<span style={{whiteSpace: 'pre-line'}}>{getPriorityType(_priority)}</span>)
            },
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
                            return <span style={{backgroundColor: 'orange', color: 'white'}} className={element.textColor}><FormattedMessage id='wizard.disabled' defaultMessage='Disabled'/></span>;
                        } else {
                            return <span><FormattedMessage id='wizard.enabled' defaultMessage='Enabled' /></span>;
                        }
                    }
                    else {
                        return <span className={element.textColor}><FormattedMessage id='wizard.corrupted' defaultMessage='Corrupted' /></span>;
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
        setVisibleColumn(displayedAttributes);
    }, [visibleColumn]);
    const renderBulkActions = () => (
        <AlertRuleBulkActions deleteAlertRulesFunction={deleteAlertRules}
                              disableAlertRulesFunction={disableAlertRules}
                              enableAlertRulesFunction={enableAlertRules} />
    );
    const onSortChange = useCallback(() => {}, []);
    const renderAlertRuleActions = useCallback((element) => {
        return (<div className='pull-left' style={{display: 'flex', columnGap: '1px'}}>
            <ButtonToSearch searchQuery1={element.searchQuery} searchQuery2={element.searchQuery2} stream1={element.streamId} stream2={element.streamId2} disabled={!element.valid}/>
            <ButtonToUpdateRule target={element.title} disabled={!element.valid}/>
            <ButtonToEventDefinition target={element.condition} disabled={!element.valid}/>
            <ButtonToNotification target={element.notification} disabled={!element.valid}/>
            <AlertRuleCloneForm alertTitle={element.title} disabled={!element.valid} onSubmit={_onCloneSubmit} />
        </div>);
    }, []);
    const _elementMatchQuery = (element, query) => {
        const lowerQuery = query ? query.toLowerCase() : '';
        const matchTitle = element.title.toLowerCase().includes(lowerQuery);
        const matchUser = element.user.toLowerCase().includes(lowerQuery);
        const matchPriority = getPriorityType(element.priority).toLowerCase().includes(lowerQuery);
        const matchCreatedAt = toDateObject(element.created).format(DATE_TIME_FORMATS.default).toLowerCase().includes(lowerQuery);
        const matchUpdatedAt = toDateObject(element.lastModified).format(DATE_TIME_FORMATS.default).toLowerCase().includes(lowerQuery);

        return matchTitle || matchUser || matchPriority || matchCreatedAt || matchUpdatedAt;
    }
    const onSearch = useCallback((newQuery, allElements = null) => {
        const usedElements = allElements ? allElements : elements;
        const newElements = usedElements.filter((elt) => _elementMatchQuery(elt, newQuery));

        setFilterElements(newElements);
        setQuery(newQuery);
    }, [query, elements]);

    const onReset = ()=> onSearch('');

    const _loadAlertRules = () => {
        AlertRuleActions.list().then(newAlerts => {
            setAlerts(newAlerts);
            const allElements = newAlerts.map(_convertAlertToElement);
            setElements(allElements);
            onSearch(query, allElements);
        });
    };

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
        Promise.all(promises).then(() => {}).finally(() => _loadAlertRules());
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
        Promise.all(promises).then(() => {}).finally(() => _loadAlertRules());
    }

    const _onCloneSubmit = (name, title, description, shouldCloneNotification) => {
        AlertRuleActions.clone(name, title, description, shouldCloneNotification)
            .then()
            .finally(() => _loadAlertRules());
    }

    useEffect(() => {
        Reflux.connect(AlertRuleStore);
        _loadAlertRules();
        }, []);

    return (
        <>
            <div style={{marginBottom: 5}}>
                <SearchForm onSearch={onSearch}
                            onReset={onReset}
                            placeholder={intl.formatMessage({
                                id: 'wizard.filter',
                                defaultMessage: 'Filter alert rules'
                            })}/>
            </div>
            <div>
                {alerts?.length === 0 ? (
                    <NoSearchResult>
                        <FormattedMessage id='wizard.noAlertFound' defaultMessage='No Alert Rule has been found' />
                    </NoSearchResult>
                ) : (
                    <EntityDataTable
                                     visibleColumns={visibleColumn}
                                     columnsOrder={columnOrder}
                                     onColumnsChange={onColumnsChange}
                                     onSortChange={onSortChange}
                                     bulkSelection={{ actions: renderBulkActions() }}
                                     columnDefinitions={columnDefinitions}
                                     columnRenderers={columnRenderers()}
                                     actionsCellWidth={520}
                                     entityActions={renderAlertRuleActions}
                                     entityAttributesAreCamelCase={false}
                                     entities={filterElements}
                    />
                )}
            </div>
        </>
    );
};

export default AlertRulesContainer;
