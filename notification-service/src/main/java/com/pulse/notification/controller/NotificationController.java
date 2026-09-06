package com.pulse.notification.controller;

import com.pulse.notification.dto.NotificationResponse;
import com.pulse.notification.service.NotificationStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationStore notificationStore;

    public NotificationController(NotificationStore notificationStore) {
        this.notificationStore = notificationStore;
    }

    @GetMapping
    public List<NotificationResponse> getLatestNotifications() {
        return notificationStore.getLatest();
    }
}
