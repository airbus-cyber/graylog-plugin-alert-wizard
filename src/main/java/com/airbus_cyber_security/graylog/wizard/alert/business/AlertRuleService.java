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

import com.airbus_cyber_security.graylog.wizard.alert.model.AlertRule;
import com.airbus_cyber_security.graylog.wizard.alert.model.AlertRuleStream;
import com.airbus_cyber_security.graylog.wizard.alert.model.FieldRule;
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.requests.AlertRuleRequest;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import org.graylog2.bindings.providers.MongoJackObjectMapperProvider;
import org.graylog2.database.CollectionName;
import org.graylog2.database.MongoConnection;
import org.graylog2.database.NotFoundException;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.Map;
import java.util.Set;


// TODO extract class that accesses to database and move into com.airbus_cyber_security.graylog.wizard.alert.database
public class AlertRuleService {

	private final JacksonDBCollection<AlertRule, String> alertRules;
	private final Validator validator;
	private static final Logger LOG = LoggerFactory.getLogger(AlertRuleService.class);
	private static final String TITLE = "title";

	// TODO should try to make these constants private and group the code (more compact)
	public static final String THRESHOLD_TYPE_MORE = ">";

	public static final String THRESHOLD_TYPE_LESS = "<";

	@Inject
	public AlertRuleService(MongoConnection mongoConnection, MongoJackObjectMapperProvider mapperProvider,
							Validator validator) {
		this.validator = validator;
		String collectionName = AlertRule.class.getAnnotation(CollectionName.class).value();
		DBCollection dbCollection = mongoConnection.getDatabase().getCollection(collectionName);
		this.alertRules = JacksonDBCollection.wrap(dbCollection, AlertRule.class, String.class, mapperProvider.get());
		this.alertRules.createIndex(new BasicDBObject(TITLE, 1), new BasicDBObject("unique", true));
	}

	public AlertRule create(AlertRule alert) {
		Set<ConstraintViolation<AlertRule>> violations = validator.validate(alert);
		if (violations.isEmpty()) {
			return this.alertRules.insert(alert).getSavedObject();
		} else {
			throw new IllegalArgumentException("Specified object failed validation: " + violations);
		}
	}
	
	public AlertRule update(String title, AlertRule alert) {
		LOG.debug("Alert to be updated [{}]", alert);

		Set<ConstraintViolation<AlertRule>> violations = validator.validate(alert);
		if (!violations.isEmpty()) {
			throw new IllegalArgumentException("Specified object failed validation: " + violations);
		}
		return this.alertRules.findAndModify(DBQuery.is(TITLE, title), new BasicDBObject(), new BasicDBObject(),
				false, alert, true, false);
	}

	public List<AlertRule> all() {
		return toAbstractListType(this.alertRules.find());
	}

	public void destroy(String alertTitle) {
		this.alertRules.remove(DBQuery.is(TITLE, alertTitle)).getN();
	}
	
	public AlertRule load(String alertTitle) throws NotFoundException {
		return this.alertRules.findOne(DBQuery.is(TITLE, alertTitle));
	}
	
	public boolean isPresent(String title) {
		return (this.alertRules.getCount(DBQuery.is(TITLE, title)) > 0);
	}

	private List<AlertRule> toAbstractListType(DBCursor<AlertRule> alerts) {
		return toAbstractListType(alerts.toArray());
	}

	private List<AlertRule> toAbstractListType(List<AlertRule> alerts) {
		List<AlertRule> result = Lists.newArrayListWithCapacity(alerts.size());
		result.addAll(alerts);

		return result;
	}

	private boolean isValidTitle(String title) {
		return !(title == null || title.isEmpty());
	}
	
	private boolean isValidStream(AlertRuleStream stream) {
		if (stream.getMatchingType().equals("AND") || stream.getMatchingType().equals("OR")) {
			for (FieldRule fieldRule: stream.getFieldRules()) {
				if (fieldRule.getField() == null || fieldRule.getField().isEmpty() ||
						fieldRule.getType() < -7 || fieldRule.getType() > 7) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
    private boolean isValidCondStatistical(Map<String, Object> conditionParameters) {
        if (!conditionParameters.containsKey(AlertRuleUtils.TYPE)) {
            LOG.debug("Missing condition parameter {}", AlertRuleUtils.TYPE);
            return false;
        }
        if (!conditionParameters.containsKey(AlertRuleUtils.FIELD)) {
            LOG.debug("Missing condition parameter {}", AlertRuleUtils.FIELD);
            return false;
        }
        String thresholdType = conditionParameters.get(AlertRuleUtils.THRESHOLD_TYPE).toString();
        if (!isValidStatThresholdType(thresholdType)) {
            LOG.debug("Invalid condition parameter {}, {}", AlertRuleUtils.THRESHOLD_TYPE, thresholdType);
            return false;
        }
        return true;
    }
	
	private boolean isValidCondCorrelation(Map<String, Object> conditionParameters, AlertRuleStream secondStream) {
		return (conditionParameters.containsKey(AlertRuleUtils.ADDITIONAL_THRESHOLD) &&
				conditionParameters.containsKey(AlertRuleUtils.ADDITIONAL_THRESHOLD_TYPE) &&
				isValidThresholdType(conditionParameters.get(AlertRuleUtils.THRESHOLD_TYPE).toString()) &&
				isValidThresholdType(conditionParameters.get(AlertRuleUtils.ADDITIONAL_THRESHOLD_TYPE).toString()) &&
				isValidStream(secondStream));
	}
	
	private boolean isValidCondOr(Map<String, Object> conditionParameters, AlertRuleStream secondStream) {
		return (isValidThresholdType(conditionParameters.get(AlertRuleUtils.THRESHOLD_TYPE).toString()) &&
				isValidStream(secondStream));
	}
	
	private boolean isValidThresholdType(String thresholdType) {
		return (thresholdType.equals(THRESHOLD_TYPE_MORE) || thresholdType.equals(THRESHOLD_TYPE_LESS));
	}
	
	private boolean isValidStatThresholdType(String thresholdType) {
		return (thresholdType.equals("<") || thresholdType.equals("<=") ||
				thresholdType.equals(">") || thresholdType.equals(">=") || thresholdType.equals("=="));
	}
	
    private boolean isValidCondition(String conditionType, Map<String, Object> conditionParameters, AlertRuleStream secondStream) {
        if (!conditionParameters.containsKey(AlertRuleUtils.TIME)) {
            LOG.debug("Missing condition parameter: {}", AlertRuleUtils.TIME);
            return false;
        }
        if (!conditionParameters.containsKey(AlertRuleUtils.THRESHOLD)) {
            LOG.debug("Missing condition parameter: {}", AlertRuleUtils.THRESHOLD);
            return false;
        }
        if (!conditionParameters.containsKey(AlertRuleUtils.THRESHOLD_TYPE)) {
            LOG.debug("Missing condition parameter: {}", AlertRuleUtils.THRESHOLD_TYPE);
            return false;
        }
        if (conditionType.equals("STATISTICAL")) {
            return isValidCondStatistical(conditionParameters);
        } else if (conditionType.equals("THEN") || conditionType.equals("AND")) {
            return isValidCondCorrelation(conditionParameters, secondStream);
        } else if (conditionType.equals("OR")) {
            return isValidCondOr(conditionParameters, secondStream);
        }
        return true;
    }

	public boolean isValidRequest(AlertRuleRequest request){
		return (isValidTitle(request.getTitle()) && 
				isValidStream(request.getStream()) &&
				isValidCondition(request.getConditionType(), request.conditionParameters(), request.getSecondStream()));
    }
}
