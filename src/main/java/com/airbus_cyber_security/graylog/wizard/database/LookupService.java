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

package com.airbus_cyber_security.graylog.wizard.database;

import com.mongodb.BasicDBObject;
import org.graylog2.database.entities.DefaultEntityScope;
import org.graylog2.lookup.LookupDefaultMultiValue;
import org.graylog2.lookup.LookupDefaultSingleValue;
import org.graylog2.lookup.caches.NullCache;
import org.graylog2.lookup.db.DBCacheService;
import org.graylog2.lookup.db.DBDataAdapterService;
import org.graylog2.lookup.db.DBLookupTableService;
import org.graylog2.lookup.dto.CacheDto;
import org.graylog2.lookup.dto.DataAdapterDto;
import org.graylog2.lookup.dto.LookupTableDto;
import org.graylog2.plugin.lookup.LookupDataAdapterConfiguration;

import jakarta.inject.Inject;
import org.graylog2.rest.models.SortOrder;
import org.graylog2.search.SearchQuery;

import java.util.Collection;
import java.util.Optional;

public class LookupService {
    private static final String FIELD_TITLE = "title";
    private static final String WIZARD_TITLE = "wizard cache";
    private final DBDataAdapterService dataAdapterService;
    private final DBCacheService cacheService;
    private final DBLookupTableService lookupTableService;

    @Inject
    public LookupService(DBDataAdapterService dataAdapterService, DBCacheService cacheService, DBLookupTableService lookupTableService) {
        this.dataAdapterService = dataAdapterService;
        this.cacheService = cacheService;
        this.lookupTableService = lookupTableService;
    }

    // source of inspiration org.graylog2.rest.resources.system.lookup.LookupTableResource.createAdapter
    public String createDataAdapter(String title, LookupDataAdapterConfiguration configuration) {

        // TODO shouldn't use the title here, rather an identifier
        String name = this.getDataAdapterName(title);
        DataAdapterDto dto = DataAdapterDto.builder()
                .title("Alert wizard data adapter for list " + title)
                .description(Description.COMMENT_ALERT_WIZARD)
                .name(name)
                .contentPack(null)
                .customErrorTTL(null)
                .customErrorTTLEnabled(null)
                .customErrorTTLUnit(null)
                .config(configuration)
                .scope(DefaultEntityScope.NAME)
                .build();

        DataAdapterDto dataAdapter = this.dataAdapterService.saveAndPostEvent(dto);
        return dataAdapter.id();
    }

    // source of inspiration org.graylog2.rest.resources.system.lookup.LookupTableResource.createCache
    private String createUniqueCache() {

        Collection<CacheDto> caches = this.cacheService.findPaginated(new BasicDBObject(FIELD_TITLE, WIZARD_TITLE), SortOrder.DESCENDING.toBsonSort(FIELD_TITLE), 1, 10).stream().toList();
        if (!caches.isEmpty()) {
            return caches.iterator().next().id();
        }

        NullCache.Config config = NullCache.Config.builder()
                .type(NullCache.NAME)
                .build();

        CacheDto dto = CacheDto.builder()
                .scope(DefaultEntityScope.NAME)
                .name("wizard-cache")
                .description(Description.COMMENT_ALERT_WIZARD)
                .title(WIZARD_TITLE)
                .contentPack(null)
                .config(config)
                .build();

        CacheDto cache = this.cacheService.saveAndPostEvent(dto);
        return cache.id();
    }

    private String getDataAdapterName(String title) {
        return "alert-wizard-list-data-adapter-" + title;
    }

    public String getLookupTableName(String title) {
        return "alert-wizard-list-lookup-table-" + title;
    }

    // source of inspiration org.graylog2.rest.resources.system.lookup.LookupTableResource.createTable
    public void createLookupTable(String adapterIdentifier, String title) {
        String cacheIdentifier = this.createUniqueCache();
        String name = this.getLookupTableName(title);

        LookupTableDto dto = LookupTableDto.builder()
                .scope(DefaultEntityScope.NAME)
                .title("Alert wizard lookup table for list " + title)
                .description(Description.COMMENT_ALERT_WIZARD)
                .name(name)
                .cacheId(cacheIdentifier)
                .dataAdapterId(adapterIdentifier)
                .contentPack(null)
                .defaultSingleValue("")
                .defaultSingleValueType(LookupDefaultSingleValue.Type.NULL)
                .defaultMultiValue("")
                .defaultMultiValueType(LookupDefaultMultiValue.Type.NULL)
                .build();

        this.lookupTableService.saveAndPostEvent(dto);
    }

    public void deleteDataAdapter(String title) {
        String adapterName = this.getDataAdapterName(title);
        Optional<DataAdapterDto> dataAdapterDto = this.dataAdapterService.get(adapterName);
        if (dataAdapterDto.isPresent()) {
            this.dataAdapterService.deleteAndPostEvent(dataAdapterDto.get().id());
        }
    }

    public void deleteLookupTable(String title) {
        String lookupTableName = this.getLookupTableName(title);
        Optional<LookupTableDto> lookupTableDto = this.lookupTableService.get(lookupTableName);
        if (lookupTableDto.isPresent()) {
            this.lookupTableService.deleteAndPostEvent(lookupTableDto.get().id());
        }
    }
}
