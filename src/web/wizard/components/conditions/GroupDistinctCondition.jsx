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
import ObjectUtils from 'util/ObjectUtils';
import FieldsInput from 'wizard/components/inputs/FieldsInput';
import NumberInput from 'wizard/components/inputs/NumberInput';
import TimeRangeInput from 'wizard/components/inputs/TimeRangeInput';
import GroupByInput from 'wizard/components/inputs/GroupByInput';
import DistinctInput from 'wizard/components/inputs/DistinctInput';
import Description from 'wizard/components/inputs/Description';
import SearchQueryInput from 'wizard/components/inputs/SearchQueryInput';
import TimeHook from './TimeHook';

const GroupDistinctCondition = ({alert, onUpdate}) => {

    const [state, setState] = useState({ alert: ObjectUtils.clone(alert), time: 0, time_type: 0 });

    useEffect(() => {
        const { time, time_type } = TimeHook.computeTime(alert);
        setState({ alert, time, time_type });
    }, []);

    const _handleChangeCondition = (field, value) => {
        const update = ObjectUtils.clone(state.alert);

        if (field === "threshold" || field === "additional_threshold" || field === "time") {
            update.condition_parameters[field] = parseInt(value);
        } else {
            update.condition_parameters[field] = value;
        }

        const { time, time_type } = TimeHook.computeTime(update);
        setState({ alert: update, time, time_type });
        onUpdate('condition_parameters', update.condition_parameters);
    };

    const _handleChangeStream = (field, value) => {
        const update = ObjectUtils.clone(state.alert);
        update.stream[field] = value;

        const { time, time_type } = TimeHook.computeTime(update);
        setState({ alert: update, time, time_type });
        onUpdate('stream', update.stream);
    };

    return (
        <>
            <SearchQueryInput onUpdate={_handleChangeCondition} search_query={state.alert.condition_parameters.search_query}/>
            <br/>
            <FieldsInput stream={state.alert.stream} onSaveStream={_handleChangeStream} />
            <br/>
            <NumberInput onUpdate={_handleChangeCondition} threshold={state.alert.condition_parameters.threshold}
                            threshold_type={state.alert.condition_parameters.threshold_type} />
            <br/>
            <TimeRangeInput onUpdate={_handleChangeCondition} time={state.time.toString()} time_type={state.time_type.toString()} />
            <br/>
            <GroupByInput onUpdate={_handleChangeCondition} grouping_fields={state.alert.condition_parameters.grouping_fields} />
            <br/>
            <DistinctInput onUpdate={_handleChangeCondition} distinct_by={state.alert.condition_parameters.distinct_by} />
            <br/>
            <Description onUpdate={onUpdate} description={state.alert.description}/>
            <br/>
        </>
    );
}

export default GroupDistinctCondition;
