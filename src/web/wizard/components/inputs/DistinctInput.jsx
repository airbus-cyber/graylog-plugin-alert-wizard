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
import { Select } from 'components/common';
import { FormattedMessage } from 'react-intl';

import withFormattedFields from './withFormattedFields';

const DistinctInput = ({onUpdate, formattedFields, distinct_by}) => {

    const [state, setState] = useState({ distinct_by });

    const _onDistinctionFieldsChange = (nextValue) => {
        setState({distinct_by: nextValue});
        onUpdate('distinct_by', nextValue);
    };

    return (
        <Row>
            <Col md={2} style={{ marginTop: 5, marginBottom: 0 }}>
                <label className="pull-right" ><FormattedMessage id= "wizard.distinctBy" defaultMessage= "Distinct by Condition" /></label>
            </Col>
            <Col md={10}>
                <label><FormattedMessage id= "wizard.distinctByLabel" defaultMessage= "Messages must be distincted by" /></label>
                <Input id="distinct_by" name="distinct_by">
                    <div style={{minWidth:'300px'}}>
                    <Select options={formattedFields}
                            value={state.distinct_by}
                            onChange={_onDistinctionFieldsChange}
                            allowCreate={true}/>
                    </div>
                </Input>
            </Col>
        </Row>
    );
}

export default withFormattedFields(DistinctInput);
