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
import { IntlProvider } from 'react-intl';
import messages from 'translations/fr.json';

import {AVAILABLE_AGGREGATION_TYPES} from './StatisticalInput';


describe('<StatisticalInput>', () => {
    it('should have a correct sum of squares translation', () => {
        const sumOfSquares = AVAILABLE_AGGREGATION_TYPES[7].label;
        const root = render(<IntlProvider locale="fr" messages={messages}>
                                    {sumOfSquares}
                            </IntlProvider>);
        const fullContentStr = root.container.innerHTML;
        expect(fullContentStr.substring(fullContentStr.lastIndexOf('</style>') + 8, fullContentStr.length)).toBe('somme des carrés');
    });

    it('should have a correct sum translation', () => {
        const sumOfSquares = AVAILABLE_AGGREGATION_TYPES[4].label;
        const root = render(<IntlProvider locale="fr" messages={messages}>
                                    {sumOfSquares}
                            </IntlProvider>);
        const fullContentStr = root.container.innerHTML;
        expect(fullContentStr.substring(fullContentStr.lastIndexOf('</style>') + 8, fullContentStr.length)).toBe('somme');
    });
});
