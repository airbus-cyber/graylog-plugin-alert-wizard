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
import { useIntl, FormattedMessage } from 'react-intl';

import { Input, Row, Col } from 'components/bootstrap';
import { Select, Spinner } from 'components/common';

import FieldRuleList from './FieldRuleList';

const FieldsInput = ({stream, onSaveStream}) => {
    const intl = useIntl();
    const messages = {
        tryFieldsCondition: intl.formatMessage({ id: "wizard.tryFieldsCondition", defaultMessage: "Try the fields condition" }),
        select: intl.formatMessage({id: "wizard.select", defaultMessage: "Select..."})
    };
    const _availableMatchingType = [
        {value: 'AND', label: <FormattedMessage id="wizard.all" defaultMessage="all" />},
        {value: 'OR', label: <FormattedMessage id="wizard.atLeastOne" defaultMessage="at least one" />}
    ];
    const matchingTypeRef = useRef();

    const _onMatchingTypeSelect = (id) => {
        _updateStreamField('matching_type', id);
    };
    
    const _updateStreamField = (field, value) => {
        onSaveStream(field, value);
    };

    const _onSaveStream = (newFieldRules) => {
        onSaveStream('field_rule', newFieldRules);
    };

    if (!stream) {
        return (
            <div style={{marginLeft: 10}}>
                <Spinner/>
            </div>
        );
    } else {
        return (
            <Row>
                <Col md={2} style={{ marginTop: 5, marginBottom: 0 }}>
                    <label className="pull-right" ><FormattedMessage id="wizard.fieldsCondition" defaultMessage="Fields Condition" /></label>
                </Col>
                <Col md={10}>
                    <label><FormattedMessage id="wizard.messagesMatch" defaultMessage="Messages must match" /></label>
                    <Input ref={matchingTypeRef} id="matching_type" name="matching_type" required>
                        <div style={{width:'150px'}}>
                            <Select
                                id="matching_type_select"
                                autosize={false}
                                required
                                clearable={false}
                                value={stream.matching_type}
                                options={_availableMatchingType}
                                matchProp="value"
                                onChange={_onMatchingTypeSelect}
                                placeholder={messages.select}
                            />
                        </div>
                    </Input>
                    <label>&nbsp; </label>
                    <label><FormattedMessage id="wizard.followingRules" defaultMessage="of the following rules:" /></label>
                    {' '}
                    <br/><br/>

                    <FieldRuleList fieldRules={stream.field_rule} onSaveStream={_onSaveStream} />
                </Col>
            </Row>
        );
    }
};

export default FieldsInput;
