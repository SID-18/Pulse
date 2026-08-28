package com.pulse.comment.repository;

import com.pulse.comment.entity.IncidentComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IncidentCommentRepository
    extends JpaRepository<IncidentComment, UUID> {

    List<IncidentComment> findByIncidentIdOrderByCreatedAtAsc(UUID incidentId);
}
