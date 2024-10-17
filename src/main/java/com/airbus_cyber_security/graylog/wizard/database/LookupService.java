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

import org.apache.commons.lang3.RandomStringUtils;
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
import java.util.Collection;
import java.util.Optional;

public class LookupService {

    private static final String RANDOM_CHARS = "0123456789abcdef";
    private static final int RANDOM_COUNT = 24;
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
        String identifier = RandomStringUtils.random(RANDOM_COUNT, RANDOM_CHARS);

        // TODO shouldn't use the title here, rather an identifier
        String name = this.getDataAdapterName(title);
        DataAdapterDto dto = DataAdapterDto.builder()
                // TODO id may be null here and it will be automatically generated? Try it out (and also for the cache and table)!
                .id(identifier)
                .title("Alert wizard data adapter for list " + title)
                .description(Description.COMMENT_ALERT_WIZARD)
                .name(name)
                .contentPack(null)
                .config(configuration)
                .build();

        DataAdapterDto dataAdapter = this.dataAdapterService.saveAndPostEvent(dto);
        return dataAdapter.id();
    }

    // source of inspiration org.graylog2.rest.resources.system.lookup.LookupTableResource.createCache
    private String createUniqueCache() {
        Collection<CacheDto> caches = this.cacheService.findAll();
        for (CacheDto cacheDto: caches) {
            if(cacheDto.title().equals("wizard cache")){
                return cacheDto.id();
            }
        }

        final String cacheID = RandomStringUtils.random(RANDOM_COUNT, RANDOM_CHARS);

        NullCache.Config config = NullCache.Config.builder()
                .type("none")
                .build();

        CacheDto dto = CacheDto.builder()
                .id(cacheID)
                .name("wizard-cache")
                .description(Description.COMMENT_ALERT_WIZARD)
                .title("wizard cache")
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
                .title("Alert wizard lookup table for list " + title)
                .description(Description.COMMENT_ALERT_WIZARD)
                .name(name)
                .cacheId(cacheIdentifier)
                .dataAdapterId(adapterIdentifier)
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
            this.dataAdapterService.delete(dataAdapterDto.get().id());
        }
    }

    public void deleteLookupTable(String title) {
        String lookupTableName = this.getLookupTableName(title);
        Optional<LookupTableDto> lookupTableDto = this.lookupTableService.get(lookupTableName);
        if (lookupTableDto.isPresent()) {
            this.lookupTableService.delete(lookupTableDto.get().id());
        }
    }
}
