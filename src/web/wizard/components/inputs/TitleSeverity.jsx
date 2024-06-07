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

const _AVAILABLE_SEVERITY_TYPES = [
    {value: 'info', label: <FormattedMessage id="wizard.info" defaultMessage="Info" />},
    {value: 'low', label: <FormattedMessage id="wizard.low" defaultMessage="Low" />},
    {value: 'medium', label: <FormattedMessage id="wizard.medium" defaultMessage="Medium" />},
    {value: 'high', label: <FormattedMessage id="wizard.high" defaultMessage="High" />},
];

const TitleSeverity = ({title, severity, onUpdate}) => {
    const intl = useIntl();

    const _onTitleChanged = (value) => {
        onUpdate('title', value);
    };

    const _onSeverityTypeChanged = (value) => {
        onUpdate('severity', value);
    };

    return (
        <Row>
            <Col md={2} style={{ marginTop: 10, marginBottom: 0 }}>
                <label className="pull-right"><FormattedMessage id="wizard.titleSeverity" defaultMessage="Alert Title and Severity" /></label>
            </Col>
            <Col md={10}>
                <Input style={{borderTopRightRadius: '0px', borderBottomRightRadius: '0px', height:'36px', width:'450px'}}
                       id="title" name="title" type="text"
                       onChange={(e) => _onTitleChanged(e.target.value)}
                       defaultValue={title}/>
                <Input id="severity" name="severity">
                    <div style={{width:'150px'}}>
                    <Select
                        value={severity}
                        options={_AVAILABLE_SEVERITY_TYPES}
                        matchProp="value"
                        onChange={_onSeverityTypeChanged}
                        clearable={false}
                        placeholder={intl.formatMessage({id: "wizard.select", defaultMessage: "Select..."})}
                    />
                    </div>
                </Input>
            </Col>
        </Row>
    );
}

TitleSeverity.propTypes = {
    onUpdate: PropTypes.func,
};

export default TitleSeverity;
