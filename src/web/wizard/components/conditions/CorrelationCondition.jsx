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

import React, {useEffect, useState} from 'react';
import { Button } from 'components/bootstrap';
import { FormattedMessage } from 'react-intl';
import { Row, Col } from 'components/bootstrap';
import ObjectUtils from 'util/ObjectUtils';
import FieldsInput from 'wizard/components/inputs/FieldsInput';
import NumberInput from 'wizard/components/inputs/NumberInput';
import TimeRangeInput from 'wizard/components/inputs/TimeRangeInput';
import Description from 'wizard/components/inputs/Description';
import GroupByInput from 'wizard/components/inputs/GroupByInput';
import IconArrowsV from 'wizard/components/icons/ArrowsV';
import HighlightedDiv from 'wizard/components/containers/HighlightedDiv';
import SearchQueryInput from 'wizard/components/inputs/SearchQueryInput';

const STREAM = {
    matching_type: '',
    field_rule: [{field: '', type: '', value: ''}],
};

const _getTimeType = (time) => {
    if (time >= 1440) {
        return 1440;
    } else if (time >= 60) {
        return 60;
    } else {
        return 1;
    }
};

const CorrelationCondition = ({alert, onUpdate, onUpdateAlert}) => {

    const [time, setTime] = useState(alert.condition_parameters.time);
    const [time_type, setTimeType] = useState(_getTimeType(alert.condition_parameters.time));

    useEffect(() => {
        const computedTimeType = _getTimeType(alert.condition_parameters.time);
        setTime(alert.condition_parameters.time / computedTimeType);
        setTimeType(computedTimeType);
    }, [alert]);

    const _handleChangeCondition = (field, value) => {
        let update = ObjectUtils.clone(alert);
        if (field === "threshold" || field === "additional_threshold" || field === "time") {
            update.condition_parameters[field] = parseInt(value);
        } else {
            update.condition_parameters[field] = value;
        }
        onUpdate('condition_parameters', update.condition_parameters);
    };

    const _handleChangeStream = (field, value) => {
        let update = ObjectUtils.clone(alert);
        if (update.stream === null) {
            update.stream = STREAM;
        }
        update.stream[field] = value;
        onUpdate('stream', update.stream);
    };

    const _handleChangeSecondStream = (field, value) => {
        let update = ObjectUtils.clone(alert);
        if (update.second_stream === null) {
            update.second_stream = STREAM;
        }
        update.second_stream[field] = value;
        onUpdate('second_stream', update.second_stream);
    };

    const _handleChangeAdditionalNbrCond = (field, value) => {
        if (field === "threshold") {
            _handleChangeCondition("additional_threshold", value);
        } else if (field === "threshold_type") {
            _handleChangeCondition("additional_threshold_type", value);
        } else {
            _handleChangeCondition(field, value);
        }
    };

    const _switchStreamNumberCondition = () => {
        let update = ObjectUtils.clone(alert);
        update.stream = alert.second_stream ? alert.second_stream : STREAM;
        update.second_stream = alert.stream ? alert.stream : STREAM;
        update.condition_parameters.search_query = alert.condition_parameters.additional_search_query;
        update.condition_parameters.threshold = alert.condition_parameters.additional_threshold;
        update.condition_parameters.threshold_type = alert.condition_parameters.additional_threshold_type;
        update.condition_parameters.additional_search_query = alert.condition_parameters.search_query;
        update.condition_parameters.additional_threshold = alert.condition_parameters.threshold;
        update.condition_parameters.additional_threshold_type = alert.condition_parameters.threshold_type;

        onUpdateAlert(update);
    };

    const buttonSwitchStream = (<Button onClick={_switchStreamNumberCondition} title="Switch" bsStyle="info" style={{ fontSize: '18px' }}><IconArrowsV/></Button>);

    let label;
    if (alert.condition_type === 'THEN') {
        label = <Row style={{ marginBottom: '0px' }}><Col md={2}></Col><Col md={2}><label><FormattedMessage id= "wizard.then" defaultMessage= "THEN" /></label></Col><Col md={8}>{buttonSwitchStream}</Col></Row>;
    } else if (alert.condition_type === 'AND') {
        label = <Row style={{ marginBottom: '0px' }}><Col md={2}></Col><Col md={10}><label><FormattedMessage id= "wizard.and" defaultMessage= "AND" /></label></Col></Row>;
    }

    return (
        <>
            <HighlightedDiv>
                <SearchQueryInput onUpdate={_handleChangeCondition} search_query={alert.condition_parameters.search_query}/>
                <br/>
                <FieldsInput stream={alert.stream} onSaveStream={_handleChangeStream} />
                <br/>
                <NumberInput onUpdate={_handleChangeCondition} threshold={alert.condition_parameters.threshold}
                                threshold_type={alert.condition_parameters.threshold_type} />
            </HighlightedDiv>
            <br/>
            {label}
            <br/>
            <HighlightedDiv>
                <SearchQueryInput onUpdate={_handleChangeCondition} search_query={alert.condition_parameters.additional_search_query} fieldName='additional_search_query' />
                <br/>
                <FieldsInput stream={alert.second_stream} onSaveStream={_handleChangeSecondStream} />
                <br/>
                <NumberInput onUpdate={_handleChangeAdditionalNbrCond} threshold={alert.condition_parameters.additional_threshold}
                                threshold_type={alert.condition_parameters.additional_threshold_type} />
            </HighlightedDiv>
            <br/>
            <TimeRangeInput onUpdate={_handleChangeCondition} time={time.toString()} time_type={time_type.toString()} />
            <br/>
            <GroupByInput onUpdate={_handleChangeCondition} grouping_fields={alert.condition_parameters.grouping_fields} />
            <br/>
            <Description onUpdate={onUpdate} description={alert.description}/>
            <br/>
        </>
    );
};

export default CorrelationCondition;
