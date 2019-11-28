package com.airbus_cyber_security.graylog.list;

import com.airbus_cyber_security.graylog.list.bundles.ExportAlertList;
import com.airbus_cyber_security.graylog.list.rest.models.requests.AlertListRequest;
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
import java.util.List;
import java.util.Set;

public class AlertListServiceImpl implements AlertListService {

    private final JacksonDBCollection<AlertListImpl, String> coll;
    private final Validator validator;
    private static final Logger LOG = LoggerFactory.getLogger(AlertListServiceImpl.class);
    private static final String TITLE = "title";

    @Inject
    public AlertListServiceImpl(MongoConnection mongoConnection, MongoJackObjectMapperProvider mapperProvider,
                                Validator validator) {
        this.validator = validator;
        final String collectionName = AlertListImpl.class.getAnnotation(CollectionName.class).value();
        final DBCollection dbCollection = mongoConnection.getDatabase().getCollection(collectionName);
        this.coll = JacksonDBCollection.wrap(dbCollection, AlertListImpl.class, String.class, mapperProvider.get());
        this.coll.createIndex(new BasicDBObject(TITLE, 1), new BasicDBObject("unique", true));
    }

    @Override
    public long count() {
        return coll.count();
    }

    @Override
    public AlertList create(AlertList list) {
        if (list instanceof AlertListImpl) {
            final AlertListImpl listImpl = (AlertListImpl) list;

            final Set<ConstraintViolation<AlertListImpl>> violations = validator.validate(listImpl);
            if (violations.isEmpty()) {
                return coll.insert(listImpl).getSavedObject();
            } else {
                throw new IllegalArgumentException("Specified object failed validation: " + violations);
            }
        } else
            throw new IllegalArgumentException(
                    "Specified object is not of correct implementation type (" + list.getClass() + ")!");
    }

    @Override
    public AlertList update(String title, AlertList list) {

        if (list instanceof AlertListImpl) {
            final AlertListImpl listImpl = (AlertListImpl) list;
            LOG.debug("List to be updated [{}]", listImpl);

            final Set<ConstraintViolation<AlertListImpl>> violations = validator.validate(listImpl);
            if (violations.isEmpty()) {

                return coll.findAndModify(DBQuery.is(TITLE, title), new BasicDBObject(), new BasicDBObject(),
                        false, listImpl, true, false);

            } else {
                throw new IllegalArgumentException("Specified object failed validation: " + violations);
            }

        } else
            throw new IllegalArgumentException(
                    "Specified object is not of correct implementation type (" + list.getClass() + ")!");
    }


    @Override
    public List<AlertList> all() {
        return toAbstractListType(coll.find());
    }

    @Override
    public int destroy(String listTitle) {

        return coll.remove(DBQuery.is(TITLE, listTitle)).getN();
    }

    @Override
    public AlertList load(String listTitle) {
        return coll.findOne(DBQuery.is(TITLE, listTitle));
    }

    @Override
    public boolean isPresent(String title) {
        return (coll.getCount(DBQuery.is(TITLE, title)) > 0);
    }

    private List<AlertList> toAbstractListType(DBCursor<AlertListImpl> lists) {
        return toAbstractListType(lists.toArray());
    }

    private List<AlertList> toAbstractListType(List<AlertListImpl> lists) {
        final List<AlertList> result = Lists.newArrayListWithCapacity(lists.size());
        result.addAll(lists);

        return result;
    }

    private boolean isValidTitle(String title) {
        return !(title == null || title.isEmpty());
    }

    @Override
    public boolean isValidRequest(AlertListRequest request){
        return (isValidTitle(request.getTitle()));
    }

    @Override
    public boolean isValidImportRequest(ExportAlertList request){
        return (isValidTitle(request.getTitle()));
    }
}
