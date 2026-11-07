package com.pulse.notification.controller;

import com.pulse.notification.dto.NotificationResponse;
import com.pulse.notification.service.NotificationServiceClient;
import com.pulse.user.dto.UserResponse;
import com.pulse.user.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final UserService userService;
    private final NotificationServiceClient notificationServiceClient;

    public NotificationController(
        UserService userService,
        NotificationServiceClient notificationServiceClient
    ) {
        this.userService = userService;
        this.notificationServiceClient = notificationServiceClient;
    }

    @GetMapping("/me")
    public List<NotificationResponse> getMyNotifications(Authentication authentication) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        return notificationServiceClient.getNotificationsFor(user);
    }

    @PatchMapping("/{eventId}/read")
    public void markMyNotificationAsRead(
        @PathVariable java.util.UUID eventId,
        Authentication authentication
    ) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        notificationServiceClient.markAsRead(eventId, user);
    }
}
