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
import createReactClass from 'create-react-class';
import LinkedStateMixin from 'react-addons-linked-state-mixin';
import {FormattedMessage} from 'react-intl';
import PropTypes from 'prop-types';

import BootstrapModalForm from 'components/bootstrap/BootstrapModalForm';
import {Input} from 'components/bootstrap';

const AlertRuleCloneForm = createReactClass({
    displayName: 'AlertForm',

    propTypes: {
        onSubmit: PropTypes.func.isRequired,
    },

    contextTypes: {
        intl: PropTypes.object.isRequired,
    },

    mixins: [LinkedStateMixin],

    componentWillMount(){
        const messages = {
            placeholderTitle: this.context.intl.formatMessage({id: "wizard.placeholderCloneTitle", defaultMessage: "A descriptive name of the new alert rule"}),
        };
        this.setState({messages: messages});
    },

    getInitialState() {
        return {
            title: '',
            description: '',
        };
    },

    _resetValues() {
        this.setState({title: ''});
        this.setState({description: ''});
    },

    _onSubmit() {
        this.props.onSubmit(this.state.origTitle, this.state.title, this.state.description);
        this.refs.modal.close();
    },

    open(title) {
        this.setState({origTitle: title});
        this._resetValues();
        this.refs.modal.open();
    },

    close() {
        this.refs.modal.close();
    },

    _onValueChanged(event) {
        this.setState({[event.target.name]: event.target.value});
    },

    render() {
        return (
            <BootstrapModalForm ref="modal"
                                title={<FormattedMessage id= "wizard.cloneAlertRule" defaultMessage= 'Cloning Alert Rule "{title}"' values={{title: this.state.origTitle }} />}
                                onSubmitForm={this._onSubmit}
                                cancelButtonText={<FormattedMessage id= "wizard.cancel" defaultMessage= "Cancel" />}
                                submitButtonText={<FormattedMessage id= "wizard.save" defaultMessage= "Save" />}>
                <Input id="title" type="text" required label={<FormattedMessage id ="wizard.title" defaultMessage="Title" />} name="title"
                       placeholder={this.state.messages.placeholderTitle}
                       onChange={this._onValueChanged} autoFocus/>
                <Input id="description" type="text" label={<FormattedMessage id= "wizard.fieldDescription" defaultMessage= "Description" />} name="description"
                       onChange={this._onValueChanged}/>
            </BootstrapModalForm>
        );
    },
});

export default AlertRuleCloneForm;
