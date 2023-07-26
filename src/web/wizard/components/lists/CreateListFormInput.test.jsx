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
//   ({ pluginRoute: () => () => '/route' })
//   views/pages/DashboardsPage.test.tsx
// testing is more complex than it seems: graylog2-web-interface are somehow configured to be wrapped by a Router. I quite don't understand this
import React from 'react';
import { render } from 'wrappedTestingLibrary';
import { asMock } from 'helpers/mocking';
import { BrowserRouter } from 'react-router-dom';

import { IntlProvider } from 'react-intl';
import CreateListFormInput from 'wizard/components/lists/CreateListFormInput';

jest.mock('routing/Routes', () => ({ pluginRoute: () => '/route' }));

describe('<CreateListFormInput>', () => {

  it('should not fail when rendering', () => {
    render(<IntlProvider locale="en" >
             <BrowserRouter history={history}>
               <CreateListFormInput onSave={jest.fn()} />
             </BrowserRouter>
           </IntlProvider>);
  });
});
