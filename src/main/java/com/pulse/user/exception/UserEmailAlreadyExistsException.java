package com.pulse.user.exception;

public class UserEmailAlreadyExistsException extends RuntimeException {

    public UserEmailAlreadyExistsException(String email) {
        super("A user with email '" + email + "' already exists");
    }
}
