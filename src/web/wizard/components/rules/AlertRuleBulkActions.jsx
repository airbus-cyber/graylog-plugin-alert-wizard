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
import { ConfirmDialog } from 'components/common';
import { MenuItem } from 'components/bootstrap';
import StringUtils from 'util/StringUtils';
import BulkActionsDropdown from 'components/common/EntityDataTable/BulkActionsDropdown';

const ACTION_TYPES = {
    DELETE: 'delete',
    DISABLE: 'disable',
    ENABLE: 'enable',
};

const AlertRuleBulkActions = ({ selectedAlertRuleIds, setSelectedAlertRuleIds, deleteAlertRulesFunction, enableAlertRulesFunction, disableAlertRulesFunction }) => {

    const intl = useIntl();

    const ACTION_TEXT = {
        [ACTION_TYPES.DELETE]: {
            dialogTitle: intl.formatMessage({id: 'wizard.delete', defaultMessage: 'Delete'}),
            dialogBody: (count) => `${count > 1 ? intl.formatMessage({id: "wizard.confirmDeletionPlural", defaultMessage: "Are you sure you want to delete alert rules"}) : intl.formatMessage({id: "wizard.confirmDeletion", defaultMessage: "Are you sure you want to delete alert rule"})}?`
        },
        [ACTION_TYPES.DISABLE]: {
            dialogTitle: intl.formatMessage({id: 'wizard.disable', defaultMessage: 'Disable'}),
            dialogBody: (count) => `${count > 1 ? intl.formatMessage({id: "wizard.confirmDisablePlural", defaultMessage: "Are you sure you want to disable alert rules"}) : intl.formatMessage({id: "wizard.confirmDisable", defaultMessage: "Are you sure you want to disable alert rule"})}?`
        },
        [ACTION_TYPES.ENABLE]: {
            dialogTitle: intl.formatMessage({id: 'wizard.enable', defaultMessage: 'Enable'}),
            dialogBody: (count) => `Are you sure you want to enable alert rule?`
        }
    };

    const [showDialog, setShowDialog] = useState(false);
    const [actionType, setActionType] = useState(null);
    const selectedItemsAmount = selectedAlertRuleIds?.length;

    const updateState = ({ show, type }) => {
        setShowDialog(show);
        setActionType(type);
    };

    const handleAction = (action) => {
        switch (action) {
            case ACTION_TYPES.DELETE:
                updateState({ show: true, type: ACTION_TYPES.DELETE });
                break;
            case ACTION_TYPES.ENABLE:
                updateState({ show: false, type: ACTION_TYPES.ENABLE });
                onAction();
                break;
            case ACTION_TYPES.DISABLE:
                updateState({ show: true, type: ACTION_TYPES.DISABLE });
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
                deleteAlertRulesFunction(selectedAlertRuleIds);
                break;
            case ACTION_TYPES.DISABLE:
                disableAlertRulesFunction(selectedAlertRuleIds);
                break;
            case ACTION_TYPES.ENABLE:
                enableAlertRulesFunction(selectedAlertRuleIds);
                break;
            default:
                break;
        }
    }, [actionType, selectedAlertRuleIds]);

    const handleConfirm = () => {
        onAction();
        setShowDialog(false);
    };

    return (
        <BulkActionsDropdown selectedEntities={selectedAlertRuleIds} setSelectedEntities={setSelectedAlertRuleIds}>
            <MenuItem onSelect={() => handleAction(ACTION_TYPES.ENABLE)}><FormattedMessage id="wizard.enable" defaultMessage="Enable" /></MenuItem>
            <MenuItem onSelect={() => handleAction(ACTION_TYPES.DISABLE)}><FormattedMessage id="wizard.disable" defaultMessage="Disable" /></MenuItem>
            <MenuItem onSelect={() => handleAction(ACTION_TYPES.DELETE)}><FormattedMessage id="wizard.delete" defaultMessage="Delete" /></MenuItem>
            {showDialog && (
                <ConfirmDialog title={ACTION_TEXT[actionType]?.dialogTitle}
                               show
                               onConfirm={handleConfirm}
                               onCancel={handleClearState}>
                    {ACTION_TEXT[actionType]?.dialogBody(selectedItemsAmount)}
                </ConfirmDialog>
            )}
        </BulkActionsDropdown>
    );
};

export default AlertRuleBulkActions;
