package com.pulse.event.outbox;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select event from OutboxEvent event
        where event.status = :status
          and event.nextAttemptAt <= :now
        order by event.createdAt asc
        """)
    List<OutboxEvent> lockPublishableEvents(
        @Param("status") OutboxEventStatus status,
        @Param("now") Instant now,
        Pageable pageable
    );
}
