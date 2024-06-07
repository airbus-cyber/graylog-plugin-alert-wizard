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

package com.airbus_cyber_security.graylog.wizard.alert.business;

import com.airbus_cyber_security.graylog.wizard.alert.model.FieldRule;

import java.util.List;

public class FieldRulesUtilities {

    public boolean isListFieldRule(FieldRule fieldRule) {
        return (fieldRule.getType() == -7 || fieldRule.getType() == 7);
    }

    public boolean hasTypeNotInList(FieldRule fieldRule) {
        return (fieldRule.getType() == -7);
    }

    public boolean isValidFieldRule(FieldRule fieldRule) {
        if (fieldRule.getField() == null) {
            return false;
        }
        if (fieldRule.getField().isEmpty()) {
            return false;
        }
        if (fieldRule.getType() < -7) {
            return false;
        }
        if (fieldRule.getType() > 7) {
            return false;
        }
        return fieldRule.getType() != 0;
    }

    public boolean hasStreamRules(List<FieldRule> fieldRules) {
        for (FieldRule fieldRule: fieldRules) {
            if (!this.isListFieldRule(fieldRule)) {
                return true;
            }
        }
        return false;
    }
}
