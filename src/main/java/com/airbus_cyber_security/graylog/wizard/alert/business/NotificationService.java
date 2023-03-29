package com.airbus_cyber_security.graylog.wizard.alert.business;

import org.graylog.events.notifications.DBNotificationService;
import org.graylog.events.notifications.NotificationDto;
import org.graylog.events.notifications.NotificationResourceHandler;
import org.graylog.security.UserContext;

import javax.inject.Inject;
import java.util.Optional;

public class NotificationService {

    private final DBNotificationService notificationService;
    private final NotificationResourceHandler notificationHandler;

    @Inject
    public NotificationService(NotificationResourceHandler notificationHandler, DBNotificationService notificationService) {
        this.notificationHandler = notificationHandler;
        this.notificationService = notificationService;
    }

    public NotificationDto get(String notificationIdentifier) {
        return this.notificationService.get(notificationIdentifier)
                .orElseThrow(() -> new javax.ws.rs.NotFoundException("Notification " + notificationIdentifier + " doesn't exist"));
    }

    public String create(NotificationDto notification, UserContext userContext) {
        NotificationDto result = this.notificationHandler.create(notification, Optional.ofNullable(userContext.getUser()));
        return result.id();
    }

    public String update(NotificationDto notification) {
        NotificationDto result = this.notificationHandler.update(notification);
        return result.id();
    }
}
