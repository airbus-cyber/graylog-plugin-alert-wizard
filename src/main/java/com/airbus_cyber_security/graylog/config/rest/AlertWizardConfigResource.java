package com.airbus_cyber_security.graylog.config.rest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.graylog2.audit.AuditEventTypes;
import org.graylog2.audit.jersey.AuditEvent;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.graylog2.shared.rest.resources.RestResource;
import org.graylog2.shared.security.RestPermissions;

import com.codahale.metrics.annotation.Timed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RequiresAuthentication
@Api(value = "System/AlertWizard", description = "Manage alert wizard setings")
@Path("/system/alertwizard")
@Produces(MediaType.APPLICATION_JSON)
public class AlertWizardConfigResource extends RestResource{
	
    private final ClusterConfigService clusterConfigService;

    @Inject
    public AlertWizardConfigResource(final ClusterConfigService clusterConfigService) {
        this.clusterConfigService = clusterConfigService;
    }


    @GET
    @Timed
    @ApiOperation(value = "Get alert wizard configuration")
    @Path("config")
    public AlertWizardConfig config() {
        checkPermission(RestPermissions.CLUSTER_CONFIG_ENTRY_READ);
        return clusterConfigService.getOrDefault(AlertWizardConfig.class,
                AlertWizardConfig.defaultConfig());
    }

    @PUT
    @Timed
    @ApiOperation(value = "Update alert wizard configuration")
    @Path("config")
    @AuditEvent(type = AuditEventTypes.MESSAGE_PROCESSOR_CONFIGURATION_UPDATE)
    public AlertWizardConfig updateConfig(@ApiParam(name = "config", required = true) final AlertWizardConfig config) {
        checkPermission(RestPermissions.CLUSTER_CONFIG_ENTRY_EDIT);
        clusterConfigService.write(config);
        return config;
    }

}
