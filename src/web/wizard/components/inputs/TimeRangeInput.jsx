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
import { useIntl, FormattedMessage } from 'react-intl';
import { Input, Row, Col } from 'components/bootstrap';
import { Select } from 'components/common';

const _AVAILABLE_TIME_TYPES = [
    {value: '1', label: <FormattedMessage id="wizard.minutes" defaultMessage="minutes" />},
    {value: '60', label: <FormattedMessage id="wizard.hours" defaultMessage="hours" />},
    {value: '1440', label: <FormattedMessage id="wizard.days" defaultMessage="days" />},
];

const TimeRangeInput = ({time, time_type, onUpdate}) => {
    const intl = useIntl();

    const _onTimeChanged = (value) => {
        onUpdate('time', value * time_type);
    };

    const _onTimeSelect = (value) => {
        onUpdate('time', parseInt(value) * time);
    };

    return (
        <Row>
            <Col md={2} style={{ marginTop: 5, marginBottom: 0 }}>
                <label className="pull-right"><FormattedMessage id="wizard.timeRangeInput" defaultMessage="Time Range Condition" /></label>
            </Col>
            <Col md={10}>
                <label><FormattedMessage id="wizard.messagesCome" defaultMessage="Messages must come in the last" /></label>
                <Input id="time" name="time" type="number" min="1"
                       value={time}
                       onChange={(e) => _onTimeChanged(e.target.value)}
                       style={{borderTopRightRadius: '0px', borderBottomRightRadius: '0px', height:'36px' , width:'100px'}} />
                <Input id="time_type" name="time_type" required className="form-control">
                    <div style={{width:'150px'}}>
                    <Select
                        required
                        clearable={false}
                        value={time_type}
                        options={_AVAILABLE_TIME_TYPES}
                        matchProp="value"
                        onChange={_onTimeSelect}
                        placeholder={intl.formatMessage({ id: "wizard.select", defaultMessage: "Select..." })}
                    />
                    </div>
                </Input>
            </Col>
        </Row>
    );
}

TimeRangeInput.propTypes = {
    onUpdate: PropTypes.func,
};

export default TimeRangeInput;
