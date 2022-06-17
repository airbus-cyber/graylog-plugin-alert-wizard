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

import com.airbus_cyber_security.graylog.wizard.alert.utilities.AlertRuleUtils;
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

import javax.inject.Inject;
import java.util.Collection;

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

    public String createDataAdapter(String title, String name, LookupDataAdapterConfiguration configuration) {
        String identifier = RandomStringUtils.random(RANDOM_COUNT, RANDOM_CHARS);

        DataAdapterDto dto = DataAdapterDto.builder()
                .id(identifier)
                .title(title)
                .description(AlertRuleUtils.COMMENT_ALERT_WIZARD)
                .name(name)
                .contentPack(null)
                .config(configuration)
                .build();

        DataAdapterDto dataAdapter = this.dataAdapterService.save(dto);
        return dataAdapter.id();
    }

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
                .description(AlertRuleUtils.COMMENT_ALERT_WIZARD)
                .title("wizard cache")
                .config(config)
                .build();

        CacheDto cache = this.cacheService.save(dto);
        return cache.id();
    }

    public String getLookupTableName(String listTitle) {
        return "alert-wizard-list-lookup-table-" + listTitle;
    }

    public void createLookupTable(String adapterIdentifier, String title, String name) {
        String cacheIdentifier = this.createUniqueCache();

        LookupTableDto dto = LookupTableDto.builder()
                .title(title)
                .description(AlertRuleUtils.COMMENT_ALERT_WIZARD)
                .name(name)
                .cacheId(cacheIdentifier)
                .dataAdapterId(adapterIdentifier)
                .defaultSingleValue("")
                .defaultSingleValueType(LookupDefaultSingleValue.Type.NULL)
                .defaultMultiValue("")
                .defaultMultiValueType(LookupDefaultMultiValue.Type.NULL)
                .build();

        this.lookupTableService.save(dto);
    }
}
