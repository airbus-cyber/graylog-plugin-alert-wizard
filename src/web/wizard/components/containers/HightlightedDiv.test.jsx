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

// TODO maybe this widget is better tested using the technique of snapshot testing
import React from 'react';
import { render } from 'wrappedTestingLibrary';
import HighlightedDiv from './HighlightedDiv';
import noir from 'theme/variants/noir';
import { ThemeProvider } from 'styled-components';

describe('<HighlightedDiv>', () => {
    it('should not fail', () => {
        const theme = {
            colors: noir
        };
        const { getByTestId } = render(
            <ThemeProvider theme={theme} >
                <HighlightedDiv data-testid="test-identifier" />
            </ThemeProvider>
        );
        const element = getByTestId('test-identifier');
        expect(element).toHaveStyleRule('background', '#222');
    });
});