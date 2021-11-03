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

package com.airbus_cyber_security.graylog.wizard.list;

import com.airbus_cyber_security.graylog.wizard.list.bundles.ExportAlertList;
import com.airbus_cyber_security.graylog.wizard.list.rest.models.requests.AlertListRequest;
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