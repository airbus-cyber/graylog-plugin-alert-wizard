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
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import org.graylog2.bindings.providers.MongoJackObjectMapperProvider;
import org.graylog2.database.MongoConnection;
import org.graylog2.database.PaginatedDbService;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;

import javax.inject.Inject;
import java.util.List;

public class AlertListCollection extends PaginatedDbService<AlertList>  {

    private static final String COLLECTION_NAME = "wizard_lists";
    private static final String TITLE = "title";

    @Inject
    public AlertListCollection(MongoConnection mongoConnection, MongoJackObjectMapperProvider mapperProvider) {
        super(mongoConnection, mapperProvider, AlertList.class, COLLECTION_NAME);
        this.db.createIndex(new BasicDBObject(TITLE, 1), new BasicDBObject("unique", true));
    }

    public AlertList create(AlertList list) {
        return this.save(list);
    }

    public AlertList update(String title, AlertList list) {
        return this.db.findAndModify(DBQuery.is(TITLE, title), new BasicDBObject(), new BasicDBObject(),
                false, list, true, false);
    }

    public List<AlertList> all() {
        try (DBCursor<AlertList> cursor = this.db.find(DBQuery.empty())) {
            return cursor.toArray();
        }
    }

    public long count() {
        return this.db.count();
    }

    public int destroy(String title) {
        return this.db.remove(DBQuery.is(TITLE, title)).getN();
    }

    public AlertList load(String title) {
        return this.db.findOne(DBQuery.is(TITLE, title));
    }

    public boolean isPresent(String title) {
        return (this.db.getCount(DBQuery.is(TITLE, title)) > 0);
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
