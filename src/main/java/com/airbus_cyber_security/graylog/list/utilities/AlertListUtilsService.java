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

package com.airbus_cyber_security.graylog.list.utilities;

import com.airbus_cyber_security.graylog.list.AlertList;
import com.airbus_cyber_security.graylog.list.AlertListImpl;
import com.airbus_cyber_security.graylog.list.AlertListService;
import com.airbus_cyber_security.graylog.list.rest.models.requests.AlertListRequest;
import org.graylog2.database.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import java.io.UnsupportedEncodingException;

public class AlertListUtilsService {

    private static final String ENCODING = "UTF-8";
    private static final Logger LOG = LoggerFactory.getLogger(AlertListUtilsService.class);

    private final AlertListService alertListService;

    public AlertListUtilsService(AlertListService alertListService) {
        this.alertListService = alertListService;
    }

    public void checkIsValidRequest(AlertListRequest request){
        if(!alertListService.isValidRequest(request)){
            LOG.error("Invalid alert list request");
            throw new BadRequestException("Invalid alert list request.");
        }
    }

    public void incrementUsage(String title) {
        try {
            AlertList oldAlertList = alertListService.load(title);
            if(oldAlertList != null) {
                alertListService.update(java.net.URLDecoder.decode(title, ENCODING),
                        AlertListImpl.create(
                                title,
                                oldAlertList.getCreatedAt(),
                                oldAlertList.getCreatorUserId(),
                                oldAlertList.getCreatedAt(),
                                oldAlertList.getDescription(),
                                oldAlertList.getUsage() + 1,
                                oldAlertList.getLists()));
            }else{
                LOG.error("Failed to increment list, "+ title + " does not exist");
            }
        } catch (UnsupportedEncodingException | NotFoundException e) {
            LOG.error("Failed to increment list " + title);
        }
    }

    public void decrementUsage(String title) {
        try {
            AlertList oldAlertList = alertListService.load(title);
            if(oldAlertList != null) {
                int usage = oldAlertList.getUsage() - 1;
                if (usage < 0) {
                    usage = 0;
                }
                alertListService.update(java.net.URLDecoder.decode(title, ENCODING),
                        AlertListImpl.create(
                                title,
                                oldAlertList.getCreatedAt(),
                                oldAlertList.getCreatorUserId(),
                                oldAlertList.getCreatedAt(),
                                oldAlertList.getDescription(),
                                usage,
                                oldAlertList.getLists()));
            }else{
                LOG.error("Failed to decrement list, "+ title + " does not exist");
            }
        } catch (UnsupportedEncodingException | NotFoundException e) {
            LOG.error("Failed to decrement list " + title);
        }
    }
}
