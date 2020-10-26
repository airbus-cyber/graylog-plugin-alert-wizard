/*
 * graylog-plugin-alert-wizard Source Code
 * Copyright (C) 2018-2020 - Airbus CyberSecurity (SAS) - All rights reserved
 *
 * This file is part of the graylog-plugin-alert-wizard GPL Source Code.
 *
 * graylog-plugin-alert-wizard Source Code is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this code.  If not, see <http://www.gnu.org/licenses/>.
 */

import Reflux from 'reflux';

const AlertRuleActions = Reflux.createActions({
    list: {asyncResult: true},
    listWithData: {asyncResult: true},
    get: {asyncResult: true},
    getData: {asyncResult: true},
    create: {asyncResult: true},
    deleteByName: {asyncResult: true},
    update: {asyncResult: true},
    clone: {asyncResult: true},
    exportAlertRules: {asyncResult: true},
    importAlertRules: {asyncResult: true},
});

export default AlertRuleActions;
