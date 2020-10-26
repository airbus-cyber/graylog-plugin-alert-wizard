/*
 * graylog-plugin-alert-wizard Source Code
 * Copyright (C) 2018-2020 - Airbus CyberSecurity (SAS) - All rights reserved
 *
 * This file is part of the graylog-plugin-alert-wizard GPL Source Code.
 *
 * graylog-plugin-alert-wizard Source Code is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this code.  If not, see <http://www.gnu.org/licenses/>.
 */

import PropTypes from 'prop-types';
import React from 'react';
import createReactClass from 'create-react-class';
import LinkedStateMixin from 'react-addons-linked-state-mixin';
import BootstrapModalForm from 'components/bootstrap/BootstrapModalForm';
import {Input} from 'components/bootstrap';
import {FormattedMessage} from 'react-intl';

const AlertForm = createReactClass({
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
        this.setState({messages:messages});
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

export default AlertForm;
