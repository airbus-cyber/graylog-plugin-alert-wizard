package com.airbus_cyber_security.graylog.list.utilities;

import com.airbus_cyber_security.graylog.list.rest.models.requests.AlertListRequest;
import com.airbus_cyber_security.graylog.list.AlertListService;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;

public class AlertListUtilsService {

    private static final Logger LOG = LoggerFactory.getLogger(AlertListUtilsService.class);

    private final AlertListService alertListService;
    private final ClusterConfigService clusterConfigService;

    public AlertListUtilsService(AlertListService alertListService,
                                 ClusterConfigService clusterConfigService) {
        this.alertListService = alertListService;
        this.clusterConfigService = clusterConfigService;
    }

    public void checkIsValidRequest(AlertListRequest request){
        if(!alertListService.isValidRequest(request)){
            LOG.error("Invalid alert list request");
            throw new BadRequestException("Invalid alert list request.");
        }
    }
}
