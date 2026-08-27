package com.pulse.service.exception;

public class MonitoredServiceNameAlreadyExistsException
    extends RuntimeException {

    public MonitoredServiceNameAlreadyExistsException(String name) {
        super("A monitored service named '" + name + "' already exists");
    }
}
