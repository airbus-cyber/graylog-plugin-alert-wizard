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
package com.airbus_cyber_security.graylog.wizard.alert.rest;

import java.util.Map;

import org.graylog2.plugin.streams.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.airbus_cyber_security.graylog.wizard.alert.business.FieldRulesUtilities;
import com.airbus_cyber_security.graylog.wizard.alert.model.AlertType;
import com.airbus_cyber_security.graylog.wizard.alert.model.FieldRule;
import com.airbus_cyber_security.graylog.wizard.alert.model.ImportAlertRule;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.AlertConditionParameters;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.AlertRuleStream;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.requests.AlertRuleRequest;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.requests.ImportAlertRuleRequest;

import jakarta.ws.rs.BadRequestException;

/**
 * Validate input requests content.
 */
public class Validator {

    private static final Logger LOG = LoggerFactory.getLogger(Validator.class);

    private final FieldRulesUtilities fieldRulesUtilities;
    private final boolean raiseException;

    public Validator(FieldRulesUtilities fieldRulesUtilities) {
        this(fieldRulesUtilities, false);
    }

    public Validator(FieldRulesUtilities fieldRulesUtilities, boolean raiseException) {
        this.fieldRulesUtilities = fieldRulesUtilities;
        this.raiseException = raiseException;
    }

    private boolean isValidTitle(String title) {
        if (title == null || title.isEmpty()) {
            return this.returnError("Invalid title: " + title);
        }
        return true;
    }

    private boolean isValidStream(AlertRuleStream stream) {
        // TODO Useless test? Replace with null != stream.getMatchingType()? Is it usefull (because of @NotNull on getMatchingType)?
        if (!stream.getMatchingType().equals(Stream.MatchingType.AND) && !stream.getMatchingType().equals(Stream.MatchingType.OR)) {
            return this.returnError("Invalid stream, matching_type=" + stream.getMatchingType());
        }
        for (FieldRule fieldRule : stream.getFieldRules()) {
            if (!fieldRulesUtilities.isValidFieldRule(fieldRule)) {
                return this.returnError("Invalid stream field_rule, field=" + fieldRule.getField() + ", type=" + fieldRule.getType());
            }
        }
        return true;
    }

    private boolean isValidStatThresholdType(String thresholdType) {
        return switch (thresholdType) {
            case "<", "<=", ">", ">=", "==" ->
                true;
            default ->
                this.returnError("Invalid threshold type=" + thresholdType);
        };
    }

    private boolean isValidCondStatistical(Map<String, Object> conditionParameters) {
        if (!this.containsParameter(conditionParameters, AlertConditionParameters.TYPE)
                || !this.containsParameter(conditionParameters, AlertConditionParameters.FIELD)) {
            return false;
        }
        String thresholdType = conditionParameters.get(AlertConditionParameters.THRESHOLD_TYPE).toString();
        if (!isValidStatThresholdType(thresholdType)) {
            return this.returnError("Invalid statistical condition parameter, " + AlertConditionParameters.THRESHOLD_TYPE + "=" + thresholdType);
        }
        return isValidStatThresholdType(thresholdType);
    }

    private boolean isValidThresholdType(Map<String, Object> conditionParameters, String field) {
        Object thresholdType = conditionParameters.get(field);
        if (AlertConditionParameters.THRESHOLD_TYPE_MORE.equals(thresholdType) || AlertConditionParameters.THRESHOLD_TYPE_LESS.equals(thresholdType)) {
            return true;
        }
        return this.returnError("Invalid condition parameter, " + field + "=" + thresholdType);
    }

    private boolean isValidCondCorrelation(Map<String, Object> conditionParameters, AlertRuleStream secondStream) {
        return (isValidThresholdType(conditionParameters, AlertConditionParameters.THRESHOLD_TYPE)
                && isValidThresholdType(conditionParameters, AlertConditionParameters.ADDITIONAL_THRESHOLD_TYPE)
                && isValidStream(secondStream));
    }

    private boolean isValidCondOr(Map<String, Object> conditionParameters, AlertRuleStream secondStream) {
        return (isValidThresholdType(conditionParameters, AlertConditionParameters.THRESHOLD_TYPE)
                && isValidThresholdType(conditionParameters, AlertConditionParameters.ADDITIONAL_THRESHOLD_TYPE)
                && isValidStream(secondStream));
    }

    private boolean isValidCondition(AlertType alertType, Map<String, Object> conditionParameters, AlertRuleStream secondStream) {
        if (!this.containsParameter(conditionParameters, AlertConditionParameters.TIME)) {
            return false;
        }
        if (!this.containsParameter(conditionParameters, AlertConditionParameters.THRESHOLD)) {
            return false;
        }
        if (!this.containsParameter(conditionParameters, AlertConditionParameters.THRESHOLD_TYPE)) {
            return false;
        }
        return switch (alertType) {
            case STATISTICAL ->
                isValidCondStatistical(conditionParameters);
            case THEN, AND ->
                isValidCondCorrelation(conditionParameters, secondStream);
            case OR ->
                isValidCondOr(conditionParameters, secondStream);
            default ->
                true;
        };
    }

    private boolean containsParameter(Map<String, Object> conditionParameters, String field) {
        if (!conditionParameters.containsKey(field)) {
            return this.returnError("Missing condition parameter " + field);
        }
        return true;
    }

    public boolean isValidRequest(AlertRuleRequest request) {
        return (isValidTitle(request.getTitle())
                && isValidStream(request.getStream())
                && isValidCondition(request.getConditionType(), request.conditionParameters(), request.getSecondStream()));
    }

    public boolean isValidAlertImport(ImportAlertRule request) {
        return (isValidTitle(request.getTitle())
                && isValidStream(request.getStream())
                && isValidCondition(request.getConditionType(), request.getConditionParameters(), request.getSecondStream()));
    }

    public void checkIsValidRequest(AlertRuleRequest request) {
        try {
            if (!this.isValidRequest(request)) {
                LOG.error("Invalid alert rule request");
                throw new BadRequestException("Invalid alert rule request.");
            }
        } catch (BadRequestException error) {
            LOG.error("Invalid alert rule request");
            throw new BadRequestException("Invalid alert rule request.");
        }
    }

    public void checkIsValidImportRequest(ImportAlertRuleRequest request) {
        for (ImportAlertRule alert : request.getRules()) {
            try {
                if (!this.isValidAlertImport(alert)) {
                    LOG.error("Invalid alert rule in imported content");
                    throw new BadRequestException("Invalid alert rule in imported content.");
                }
            } catch (BadRequestException error) {
                String message = "Invalid alert rule in imported content, alert=" + alert.getTitle() + ", cause=" + error.getMessage();
                LOG.error(message);
                throw new BadRequestException(message);
            }
        }
    }

    /**
     * Depends on the class initialization to raise an exception or just return
     * false. In any case, log the error message.
     */
    private boolean returnError(String message) {
        if (this.raiseException) {
            this.raiseError(message);
        } else {
            LOG.error(message);
        }
        return false;
    }

    /**
     * Log the error message and raise a BadRequestException.
     */
    private void raiseError(String message) {
        LOG.error(message);
        throw new BadRequestException(message);
    }
}
