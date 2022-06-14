package com.airbus_cyber_security.graylog.wizard.alert.database;

import com.airbus_cyber_security.graylog.wizard.alert.utilities.AlertRuleUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.graylog2.lookup.db.DBDataAdapterService;
import org.graylog2.lookup.dto.DataAdapterDto;
import org.graylog2.plugin.lookup.LookupDataAdapterConfiguration;

public class LookupService {

    private static final String RANDOM_CHARS = "0123456789abcdef";
    private static final int RANDOM_COUNT = 24;
    private final DBDataAdapterService dataAdapterService;

    public LookupService(DBDataAdapterService dataAdapterService) {
        this.dataAdapterService = dataAdapterService;
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
}
