package com.airbus_cyber_security.graylog.wizard.alert.business;

import com.airbus_cyber_security.graylog.events.config.LoggingAlertConfig;
import com.airbus_cyber_security.graylog.events.config.SeverityType;
import com.airbus_cyber_security.graylog.events.notifications.types.LoggingNotificationConfig;
import org.graylog.events.notifications.DBNotificationService;
import org.graylog.events.notifications.NotificationDto;
import org.graylog.events.notifications.NotificationResourceHandler;
import org.graylog.security.UserContext;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
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

    public NotificationDto get(String notificationIdentifier) {
        return this.notificationService.get(notificationIdentifier)
                .orElseThrow(() -> new javax.ws.rs.NotFoundException("Notification " + notificationIdentifier + " doesn't exist"));
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

    public String createNotification(String alertTitle, String severity, UserContext userContext) {
        LoggingNotificationConfig loggingNotificationConfig = LoggingNotificationConfig.builder()
                .singleMessage(false)
                .severity(SeverityType.valueOf(severity.toUpperCase()))
                .logBody(this.getDefaultLogBody())
                .aggregationTime(this.getDefaultTime())
                .build();
        NotificationDto notification = NotificationDto.builder()
                .config(loggingNotificationConfig)
                .title(alertTitle)
                .description(AlertRuleUtils.COMMENT_ALERT_WIZARD)
                .build();
        return this.create(notification, userContext);
    }

    public void updateNotification(String title, String notificationID, String severity) {
        NotificationDto notification = this.get(notificationID);
        LoggingNotificationConfig loggingNotificationConfig = (LoggingNotificationConfig) notification.config();
        if (!loggingNotificationConfig.severity().getType().equals(severity) || !notification.title().equals(title)) {
            LOG.debug("Update Notification " + title);
            if (!loggingNotificationConfig.severity().getType().equals(severity)) {
                LOG.debug("Update severity, old one: " + loggingNotificationConfig.severity().getType() + " New one: " + severity);
                loggingNotificationConfig = LoggingNotificationConfig.builder()
                        .severity(SeverityType.valueOf(severity.toUpperCase()))
                        .logBody(loggingNotificationConfig.logBody())
                        .splitFields(loggingNotificationConfig.splitFields())
                        .aggregationTime(loggingNotificationConfig.aggregationTime())
                        .alertTag(loggingNotificationConfig.alertTag())
                        .singleMessage(loggingNotificationConfig.singleMessage())
                        .build();
            }
            notification = NotificationDto.builder()
                    .id(notification.id())
                    .config(loggingNotificationConfig)
                    .title(title)
                    .description(notification.description())
                    .build();
            this.update(notification);
        }
    }
}
