package com.pulse.task.exception;

import java.util.UUID;

public class IncidentTaskNotFoundException extends RuntimeException {

    public IncidentTaskNotFoundException(UUID id) {
        super("Incident task with id " + id + " was not found");
    }
}
