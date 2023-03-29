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

package com.airbus_cyber_security.graylog.wizard.alert.business;

import org.graylog2.alerts.Alert;
import org.joda.time.DateTime;

import javax.inject.Inject;
import java.util.List;

public class AlertService {

    private final org.graylog2.alerts.AlertService alertService;

    @Inject
    public AlertService(org.graylog2.alerts.AlertService alertService) {
        this.alertService = alertService;
    }

    public int countAlerts(String streamID, DateTime since) {
        List<Alert> alerts = this.alertService.loadRecentOfStream(streamID, since, 999);
        return alerts.size();
    }
}
