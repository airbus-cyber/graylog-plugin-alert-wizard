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
import { Input } from 'components/bootstrap';
import { getValueFromInput } from 'util/FormsUtils';
import cloneDeep from 'lodash/cloneDeep';

type Props = {
    config: any,
    onChange: (newConfig: Props['config']) => void,
}

export const AGGREGATION_TYPE = 'aggregation-v1';
export const AGGREGATION_DEFAULT_CONFIG = {
    aggregation_time_range: 0
};
export const AGGREGATION_REQUIRED_FIELDS = [
    'aggregation_time_range'
];

const AggregationFieldValueProviderForm = ({config, onChange}: Props) => {

    const getProvider = () => {
        return config.providers.find((provider) => provider.type === AGGREGATION_TYPE);
    }

    const _onUpdate = (event: React.ChangeEvent<HTMLInputElement>) => {
        const { name } = event.target;
        const value = getValueFromInput(event.target);

        const nextProviders = cloneDeep(config.providers);
        const templateProvider = nextProviders.find((provider) => provider.type === AGGREGATION_TYPE);

        templateProvider[name] = value;
        onChange({ ...config, providers: nextProviders });
    };

    return (
        <Input
            id="aggregation_time_range"
            type="number"
            label="Notification Aggregation Time Range"
            help="The default number of minutes to use previous aggregate id"
            name="aggregation_time_range"
            min="0"
            value={getProvider().aggregation_time_range}
            onChange={_onUpdate}
        />
    );
};

export default AggregationFieldValueProviderForm;
