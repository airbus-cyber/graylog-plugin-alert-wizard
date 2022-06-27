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
import { Input } from 'components/bootstrap';
import { MultiSelect } from 'components/common';
import { Row, Col } from 'components/graylog';

import withFormattedFields from '../withFormattedFields';

const GroupByCondition = createReactClass({
    displayName: 'GroupByCondition',

    propTypes: {
        onUpdate: PropTypes.func,
        formattedFields: PropTypes.array.isRequired,
    },
    getInitialState() {
        return {
            grouping_fields:this.props.grouping_fields,
        };
    },
    _onGroupingFieldsChange(nextValue) {
        const values = (nextValue === '' ? [] : nextValue.split(','));
        this.setState({grouping_fields: values});
        this.props.onUpdate('grouping_fields', values);
    },

    render() {
        const { formattedFields } = this.props;

        return (
            <Row>
                <Col md={2} style={{ marginTop: 5, marginBottom: 0 }}>
                    <label className="pull-right" ><FormattedMessage id= "wizard.groupBy" defaultMessage= "Group by Condition" /></label>
                </Col>
                <Col md={10}>
                    <label><FormattedMessage id= "wizard.groupByLabel" defaultMessage= "Messages must be grouped by" /></label>
                    <Input ref="grouping_fields" id="grouping_fields" name="grouping_fields">
                        <div style={{minWidth:'300px'}}>
                        <MultiSelect autoFocus={false}
                                 options={formattedFields}
                                 value={this.state.grouping_fields ? (Array.isArray(this.state.grouping_fields) ? this.state.grouping_fields.join(',') : this.state.grouping_fields) : undefined}
                                 onChange={this._onGroupingFieldsChange}
                                 allowCreate={true}/>
                        </div>
                    </Input>
                </Col>
            </Row>
        );
        
    },
});

export default withFormattedFields(GroupByCondition);
