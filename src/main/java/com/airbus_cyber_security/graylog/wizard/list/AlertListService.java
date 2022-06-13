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

package com.airbus_cyber_security.graylog.wizard.list;

import com.airbus_cyber_security.graylog.wizard.list.bundles.ExportAlertList;
import com.airbus_cyber_security.graylog.wizard.list.rest.models.requests.AlertListRequest;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import org.graylog2.bindings.providers.MongoJackObjectMapperProvider;
import org.graylog2.database.CollectionName;
import org.graylog2.database.MongoConnection;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;
import au.com.bytecode.opencsv.CSVWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class AlertListService {

    private static final Path LISTS_PATH = Paths.get("/usr/share/graylog/data/alert-lists");
    private final JacksonDBCollection<AlertList, String> coll;
    private final Validator validator;
    private static final Logger LOG = LoggerFactory.getLogger(AlertListService.class);
    private static final String TITLE = "title";

    @Inject
    public AlertListService(MongoConnection mongoConnection, MongoJackObjectMapperProvider mapperProvider,
                            Validator validator) {
        this.validator = validator;
        String collectionName = AlertList.class.getAnnotation(CollectionName.class).value();
        // TODO: MongoCollection<Document> collection = mongoConnection.getMongoDatabase().getCollection(collectionName);
        //       with import com.mongodb.client.MongoCollection;
        //            import org.bson.Document;
        DBCollection dbCollection = mongoConnection.getDatabase().getCollection(collectionName);
        this.coll = JacksonDBCollection.wrap(dbCollection, AlertList.class, String.class, mapperProvider.get());
        this.coll.createIndex(new BasicDBObject(TITLE, 1), new BasicDBObject("unique", true));
    }

    public long count() {
        return coll.count();
    }

    // TODO should not need this code: the AlertList object should directly return an array of Strings
    private String[] getListValues(AlertList list) {
        // TODO getLists should never return null
        String[] results = list.getLists().split(";");
        for (int i = 0; i < results.length; i++) {
            results[i] = results[i].trim();
        }
        return results;
    }

    public AlertList create(AlertList list) throws IOException {
        Set<ConstraintViolation<AlertList>> violations = this.validator.validate(list);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException("Specified object failed validation: " + violations);
        }
        Files.createDirectories(LISTS_PATH);
        Writer writer = Files.newBufferedWriter(LISTS_PATH.resolve(list.getTitle() + ".csv"));
        try (CSVWriter csvWriter = new CSVWriter(writer)) {
            csvWriter.writeNext(new String[] {"key", "value"});

            for (String value: this.getListValues(list)) {
                csvWriter.writeNext(new String[]{value, value});
            }
        }
        return coll.insert(list).getSavedObject();
    }

    public AlertList update(String title, AlertList list) {
        LOG.debug("List to be updated [{}]", list);

        final Set<ConstraintViolation<AlertList>> violations = this.validator.validate(list);
        if (violations.isEmpty()) {

            return coll.findAndModify(DBQuery.is(TITLE, title), new BasicDBObject(), new BasicDBObject(),
                    false, list, true, false);

        } else {
            throw new IllegalArgumentException("Specified object failed validation: " + violations);
        }
    }

    public List<AlertList> all() {
        return toAbstractListType(coll.find());
    }

    public int destroy(String listTitle) {

        return coll.remove(DBQuery.is(TITLE, listTitle)).getN();
    }

    public AlertList load(String listTitle) {
        return coll.findOne(DBQuery.is(TITLE, listTitle));
    }

    public boolean isPresent(String title) {
        return (coll.getCount(DBQuery.is(TITLE, title)) > 0);
    }

    private List<AlertList> toAbstractListType(DBCursor<AlertList> lists) {
        return toAbstractListType(lists.toArray());
    }

    private List<AlertList> toAbstractListType(List<AlertList> lists) {
        final List<AlertList> result = Lists.newArrayListWithCapacity(lists.size());
        result.addAll(lists);

        return result;
    }

    private boolean isValidTitle(String title) {
        return !(title == null || title.isEmpty());
    }

    public boolean isValidRequest(AlertListRequest request){
        return (isValidTitle(request.getTitle()));
    }

    public boolean isValidImportRequest(ExportAlertList request){
        return (isValidTitle(request.getTitle()));
    }
}
