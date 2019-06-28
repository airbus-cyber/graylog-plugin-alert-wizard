package com.airbus_cyber_security.graylog.alert;

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.airbus_cyber_security.graylog.alert.bundles.ExportAlertRule;
import org.graylog2.database.NotFoundException;

import com.airbus_cyber_security.graylog.alert.rest.models.requests.AlertRuleRequest;
import com.mongodb.MongoException;

public interface AlertRuleService {
    long count();

    AlertRule update(String title, AlertRule alert);

    AlertRule create(AlertRule alert);
    
    List<AlertRule> all();
    
    int destroy(String alertTitle) throws MongoException, UnsupportedEncodingException;

    AlertRule load(String title)  throws NotFoundException;
    
    boolean isPresent(String title);

    boolean isValidRequest(AlertRuleRequest request);

    boolean isValidImportRequest(ExportAlertRule request);

}
