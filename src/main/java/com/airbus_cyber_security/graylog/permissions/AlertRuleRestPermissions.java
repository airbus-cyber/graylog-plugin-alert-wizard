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