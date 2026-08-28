package com.pulse.comment.service;

import com.pulse.comment.dto.CreateIncidentCommentRequest;
import com.pulse.comment.dto.IncidentCommentResponse;
import com.pulse.comment.entity.IncidentComment;
import com.pulse.comment.repository.IncidentCommentRepository;
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

    @Transactional
    public IncidentCommentResponse createComment(
        UUID incidentId,
        CreateIncidentCommentRequest request
    ) {
        Incident incident = findIncidentById(incidentId);
        User author = findUserById(request.authorId());
        IncidentComment comment = new IncidentComment(
            request.content(),
            incident,
            author
        );

        return toResponse(incidentCommentRepository.save(comment));
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

    private User findUserById(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
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
