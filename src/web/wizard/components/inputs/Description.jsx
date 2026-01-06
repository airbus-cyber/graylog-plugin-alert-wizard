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
import { Input, Row, Col } from 'components/bootstrap';
import { FormattedMessage } from 'react-intl';

const Description = ({description, onUpdate}) => {

    const [state, setState] = useState({description: description});

    const _onValueChanged = (field) => {
        return e => {
            setState({description: e.target.value});
            onUpdate(field, e.target.value);
        };
    };

    return (
        <Row>
            <Col md={2} style={{ marginTop: 5, marginBottom: 0 }}>
                <label className="pull-right" ><FormattedMessage id= "wizard.titleDescription" defaultMessage= "Description (optional)" /></label>
            </Col>
            <Col md={10}>
                <Input style={{minWidth: 600}} id="description" name="description" type="textarea"
                               onChange={_onValueChanged("description")} defaultValue={state.description}/>
            </Col>
        </Row>
    );
}

Description.defaultProps = {
    description: '',
    onUpdate: () => {}
}

export default Description;
