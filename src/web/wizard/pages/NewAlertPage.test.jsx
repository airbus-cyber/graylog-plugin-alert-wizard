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
import { act } from 'react-dom/test-utils';
import { render } from 'wrappedTestingLibrary';
import { StoreMock as MockStore } from 'helpers/mocking';
import { BrowserRouter } from 'react-router-dom';
import fetch from 'logic/rest/FetchProvider';

import NewAlertPage from './NewAlertPage';

// sources of inspiration for this code:
// * views/components/SearchBar.test.tsx
jest.mock('routing/Routes', () => ({ pluginRoute: () => '/route' }));
jest.mock('stores/users/CurrentUserStore', () => ({ CurrentUserStore: MockStore('get') }));
jest.mock('stores/nodes/NodesStore', () => ({
    NodesActions: { list: (...args) => Promise.resolve({ nodes: [] }) },
    NodesStore: MockStore(),
}));
// TODO I do not understand why this mock is necessary, shouldn't mock of FetchProvider url /streams sufficient?
jest.mock('stores/streams/StreamsStore', () => MockStore(
  ['listStreams', () => ({ then: jest.fn() })],
  'availableStreams',
));

jest.mock('logic/rest/FetchProvider', () => jest.fn((method, url) => {
    const MOCKED_REQUESTS_RESULTS = {
        // TODO should check the value that is returned by /system/locales in real life
        '/system/locales': { 'locales': '' },
        '/plugins/com.airbus_cyber_security.graylog.wizard/config': {
            'default_values': {
                title: 'title',
                severity: 'info',
                matching_type: 'AND',
                threshold_type: '>',
                threshold: 0,
                time: 1,
                time_type: 1,
                backlog: 500,
                grace: 1
            }
        }
    };
    const path = url.slice('http://localhost'.length)
    const result = MOCKED_REQUESTS_RESULTS[path]
    return Promise.resolve(result);
}));

describe('<NewAlertPage>', () => {
    it('should not fail', async () => {
        await act(async () => {
            render(<BrowserRouter>
                       <NewAlertPage />
                   </BrowserRouter>);
        });
    });
});
