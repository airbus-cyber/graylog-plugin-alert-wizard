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

package com.airbus_cyber_security.graylog.wizard.list.utilities;

import com.airbus_cyber_security.graylog.wizard.list.AlertList;
import com.airbus_cyber_security.graylog.wizard.list.AlertListImpl;
import com.airbus_cyber_security.graylog.wizard.list.AlertListService;
import com.airbus_cyber_security.graylog.wizard.list.rest.models.requests.AlertListRequest;
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
            } else {
                LOG.error("Failed to increment list, " + title + " does not exist");
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
