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

package com.airbus_cyber_security.graylog.wizard.alert.business;

import com.airbus_cyber_security.graylog.wizard.config.rest.AlertWizardConfig;
import com.airbus_cyber_security.graylog.wizard.config.rest.AlertWizardConfigurationService;
import com.airbus_cyber_security.graylog.wizard.config.rest.DefaultValues;
import com.google.common.collect.ImmutableList;
import org.graylog.events.notifications.EventNotificationHandler;
import org.graylog.events.notifications.EventNotificationSettings;
import org.graylog.events.processor.DBEventDefinitionService;
import org.graylog.events.processor.EventDefinitionDto;
import org.graylog.events.processor.EventDefinitionHandler;
import org.graylog.events.processor.EventProcessorConfig;
import org.graylog.security.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import java.util.Optional;

// TODO I am not sure to like this name EventDefinitionHandler? EventDefinitionBusiness? EventDefinitionOperations?
public class EventDefinitionService {

    private static final Logger LOG = LoggerFactory.getLogger(EventDefinitionService.class);

    private final EventDefinitionHandler eventDefinitionHandler;

    private final DBEventDefinitionService eventDefinitionService;

    private final AlertWizardConfigurationService configurationService;

    @Inject
    public EventDefinitionService(EventDefinitionHandler eventDefinitionHandler,
                                  DBEventDefinitionService eventDefinitionService,
                                  AlertWizardConfigurationService configurationService) {
        this.eventDefinitionHandler = eventDefinitionHandler;
        this.eventDefinitionService = eventDefinitionService;
        this.configurationService = configurationService;
    }

    public Optional<EventDefinitionDto> getEventDefinition(String eventDefinitionIdentifier) {
        return this.eventDefinitionService.get(eventDefinitionIdentifier);
    }

    private String createEventFromDto(EventDefinitionDto eventDefinition, UserContext userContext) {
        EventDefinitionDto result = this.eventDefinitionHandler.create(eventDefinition, Optional.of(userContext.getUser()));
        return result.id();
    }

    public String createEvent(String alertTitle, String description, Integer priority, String notificationIdentifier, EventProcessorConfig configuration, UserContext userContext) {
        LOG.debug("Create Event: " + alertTitle);
        EventNotificationHandler.Config notificationConfiguration = EventNotificationHandler.Config.builder()
                .notificationId(notificationIdentifier)
                .build();

        AlertWizardConfig pluginConfiguration = this.configurationService.getConfiguration();
        DefaultValues defaultValues = pluginConfiguration.accessDefaultValues();
        EventDefinitionDto eventDefinition = EventDefinitionDto.builder()
                .title(alertTitle)
                .description(description)
                .config(configuration)
                .alert(true)
                .priority(priority)
                .keySpec(ImmutableList.of())
                .notifications(ImmutableList.<EventNotificationHandler.Config>builder().add(notificationConfiguration).build())
                .notificationSettings(EventNotificationSettings.builder()
                        .gracePeriodMs(0L)
                        .backlogSize(defaultValues.getBacklog())
                        .build())
                .build();

        return this.createEventFromDto(eventDefinition, userContext);
    }

    public void updateEvent(String alertTitle, String description, Integer priority, String eventIdentifier, EventProcessorConfig configuration) {
        LOG.debug("Update event: {}, identifier: {}", alertTitle, eventIdentifier);
        EventDefinitionDto event = this.getEventDefinition(eventIdentifier)
                .orElseThrow(() -> new jakarta.ws.rs.NotFoundException("Event definition <" + eventIdentifier + "> doesn't exist"));

        this.updateEvent(alertTitle, description, priority, event, configuration);
    }

    private void updateEvent(String title, String description, Integer priority, EventDefinitionDto event, EventProcessorConfig configuration) {
        EventDefinitionDto updatedEvent = EventDefinitionDto.builder()
                .id(event.id())
                .title(title)
                .description(description)
                .priority(priority)
                .alert(event.alert())
                .config(configuration)
                .fieldSpec(event.fieldSpec())
                .keySpec(event.keySpec())
                .notificationSettings(event.notificationSettings())
                .notifications(event.notifications())
                .storage(event.storage())
                .build();
        this.eventDefinitionHandler.update(updatedEvent, true);
    }

    public void delete(String identifier) {
        this.eventDefinitionHandler.delete(identifier);
    }
}
