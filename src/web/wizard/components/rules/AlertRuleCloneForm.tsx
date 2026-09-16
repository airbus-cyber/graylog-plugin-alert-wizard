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
import { useState } from 'react';
import { useIntl, IntlShape, FormattedMessage } from 'react-intl';

import { Input } from 'components/bootstrap';
import { Button } from 'components/bootstrap';
import Select from 'components/common/Select/Select';
import BootstrapModalForm from 'components/bootstrap/BootstrapModalForm';
import BootstrapModalConfirm from 'components/bootstrap/BootstrapModalConfirm';

// source of inspiration: components/common/URLWhiteListFormModal

interface CloneValues {
    title?: string
    description?: string
    shouldCloneNotification?: boolean
    conditionType?: string
}

/**
 * 
 * @param title [string] 
 * @param disabled [boolean]
 * @param onSubmit [(title: string, newTitle: string, description: string, 
 *                   shouldCloneNotification: boolean, conditionType: string, policy: null|'RENAME'|'REPLACE') => void]
 * @param messages [{infoClone: string, modalTitle: string, placeholderTitle: string}]
 * @param onValidate [(newTitle: string, onValid: () => void, onNonValid: () => void) => void]
 * @returns 
 */
const CloneButton = ({title, disabled = false, onSubmit, messages, onValidate}) => {
    const [state, setState] = useState<CloneValues>({title: '', description: '', shouldCloneNotification: false, conditionType: ''});
    const [showConfigModal, setShowConfigModal] = useState<boolean>(false);
    const [showConfirmModal, setShowConfirmModal] = useState<boolean>(false);
    const intl: IntlShape = useIntl();
    const AVAILABLE_CONDITION_TYPES = [
        {value: 'COUNT', label: intl.formatMessage({id: "wizard.countCondition", defaultMessage: "Count"})},
        {value: 'GROUP_DISTINCT', label: intl.formatMessage({id: "wizard.groupDistinctCondition", defaultMessage: "Group / Distinct"})},
        {value: 'STATISTICAL', label: intl.formatMessage({id: "wizard.StatisticsCondition", defaultMessage: "Statistics"})},
        {value: 'THEN', label: intl.formatMessage({id: "wizard.thenCondition", defaultMessage: "THEN"})},
        {value: 'AND', label: intl.formatMessage({id: "wizard.andCondition", defaultMessage: "AND"})},
        {value: 'OR', label: intl.formatMessage({id: "wizard.orCondition", defaultMessage: "OR"})},
    ];

    const openModal = () => {
        setShowConfigModal(true);
    };

    const closeModal = () => {
        setShowConfigModal(false);
    };

    /**
     * After failed validation, confirm modal is shown. This behavior matches the confirm action from this modal.
     */
    const confirmSubmit = () => {
        internalSubmit('REPLACE');
        setShowConfirmModal(false);
    }

    /**
     * After failed validation, confirm modal is shown. This behavior matches the cancel action from this modal.
     */
    const cancelSubmit = () => {
        setShowConfirmModal(false);
    }

    /**
     * Final submit action after validation or not.
     */
    const internalSubmit = (policy: null | 'RENAME' | 'REPLACE'=null) => {
        onSubmit(title, state.title, state.description, state.shouldCloneNotification, state.conditionType, policy);
    }

    const submit = () => {
        if (onValidate) {
            // If exists ask for confirm
            // => confirm : submit and close all modals
            // => cancel : only show initial cloning modal
            // If does not exist, submit it
            onValidate(state.title, 
                () => {
                    internalSubmit();
                },
                () => {
                    // TODO show confirm modal
                    setShowConfirmModal(true)
                }
            )
        } else {
            internalSubmit();
        }
        closeModal();
    };

    const onValueChanged = (event) => {
        updateState({[event.target.name]: event.target.value});
    };

    const onConditionTypeChanged = (value) => {
        updateState({conditionType: value});
    };

    const handleUseCronSchedulingChange = (event) => {
        updateState({shouldCloneNotification: event.target.checked});
    };

    const updateState = (newValues: CloneValues) => {
        const newState: CloneValues = Object.assign({}, state, newValues);
        setState(newState);
    }

    return (
        <>
            <Button type="button" bsStyle="info" onClick={openModal} disabled={disabled} title={messages.infoClone} >
                <FormattedMessage id="wizard.clone" defaultMessage="Clone" />
            </Button>
            <BootstrapModalForm show={showConfigModal}
                                title={messages.modalTitle}
                                onCancel={closeModal}
                                onSubmitForm={submit}
                                cancelButtonText={intl.formatMessage({id:"wizard.cancel", defaultMessage:"Cancel"})}
                                submitButtonText={intl.formatMessage({id:"wizard.save", defaultMessage:"Save"})}>
                <Input id="title" type="text" required label={intl.formatMessage({id:"wizard.title", defaultMessage:"Title"})} name="title"
                       placeholder={messages.placeholderTitle}
                       onChange={onValueChanged} autoFocus />
                <Input id="description" type="text" 
                       label={intl.formatMessage({id:"wizard.fieldDescription", defaultMessage:"Description"})}
                       name="description"
                       onChange={onValueChanged} />
                <Input label={intl.formatMessage({id:"wizard.ruleType", defaultMessage:"Type de règle"})} >
                    <Select id="condition_type"
                            options={AVAILABLE_CONDITION_TYPES}
                            matchProp="value"
                            onChange={onConditionTypeChanged}
                    />
                </Input>
                <Input id="should-clone-notification"
                       type="checkbox"
                       label={intl.formatMessage({id:"wizard.cloneNotification", defaultMessage:"Clone notification"})}
                       help={intl.formatMessage({id:"wizard.cloneNotificationHelp", defaultMessage:"When this is checked, cloning will clone the notification too"})}
                       checked={state.shouldCloneNotification}
                       onChange={handleUseCronSchedulingChange} />
            </BootstrapModalForm>

            <BootstrapModalConfirm
                showModal={showConfirmModal}
                title={intl.formatMessage({id:"wizard.cloneConfirmLabel", defaultMessage:"Override existing alert rule?"})}
                onConfirm={confirmSubmit}
                onCancel={cancelSubmit}>
                Alert rule with same title already exists. Confirm will override it.
            </BootstrapModalConfirm>
        </>
    );
};

/**
 * 
 * @param title [string] 
 * @param disabled [boolean]
 * @param onSubmit [(title: string, newTitle: string, description: string, 
 *                   shouldCloneNotification: boolean, conditionType: string, policy: null|'RENAME'|'REPLACE') => void]
 * @param onValidate [(newTitle: string, onValid: () => void, onNonValid: () => void) => void]
 * @returns 
 */
const AlertRuleCloneForm = ({alertTitle, disabled = false, onSubmit, onValidate}) => {
    const intl = useIntl();
    const messages = {
        infoClone: intl.formatMessage({id: "wizard.buttonInfoClone", defaultMessage: "Clone this alert rule"}),
        placeholderTitle: intl.formatMessage({id: "wizard.placeholderCloneTitle", defaultMessage: "A descriptive name of the new alert rule"}),
        modalTitle: intl.formatMessage({id:"wizard.cloneAlertRule", defaultMessage:'Cloning Alert Rule "{title}"'}, {title: alertTitle})
    };

    return (
        <CloneButton title={alertTitle} disabled={disabled} onSubmit={onSubmit} messages={messages} onValidate={onValidate}/>
    );
};

export default AlertRuleCloneForm;
