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

import PropTypes from 'prop-types';
import React from 'react';
import createReactClass from 'create-react-class';
import BootstrapModalForm from 'components/bootstrap/BootstrapModalForm';
import { Input } from 'components/bootstrap';
import { injectIntl, FormattedMessage } from 'react-intl';

const AlertListForm = createReactClass({
    displayName: 'AlertListForm',

    propTypes: {
        onSubmit: PropTypes.func.isRequired,
    },

    // TODO replace deprecated componentWillMount into a combination of getInitialState and componentDidMount
    componentWillMount(){
        const messages = {
            placeholderTitle: this.props.intl.formatMessage({id: "wizard.placeholderCloneTitleList", defaultMessage: "A descriptive name of the new alert list"}),
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
                                title={<FormattedMessage id= "wizard.cloneList" defaultMessage= 'Cloning List "{title}"' values={{title: this.state.origTitle }} />}
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

export default injectIntl(AlertListForm, {forwardRef: true});
