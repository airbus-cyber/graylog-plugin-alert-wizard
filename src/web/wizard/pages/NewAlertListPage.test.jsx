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
import { Router } from 'react-router-dom';
import history from 'util/History';

import NewAlertListPage from './NewAlertListPage';

jest.mock('routing/Routes', () => ({ pluginRoute: () => '/route' }));

describe('<NewAlertListPage>', () => {
    it('should not fail', () => {
        render(<Router history={history}>
                 <NewAlertListPage />
               </Router>);
    });
});