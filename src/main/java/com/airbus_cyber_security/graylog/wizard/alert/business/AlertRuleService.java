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
import java.util.Set;


public class AlertRuleService {

	private final JacksonDBCollection<AlertRule, String> alertRules;
	private final Validator validator;
	private static final Logger LOG = LoggerFactory.getLogger(AlertRuleService.class);
	private static final String TITLE = "title";

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
		try (DBCursor<AlertRule> cursor = this.alertRules.find()) {
			return cursor.toArray();
		}
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

}
