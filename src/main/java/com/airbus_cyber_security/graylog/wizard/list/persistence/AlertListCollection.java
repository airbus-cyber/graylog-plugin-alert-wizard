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

import com.airbus_cyber_security.graylog.wizard.list.model.AlertList;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.ReturnDocument;
import org.graylog2.database.MongoCollection;
import org.graylog2.database.MongoCollections;


import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

public class AlertListCollection {

    private static final String COLLECTION_NAME = "wizard_lists";
    private static final String TITLE = "title";

    private final MongoCollection<AlertList> collection;

    @Inject
    public AlertListCollection(MongoCollections mongoCollections) {
        this.collection = mongoCollections.collection(COLLECTION_NAME, AlertList.class);
        this.collection.createIndex(new BasicDBObject(TITLE, 1), new IndexOptions().unique(true));
    }

    public AlertList create(AlertList list) {
        return this.collection.getOrCreate(list);
    }

    public AlertList update(String title, AlertList list) {
        return this.collection.findOneAndReplace(new BasicDBObject(TITLE, title), list, new FindOneAndReplaceOptions().returnDocument(ReturnDocument.AFTER));
    }

    public List<AlertList> all() {
        return this.collection.find().into(new ArrayList<>());
    }

    public long count() {
        return this.collection.countDocuments();
    }

    public void destroy(String title) {
        this.collection.deleteOne(new BasicDBObject(TITLE, title));
    }

    public AlertList load(String title) {
        return this.collection.find(new BasicDBObject(TITLE, title)).first();
    }

    public boolean isPresent(String title) {
        return this.collection.countDocuments(new BasicDBObject(TITLE, title)) > 0;
    }
}
