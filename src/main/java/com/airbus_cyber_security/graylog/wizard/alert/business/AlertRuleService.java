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
import com.google.common.collect.ImmutableList;
import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.Variable;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.graylog.events.processor.DBEventDefinitionService;
import org.graylog2.bindings.providers.MongoJackObjectMapperProvider;
import org.graylog2.database.MongoConnection;
import org.graylog2.database.NotFoundException;
import org.graylog2.database.PaginatedDbService;
import org.graylog2.database.PaginatedList;
import org.graylog2.search.SearchQuery;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;


// TODO split this into AlertRuleCollection and move it down in the persistence namespace
public class AlertRuleService extends PaginatedDbService<AlertRule> {

	private static final String COLLECTION_NAME = "wizard_alerts";
	private final Validator validator;
	private static final Logger LOG = LoggerFactory.getLogger(AlertRuleService.class);
	private static final String TITLE = "title";
	private static final List<String> STRING_FIELDS = List.of("title", "description", "creator_user_id");
	private final MongoCollection<Document> collection;

	@Inject
	public AlertRuleService(MongoConnection mongoConnection, MongoJackObjectMapperProvider mapperProvider,
							Validator validator) {
		super(mongoConnection, mapperProvider, AlertRule.class, COLLECTION_NAME);
		this.validator = validator;
		this.db.createIndex(new BasicDBObject(TITLE, 1), new BasicDBObject("unique", true));
		this.collection = mongoConnection.getMongoDatabase().getCollection(COLLECTION_NAME);
	}

	public AlertRule create(AlertRule alert) {
		Set<ConstraintViolation<AlertRule>> violations = validator.validate(alert);
		if (violations.isEmpty()) {
			return this.save(alert);
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

		return this.save(alert);
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

	public PaginatedList<AlertRule> searchPaginated(SearchQuery query, Predicate<AlertRule> predicate,
													String order, String sortField, int page, int perPage) {
		final Bson dbQuery = query.toBson();

		var pipelineBuilder = ImmutableList.<Bson>builder()
				.add(Aggregates.match(dbQuery));

		if (sortField.equals("priority") || sortField.equals("description")) {
			String eventField = "$event_definition." + sortField;
			pipelineBuilder.add(Aggregates.lookup(
							DBEventDefinitionService.COLLECTION_NAME,
							List.of(new Variable<>("event_identifier", doc("$toObjectId", "$alert_pattern.event_identifier")),
									new Variable<>("event_identifier1", doc("$toObjectId", "$alert_pattern.event_identifier1"))),
							List.of(Aggregates.match(doc("$or",
									List.of(
											doc("$expr", doc("$eq", List.of("$_id", "$$event_identifier"))),
											doc("$expr", doc("$eq", List.of("$_id", "$$event_identifier1")))
									)))),
							"event_definition"
					))
					.add(Aggregates.set(new Field<>(sortField, doc("$first", eventField))))
					.add(Aggregates.unset("event_definition"));
		}

		if (isStringField(sortField)) {
			pipelineBuilder.add(Aggregates.set(new Field<>("lower" + sortField, doc("$toLower", "$" + sortField))))
					.add(Aggregates.sort(getSortBuilder(order, "lower" + sortField)))
					.add(Aggregates.unset("lower" + sortField));
		} else {
			pipelineBuilder.add(Aggregates.sort(getSortBuilder(order, sortField)));
		}

		final AggregateIterable<Document> result = collection.aggregate(pipelineBuilder.build());

		final List<AlertRule> alertRuleList = StreamSupport.stream(result.spliterator(), false)
				.map(AlertRule::fromDocument)
				.filter(predicate)
				.toList();

		final long grandTotal = db.find(DBQuery.empty()).toArray()
				.stream()
				.filter(predicate)
				.count();

		final List<AlertRule> paginatedAlerts = perPage > 0
				? alertRuleList.stream()
				.skip((long) perPage * Math.max(0, page - 1))
				.limit(perPage)
				.toList()
				: alertRuleList;

		return new PaginatedList<>(paginatedAlerts, alertRuleList.size(), page, perPage, grandTotal);
	}

	private Document doc(String key, Object value) {
		return new Document(key, value);
	}

	private boolean isStringField(String sortField) {
		return STRING_FIELDS.contains(sortField);
	}
}
