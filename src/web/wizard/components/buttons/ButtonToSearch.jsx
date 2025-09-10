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
import PropTypes from 'prop-types';
import { Button } from 'components/bootstrap';
import { Icon } from 'components/common';
import { useIntl } from 'react-intl';
import Routes from 'routing/Routes';

function normalizeSearchQuery(searchQuery) {
    if (searchQuery && searchQuery !== '*') {
        return searchQuery;
    }

    return '';
}

function createUrl(searchQuery, stream) {
    const normSearchQuery = normalizeSearchQuery(searchQuery);
    return Routes.search_with_query(normSearchQuery, "relative", {"relative": 86400}, [stream]);
}

const ButtonToSearch = ({searchQuery1, searchQuery2, stream1, stream2, disabled}) => {
    const intl = useIntl();
    const tooltip = intl.formatMessage({id: "wizard.tooltipSearch", defaultMessage: "Launch search for this alert rule"});

    const openSearchTabs = () => {
        const url1 = createUrl(searchQuery1, stream1);
        window.open(url1, '_blank', 'noopener,noreferrer');

        if(stream2 || searchQuery2) {
            const url2 = createUrl(searchQuery2, stream2);
            window.open(url2, '_blank', 'noopener,noreferrer');
        }
    };

    return (
        <Button bsStyle="info" title={tooltip} disabled={disabled} onClick={openSearchTabs}>
            <Icon name="play_arrow" />
        </Button>
    );
}

ButtonToSearch.propTypes = {
    searchQuery: PropTypes.string,
    stream1: PropTypes.string,
    stream2: PropTypes.string,
    disabled: PropTypes.bool,
};

export default ButtonToSearch;