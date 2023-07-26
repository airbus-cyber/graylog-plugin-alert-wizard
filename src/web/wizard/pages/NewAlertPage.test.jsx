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

jest.mock('routing/Routes', () => ({ pluginRoute: () => '/route' }));
jest.mock('stores/users/CurrentUserStore', () => ({ CurrentUserStore: MockStore('get') }));
jest.mock('stores/nodes/NodesStore', () => ({
    NodesActions: { list: (...args) => Promise.resolve({ nodes: [] }) },
    NodesStore: MockStore(),
}));
jest.mock('logic/rest/FetchProvider', () => jest.fn((method, url) => {
    if (url === 'http://localhost/system/locales') {
        // TODO should check the value that is returned by /system/locales in real life
        return Promise.resolve({ 'locales': '' })
    }
    if (url === 'http://localhost/plugins/com.airbus_cyber_security.graylog.wizard/config') {
        // TODO should check the value that is returned by /system/locales in real life
        return Promise.resolve()
    }
    return Promise.resolve()
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
