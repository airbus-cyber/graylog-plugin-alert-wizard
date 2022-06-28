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

package com.airbus_cyber_security.graylog.wizard.config.rest;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.graylog2.audit.AuditEventTypes;
import org.graylog2.audit.jersey.AuditEvent;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.graylog2.plugin.rest.PluginRestResource;
import org.graylog2.shared.rest.resources.RestResource;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

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
    public AlertWizardConfigResource(ClusterConfigService clusterConfigService) {
        this.clusterConfigService = requireNonNull(clusterConfigService);
    }

    @GET
    @Timed
    @ApiOperation(value = "Get alert wizard configuration")
    @RequiresPermissions({CLUSTER_CONFIG_ENTRY_READ})
    public AlertWizardConfig config() {
        return this.clusterConfigService.getOrDefault(AlertWizardConfig.class, AlertWizardConfig.defaultConfig());
    }

    @PUT
    @Timed
    @ApiOperation(value = "Update alert wizard configuration")
    @RequiresPermissions({CLUSTER_CONFIG_ENTRY_READ})
    @AuditEvent(type = AuditEventTypes.AUTHENTICATION_PROVIDER_CONFIGURATION_UPDATE)
    public AlertWizardConfig updateConfig(@ApiParam(name = "config", required = true) final AlertWizardConfig config) {
        this.clusterConfigService.write(config);
        return config;
    }

}
