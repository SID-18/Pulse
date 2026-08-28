package com.pulse.alert.exception;

import java.util.UUID;

public class AlertNotFoundException extends RuntimeException {

    public AlertNotFoundException(UUID id) {
        super("Alert with id " + id + " was not found");
    }
}
