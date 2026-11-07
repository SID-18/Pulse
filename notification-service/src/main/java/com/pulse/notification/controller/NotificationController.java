package com.pulse.notification.controller;

import com.pulse.notification.dto.NotificationResponse;
import com.pulse.notification.service.NotificationStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationStore notificationStore;

    public NotificationController(NotificationStore notificationStore) {
        this.notificationStore = notificationStore;
    }

    @GetMapping
    public List<NotificationResponse> getLatestNotifications(
        @RequestParam(required = false) UUID recipientUserId,
        @RequestParam(required = false) UUID recipientTeamId
    ) {
        return notificationStore.getLatest(recipientUserId, recipientTeamId);
    }

    @PatchMapping("/{eventId}/read")
    public void markNotificationAsRead(
        @PathVariable UUID eventId,
        @RequestParam UUID recipientUserId,
        @RequestParam(required = false) UUID recipientTeamId
    ) {
        if (!notificationStore.markAsRead(eventId, recipientUserId, recipientTeamId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification was not found");
        }
    }
}
