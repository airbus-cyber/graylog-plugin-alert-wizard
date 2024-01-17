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
import { StoreMock as MockStore } from 'helpers/mocking';
import { IntlProvider } from 'react-intl';

import FieldRuleList from './FieldRuleList';

jest.mock('wizard/actions/AlertListActions', () => ({
    list: jest.fn(() => Promise.resolve([]))
}));

jest.mock('stores/sessions/SessionStore', () => ({
  SessionStore: {
      isLoggedIn: jest.fn(() => true)
  }
}));

// TODO could instead maybe mock method FetchProvider.fetchPeriodically???
jest.mock('stores/nodes/NodesStore', () => ({
  NodesActions: { list: (...args) => Promise.resolve({ nodes: [] }) },
  NodesStore: MockStore(),
}));

// source of inspiration: components/messageloaders/RawMessageLoader.test.tsx
jest.mock('stores/system/SystemStore', () => ({ SystemStore: MockStore() }));

jest.mock('logic/rest/FetchProvider', () => {
    return jest.fn((method, url) => {
        console.log('URL: ' + url);
        const MOCKED_REQUESTS_RESULTS = {
            '/system/fields': {
                fields: []
            }
        };
        const path = url.slice('http://localhost'.length)
        const result = MOCKED_REQUESTS_RESULTS[path]
        return Promise.resolve(result);
    })
});

describe('<FieldRuleList>', () => {
    it('should not fail', async () => {
        const rules = [{
            field: 'field',
            type: 5,
            value: 'hello'
        }];
        const onSaveStream = jest.fn();
        // TODO use screen instead??
        const subject = render(<IntlProvider locale="en" >
                   <FieldRuleList fieldRules={rules} onSaveStream={onSaveStream} />
               </IntlProvider>);
        await subject.findAllByRole('textbox');
    });
});

