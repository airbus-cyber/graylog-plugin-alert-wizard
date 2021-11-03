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

package com.airbus_cyber_security.graylog.wizard;

import com.airbus_cyber_security.graylog.wizard.alert.AlertRuleService;
import com.airbus_cyber_security.graylog.wizard.alert.AlertRuleServiceImpl;
import com.airbus_cyber_security.graylog.wizard.alert.rest.AlertRuleResource;
import com.airbus_cyber_security.graylog.wizard.audit.AlertWizardAuditEventTypes;
import com.airbus_cyber_security.graylog.wizard.config.rest.AlertWizardConfigResource;
import com.airbus_cyber_security.graylog.wizard.list.AlertListService;
import com.airbus_cyber_security.graylog.wizard.list.AlertListServiceImpl;
import com.airbus_cyber_security.graylog.wizard.list.rest.AlertListResource;
import com.airbus_cyber_security.graylog.wizard.permissions.AlertRuleRestPermissions;
import org.graylog2.plugin.PluginConfigBean;
import org.graylog2.plugin.PluginModule;

import java.util.Collections;
import java.util.Set;

/**
 * Extend the PluginModule abstract class here to add you plugin to the system.
 */
public class AlertWizardModule extends PluginModule {
    /**
     * Returns all configuration beans required by this plugin.
     *
     * Implementing this method is optional. The default method returns an empty {@link Set}.
     */
    @Override
    public Set<? extends PluginConfigBean> getConfigBeans() {
        return Collections.emptySet();
    }

    @Override
    protected void configure() {
    	bind(AlertRuleService.class).to(AlertRuleServiceImpl.class);
        bind(AlertListService.class).to(AlertListServiceImpl.class);

        addPermissions(AlertRuleRestPermissions.class);
        addRestResource(AlertRuleResource.class);
        addAuditEventTypes(AlertWizardAuditEventTypes.class);
        addRestResource(AlertWizardConfigResource.class);
        addRestResource(AlertListResource.class);
    }
}
