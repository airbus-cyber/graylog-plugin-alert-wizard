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
import { FormattedMessage } from 'react-intl';
import Reflux from 'reflux';
import { Spinner } from 'components/common';
import ObjectUtils from 'util/ObjectUtils';
import Routes from 'routing/Routes';
import { LinkContainer } from 'react-router-bootstrap';
import { Input, Button, Col, Row } from 'components/bootstrap';


const INIT_LIST = {
    title: '',
    description: '',
    lists: '',
};

const CreateListFormInput = createReactClass({
    displayName: 'CreateListFormInput',

    propTypes: {
        list: PropTypes.object,
        onSave: PropTypes.func.isRequired,
    },

    contextTypes: {
        intl: PropTypes.object.isRequired,
    },

    getDefaultProps() {
        return {
            list: INIT_LIST
        };
    },

    getInitialState() {
        let list = ObjectUtils.clone(this.props.list);

        return {
            list: list,
            contentComponent: <Spinner/>,
        };
    },

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

        const buttonCancel = (
            <LinkContainer to={Routes.pluginRoute('WIZARD_LISTS')}>
                <Button><FormattedMessage id= "wizard.cancel" defaultMessage="Cancel" /></Button>
            </LinkContainer>
        );

        const buttonSave = (
            <LinkContainer to={Routes.pluginRoute('WIZARD_LISTS')}>
                <Button onClick={() => this.props.onSave(this.state.list)} 
                        disabled={this.state.list.title === '' || this.state.list.lists === ''} 
                        className="btn btn-md btn-primary">
                    <FormattedMessage id="wizard.save" defaultMessage="Save"/>
                </Button>
            </LinkContainer>
        );

        const actions = (
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
                        <Input id="description" type="text" label={<FormattedMessage id= "wizard.fieldOptionalDescription" defaultMessage="Description (optional)" />}
                               onChange={this._onUpdate('description')}
                               defaultValue={this.state.list.description}
                               name="description"/>
                    </Col>
                </Row>
                <Row style={style}>
                    <Col md={5}>
                    <Input style={{minWidth: 600}} ref="lists" id="lists" name="lists" type="textarea" rows="10"
                           label={<FormattedMessage id="wizard.fieldListwithexemple" defaultMessage="List (example : 172.10.0.1; 192.168.1.4; ...)" />}
                           onChange={this._onUpdate('lists')} defaultValue={this.state.list.lists} />
                        {actions}
                    </Col>
                </Row>
            </div>
        );
    },
});

export default CreateListFormInput;
