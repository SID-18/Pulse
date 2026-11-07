package com.pulse.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulse.notification.dto.NotificationResponse;
import com.pulse.notification.kafka.NotificationEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class NotificationStore {

    private static final int MAX_NOTIFICATIONS = 100;
    private static final Duration RETENTION = Duration.ofDays(30);
    private static final String PREFIX = "pulse:notifications:";
    private static final String FEED_KEY = PREFIX + "feed";

    private final ConcurrentLinkedDeque<NotificationResponse> inMemoryNotifications =
        new ConcurrentLinkedDeque<>();
    private final Set<UUID> inMemoryProcessedEventIds = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<UUID, Set<UUID>> inMemoryReadByUsers = new ConcurrentHashMap<>();
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /** Used by focused unit tests; the running service always receives Redis. */
    public NotificationStore() {
        this.redisTemplate = null;
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Autowired
    public NotificationStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    public boolean record(NotificationEvent event) {
        return redisTemplate == null ? recordInMemory(event) : recordInRedis(event);
    }

    public List<NotificationResponse> getLatest(UUID recipientUserId, UUID recipientTeamId) {
        return redisTemplate == null
            ? getLatestInMemory(recipientUserId, recipientTeamId)
            : getLatestFromRedis(recipientUserId, recipientTeamId);
    }

    public boolean markAsRead(UUID eventId, UUID recipientUserId, UUID recipientTeamId) {
        return redisTemplate == null
            ? markAsReadInMemory(eventId, recipientUserId, recipientTeamId)
            : markAsReadInRedis(eventId, recipientUserId, recipientTeamId);
    }

    private boolean recordInMemory(NotificationEvent event) {
        if (!inMemoryProcessedEventIds.add(event.eventId())) {
            return false;
        }
        inMemoryNotifications.addFirst(toNotification(event));
        while (inMemoryNotifications.size() > MAX_NOTIFICATIONS) {
            NotificationResponse removed = inMemoryNotifications.pollLast();
            if (removed != null) {
                inMemoryProcessedEventIds.remove(removed.eventId());
                inMemoryReadByUsers.remove(removed.eventId());
            }
        }
        return true;
    }

    private boolean recordInRedis(NotificationEvent event) {
        String processedKey = processedKey(event.eventId());
        if (!Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(processedKey, "1", RETENTION))) {
            return false;
        }
        NotificationResponse notification = toNotification(event);
        try {
            redisTemplate.opsForValue().set(notificationKey(event.eventId()),
                objectMapper.writeValueAsString(notification), RETENTION);
            redisTemplate.opsForZSet().add(FEED_KEY, event.eventId().toString(),
                notification.receivedAt().toEpochMilli());
            redisTemplate.expire(FEED_KEY, RETENTION);
            trimRedisFeed();
            return true;
        } catch (JsonProcessingException exception) {
            redisTemplate.delete(processedKey);
            throw new IllegalStateException("Unable to store notification", exception);
        }
    }

    private List<NotificationResponse> getLatestInMemory(UUID recipientUserId, UUID recipientTeamId) {
        return inMemoryNotifications.stream()
            .filter(notification -> isVisibleTo(notification, recipientUserId, recipientTeamId))
            .map(notification -> withReadState(notification, recipientUserId,
                recipientUserId != null && inMemoryReadByUsers
                    .getOrDefault(notification.eventId(), Set.of()).contains(recipientUserId)))
            .toList();
    }

    private List<NotificationResponse> getLatestFromRedis(UUID recipientUserId, UUID recipientTeamId) {
        Set<String> eventIds = redisTemplate.opsForZSet().reverseRange(FEED_KEY, 0, MAX_NOTIFICATIONS - 1);
        if (eventIds == null) {
            return List.of();
        }
        return eventIds.stream()
            .map(this::readNotification)
            .filter(java.util.Objects::nonNull)
            .filter(notification -> isVisibleTo(notification, recipientUserId, recipientTeamId))
            .map(notification -> withReadState(notification, recipientUserId,
                isReadInRedis(notification.eventId(), recipientUserId)))
            .toList();
    }

    private boolean markAsReadInMemory(UUID eventId, UUID recipientUserId, UUID recipientTeamId) {
        boolean visibleToRecipient = inMemoryNotifications.stream().anyMatch(notification ->
            notification.eventId().equals(eventId) && isVisibleTo(notification, recipientUserId, recipientTeamId));
        if (!visibleToRecipient) {
            return false;
        }
        inMemoryReadByUsers.computeIfAbsent(eventId, ignored -> ConcurrentHashMap.newKeySet()).add(recipientUserId);
        return true;
    }

    private boolean markAsReadInRedis(UUID eventId, UUID recipientUserId, UUID recipientTeamId) {
        NotificationResponse notification = readNotification(eventId.toString());
        if (notification == null || !isVisibleTo(notification, recipientUserId, recipientTeamId)) {
            return false;
        }
        redisTemplate.opsForSet().add(readKey(eventId), recipientUserId.toString());
        redisTemplate.expire(readKey(eventId), RETENTION);
        return true;
    }

    private NotificationResponse readNotification(String eventId) {
        try {
            String json = redisTemplate.opsForValue().get(notificationKey(UUID.fromString(eventId)));
            return json == null ? null : objectMapper.readValue(json, NotificationResponse.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to read notification", exception);
        }
    }

    private void trimRedisFeed() {
        Long feedSize = redisTemplate.opsForZSet().size(FEED_KEY);
        if (feedSize == null || feedSize <= MAX_NOTIFICATIONS) {
            return;
        }
        long itemsToRemove = feedSize - MAX_NOTIFICATIONS;
        Set<String> oldEventIds = redisTemplate.opsForZSet().range(FEED_KEY, 0, itemsToRemove - 1);
        redisTemplate.opsForZSet().removeRange(FEED_KEY, 0, itemsToRemove - 1);
        if (oldEventIds != null) {
            oldEventIds.forEach(eventId -> redisTemplate.delete(List.of(
                notificationKey(UUID.fromString(eventId)), processedKey(UUID.fromString(eventId)),
                readKey(UUID.fromString(eventId))
            )));
        }
    }

    private NotificationResponse toNotification(NotificationEvent event) {
        return new NotificationResponse(event.eventId(), event.incidentId(), event.recipientUserId(),
            event.recipientTeamId(), event.type(), event.message(), event.occurredAt(), Instant.now(), false);
    }

    private boolean isVisibleTo(NotificationResponse notification, UUID recipientUserId, UUID recipientTeamId) {
        return (recipientUserId == null && recipientTeamId == null)
            || recipientUserId != null && recipientUserId.equals(notification.recipientUserId())
            || recipientTeamId != null && recipientTeamId.equals(notification.recipientTeamId());
    }

    private NotificationResponse withReadState(NotificationResponse notification, UUID recipientUserId, boolean read) {
        return new NotificationResponse(notification.eventId(), notification.incidentId(), notification.recipientUserId(),
            notification.recipientTeamId(), notification.type(), notification.message(), notification.occurredAt(),
            notification.receivedAt(), recipientUserId != null && read);
    }

    private boolean isReadInRedis(UUID eventId, UUID recipientUserId) {
        return recipientUserId != null && Boolean.TRUE.equals(redisTemplate.opsForSet()
            .isMember(readKey(eventId), recipientUserId.toString()));
    }

    private String notificationKey(UUID eventId) { return PREFIX + "item:" + eventId; }
    private String processedKey(UUID eventId) { return PREFIX + "processed:" + eventId; }
    private String readKey(UUID eventId) { return PREFIX + "read:" + eventId; }
}
