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

import React, {useRef} from 'react';
import { FormattedMessage, useIntl } from 'react-intl';
import { Input, Row, Col } from 'components/bootstrap';
import { Select } from 'components/common';

const NumberInput = ({onUpdate, threshold = '', threshold_type = ''}) => {
    const thresholdRef = useRef();
    const thresholdTypeRef = useRef();

    const intl = useIntl();

    const _availableThresholdTypes = [
        {value: '>', label: <FormattedMessage id="wizard.more" defaultMessage="more than" />},
        {value: '<', label: <FormattedMessage id="wizard.less" defaultMessage="less than" />}
    ];

    const _onThresholdTypeSelect = (id) => {
        onUpdate('threshold_type', id);
    };

    const _onThresholdChanged = e => {
        onUpdate('threshold', e.target.value);
    };

    return (
        <Row>
            <Col md={2} style={{ marginTop: 5, marginBottom: 0 }}>
                <label className="pull-right"><FormattedMessage id="wizard.NumberInput" defaultMessage="Count Condition" /></label>
            </Col>
            <Col md={10}>
                <label><FormattedMessage id="wizard.thereMustBe" defaultMessage="There must be " /></label>
                <Input ref={thresholdTypeRef} id="threshold_type" name="threshold_type" required>
                    <div style={{width:'150px'}}>
                    <Select
                        required
                        clearable={false}
                        value={threshold_type}
                        options={_availableThresholdTypes}
                        matchProp="value"
                        onChange={_onThresholdTypeSelect}
                        placeholder={intl.formatMessage({ id: "wizard.select", defaultMessage: "Select..." })}
                    />
                    </div>
                </Input>
                <Input ref={thresholdRef} id="threshold" name="threshold" type="number" min="0" onChange={_onThresholdChanged}
                       value={threshold}
                       style={{borderTopLeftRadius: '0px', borderBottomLeftRadius: '0px', height:'36px', width:'100px'}} />
                <label>&nbsp; </label>
                <label><FormattedMessage id="wizard.messages" defaultMessage="messages" /></label>
            </Col>
        </Row>
    );
};

export default NumberInput;
