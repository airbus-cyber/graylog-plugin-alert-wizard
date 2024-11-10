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

import com.airbus_cyber_security.graylog.events.config.LoggingAlertConfig;
import com.airbus_cyber_security.graylog.events.notifications.types.LoggingNotificationConfig;
import com.airbus_cyber_security.graylog.wizard.database.Description;
import org.graylog.events.notifications.DBNotificationService;
import org.graylog.events.notifications.NotificationDto;
import org.graylog.events.notifications.NotificationResourceHandler;
import org.graylog.security.UserContext;
import org.graylog2.database.NotFoundException;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import java.util.Optional;

public class NotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

    private final DBNotificationService notificationService;
    private final NotificationResourceHandler notificationHandler;
    private final ClusterConfigService clusterConfigService;

    @Inject
    public NotificationService(NotificationResourceHandler notificationHandler,
                               DBNotificationService notificationService,
                               ClusterConfigService clusterConfigService) {
        this.notificationHandler = notificationHandler;
        this.notificationService = notificationService;
        this.clusterConfigService = clusterConfigService;
    }

    public Optional<NotificationDto> get(String notificationIdentifier) {
        return this.notificationService.get(notificationIdentifier);
    }

    private String create(NotificationDto notification, UserContext userContext) {
        NotificationDto result = this.notificationHandler.create(notification, Optional.ofNullable(userContext.getUser()));
        return result.id();
    }

    private String update(NotificationDto notification) {
        NotificationDto result = this.notificationHandler.update(notification);
        return result.id();
    }

    private String getDefaultLogBody() {
        LoggingAlertConfig generalConfig = this.clusterConfigService.getOrDefault(LoggingAlertConfig.class,
                LoggingAlertConfig.createDefault());
        return generalConfig.accessLogBody();
    }

    private int getDefaultTime() {
        LoggingAlertConfig configuration = this.clusterConfigService.getOrDefault(LoggingAlertConfig.class,
                LoggingAlertConfig.createDefault());
        return configuration.accessAggregationTime();
    }

    public String createNotification(String alertTitle, UserContext userContext) {
        LoggingNotificationConfig loggingNotificationConfig = LoggingNotificationConfig.builder()
                .singleMessage(false)
                .logBody(this.getDefaultLogBody())
                .aggregationTime(this.getDefaultTime())
                .build();
        NotificationDto notification = NotificationDto.builder()
                .config(loggingNotificationConfig)
                .title(alertTitle)
                .description(Description.COMMENT_ALERT_WIZARD)
                .build();
        return this.create(notification, userContext);
    }

    public void updateNotification(String title, String notificationIdentifier) {
        NotificationDto notification = this.get(notificationIdentifier)
                .orElseThrow(() -> new jakarta.ws.rs.NotFoundException("Notification " + notificationIdentifier + " doesn't exist"));
        LoggingNotificationConfig loggingNotificationConfig = (LoggingNotificationConfig) notification.config();
        if (!notification.title().equals(title)) {
            LOG.debug("Update Notification " + title);
            notification = NotificationDto.builder()
                    .id(notification.id())
                    .config(loggingNotificationConfig)
                    .title(title)
                    .description(notification.description())
                    .build();
            this.update(notification);
        }
    }

    public String cloneNotification(String notificationID, String alertTitle, UserContext userContext) throws NotFoundException {
        Optional<NotificationDto> optSourceNotification = get(notificationID);
        if (optSourceNotification.isPresent()) {
            NotificationDto sourceNotification = optSourceNotification.get();
            NotificationDto clonedNotification = NotificationDto.builder()
                    .config(sourceNotification.config())
                    .title(alertTitle)
                    .description(sourceNotification.description())
                    .build();
            return this.create(clonedNotification, userContext);
        } else {
            throw new NotFoundException("No notification found for ID: " + notificationID);
        }
    }
}
