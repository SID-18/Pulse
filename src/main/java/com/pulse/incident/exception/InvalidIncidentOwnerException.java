package com.pulse.incident.exception;

import com.pulse.user.entity.UserRole;

public class InvalidIncidentOwnerException extends RuntimeException {

    public InvalidIncidentOwnerException(UserRole role) {
        super("Only a manager or engineer can own an incident; received " + role + ".");
    }
}
