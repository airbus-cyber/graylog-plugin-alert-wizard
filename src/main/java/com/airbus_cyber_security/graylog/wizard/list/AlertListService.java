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

import com.airbus_cyber_security.graylog.wizard.database.LookupService;
import com.airbus_cyber_security.graylog.wizard.list.bundles.ExportAlertList;
import com.airbus_cyber_security.graylog.wizard.list.persistence.AlertListCollection;
import com.airbus_cyber_security.graylog.wizard.list.rest.models.requests.AlertListRequest;
import org.graylog2.lookup.adapters.CSVFileDataAdapter;
import org.mongojack.DBQuery;
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

// TODO move class into sub-namespace business
public class AlertListService {

    private static final Path LISTS_PATH = Paths.get("/usr/share/graylog/data/alert-lists");
    private static final String KEY_COLUMN = "key";
    private static final String VALUE_COLUMN = "value";

    private final AlertListCollection collection;
    private final Validator validator;

    private final LookupService lookupService;
    private static final Logger LOG = LoggerFactory.getLogger(AlertListService.class);
    private static final String TITLE = "title";

    @Inject
    public AlertListService(Validator validator, LookupService lookupService, AlertListCollection collection) {
        this.validator = validator;
        this.lookupService = lookupService;
        this.collection = collection;
    }

    public long count() {
        return this.collection.count();
    }

    public List<AlertList> all() {
        return this.collection.all();
    }

    public AlertList load(String listTitle) {
        return this.collection.load(listTitle);
    }

    public boolean isPresent(String title) {
        return this.collection.isPresent(title);
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

    private Path getCSVFilePath(String title) {
        return LISTS_PATH.resolve(title + ".csv");
    }

    public AlertList create(AlertList list) throws IOException {
        Set<ConstraintViolation<AlertList>> violations = this.validator.validate(list);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException("Specified object failed validation: " + violations);
        }
        Path path = writeCSV(list);

        CSVFileDataAdapter.Config dataAdapterConfiguration = CSVFileDataAdapter.Config.builder()
                .type(CSVFileDataAdapter.NAME)
                .path(path.toString())
                .separator(",")
                .quotechar("\"")
                .keyColumn(KEY_COLUMN)
                .valueColumn(VALUE_COLUMN)
                .checkInterval(60)
                .caseInsensitiveLookup(false)
                .build();

        String title = list.getTitle();
        String adapterIdentifier = this.lookupService.createDataAdapter(title, dataAdapterConfiguration);
        this.lookupService.createLookupTable(adapterIdentifier, title);

        return this.collection.insert(list);
    }

    private Path writeCSV(AlertList list) throws IOException {
        Files.createDirectories(LISTS_PATH);
        Path path = getCSVFilePath(list.getTitle());
        // TODO shouldn't use the title here, rather an identifier
        Writer writer = Files.newBufferedWriter(path);
        try (CSVWriter csvWriter = new CSVWriter(writer)) {
            csvWriter.writeNext(new String[] {KEY_COLUMN, VALUE_COLUMN});

            for (String value: this.getListValues(list)) {
                csvWriter.writeNext(new String[]{value, value});
            }
        }
        return path;
    }

    public AlertList update(String title, AlertList list) throws IOException {
        LOG.debug("List to be updated [{}]", list);

        Set<ConstraintViolation<AlertList>> violations = this.validator.validate(list);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException("Specified object failed validation: " + violations);
        }
        this.writeCSV(list);
        return this.collection.findAndModify(title, list);
    }

    public int destroy(String title) throws IOException {
        this.lookupService.deleteLookupTable(title);
        this.lookupService.deleteDataAdapter(title);
        Files.delete(getCSVFilePath(title));
        return this.collection.remove(title);
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
