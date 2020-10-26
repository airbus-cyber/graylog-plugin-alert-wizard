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

package com.airbus_cyber_security.graylog.audit;

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
