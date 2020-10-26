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

package com.airbus_cyber_security.graylog.list;

import com.airbus_cyber_security.graylog.list.bundles.ExportAlertList;
import com.airbus_cyber_security.graylog.list.rest.models.requests.AlertListRequest;
import com.mongodb.MongoException;
import org.graylog2.database.NotFoundException;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface AlertListService {
    long count();

    AlertList update(String title, AlertList list);

    AlertList create(AlertList list);

    List<AlertList> all();

    int destroy(String listTitle) throws MongoException, UnsupportedEncodingException;

    AlertList load(String title)  throws NotFoundException;

    boolean isPresent(String title);

    boolean isValidRequest(AlertListRequest request);

    boolean isValidImportRequest(ExportAlertList request);
}
