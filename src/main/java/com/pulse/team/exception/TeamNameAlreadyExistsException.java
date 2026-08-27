package com.pulse.team.exception;

public class TeamNameAlreadyExistsException extends RuntimeException {

    public TeamNameAlreadyExistsException(String name) {
        super("A team named '" + name + "' already exists");
    }
}
