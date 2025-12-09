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

import React, { useState } from 'react';
import { Button, Table } from 'components/bootstrap';
import { Icon } from 'components/common';

type Props = {
    fieldName: string,
    config: any,
    keys: string[],
}

const AggregationFieldValueProviderSummary = ({ fieldName, config, keys }: Props) => {

    const [displayDetails, setDisplayDetails] = useState(false);

    const toggleDisplayDetails = () => {
        setDisplayDetails(!displayDetails);
    };

    return (
        <dl>
            <dt>{fieldName}</dt>
            <Button bsStyle="link" className="btn-text" bsSize="xsmall" onClick={toggleDisplayDetails}>
                <Icon name={`arrow_${displayDetails ? 'drop_down' : 'right'}`} />&nbsp;
                {displayDetails ? 'Less details' : 'More details'}
            </Button>
            {displayDetails && (
                <Table condensed hover>
                    <tbody>
                        <tr>
                            <td>Is Key?</td>
                            <td>{keys.includes(fieldName) ? 'Yes' : 'No'}</td>
                        </tr>
                        <tr>
                            <td>Aggregation Time Range</td>
                            <td>{`${config.providers[0].aggregation_time_range} min`}</td>
                        </tr>
                    </tbody>
                </Table>
            )}
        </dl>
    );
};

export default AggregationFieldValueProviderSummary;
