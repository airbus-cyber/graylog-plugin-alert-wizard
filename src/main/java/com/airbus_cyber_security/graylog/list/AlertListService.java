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
