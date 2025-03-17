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
import { useCallback } from 'react';
import { MenuItem } from 'components/bootstrap';
import useHistory from 'routing/useHistory';
import Navigation from 'wizard/routing/Navigation';

const CreateRuleSearchAction = (searchActionProps) => {

    const history = useHistory();

    function _extractQueryString(searchView) {
        if(searchView.search.queries && searchView.search.queries.size > 0)
            return searchView.search.queries.first().query.query_string;

        return '*';
    }

    const createWizardRule = useCallback(() => {
        const queryString = _extractQueryString(searchActionProps.search);
        history.pushWithState(Navigation.getWizardNewAlertRoute(), { queryString });
    }, [history]);

    return (
        <>
            <MenuItem onSelect={createWizardRule} icon="edit">
                Create wizard alert rule
            </MenuItem>
        </>
    )
}

export default CreateRuleSearchAction;