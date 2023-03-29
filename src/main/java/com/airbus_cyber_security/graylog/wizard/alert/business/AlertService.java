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
