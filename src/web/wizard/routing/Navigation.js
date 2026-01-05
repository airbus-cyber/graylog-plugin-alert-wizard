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

import Routes from 'routing/Routes';

// Note: it seems (I am not 100% sure), we can't just set '/wizard/AlertRules' as a value here
//       thus losing the SSOT with the paths used when registering the plugin
//       this is because graylog function Routes.pluginRoute prefix the path with some prefix that may have been configured for the graylog server
// TODO check this is true
// TODO this may simply be a constant we export const WIZARD_ROUTE = Routes.pluginRoute('WIZARD_ALERTRULES');
function getWizardRoute() {
    return Routes.pluginRoute('WIZARD_ALERTRULES');
}

function getWizardNewAlertRoute() {
    return Routes.pluginRoute('WIZARD_NEWALERT');
}

function getWizardListRoute() {
    return Routes.pluginRoute('WIZARD_LISTS');
}

function getWizardNewListRoute() {
    return Routes.pluginRoute('WIZARD_NEWLIST');
}

const Navigation = {
    getWizardRoute,
    getWizardNewAlertRoute,
    getWizardListRoute,
    getWizardNewListRoute
};

export default Navigation;