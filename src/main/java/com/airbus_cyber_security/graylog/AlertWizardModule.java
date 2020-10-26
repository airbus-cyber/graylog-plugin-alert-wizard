/*
 * graylog-plugin-alert-wizard Source Code
 * Copyright (C) 2018-2020 - Airbus CyberSecurity (SAS) - All rights reserved
 *
 * This file is part of the graylog-plugin-alert-wizard GPL Source Code.
 *
 * graylog-plugin-alert-wizard Source Code is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this code.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.airbus_cyber_security.graylog;

import com.airbus_cyber_security.graylog.alert.AlertRuleService;
import com.airbus_cyber_security.graylog.alert.AlertRuleServiceImpl;
import com.airbus_cyber_security.graylog.alert.rest.AlertRuleResource;
import com.airbus_cyber_security.graylog.audit.AlertWizardAuditEventTypes;
import com.airbus_cyber_security.graylog.config.rest.AlertWizardConfigResource;
import com.airbus_cyber_security.graylog.list.AlertListService;
import com.airbus_cyber_security.graylog.list.AlertListServiceImpl;
import com.airbus_cyber_security.graylog.list.rest.AlertListResource;
import com.airbus_cyber_security.graylog.permissions.AlertRuleRestPermissions;
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
