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

package com.airbus_cyber_security.graylog.wizard.list.bundles;

import com.airbus_cyber_security.graylog.wizard.list.AlertList;
import com.airbus_cyber_security.graylog.wizard.list.AlertListServiceImpl;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AlertListExporter {

    private static final Logger LOG = LoggerFactory.getLogger(AlertListExporter.class);

    private final AlertListServiceImpl alertListService;

    public AlertListExporter(AlertListServiceImpl alertListService){
        this.alertListService = alertListService;
    }

    public List<ExportAlertList> export(List<String> titles){
        List<ExportAlertList> listAlertLists = Lists.newArrayListWithCapacity(titles.size());

        for (String title : titles) {
            try {
                final AlertList list = alertListService.load(title);

                listAlertLists.add(ExportAlertList.create(
                        title,
                        list.getDescription(),
                        list.getLists()));

            }catch(Exception e) {
                /* Can't find stream, condition or notification */
                LOG.warn("Can't export alert list "+ title + ": "+e.getMessage());
            }
        }

        return listAlertLists;
    }
}
