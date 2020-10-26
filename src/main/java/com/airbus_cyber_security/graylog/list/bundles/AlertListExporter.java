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

package com.airbus_cyber_security.graylog.list.bundles;

import com.airbus_cyber_security.graylog.list.AlertList;
import com.airbus_cyber_security.graylog.list.AlertListService;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AlertListExporter {

    private static final Logger LOG = LoggerFactory.getLogger(AlertListExporter.class);

    private final AlertListService alertListService;

    public AlertListExporter(AlertListService alertListService){
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
