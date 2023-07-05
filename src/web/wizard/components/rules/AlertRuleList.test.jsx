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
// * views/components/widgets/Widget.test.tsx
// * views/components/aggregationwizard/AggregationWizard.test.tsx

import React from 'react';
import { render } from 'wrappedTestingLibrary';
import { act } from 'react-dom/test-utils';
import { IntlProvider } from 'react-intl';
import AlertRuleList from './AlertRuleList';
//import fetch from 'logic/rest/FetchProvider';
import { StoreMock as MockStore } from 'helpers/mocking';

jest.mock('routing/Routes', () => ({ pluginRoute: () => '/route' }));
jest.mock('stores/users/CurrentUserStore', () => ({ CurrentUserStore: {} }));
jest.mock('stores/nodes/NodesStore', () => ({
  NodesActions: { list: (...args) => Promise.resolve({ nodes: [] }) },
  NodesStore: {}
}));

jest.mock('logic/rest/FetchProvider', () => jest.fn((method, url) => {
    if (url === 'http://localhost/system/locales') {
        // TODO should check the value that is returned by /system/locales in real life
        return Promise.resolve({ 'locales': '' })
    }
    if (url == 'http://localhost/plugins/com.airbus_cyber_security.graylog.wizard/alerts/data') {
        // TODO should return [] here, but there is lots of work to be done before this code can really be testable...
        //      so for the time being, I am giving up for lack of time
        return Promise.resolve()
    }
    return Promise.resolve()
}));

import { injectIntl, FormattedMessage } from 'react-intl';

describe('<AlertRuleList>', () => {
    it('should not fail', async () => {
//    fetch.mockReturnValueOnce(Promise.resolve([]))
        const field_order = [];
        await act(async () => {
            render(<IntlProvider locale="en">
                       <AlertRuleList field_order={field_order} />
                   </IntlProvider>)
        });
    });
});

/* Note:
   An alternative to mocking pluginRoute like this:
   jest.mock('routing/Routes', () => ({ pluginRoute: () => '/route' }));
   could be to register and unregister like this:
   import { PluginManifest, PluginStore } from 'graylog-web-plugin/plugin';
   const pluginManifest = new PluginManifest({}, {
       routes: [
           {path: '/wizard/NewAlert', component: () => <div>fake widget</div>},
           {path: '/wizard/ImportAlert', component: () => <div>fake widget</div>},
           {path: '/wizard/ExportAlert', component: () => <div>fake widget</div>},
       ]
   });
    beforeAll(() => PluginStore.register(pluginManifest));

    afterAll(() => PluginStore.unregister(pluginManifest));
*/