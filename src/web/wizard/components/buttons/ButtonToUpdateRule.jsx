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
import { IfPermitted } from 'components/common';
import { useIntl, FormattedMessage } from 'react-intl';
import Routes from 'routing/Routes';

const ButtonToUpdateRule = ({target, disabled}) => {
    const intl = useIntl();
    const tooltip = intl.formatMessage({id: "wizard.buttonInfoUpdate", defaultMessage: "Edit this alert rule"});

    const openUpdateRule = () => {
        const url = Routes.pluginRoute('WIZARD_UPDATEALERT_ALERTRULETITLE')(target.replace(/\//g, '%2F'));
        window.open(url,"_self");
    }

    return (
        <IfPermitted permissions="wizard_alerts_rules:read">
            <Button bsStyle="info" title={tooltip} disabled={disabled} onClick={openUpdateRule}>
                <FormattedMessage id="wizard.edit" defaultMessage="Edit"/>
            </Button>
        </IfPermitted>
    );
}

ButtonToUpdateRule.propTypes = {
    target: PropTypes.string.isRequired,
    disabled: PropTypes.bool,
};

export default ButtonToUpdateRule;