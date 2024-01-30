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
import { useIntl, FormattedMessage } from 'react-intl';

import CloneButton from 'wizard/components/buttons/CloneButton';


const AlertListCloneForm = ({listTitle, onSubmit}) => {
    const intl = useIntl();
    // TODO uniformize: use only FormattedMessage here and remove intl? (if possible, otherwise try only intl)
    const messages = {
        infoClone: intl.formatMessage({id: "wizard.buttonInfoCloneList", defaultMessage: "Clone this alert list"}),
        placeholderTitle: intl.formatMessage({id: "wizard.placeholderCloneTitleList", defaultMessage: "A descriptive name of the new alert list"}),
        modalTitle: <FormattedMessage id="wizard.cloneList" defaultMessage='Cloning List "{title}"' values={{title: listTitle}} />
    };

    return (
        <CloneButton title={listTitle} onSubmit={onSubmit} messages={messages} />
    );
};

export default AlertListCloneForm;
