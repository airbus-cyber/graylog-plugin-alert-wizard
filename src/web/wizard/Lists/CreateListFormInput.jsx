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
import Reflux from 'reflux';
import {Button, Col, Row} from 'components/graylog';
import {Spinner} from 'components/common';
import ObjectUtils from 'util/ObjectUtils';
import Routes from 'routing/Routes';
import {LinkContainer} from 'react-router-bootstrap';
import 'react-confirm-alert/src/react-confirm-alert.css';
import {FormattedMessage} from 'react-intl';
import {Input} from 'components/bootstrap';
import AlertListActions from "./AlertListActions";
import AlertListStore from "./AlertListStore";

const INIT_LIST = {
    title: '',
    description: '',
    lists: '',
    };

const CreateListFormInput = createReactClass({
        displayName: 'CreateListFormInput',

    mixins: [Reflux.connect(AlertListStore)],

    propTypes: {
        list: PropTypes.object,
        create: PropTypes.bool.isRequired,
    },
    contextTypes: {
        intl: PropTypes.object.isRequired,
    },


    getDefaultProps() {
        return {
            list: INIT_LIST,
            default_values: {
                title: '',
                description: '',
                lists: '',
            },
        };
    },

    getInitialState() {
        let list = ObjectUtils.clone(this.props.list);

        if (this.props.create) {
            list.title = this.props.default_values.title;
            list.description = this.props.default_values.description;
            list.lists = this.props.default_values.lists;
        }

        return {
            list: list,
            isModified: false,
            contentComponent: <Spinner/>,
        };
    },

    _save() {
        AlertListActions.create.triggerPromise(this.state.list).then((response) => {
            if (response === true) {
                    this.setState({list: list});
            }
        });
        this.setState({isModified: false});
    },

    _update() {
        AlertListActions.update.triggerPromise(this.props.list.title, this.state.list).then((response) => {
            if (response === true) {
                    this.setState({list: list});
            }
        });
        this.setState({isModified: false});
    },

    // TODO add a button to import a file into the list field 
    /*onSubmitUploadFile(submitEvent) {
        submitEvent.preventDefault();
        if (!this.refs.uploadedFile.files || !this.refs.uploadedFile.files[0]) {
            return;
        }

        const reader = new FileReader();
        reader.onload = (evt) => {
            this.setState({alertLists: JSON.parse(evt.target.result)});
        };

        reader.readAsText(this.refs.uploadedFile.files[0]);
    },*/

    _updateConfigField(field, value) {
        const update = ObjectUtils.clone(this.state.list);
        update[field] = value;
        this.setState({ list: update });
    },

    _onUpdate(field) {
        return e => {
            this._updateConfigField(field, e.target.value);
        };
    },

    render: function() {

        let actions;

        const buttonCancel = (
            <LinkContainer to={Routes.pluginRoute('WIZARD_LISTS')}>
                <Button><FormattedMessage id= "wizard.cancel" defaultMessage= "Cancel" /></Button>
            </LinkContainer>
        );

        let buttonSave;
        if (this.props.create) {
            buttonSave = (
                <LinkContainer to={Routes.pluginRoute('WIZARD_LISTS')}>
                    <Button onClick={this._save} disabled={this.state.list.title === '' || this.state.list.lists === ''} className="btn btn-md btn-primary">
                        <FormattedMessage id="wizard.save" defaultMessage="Save"/>
                    </Button>
                </LinkContainer>
            );
        }
        else {
            buttonSave = (
                <LinkContainer to={Routes.pluginRoute('WIZARD_LISTS')}>
                    <Button onClick={this._update}
                            className="btn btn-md btn-primary">
                        <FormattedMessage id= "wizard.save" defaultMessage= "Save" />
                    </Button>
                </LinkContainer>
            );
        }

        actions = (
            <div className="alert-actions pull-left">
                {buttonCancel}{' '}
                {buttonSave}{' '}
            </div>);

        const style = { display: 'flex', alignItems: 'center' };

        return (
            <div>
                <Row>
                    <Col md={4}>
                        <Input id="title" type="text" required label={<FormattedMessage id ="wizard.title" defaultMessage="Title" />}
                               onChange={this._onUpdate('title')} defaultValue={this.state.list.title} name="title" disabled={this.state.list.usage}/>
                        <Input id="description" type="text" label={<FormattedMessage id= "wizard.fieldOptionalDescription" defaultMessage= "Description (optional)" />}
                               onChange={this._onUpdate('description')}
                               defaultValue = {this.state.list.description}
                               name="description"/>
                    </Col>
                </Row>
                <Row style={style}>
                    <Col md={5}>
                    <Input style={{minWidth: 600}} ref="lists" id="lists" name="lists" type="textarea" rows="10"
                           label={<FormattedMessage id ="wizard.fieldListwithexemple" defaultMessage="List (example : 172.10.0.1; 192.168.1.4; ...)" />}
                           onChange={this._onUpdate('lists')} defaultValue = {this.state.list.lists}/>
                        {actions}
                    </Col>
                </Row>
            </div>
        );
    },
});

export default CreateListFormInput;
