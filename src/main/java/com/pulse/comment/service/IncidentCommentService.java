package com.pulse.comment.service;

import com.pulse.comment.dto.CreateIncidentCommentRequest;
import com.pulse.comment.dto.IncidentCommentResponse;
import com.pulse.comment.entity.IncidentComment;
import com.pulse.comment.repository.IncidentCommentRepository;
import com.pulse.event.entity.IncidentEventType;
import com.pulse.event.service.IncidentEventService;
import com.pulse.incident.entity.Incident;
import com.pulse.incident.exception.IncidentNotFoundException;
import com.pulse.incident.repository.IncidentRepository;
import com.pulse.user.entity.User;
import com.pulse.user.exception.UserNotFoundException;
import com.pulse.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IncidentCommentService {

    private final IncidentCommentRepository incidentCommentRepository;
    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final IncidentEventService incidentEventService;

    @Transactional
    public IncidentCommentResponse createComment(
        UUID incidentId,
        String authorEmail,
        CreateIncidentCommentRequest request
    ) {
        Incident incident = findIncidentById(incidentId);
        User author = findUserByEmail(authorEmail);
        IncidentComment comment = new IncidentComment(
            request.content(),
            incident,
            author
        );

        IncidentComment savedComment = incidentCommentRepository.save(comment);
        incidentEventService.record(
            incident,
            IncidentEventType.COMMENT_ADDED,
            "Comment added by " + author.getName() + "."
        );

        return toResponse(savedComment);
    }

    @Transactional(readOnly = true)
    public List<IncidentCommentResponse> getComments(UUID incidentId) {
        findIncidentById(incidentId);

        return incidentCommentRepository
            .findByIncidentIdOrderByCreatedAtAsc(incidentId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private Incident findIncidentById(UUID id) {
        return incidentRepository.findById(id)
            .orElseThrow(() -> new IncidentNotFoundException(id));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException(email));
    }

    private IncidentCommentResponse toResponse(IncidentComment comment) {
        User author = comment.getAuthor();

        return new IncidentCommentResponse(
            comment.getId(),
            comment.getContent(),
            comment.getIncident().getId(),
            author.getId(),
            author.getName(),
            comment.getCreatedAt()
        );
    }
}
