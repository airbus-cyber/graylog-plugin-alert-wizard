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

import javax.inject.Inject;
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

    public EventDefinitionDto getEventDefinition(String eventDefinitionIdentifier) {
        return this.eventDefinitionService.get(eventDefinitionIdentifier)
                .orElseThrow(() -> new javax.ws.rs.NotFoundException("Event definition <" + eventDefinitionIdentifier + "> doesn't exist"));
    }

    private String createEventFromDto(EventDefinitionDto eventDefinition, UserContext userContext) {
        EventDefinitionDto result = this.eventDefinitionHandler.create(eventDefinition, Optional.of(userContext.getUser()));
        return result.id();
    }

    private String updateEventFromDto(EventDefinitionDto eventDefinition) {
        EventDefinitionDto result = this.eventDefinitionHandler.update(eventDefinition, true);
        return result.id();
    }

    public String createEvent(String alertTitle, String notificationIdentifier, EventProcessorConfig configuration, UserContext userContext) {
        LOG.debug("Create Event: " + alertTitle);
        EventNotificationHandler.Config notificationConfiguration = EventNotificationHandler.Config.builder()
                .notificationId(notificationIdentifier)
                .build();

        AlertWizardConfig pluginConfiguration = this.configurationService.getConfiguration();
        DefaultValues defaultValues = pluginConfiguration.accessDefaultValues();
        EventDefinitionDto eventDefinition = EventDefinitionDto.builder()
                .title(alertTitle)
                .description(AlertRuleUtils.COMMENT_ALERT_WIZARD)
                .config(configuration)
                .alert(true)
                .priority(2)
                .keySpec(ImmutableList.of())
                .notifications(ImmutableList.<EventNotificationHandler.Config>builder().add(notificationConfiguration).build())
                .notificationSettings(EventNotificationSettings.builder()
                        .gracePeriodMs(0L)
                        .backlogSize(defaultValues.getBacklog())
                        .build())
                .build();

        return this.createEventFromDto(eventDefinition, userContext);
    }

    public void updateEvent(String alertTitle, String eventID, EventProcessorConfig configuration) {
        LOG.debug("Update event: {}, identifier: {}", alertTitle, eventID);
        EventDefinitionDto event = this.getEventDefinition(eventID);
        EventDefinitionDto updatedEvent = EventDefinitionDto.builder()
                .id(event.id())
                .title(alertTitle)
                .description(event.description())
                .priority(event.priority())
                .alert(event.alert())
                .config(configuration)
                .fieldSpec(event.fieldSpec())
                .keySpec(event.keySpec())
                .notificationSettings(event.notificationSettings())
                .notifications(event.notifications())
                .storage(event.storage())
                .build();
        this.updateEventFromDto(updatedEvent);
    }

    public void delete(String identifier) {
        this.eventDefinitionHandler.delete(identifier);
    }
}
