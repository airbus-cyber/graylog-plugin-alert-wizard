package com.airbus_cyber_security.graylog.list.utilities;

import com.airbus_cyber_security.graylog.list.AlertList;
import com.airbus_cyber_security.graylog.list.AlertListImpl;
import com.airbus_cyber_security.graylog.list.AlertListService;
import com.airbus_cyber_security.graylog.list.rest.models.requests.AlertListRequest;
import org.graylog2.database.NotFoundException;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.joda.time.DateTime;
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
            AlertList oldAlert = alertListService.load(title);
            alertListService.update(java.net.URLDecoder.decode(title, ENCODING),
                    AlertListImpl.create(
                            title,
                            oldAlert.getCreatedAt(),
                            oldAlert.getCreatorUserId(),
                            oldAlert.getCreatedAt(),
                            oldAlert.getDescription(),
                            oldAlert.getUsage()+1,
                            oldAlert.getLists()));
        } catch (UnsupportedEncodingException | NotFoundException e) {
            LOG.error("Failed to increment list");
        }
    }

    public void decrementUsage(String title) {
        try {
            AlertList oldAlert = alertListService.load(title);
            int usage = oldAlert.getUsage()-1;
            if(usage < 0){
                usage = 0;
            }
            alertListService.update(java.net.URLDecoder.decode(title, ENCODING),
                    AlertListImpl.create(
                            title,
                            oldAlert.getCreatedAt(),
                            oldAlert.getCreatorUserId(),
                            oldAlert.getCreatedAt(),
                            oldAlert.getDescription(),
                            usage,
                            oldAlert.getLists()));
        } catch (UnsupportedEncodingException | NotFoundException e) {
            LOG.error("Failed to decrement list");
        }
    }
}
