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
import { useIntl, FormattedMessage } from 'react-intl';

import { Input, Row, Col } from 'components/bootstrap';
import { Select } from 'components/common';

const _AVAILABLE_PRIORITY_TYPES = [
    {value: 1, label: <FormattedMessage id="wizard.low" defaultMessage="Low" />},
    {value: 2, label: <FormattedMessage id="wizard.medium" defaultMessage="Normal" />},
    {value: 3, label: <FormattedMessage id="wizard.high" defaultMessage="High" />},
];

const TitlePriority = ({title, priority, onUpdate}) => {
    const intl = useIntl();

    const _onTitleChanged = (value) => {
        onUpdate('title', value);
    };

    const _onPriorityChanged = (value) => {
        onUpdate('priority', value);
    };

    return (
        <Row>
            <Col md={2} style={{ marginTop: 10, marginBottom: 0 }}>
                <label className="pull-right"><FormattedMessage id="wizard.titlePriority" defaultMessage="Alert Title and Priority" /></label>
            </Col>
            <Col md={10}>
                <Input style={{borderTopRightRadius: '0px', borderBottomRightRadius: '0px', height:'36px', width:'450px'}}
                       id="title" name="title" type="text"
                       onChange={(e) => _onTitleChanged(e.target.value)}
                       defaultValue={title}/>
                <Input id="priority" name="priority">
                    <div style={{width:'150px'}}>
                    <Select
                        value={priority}
                        options={_AVAILABLE_PRIORITY_TYPES}
                        onChange={_onPriorityChanged}
                        clearable={false}
                        placeholder={intl.formatMessage({id: "wizard.select", defaultMessage: "Select..."})}
                    />
                    </div>
                </Input>
            </Col>
        </Row>
    );
}

TitlePriority.propTypes = {
    onUpdate: PropTypes.func,
};

export default TitlePriority;
