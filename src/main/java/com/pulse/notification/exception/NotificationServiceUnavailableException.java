package com.pulse.notification.exception;

public class NotificationServiceUnavailableException extends RuntimeException {

    public NotificationServiceUnavailableException(Throwable cause) {
        super("Notifications are temporarily unavailable. Please try again shortly.", cause);
    }
}
