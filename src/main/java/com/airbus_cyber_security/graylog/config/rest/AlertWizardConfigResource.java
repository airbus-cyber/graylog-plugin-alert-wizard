package com.airbus_cyber_security.graylog.config.rest;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.graylog2.audit.AuditEventTypes;
import org.graylog2.audit.jersey.AuditEvent;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.graylog2.plugin.rest.PluginRestResource;
import org.graylog2.shared.rest.resources.RestResource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import static java.util.Objects.requireNonNull;
import static org.graylog2.shared.security.RestPermissions.CLUSTER_CONFIG_ENTRY_READ;

@RequiresAuthentication
@Api(value = "Wizard/Config", description = "Manage alert wizard setings")
@Path("/config")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AlertWizardConfigResource extends RestResource implements PluginRestResource {
	
    private final ClusterConfigService clusterConfigService;

    @Inject
    public AlertWizardConfigResource(final ClusterConfigService clusterConfigService) {
        this.clusterConfigService = requireNonNull(clusterConfigService);
    }

    @GET
    @Timed
    @ApiOperation(value = "Get alert wizard configuration")
    @RequiresPermissions({CLUSTER_CONFIG_ENTRY_READ})
    public AlertWizardConfig config() {
        return clusterConfigService.getOrDefault(AlertWizardConfig.class,
                AlertWizardConfig.defaultConfig());
    }

    @PUT
    @Timed
    @ApiOperation(value = "Update alert wizard configuration")
    @RequiresPermissions({CLUSTER_CONFIG_ENTRY_READ})
    @AuditEvent(type = AuditEventTypes.AUTHENTICATION_PROVIDER_CONFIGURATION_UPDATE)
    public AlertWizardConfig updateConfig(@ApiParam(name = "config", required = true) final AlertWizardConfig config) {
        clusterConfigService.write(config);
        return config;
    }

}
