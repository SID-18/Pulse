package com.pulse.task.exception;

import com.pulse.task.entity.IncidentTaskStatus;

public class InvalidIncidentTaskStatusTransitionException
    extends RuntimeException {

    public InvalidIncidentTaskStatusTransitionException(
        IncidentTaskStatus currentStatus,
        IncidentTaskStatus targetStatus
    ) {
        super(
            "Cannot change incident task status from "
                + currentStatus + " to " + targetStatus
        );
    }
}
