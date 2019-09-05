package com.airbus_cyber_security.graylog.list.bundles;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.airbus_cyber_security.graylog.list.AlertList;
import com.airbus_cyber_security.graylog.list.AlertListService;
import com.google.common.collect.Lists;

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
                        list.getDescription()));

            }catch(Exception e) {
                /* Can't find stream, condition or notification */
                LOG.warn("Can't export alert list "+ title + ": "+e.getMessage());
            }
        }

        return listAlertLists;
    }
}
