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
import * as React from 'react';
import { useCallback, useState } from 'react';
import { FormattedMessage, useIntl } from 'react-intl';
import { useQueryClient } from '@tanstack/react-query';
import { keyFn } from './hooks/useAlertRules';
import { ConfirmDialog } from 'components/common';
import { MenuItem } from 'components/bootstrap';
import EventDefinitionResources from 'wizard/resources/EventDefinitionResource';
import StreamsStore from 'stores/streams/StreamsStore';
import AllActionsDropdown from './AllActionsDropdown';
import AlertRuleActions from 'wizard/actions/AlertRuleActions';
import { convertAlertToElement } from 'wizard/logic/ConverterUtil';
import RulesImportExport from 'wizard/logic/RulesImportExport';
import { EventNotificationsActions } from 'stores/event-notifications/EventNotificationsStore';
import UserNotification from 'util/UserNotification';
import { adjustFormat } from 'util/DateTime';
import FileSaver from 'wizard/logic/FileSaver';

const ACTION_TYPES = {
    DELETE: 'delete',
    DISABLE: 'disable',
    ENABLE: 'enable',
    EXPORT: 'export',
};

const AlertRuleAllActions = ({  }) => {

    const queryClient = useQueryClient();
    const _loadAlertRules = () => queryClient.invalidateQueries(keyFn());
    const intl = useIntl();

    const ACTION_TEXT = {
        [ACTION_TYPES.DELETE]: {
            dialogTitle: intl.formatMessage({id: 'wizard.deleteAll', defaultMessage: 'Delete All'}),
            dialogBody: () => intl.formatMessage({id: "wizard.confirmDeletionAll", defaultMessage: "Are you sure you want to delete all alert rules"})
        },
        [ACTION_TYPES.DISABLE]: {
            dialogTitle: intl.formatMessage({id: 'wizard.disableAll', defaultMessage: 'Disable all'}),
            dialogBody: () => intl.formatMessage({id: "wizard.confirmDisableAll", defaultMessage: "Are you sure you want to disable all alert rules"})
        },
        [ACTION_TYPES.ENABLE]: {
            dialogTitle: intl.formatMessage({id: 'wizard.enableAll', defaultMessage: 'Enable all'}),
            dialogBody: () => intl.formatMessage({id: "wizard.confirmEnableAll", defaultMessage: "Are you sure you want to enable all alert rules"})
        }
    };

    const [showDialog, setShowDialog] = useState(false);
    const [actionType, setActionType] = useState(null);

    const updateState = ({ show, type }) => {
        setActionType(type);
        setShowDialog(show);
    };

    const deleteAlertRules = async () => {
        // TODO
        const alerts = await AlertRuleActions.list();
        const promises = alerts.map(alert => AlertRuleActions.delete(alert.id));
        Promise.all(promises).then(() => {}).finally(() => _loadAlertRules());
    }

    const disableAlertRules = async () => {
        // TODO
        const tempElements = [];

        const alerts = await AlertRuleActions.list();
        for(const alert of alerts) {
            tempElements.push(convertAlertToElement(alert));
        }

        const promises = [];
        for(const elt of tempElements) {
            promises.push(_onPause(elt.title, elt.condition, elt.streamId, elt.secondEventDefinition, elt.streamId2));
        }
        Promise.all(promises).then(() => {}).finally(() => _loadAlertRules());
    }

    const enableAlertRules = async () => {
        // TODO
        const tempElements = [];

        const alerts = await AlertRuleActions.list();
        for(const alert of alerts) {
            tempElements.push(convertAlertToElement(alert));
        }

        const promises = [];
        for(const elt of tempElements) {
            promises.push(_onResume(elt.condition, elt.streamId, elt.secondEventDefinition, elt.streamId2));
        }
        Promise.all(promises).then(() => {}).finally(() => _loadAlertRules());
    }

    const exportAlertRules = async () => {
        // TODO
        const alerts = await AlertRuleActions.list();
        for(const alert of alerts) {
            const notification = await EventNotificationsActions.get(alert.notification);
            alert.notification_parameters = notification.config;
        }
        UserNotification.success('Successfully export alert rules. Starting download...', 'Success!');
        let exportData = RulesImportExport.createExportDataFromRules(alerts);
        let date = adjustFormat(new Date()).replace(/:/g, '').replace(/ /g, '_');
        FileSaver.save(JSON.stringify(exportData), date+'_alert_rules.json', 'application/json', 'utf-8');
        _loadAlertRules();
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


    const handleAction = (action) => {
        switch (action) {
            case ACTION_TYPES.DELETE:
                updateState({ show: true, type: ACTION_TYPES.DELETE });
                break;
            case ACTION_TYPES.ENABLE:
                updateState({ show: true, type: ACTION_TYPES.ENABLE });
                break;
            case ACTION_TYPES.DISABLE:
                updateState({ show: true, type: ACTION_TYPES.DISABLE });
                break;
            case ACTION_TYPES.EXPORT:
                exportAlertRules();
                break;
            default:
                break;
        }
    };

    const handleClearState = () => {
        updateState({ show: false, type: null });
    };

    const onAction = useCallback(() => {
        switch (actionType) {
            case ACTION_TYPES.DELETE:
                deleteAlertRules();
                break;
            case ACTION_TYPES.DISABLE:
                disableAlertRules();
                break;
            case ACTION_TYPES.ENABLE:
                enableAlertRules();
                break;
            case ACTION_TYPES.EXPORT:
                exportAlertRules();
                break;
            default:
                break;
        }
    }, [actionType]);

    const handleConfirm = () => {
        onAction();
        setShowDialog(false);
    };

    return (
        <>
            <AllActionsDropdown>
                <MenuItem onSelect={() => handleAction(ACTION_TYPES.ENABLE)}><FormattedMessage id="wizard.enableAll" defaultMessage="Enable all" /></MenuItem>
                <MenuItem onSelect={() => handleAction(ACTION_TYPES.DISABLE)}><FormattedMessage id="wizard.disableAll" defaultMessage="Disable all" /></MenuItem>
                <MenuItem onSelect={() => handleAction(ACTION_TYPES.DELETE)} variant="danger"><FormattedMessage id="wizard.deleteAll" defaultMessage="Delete all"/></MenuItem>
                <MenuItem onSelect={() => handleAction(ACTION_TYPES.EXPORT)}><FormattedMessage id="wizard.exportAll" defaultMessage="Export all" /></MenuItem>
            </AllActionsDropdown>
            {showDialog && (
                <ConfirmDialog title={ACTION_TEXT[actionType]?.dialogTitle}
                               show
                               onConfirm={handleConfirm}
                               onCancel={handleClearState}>
                    {ACTION_TEXT[actionType]?.dialogBody()}
                </ConfirmDialog>
            )}
        </>
    );
};

export default AlertRuleAllActions;
