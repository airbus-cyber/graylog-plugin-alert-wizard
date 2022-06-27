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

import { Spinner } from '../graylog2-server/graylog2-web-interface/src/components/common';

import lodash from 'lodash';
import connect from '../graylog2-server/graylog2-web-interface/src/stores/connect';
import { FieldTypesStore } from '../graylog2-server/graylog2-web-interface/src/views/stores/FieldTypesStore';
import { defaultCompare } from '../graylog2-server/graylog2-web-interface/src/views/logic/DefaultCompare';

export default function(WrappedComponent) {
    const Container = class extends React.Component {
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
            const { fieldTypes, ...passThroughProps } = this.props;

            const isLoading = typeof fieldTypes.all !== 'object';

            if (isLoading) {
                return <Spinner text="Loading, please wait..." />;
            }

            const allFieldTypes = fieldTypes.all.toJS();
            const formattedFields = this.formatFields(allFieldTypes);

            return (
                <WrappedComponent
                    formattedFields={formattedFields}
                    {...passThroughProps}
                />
            );
        }
    };
    return connect(Container, {
        fieldTypes: FieldTypesStore
    });
}
