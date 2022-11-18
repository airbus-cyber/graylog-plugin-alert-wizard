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

// sources of inspiration for this code:
// * views/components/searchbar/queryvalidation/validateQuery.test.ts
// * views/components/searchbar/completions/FieldValueCompletion.test.ts
// * routing/AppRouter.test.tsx
// * pages/ShowMessagePage.test.tsx
import React from 'react';
import { render } from 'wrappedTestingLibrary';
import { StoreMock as MockStore, asMock } from 'helpers/mocking';
import WizardPage from './WizardPage';

import fetch from 'logic/rest/FetchProvider';

//jest.mock('logic/rest/FetchProvider', () => jest.fn(() => Promise.resolve()));
jest.mock('logic/rest/FetchProvider', () => jest.fn((method, url) => {
    if (url === 'http://localhost/system/locales') {
        // TODO should check the value that is returned by /system/locales in real life
        return Promise.resolve({ 'locales': '' })
    }
    return Promise.resolve()
}));
jest.mock('stores/users/CurrentUserStore', () => ({ CurrentUserStore: MockStore('get') }));
// TODO: this is not totally satisfactory as there will be nodes (to retrieve the plugin's version).
//       It just shows the code is complex and not testable
jest.mock('stores/nodes/NodesStore', () => ({
  NodesActions: { list: (...args) => Promise.resolve({ nodes: [] }) },
  NodesStore: MockStore(),
}));

describe('<WizardPage>', () => {
    it('should not fail', () => {
        fetch.mockReturnValueOnce(Promise.resolve({ field_order: [] }))
        render(<WizardPage />);
    });
});
