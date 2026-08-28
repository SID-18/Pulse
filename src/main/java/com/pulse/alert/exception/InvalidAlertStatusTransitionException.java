package com.pulse.alert.exception;

import com.pulse.alert.entity.AlertStatus;

public class InvalidAlertStatusTransitionException extends RuntimeException {

    public InvalidAlertStatusTransitionException(
        AlertStatus currentStatus,
        AlertStatus targetStatus
    ) {
        super("Cannot change alert status from " + currentStatus + " to " + targetStatus);
    }
}
