package com.pulse.service.exception;

import java.util.UUID;

public class MonitoredServiceNotFoundException extends RuntimeException {

    public MonitoredServiceNotFoundException(UUID id) {
        super("Monitored service with id " + id + " was not found");
    }
}
