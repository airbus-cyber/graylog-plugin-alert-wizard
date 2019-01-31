package com.airbus_cyber_security.graylog.audit;

import java.util.Set;

import org.graylog2.audit.PluginAuditEventTypes;

import com.google.common.collect.ImmutableSet;

public class AlertWizardAuditEventTypes implements PluginAuditEventTypes {
    public static final String NAMESPACE = "wizard";
    private static final String PREFIX = NAMESPACE + ":";

    public static final String WIZARD_ALERTS_RULES_READ = PREFIX + "wizard_alerts_rules:read";
    public static final String WIZARD_ALERTS_RULES_CREATE = PREFIX + "wizard_alerts_rules:create";
    public static final String WIZARD_ALERTS_RULES_UPDATE = PREFIX + "wizard_alerts_rules:update";
    public static final String WIZARD_ALERTS_RULES_DELETE = PREFIX + "wizard_alerts_rules:delete";

    private static final ImmutableSet<String> EVENT_TYPES = ImmutableSet.<String>builder()
    		.add(WIZARD_ALERTS_RULES_READ)
            .add(WIZARD_ALERTS_RULES_CREATE)
            .add(WIZARD_ALERTS_RULES_UPDATE)
            .add(WIZARD_ALERTS_RULES_DELETE)
            .build();

    @Override
    public Set<String> auditEventTypes() {
        return EVENT_TYPES;
    }

}
