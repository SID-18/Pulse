package com.pulse.team.exception;

import java.util.UUID;

public class TeamNotFoundException extends RuntimeException {

    public TeamNotFoundException(UUID id) {
        super("Team with id " + id + " was not found");
    }
}
