package com.pulse.incident.exception;

import com.pulse.incident.entity.IncidentStatus;

public class InvalidIncidentStatusTransitionException extends RuntimeException {

    public InvalidIncidentStatusTransitionException(
        IncidentStatus currentStatus,
        IncidentStatus targetStatus
    ) {
        super(
            "Cannot change incident status from "
                + currentStatus
                + " to "
                + targetStatus
        );
    }
}
