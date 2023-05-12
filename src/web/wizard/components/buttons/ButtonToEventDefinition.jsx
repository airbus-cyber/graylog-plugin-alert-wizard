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

import createReactClass from 'create-react-class';
import React from 'react';
import PropTypes from 'prop-types';
import { LinkContainer } from 'react-router-bootstrap';
import { Button } from 'components/bootstrap';
import { useIntl, FormattedMessage } from 'react-intl';
import Routes from 'routing/Routes';

const ButtonToEventDefinition = ({target, disabled}) => {
    const intl = useIntl();
    const tooltip = intl.formatMessage({id: "wizard.tooltipEventDefinition", defaultMessage: "Edit event definition for this alert rule"});

    return (
        <LinkContainer disabled={disabled} to={Routes.ALERTS.DEFINITIONS.edit(target)} >
            <Button bsStyle="info" title={tooltip} >
                <FormattedMessage id="wizard.eventDefinition" defaultMessage="Event definition" />
            </Button>
        </LinkContainer>
    );
}

ButtonToEventDefinition.propTypes = {
    target: PropTypes.string.isRequired,
    disabled: PropTypes.bool,
};

export default ButtonToEventDefinition;