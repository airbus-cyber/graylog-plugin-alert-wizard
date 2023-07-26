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
import { render } from 'wrappedTestingLibrary';
import ManageSettings from './ManageSettings';
import { IntlProvider } from 'react-intl';

describe('<ManageSettings>', () => {
    it('should not fail', () => {
        const onSave = jest.fn();
        const configuration = {
            field_order: [{ name: 'User', enabled: true }],
            default_values: {
                title: 'title',
                severity: 'info',
                matching_type: 'AND',
                threshold_type: '>',
                threshold: 0,
                time: 1,
                time_type: 1,
                backlog: 500,
                grace: 1
            },
            import_policy: 'REPLACE'
        };
        render(<IntlProvider locale="en" >
                   <ManageSettings config={configuration} onSave={onSave} />
               </IntlProvider>)
        // config={configuration}
    });
});
