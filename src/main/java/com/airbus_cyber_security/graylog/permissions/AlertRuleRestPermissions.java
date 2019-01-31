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