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

package com.airbus_cyber_security.graylog.wizard.list.persistence;

import com.airbus_cyber_security.graylog.wizard.database.LookupService;
import com.airbus_cyber_security.graylog.wizard.list.AlertList;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import org.graylog2.bindings.providers.MongoJackObjectMapperProvider;
import org.graylog2.database.CollectionName;
import org.graylog2.database.MongoConnection;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;

import javax.inject.Inject;
import javax.validation.Validator;
import java.util.List;

public class AlertListCollection {

    private final JacksonDBCollection<AlertList, String> collection;
    private static final String TITLE = "title";

    @Inject
    public AlertListCollection(MongoConnection mongoConnection, MongoJackObjectMapperProvider mapperProvider) {
        String collectionName = AlertList.class.getAnnotation(CollectionName.class).value();
        // TODO: MongoCollection<Document> collection = mongoConnection.getMongoDatabase().getCollection(collectionName);
        //       with import com.mongodb.client.MongoCollection;
        //            import org.bson.Document;
        DBCollection dbCollection = mongoConnection.getDatabase().getCollection(collectionName);
        this.collection = JacksonDBCollection.wrap(dbCollection, AlertList.class, String.class, mapperProvider.get());
        this.collection.createIndex(new BasicDBObject(TITLE, 1), new BasicDBObject("unique", true));
    }

    public long count() {
        return this.collection.count();
    }

    public AlertList insert(AlertList list) {
        return this.collection.insert(list).getSavedObject();
    }

    public AlertList findAndModify(String title, AlertList list) {
        return this.collection.findAndModify(DBQuery.is(TITLE, title), new BasicDBObject(), new BasicDBObject(),
                false, list, true, false);
    }

    public List<AlertList> all() {
        return toAbstractListType(this.collection.find());
    }

    public int remove(String title) {
        return this.collection.remove(DBQuery.is(TITLE, title)).getN();
    }

    public AlertList load(String listTitle) {
        return this.collection.findOne(DBQuery.is(TITLE, listTitle));
    }

    public boolean isPresent(String title) {
        return (this.collection.getCount(DBQuery.is(TITLE, title)) > 0);
    }

    private List<AlertList> toAbstractListType(List<AlertList> lists) {
        final List<AlertList> result = Lists.newArrayListWithCapacity(lists.size());
        result.addAll(lists);

        return result;
    }

    private List<AlertList> toAbstractListType(DBCursor<AlertList> lists) {
        return toAbstractListType(lists.toArray());
    }
}
