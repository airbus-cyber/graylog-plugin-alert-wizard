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

import React, {useState} from 'react';
import { FormattedMessage } from 'react-intl';
import ObjectUtils from 'util/ObjectUtils';
import Routes from 'routing/Routes';
import { LinkContainer } from 'react-router-bootstrap';
import { Input, Button, Col, Row } from 'components/bootstrap';

const CreateListFormInput = ({list, onSave}) =>  {

    const [newList, setNewList] = useState(list);

    const _updateConfigField = (field, value) => {
        const update = ObjectUtils.clone(newList);
        update[field] = value;
        setNewList(update);
    };

    const _onUpdate = (field) => {
        return e => {
            _updateConfigField(field, e.target.value);
        };
    };

    const buttonCancel = (
        <LinkContainer to={Routes.pluginRoute('WIZARD_LISTS')}>
            <Button><FormattedMessage id= "wizard.cancel" defaultMessage="Cancel" /></Button>
        </LinkContainer>
    );

    const buttonSave = (
        <Button onClick={() => onSave(newList)}
                disabled={newList.title === '' || newList.lists === ''}
                bsStyle="primary" className="btn btn-primary">
            <FormattedMessage id="wizard.save" defaultMessage="Save" />
        </Button>
    );

    const actions = (
        <div className="pull-left">
            {buttonCancel}{' '}
            {buttonSave}{' '}
        </div>);

    const style = { display: 'flex', alignItems: 'center' };

    return (
        <div>
            <Row>
                <Col md={4}>
                    <Input id="title" type="text" required label={<FormattedMessage id ="wizard.title" defaultMessage="Title" />}
                           onChange={_onUpdate('title')} defaultValue={newList.title} name="title" disabled={newList.usage}/>
                    <Input id="description" type="text" label={<FormattedMessage id= "wizard.fieldOptionalDescription" defaultMessage="Description (optional)" />}
                           onChange={_onUpdate('description')}
                           defaultValue={newList.description}
                           name="description"/>
                </Col>
            </Row>
            <Row style={style}>
                <Col md={5}>
                <Input style={{minWidth: 600}} id="lists" name="lists" type="textarea" rows="10"
                       label={<FormattedMessage id="wizard.fieldListwithexemple" defaultMessage="List (example : 172.10.0.1; 192.168.1.4; ...)" />}
                       onChange={_onUpdate('lists')} defaultValue={newList.lists} />
                    {actions}
                </Col>
            </Row>
        </div>
    );
};

CreateListFormInput.defaultProps = {
    list: {
        title: '',
        description: '',
        lists: ''
    },
    onSave: () => {}
}

export default CreateListFormInput;
