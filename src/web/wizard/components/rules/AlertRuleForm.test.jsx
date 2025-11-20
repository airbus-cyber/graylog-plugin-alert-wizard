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
import { BrowserRouter } from 'react-router-dom';
import { IntlProvider } from 'react-intl';
import { StoreMock as MockStore } from 'helpers/mocking';

//import AlertRuleForm from './AlertRuleForm';

jest.mock('routing/Routes', () => ({ pluginRoute: () => '/route' }));
// TODO I do not understand why this mock is necessary, shouldn't mock of FetchProvider url /streams sufficient?
jest.mock('stores/streams/StreamsStore', () => MockStore(
  ['listStreams', () => ({ then: jest.fn() })],
));

xdescribe('<AlertRuleForm>', () => {
    it('should not fail', () => {
        const alert = {
            condition_parameters: {
                time: 1
            }
        };
        const onSave = jest.fn();
        render(<BrowserRouter>
                   <IntlProvider locale="en" >
                        <AlertRuleForm initialAlert={alert} onSave={onSave} />
                   </IntlProvider>
               </BrowserRouter>);
    });
});
