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

package com.airbus_cyber_security.graylog.wizard.audit;

import com.google.common.collect.ImmutableSet;
import org.graylog2.audit.PluginAuditEventTypes;

import java.util.Set;

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
