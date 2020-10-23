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
