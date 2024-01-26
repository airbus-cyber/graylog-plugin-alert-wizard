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
import { useIntl, FormattedMessage } from 'react-intl';

import { Input } from 'components/bootstrap';
import { Button } from 'components/bootstrap';
import BootstrapModalForm from 'components/bootstrap/BootstrapModalForm';

// source of inspiration: components/common/URLWhiteListFormModal

const AlertRuleCloneForm = ({alertTitle, alertValid, onSubmit}) => {
    const intl = useIntl();
    const messages = {
        infoClone: intl.formatMessage({id: "wizard.buttonInfoClone", defaultMessage: "Clone this alert rule"}),
        placeholderTitle: intl.formatMessage({id: "wizard.placeholderCloneTitle", defaultMessage: "A descriptive name of the new alert rule"}),
    };
    const [state, setState] = useState({title: '', description: ''});
    const [showConfigModal, setShowConfigModal] = useState(false);

    const openModal = () => {
        setShowConfigModal(true);
    };

    const closeModal = () => {
        setShowConfigModal(false);
    };

    const submit = () => {
        onSubmit(alertTitle, state.title, state.description);
        closeModal();
    };

    const _onValueChanged = (event) => {
        const newState = {
            ...state,
            [event.target.name]: event.target.value
        };
        setState(newState);
    };

    return (
        <>
            <Button id="clone-alert" type="button" bsStyle="info" onClick={openModal} disabled={!alertValid} title={messages.infoClone} >
                <FormattedMessage id="wizard.clone" defaultMessage="Clone" />
            </Button>
            <BootstrapModalForm show={showConfigModal}
                                title={<FormattedMessage id= "wizard.cloneAlertRule" defaultMessage= 'Cloning Alert Rule "{title}"' values={{title: alertTitle}} />}
                                onCancel={closeModal}
                                onSubmitForm={submit}
                                cancelButtonText={<FormattedMessage id= "wizard.cancel" defaultMessage= "Cancel" />}
                                submitButtonText={<FormattedMessage id= "wizard.save" defaultMessage= "Save" />}>
                <Input id="title" type="text" required label={<FormattedMessage id ="wizard.title" defaultMessage="Title" />} name="title"
                       placeholder={messages.placeholderTitle}
                       onChange={_onValueChanged} autoFocus />
                <Input id="description" type="text" label={<FormattedMessage id= "wizard.fieldDescription" defaultMessage= "Description" />} name="description"
                       onChange={_onValueChanged} />
            </BootstrapModalForm>
        </>
    );
};

export default AlertRuleCloneForm;
