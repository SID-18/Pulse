package com.pulse.notification.service;

import com.pulse.notification.dto.NotificationResponse;
import com.pulse.notification.exception.NotificationServiceUnavailableException;
import com.pulse.user.dto.UserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class NotificationServiceClient {

    private final RestClient restClient;

    public NotificationServiceClient(
        @Value("${pulse.notification.base-url}") String baseUrl
    ) {
        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .requestFactory(new HttpComponentsClientHttpRequestFactory())
            .build();
    }

    public List<NotificationResponse> getNotificationsFor(UserResponse user) {
        try {
            NotificationResponse[] notifications = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/notifications")
                    .queryParam("recipientUserId", user.id())
                    .queryParam("recipientTeamId", user.teamId())
                    .build())
                .retrieve()
                .body(NotificationResponse[].class);
            return notifications == null ? List.of() : Arrays.asList(notifications);
        } catch (Exception exception) {
            throw new NotificationServiceUnavailableException(exception);
        }
    }

    public void markAsRead(UUID eventId, UserResponse user) {
        try {
            restClient.method(HttpMethod.PATCH)
                .uri(uriBuilder -> uriBuilder.path("/api/notifications/{eventId}/read")
                    .queryParam("recipientUserId", user.id())
                    .queryParam("recipientTeamId", user.teamId())
                    .build(eventId))
                .retrieve()
                .toBodilessEntity();
        } catch (Exception exception) {
            throw new NotificationServiceUnavailableException(exception);
        }
    }
}
