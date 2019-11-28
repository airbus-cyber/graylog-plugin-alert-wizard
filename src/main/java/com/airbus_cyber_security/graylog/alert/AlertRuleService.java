package com.airbus_cyber_security.graylog.alert;

import com.airbus_cyber_security.graylog.alert.bundles.ExportAlertRule;
import com.airbus_cyber_security.graylog.alert.rest.models.requests.AlertRuleRequest;
import com.mongodb.MongoException;
import org.graylog2.database.NotFoundException;

import java.io.UnsupportedEncodingException;
import java.util.List;

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
