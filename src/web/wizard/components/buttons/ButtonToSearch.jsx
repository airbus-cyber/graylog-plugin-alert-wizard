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
import { LinkContainer } from 'react-router-bootstrap';
import { Button } from 'components/bootstrap';
import { Icon } from 'components/common';
import { useIntl, FormattedMessage } from 'react-intl';
import Routes from 'routing/Routes';

const ButtonToSearch = ({stream1, stream2, disabled}) => {
    const intl = useIntl();
    const tooltip = intl.formatMessage({id: "wizard.tooltipSearch", defaultMessage: "Launch search for this alert rule"});
    const stream = [stream1];
    if (stream2) {
        stream.push(stream2);
    }
    const searchURL = Routes.search_with_query("", "relative", {"relative": 86400}, stream);
    const link = {
        pathname: Routes.SEARCH,
        search: searchURL.substring(searchURL.indexOf('?'))
    }

    return (
        <LinkContainer disabled={disabled} to={link} >
            <Button bsStyle="info" title={tooltip} >
                <Icon name="play_arrow" />
            </Button>
        </LinkContainer>
    );
}

ButtonToSearch.propTypes = {
    stream: PropTypes.string.isRequired,
    disabled: PropTypes.bool,
};

export default ButtonToSearch;