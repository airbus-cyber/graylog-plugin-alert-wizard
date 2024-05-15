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
import org.graylog2.database.MongoConnection;
import org.graylog2.database.NotFoundException;
import org.graylog2.database.PaginatedDbService;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.swing.text.html.Option;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.Optional;
import java.util.Set;


public class AlertRuleService extends PaginatedDbService<AlertRule> {

	private static final String COLLECTION_NAME = "wizard_alerts";
	private final Validator validator;
	private static final Logger LOG = LoggerFactory.getLogger(AlertRuleService.class);
	private static final String TITLE = "title";

	@Inject
	public AlertRuleService(MongoConnection mongoConnection, MongoJackObjectMapperProvider mapperProvider,
							Validator validator) {
		super(mongoConnection, mapperProvider, AlertRule.class, COLLECTION_NAME);
		this.validator = validator;
		this.db.createIndex(new BasicDBObject(TITLE, 1), new BasicDBObject("unique", true));
	}

	public AlertRule create(AlertRule alert) {
		Set<ConstraintViolation<AlertRule>> violations = validator.validate(alert);
		if (violations.isEmpty()) {
			return this.save(alert);
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
		// TODO would be easier to just do a this.save, but unfortunately we are using title as identifier. We should use an identifier instead!!!
		return this.db.findAndModify(DBQuery.is(TITLE, title), new BasicDBObject(), new BasicDBObject(),
				                    false, alert, true, false);
	}

	public List<AlertRule> all() {
		try (DBCursor<AlertRule> cursor = this.db.find(DBQuery.empty())) {
			return cursor.toArray();
		}
	}

	public void destroy(String alertTitle) {
		this.db.remove(DBQuery.is(TITLE, alertTitle)).getN();
		// TODO would be simpler: this.delete(alertTitle);
	}
	
	public AlertRule load(String title) throws NotFoundException {
		return this.db.findOne(DBQuery.is(TITLE, title));
	}
	
	public boolean isPresent(String title) {
		return (this.db.getCount(DBQuery.is(TITLE, title)) > 0);
	}
}
