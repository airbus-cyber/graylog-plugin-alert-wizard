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
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.ReturnDocument;
import org.graylog2.database.MongoCollection;
import org.graylog2.database.MongoCollections;
import org.graylog2.database.utils.MongoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;


public class AlertRuleService {

	private static final Logger LOG = LoggerFactory.getLogger(AlertRuleService.class);
	private static final String COLLECTION_NAME = "wizard_alerts";

	private final Validator validator;

	private static final String TITLE = "title";
	private final MongoCollection<AlertRule> collection;

	@Inject
	public AlertRuleService(MongoCollections mongoCollections, Validator validator) {
		this.validator = validator;
		this.collection = mongoCollections.collection(COLLECTION_NAME, AlertRule.class);
		this.collection.createIndex(new BasicDBObject(TITLE, 1), new IndexOptions().unique(true));
	}

	public AlertRule create(AlertRule alert) {
		Set<ConstraintViolation<AlertRule>> violations = validator.validate(alert);
		if (violations.isEmpty()) {
			return this.collection.getOrCreate(alert);
		} else {
			throw new IllegalArgumentException("Specified object failed validation: " + violations);
		}
	}
	
	public AlertRule update(AlertRule alert) {
		LOG.debug("Alert to be updated [{}]", alert);

		Set<ConstraintViolation<AlertRule>> violations = validator.validate(alert);
		if (!violations.isEmpty()) {
			throw new IllegalArgumentException("Specified object failed validation: " + violations);
		}

		return this.collection.findOneAndReplace(MongoUtils.idEq(alert.id()), alert, new FindOneAndReplaceOptions().returnDocument(ReturnDocument.AFTER));
	}

	public List<AlertRule> all() {
		return this.collection.find().into(new ArrayList<>());
	}

	public void destroy(String alertTitle) {
		this.collection.deleteOne(new BasicDBObject(TITLE, alertTitle));
	}
	
	public AlertRule load(String title) {
		return this.collection.find(new BasicDBObject(TITLE, title)).first();
	}

	public boolean isPresent(String title) {
		return this.collection.countDocuments(new BasicDBObject(TITLE, title)) > 0;
	}

	public Optional<AlertRule> get(String id) {
		return Optional.ofNullable(this.collection.find(MongoUtils.idEq(id)).first());
	}

	public void delete(String id) {
		this.collection.deleteOne(MongoUtils.idEq(id));
	}
}
