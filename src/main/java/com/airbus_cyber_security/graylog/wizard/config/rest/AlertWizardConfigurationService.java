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

import org.graylog2.plugin.cluster.ClusterConfigService;

import javax.inject.Inject;


public class AlertWizardConfigurationService {

    private final ClusterConfigService clusterConfigurationService;

    @Inject
    public AlertWizardConfigurationService(ClusterConfigService clusterConfigurationService) {
        this.clusterConfigurationService = clusterConfigurationService;
    }

    public AlertWizardConfig getConfiguration() {
        return this.clusterConfigurationService.getOrDefault(AlertWizardConfig.class, AlertWizardConfig.defaultConfig());
    }

    public void updateConfiguration(AlertWizardConfig configuration) {
        this.clusterConfigurationService.write(configuration);
    }
}
