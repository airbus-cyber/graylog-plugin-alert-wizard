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
import type { PropsWithChildren } from 'react';
import * as React from 'react';
import { useIntl } from 'react-intl';

import { DropdownButton, MenuItem } from 'components/bootstrap';
import type { BsSize } from 'components/bootstrap/types';

const AllActionsDropdown = ({ children = undefined }: PropsWithChildren<{ bsSize?: BsSize }>) => {
    const intl = useIntl();

  return (
    <DropdownButton
      bsStyle="success"
      title={intl.formatMessage({id:"wizard.allActionDropdownLabel", defaultMessage:"Global actions"})}
      id="all-actions-dropdown">
      {children}
    </DropdownButton>
  );
};

export default AllActionsDropdown;
