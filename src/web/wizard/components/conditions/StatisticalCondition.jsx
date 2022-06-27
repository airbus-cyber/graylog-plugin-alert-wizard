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
import { Row, Col } from 'components/graylog';
import { Input} from 'components/bootstrap';
import { Select } from 'components/common';
import ObjectUtils from 'util/ObjectUtils';

import withFormattedFields from './withFormattedFields';

const StatisticalCondition = createReactClass({
    displayName: 'StatisticalCondition',

    propTypes: {
        onUpdate: PropTypes.func.isRequired,
        formattedFields: PropTypes.array.isRequired,
    },

    getInitialState() {
        return {
            type: this.props.type,
            field: this.props.field,
            threshold: this.props.threshold,
            threshold_type: this.props.threshold_type,
        };
    },
    _availableAggregationTypes() {
        return [
            {value: 'AVG', label: <FormattedMessage id= "wizard.meanValue" defaultMessage= "average value" />},
            {value: 'STDDEV', label: <FormattedMessage id= "wizard.standardDeviation" defaultMessage= "standard deviation" />},
            {value: 'MIN', label: <FormattedMessage id= "wizard.minValue" defaultMessage= "min value" />},
            {value: 'MAX', label: <FormattedMessage id= "wizard.maxValue" defaultMessage= "max value" />},
            {value: 'SUM', label: <FormattedMessage id= "wizard.sum" defaultMessage= "sum" />},
            {value: 'CARD', label: <FormattedMessage id= "wizard.card" defaultMessage= "cardinality" />},
            {value: 'COUNT', label: <FormattedMessage id= "wizard.count" defaultMessage= "count" />},
            {value: 'SUMOFSQUARES', label: <FormattedMessage id= "wizard.sum" defaultMessage= "sum of squares" />},
            {value: 'VARIANCE', label: <FormattedMessage id= "wizard.variance" defaultMessage= "variance" />},
        ];
    },
    _onAggregationTypeSelect(value) {
        this.setState({type: value});
        this.props.onUpdate('type', value);
    },
    _availableThresholdTypes() {
        return [
            {value: '>', label: <FormattedMessage id= "wizard.higher" defaultMessage= "higher than" />},
            {value: '>=', label: <FormattedMessage id= "wizard.higherEqual" defaultMessage= "higher or equal than" />},
            {value: '<', label: <FormattedMessage id= "wizard.lower" defaultMessage= "lower than" />},
            {value: '<=', label: <FormattedMessage id= "wizard.lowerEqual" defaultMessage= "lower or equal than" />},
            {value: '==', label: <FormattedMessage id= "wizard.equal" defaultMessage= "equal" />},
        ];
    },
    _onThresholdTypeSelect(value) {
        this.setState({threshold_type: value});
        this.props.onUpdate('threshold_type', value);
    },
    
    _updateAlertField(field, value) {
        const update = ObjectUtils.clone(this.state);
        update[field] = value;
        this.setState({parameters: update});

        this.props.onUpdate(field, value);
    },
    _onThresholdChanged() {
        return e => {
            this.setState({threshold: e.target.value});
            this.props.onUpdate('threshold', e.target.value);
        };
    },
    _onParameterFieldSelect(value) {
        this.setState({field: value});
        this.props.onUpdate('field', value);

        //add value to list fields if not present
        if (value !== '' && this.state.fields.indexOf(value) < 0) {
            const update = ObjectUtils.clone(this.state.fields);
            update.push(value);
            this.setState({fields: update});
        }
    },

    render() {
        const { formattedFields } = this.props;

        return (
                    <Row>
                        <Col md={2} style={{ marginTop: 5, marginBottom: 0 }}>
                            <label className="pull-right"><FormattedMessage id= "wizard.statisticalCondition" defaultMessage= "Statistical Condition" /></label>
                        </Col>
                        <Col md={10}>
                            <label><FormattedMessage id= "wizard.the" defaultMessage= "The" /></label>
                            <Input ref="type" id="type" name="type" required>
                                <div style={{width:'200px'}}>
                                <Select
                                    required
                                    value={this.state.type}
                                    options={this._availableAggregationTypes()}
                                    matchProp="value"
                                    onChange={this._onAggregationTypeSelect}
                                    placeholder={<FormattedMessage id= "wizard.select" defaultMessage= "Select..." />}
                                />
                                </div>
                            </Input>
                            <label>&nbsp; </label>
                            <label><FormattedMessage id= "wizard.of" defaultMessage= "of" /></label> 
                            <Input ref="field" id="field" name="field">
                                <div style={{width:'200px'}}>
                                <Select
                                    required
                                    value={this.state.field}
                                    options={formattedFields}
                                    matchProp="value"
                                    onChange={this._onParameterFieldSelect}
                                    allowCreate={true}
                                    placeholder={<FormattedMessage id= "wizard.select" defaultMessage= "Select..." />}
                                />
                                </div>
                            </Input>
                            <label>&nbsp;</label>
                            <label><FormattedMessage id= "wizard.mustBe" defaultMessage= "must be" /></label>
                            <Input ref="threshold_type" id="threshold_type" name="threshold_type" required>
                                <div style={{width:'200px'}}>
                                <Select
                                    required
                                    value={this.state.threshold_type}
                                    options={this._availableThresholdTypes()}
                                    matchProp="value"
                                    onChange={this._onThresholdTypeSelect}
                                    placeholder={<FormattedMessage id= "wizard.select" defaultMessage= "Select..." />}
                                />
                                </div>
                            </Input>
                            <Input ref="threshold" id="threshold" name="threshold" type="number" min="0"
                                   onChange={this._onThresholdChanged()}
                                   value={this.state.threshold}
                                   style={{borderTopLeftRadius: '0px', borderBottomLeftRadius: '0px', height:'36px', width:'100px'}}/>
                        </Col>
                    </Row>
        );
    },
});

export default withFormattedFields(StatisticalCondition);
