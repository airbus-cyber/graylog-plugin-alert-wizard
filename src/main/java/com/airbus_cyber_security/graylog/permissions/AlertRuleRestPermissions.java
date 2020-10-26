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

package com.airbus_cyber_security.graylog.permissions;

import com.google.common.collect.ImmutableSet;
import org.graylog2.plugin.security.Permission;
import org.graylog2.plugin.security.PluginPermissions;

import java.util.Collections;
import java.util.Set;

import static org.graylog2.plugin.security.Permission.create;

public class AlertRuleRestPermissions implements PluginPermissions {
    public static final String WIZARD_ALERTS_RULES_READ = "wizard_alerts_rules:read";
    public static final String WIZARD_ALERTS_RULES_CREATE = "wizard_alerts_rules:create";
    public static final String WIZARD_ALERTS_RULES_UPDATE = "wizard_alerts_rules:update";
    public static final String WIZARD_ALERTS_RULES_DELETE = "wizard_alerts_rules:delete";

    private final ImmutableSet<Permission> permissions = ImmutableSet.of(
            create(WIZARD_ALERTS_RULES_READ, "Read wizard alerts rules"),
            create(WIZARD_ALERTS_RULES_CREATE, "Create wizard alerts rules"),
            create(WIZARD_ALERTS_RULES_UPDATE, "Update wizard alerts rules"),
            create(WIZARD_ALERTS_RULES_DELETE, "Delete wizard alerts rules")
    );

    @Override
    public Set<Permission> permissions() {
        return permissions;
    }

    @Override
    public Set<Permission> readerBasePermissions() {
        return Collections.emptySet();
    }
}