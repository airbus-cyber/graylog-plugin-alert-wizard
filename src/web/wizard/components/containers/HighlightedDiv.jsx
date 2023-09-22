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

import styled, { css } from 'styled-components';

// sources of inspiration for this code:
// * views/components/messagelist/decorators/AddDecoratorButton.tsx
// * views/components/widgets/MessageTable.tsx
// * theme/docs/ThemeProvider.md
const HighlightedDiv = styled.div(({ theme }) => css`
  margin-bottom: 10px;
  margin-top: 10px;
  margin-left: 5px;
  display: inline-block;
  border-style: solid;
  border-color: ${theme.colors.gray[80]};
  border-radius: 5px;
  border-width: 1px;
  padding: 10px;
  background: ${theme.colors.global.background};
`);

export default HighlightedDiv;