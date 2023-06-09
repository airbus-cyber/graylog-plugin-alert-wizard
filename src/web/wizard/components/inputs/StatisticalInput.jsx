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
import React, { useState } from 'react';
import { FormattedMessage } from 'react-intl';
import { Input, Row, Col } from 'components/bootstrap';
import { Select } from 'components/common';

import withFormattedFields from './withFormattedFields';

export const AVAILABLE_AGGREGATION_TYPES = [
    {value: 'AVG', label: <FormattedMessage id="wizard.meanValue" defaultMessage="average value" />},
    {value: 'STDDEV', label: <FormattedMessage id="wizard.standardDeviation" defaultMessage="standard deviation" />},
    {value: 'MIN', label: <FormattedMessage id="wizard.minValue" defaultMessage="min value" />},
    {value: 'MAX', label: <FormattedMessage id="wizard.maxValue" defaultMessage="max value" />},
    {value: 'SUM', label: <FormattedMessage id="wizard.sum" defaultMessage="sum" />},
    {value: 'CARD', label: <FormattedMessage id="wizard.card" defaultMessage="cardinality" />},
    {value: 'COUNT', label: <FormattedMessage id="wizard.count" defaultMessage="count" />},
    {value: 'SUMOFSQUARES', label: <FormattedMessage id="wizard.sum" defaultMessage="sum of squares" />},
    {value: 'VARIANCE', label: <FormattedMessage id="wizard.variance" defaultMessage="variance" />},
];

// TODO this is also probably a duplicate with the code in AlertRuleText => try to factor
const AVAILABLE_THRESHOLD_TYPES = [
    {value: '>', label: <FormattedMessage id="wizard.higher" defaultMessage="higher than" />},
    {value: '>=', label: <FormattedMessage id="wizard.higherEqual" defaultMessage="higher or equal than" />},
    {value: '<', label: <FormattedMessage id="wizard.lower" defaultMessage="lower than" />},
    {value: '<=', label: <FormattedMessage id="wizard.lowerEqual" defaultMessage="lower or equal than" />},
    {value: '==', label: <FormattedMessage id="wizard.equal" defaultMessage="equal" />},
];

const StatisticalInput = ({onUpdate, type, field, thresholdType, threshold, formattedFields}) => {
    // TODO: a state is necessary for the input type number, but not for the Select, why?
    //       Maybe the state should be put in a new component, NumericalInput (factor if there ever is the same pattern in other components)
    const [currentThreshold, setCurrentThreshold] = useState(threshold);

    const _onAggregationTypeSelect = (value) => {
        onUpdate('type', value);
    };

    const _onParameterFieldSelect = (value) => {
        onUpdate('field', value);
    };

    const _onThresholdChanged = (e) => {
        const value = e.target.value;
        setCurrentThreshold(value);
        onUpdate('threshold', value);
    };

    const _onThresholdTypeSelect = (value) => {
        onUpdate('threshold_type', value);
    };

    return (
        <Row>
            <Col md={2} style={{ marginTop: 5, marginBottom: 0 }}>
                <label className="pull-right"><FormattedMessage id="wizard.statisticalInput" defaultMessage="Statistical Condition" /></label>
            </Col>
            <Col md={10}>
                <label><FormattedMessage id="wizard.the" defaultMessage="The" /></label>
                <Input id="type" name="type" required>
                    <div style={{width:'200px'}}>
                    <Select
                        required
                        clearable={false}
                        value={type}
                        options={AVAILABLE_AGGREGATION_TYPES}
                        matchProp="value"
                        onChange={_onAggregationTypeSelect}
                        placeholder={<FormattedMessage id="wizard.select" defaultMessage="Select..." />}
                    />
                    </div>
                </Input>
                <label>&nbsp; </label>
                <label><FormattedMessage id="wizard.of" defaultMessage="of" /></label>
                <Input id="field" name="field">
                    <div style={{width:'200px'}}>
                    <Select
                        required
                        value={field}
                        options={formattedFields}
                        matchProp="value"
                        onChange={_onParameterFieldSelect}
                        allowCreate={true}
                        placeholder={<FormattedMessage id="wizard.select" defaultMessage="Select..." />}
                    />
                    </div>
                </Input>
                <label>&nbsp;</label>
                <label><FormattedMessage id="wizard.mustBe" defaultMessage="must be" /></label>
                <Input id="threshold_type" name="threshold_type" required>
                    <div style={{width:'200px'}}>
                    <Select
                        required
                        clearable={false}
                        value={thresholdType}
                        options={AVAILABLE_THRESHOLD_TYPES}
                        matchProp="value"
                        onChange={_onThresholdTypeSelect}
                        placeholder={<FormattedMessage id="wizard.select" defaultMessage="Select..." />}
                    />
                    </div>
                </Input>
                <Input id="threshold" name="threshold" type="number" min="0"
                       onChange={_onThresholdChanged}
                       value={currentThreshold}
                       style={{borderTopLeftRadius: '0px', borderBottomLeftRadius: '0px', height:'36px', width:'100px'}}/>
            </Col>
        </Row>
    );
}

StatisticalInput.propTypes = {
    onUpdate: PropTypes.func.isRequired,
    formattedFields: PropTypes.array.isRequired,
}

export default withFormattedFields(StatisticalInput);
