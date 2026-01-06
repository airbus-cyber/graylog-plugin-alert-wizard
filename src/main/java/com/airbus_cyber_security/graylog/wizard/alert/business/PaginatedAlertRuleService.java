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
import com.airbus_cyber_security.graylog.wizard.alert.rest.models.responses.GetDataAlertRule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.Variable;
import jakarta.inject.Inject;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.graylog.events.processor.DBEventDefinitionService;
import org.graylog2.database.MongoCollection;
import org.graylog2.database.MongoCollections;
import org.graylog2.database.PaginatedList;
import org.graylog2.database.utils.MongoUtils;
import org.graylog2.rest.models.SortOrder;
import org.graylog2.search.SearchQuery;
import org.graylog2.search.SearchQueryParser;

import java.util.List;
import java.util.function.Predicate;

public class PaginatedAlertRuleService {

    private static final String COLLECTION_NAME = "wizard_alerts";
    private static final List<String> STRING_FIELDS = List.of("title", "description", "creator_user_id");
    private final MongoCollection<AlertRule> collection;

    @Inject
    public PaginatedAlertRuleService(MongoCollections mongoCollections) {
        this.collection = mongoCollections.collection(COLLECTION_NAME, AlertRule.class);
    }

    public long count() {
        return collection.countDocuments();
    }

    public PaginatedList<AlertRule> findPaginated(SearchQuery query,
                                                  Predicate<AlertRule> predicate,
                                                  int page,
                                                  int perPage,
                                                  String sortField,
                                                  SortOrder order) {
        final Bson dbQuery = query.toBson();

        var pipelineBuilder = ImmutableList.<Bson>builder();

        if (!(query.getQueryMap().containsKey(GetDataAlertRule.FIELD_DESCRIPTION)
                || query.getQueryMap().containsKey(GetDataAlertRule.FIELD_PRIORITY))) {
            pipelineBuilder.add(Aggregates.match(dbQuery));
        } else {
            final ImmutableMultimap.Builder<String, SearchQueryParser.FieldValue> builder = ImmutableMultimap.builder();
            final ImmutableSet.Builder<String> disallowedKeys = ImmutableSet.builder();
            final Bson emptyDbQuery = new SearchQuery("", builder.build(), disallowedKeys.build()).toBson();
            pipelineBuilder.add(Aggregates.match(emptyDbQuery));
        }

        if (sortField.equals(GetDataAlertRule.FIELD_PRIORITY)
                || sortField.equals(GetDataAlertRule.FIELD_DESCRIPTION)
                || query.getQueryMap().containsKey(GetDataAlertRule.FIELD_DESCRIPTION)
                || query.getQueryMap().containsKey(GetDataAlertRule.FIELD_PRIORITY)) {
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
                    .add(Aggregates.set(
                            new Field<>("priority", doc("$first", "$event_definition.priority")),
                            new Field<>("description", doc("$first", "$event_definition.description"))))
                    .add(Aggregates.unset("event_definition"));
        }

        if (query.getQueryMap().containsKey(GetDataAlertRule.FIELD_DESCRIPTION)
                || query.getQueryMap().containsKey(GetDataAlertRule.FIELD_PRIORITY)) {
            pipelineBuilder.add(Aggregates.match(dbQuery));
        }

        if (isStringField(sortField)) {
            pipelineBuilder.add(Aggregates.set(new Field<>("lower" + sortField, doc("$toLower", "$" + sortField))))
                    .add(Aggregates.sort(order.toBsonSort("lower" + sortField)))
                    .add(Aggregates.unset("lower" + sortField));
        } else {
            pipelineBuilder.add(Aggregates.sort(order.toBsonSort(sortField)));
        }

        pipelineBuilder.add(Aggregates.unset("priority", "description"));

        final List<AlertRule> alertRuleList;
        try (final var results = MongoUtils.stream(collection.aggregate(pipelineBuilder.build()))) {
            alertRuleList = results.filter(predicate).toList();
        }

        final long grandTotal;
        try (final var stream = MongoUtils.stream(collection.find())) {
            grandTotal = stream.filter(predicate).count();
        }

        final List<AlertRule> paginatedAlerts = perPage > 0
                ? alertRuleList.stream()
                .skip((long) perPage * Math.max(0, page - 1))
                .limit(perPage)
                .toList()
                : alertRuleList;

        return new PaginatedList<>(paginatedAlerts, alertRuleList.size(), page, perPage, grandTotal);
    }

    private boolean isStringField(String sortField) {
        return STRING_FIELDS.contains(sortField);
    }

    private Document doc(String key, Object value) {
        return new Document(key, value);
    }
}
