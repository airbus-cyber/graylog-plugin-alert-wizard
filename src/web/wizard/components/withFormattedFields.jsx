/*
 * graylog-plugin-alert-wizard Source Code
 * Copyright (C) 2018-2020 - Airbus CyberSecurity (SAS) - All rights reserved
 *
 * This file is part of the graylog-plugin-alert-wizard GPL Source Code.
 *
 * graylog-plugin-alert-wizard Source Code is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this code.  If not, see <http://www.gnu.org/licenses/>.
 */

import React from 'react';

import { Spinner } from 'components/common';

import lodash from 'lodash';
import connect from 'stores/connect';
import { FieldTypesStore } from 'views/stores/FieldTypesStore';
import { defaultCompare } from 'views/logic/DefaultCompare';

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
