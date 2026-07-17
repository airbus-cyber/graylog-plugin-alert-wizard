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

import { qualifyUrls } from 'routing/Routes';
// XHRGET
// http://127.0.0.1:9000/api/plugins/com.airbus_cyber_security.graylog.wizard/alerts/paginated?page=1&per_page=20&sort=title&order=asc
// [HTTP/1.1 200 OK 103ms]

	
// query	""
// pagination	{ total: 1, count: 1, page: 1, … }
// total	1
// sort	"title"
// order	"asc"
// elements	[ {…} ]
// 0	{ id: "6a4f45b621fd20cd7961caf3", condition: "6a4f45b621fd20cd7961caf0", priority: 1, … }
// attributes	[]
// defaults	{ sort: {…} }
const AlertWizardRoutes = {
  WIZARD: {
    ALERTRULES: '/wizard/AlertRules',
    NEWALERT: '/wizard/NewAlert',
    LISTS: '/wizard/Lists',
    NEWLIST: '/wizard/NewList',
    UPDATEALERT: (alertId: string) => `/wizard/UpdateAlert/${alertId}`,
    IMPORTALERT: '/wizard/ImportAlert',
    UPDATELIST: (alertListTitle: string) => `/wizard/UpdateList/${alertListTitle}`,
    IMPORTLIST: '/wizard/ImportList',
    EXPORTLIST: '/wizard/ExportList',
  },
};

const qualifiedRoutes = qualifyUrls(AlertWizardRoutes);

const unqualified = AlertWizardRoutes;

const defaultExport = {
  ...qualifiedRoutes,
  unqualified,
};

export default defaultExport;
