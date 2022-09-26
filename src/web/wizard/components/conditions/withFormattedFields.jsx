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

import { Spinner } from 'components/common';

import lodash from 'lodash';
import { defaultCompare } from 'views/logic/DefaultCompare';
import useFieldTypes from 'views/logic/fieldtypes/useFieldTypes';
import { ALL_MESSAGES_TIMERANGE } from 'views/Constants';


// TODO try to avoid this HOL. We just like to get the formattedFields...
export default function(WrappedComponent) {
    const Container = class extends React.Component {
        // TODO maybe should use react's useMemo instead of lodash.memoize
        formatFields = lodash.memoize(
            (fieldTypes) => {
                return fieldTypes
                    .sort((ftA, ftB) => defaultCompare(ftA.name, ftB.name))
                    .map((fieldType) => {
                        return {
                            label: `${fieldType.name} – ${fieldType.value.type.type}`,
                            value: fieldType.name,
                        };
                    }
                );
            },
            (fieldTypes) => fieldTypes.map((ft) => ft.name).join('-'),
        );

        render() {
            const { data: fieldTypes } = useFieldTypes([], ALL_MESSAGES_TIMERANGE);
            const isLoading = !fieldTypes;

            if (isLoading) {
                return <Spinner text="Loading, please wait..." />;
            }

            const formattedFields = this.formatFields(fieldTypes);

            return (
                <WrappedComponent formattedFields={formattedFields} {...this.props} />
            );
        }
    };
    return Container;
}
