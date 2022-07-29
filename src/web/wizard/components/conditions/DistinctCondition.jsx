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
import PropTypes from 'prop-types';
import createReactClass from 'create-react-class';
import { Input } from 'components/bootstrap';
import { Select } from 'components/common';
import { FormattedMessage } from 'react-intl';
import { Row, Col } from 'components/graylog';

import withFormattedFields from './withFormattedFields';

const DistinctCondition = createReactClass({
    displayName: 'DistinctCondition',

    propTypes: {
        onUpdate: PropTypes.func,
        formattedFields: PropTypes.array.isRequired,
    },   
    getInitialState() {
        return {
            distinction_fields:this.props.distinction_fields,
        };
    },
    _onDistinctionFieldsChange(nextValue) {
        const values = (nextValue === '' ? [] : [nextValue]);
        this.setState({distinction_fields: values});
        this.props.onUpdate('distinction_fields', values);
    },

    render() {
        const { formattedFields } = this.props;

        return (
            <Row>
                <Col md={2} style={{ marginTop: 5, marginBottom: 0 }}>
                    <label className="pull-right" ><FormattedMessage id= "wizard.distinctBy" defaultMessage= "Distinct by Condition" /></label>
                </Col>
                <Col md={10}>
                    <label><FormattedMessage id= "wizard.distinctByLabel" defaultMessage= "Messages must be distincted by" /></label>
                    <Input ref="distinction_fields" id="distinction_fields" name="distinction_fields">
                        <div style={{minWidth:'300px'}}>
                        <Select options={formattedFields}
                                value={this.state.distinction_fields.length === 0 ? '' : this.state.distinction_fields[0]}
                                onChange={this._onDistinctionFieldsChange}
                                allowCreate={true}/>
                        <Input />
                        </div>
                    </Input>
                </Col>
            </Row>
        );
    },
});

export default withFormattedFields(DistinctCondition);
