package com.pulse.common.exception;

import com.pulse.incident.exception.IncidentNotFoundException;
import com.pulse.incident.exception.InvalidIncidentStatusTransitionException;
import com.pulse.service.exception.MonitoredServiceNameAlreadyExistsException;
import com.pulse.service.exception.MonitoredServiceNotFoundException;
import com.pulse.team.exception.TeamNameAlreadyExistsException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleInvalidRequestBody(
        MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Request validation failed"
        );
        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail handleInvalidRequestParameters(
        HandlerMethodValidationException exception
    ) {
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "One or more request parameters are invalid"
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(
        ConstraintViolationException exception
    ) {
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "One or more request parameters are invalid"
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleInvalidRequestParameterType(
        MethodArgumentTypeMismatchException exception
    ) {
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "A request parameter has an invalid value"
        );
    }

    @ExceptionHandler(IncidentNotFoundException.class)
    public ProblemDetail handleIncidentNotFound(
        IncidentNotFoundException exception
    ) {
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            exception.getMessage()
        );
    }

    @ExceptionHandler(InvalidIncidentStatusTransitionException.class)
    public ProblemDetail handleInvalidIncidentStatusTransition(
        InvalidIncidentStatusTransitionException exception
    ) {
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            exception.getMessage()
        );
    }

    @ExceptionHandler(MonitoredServiceNameAlreadyExistsException.class)
    public ProblemDetail handleMonitoredServiceNameAlreadyExists(
        MonitoredServiceNameAlreadyExistsException exception
    ) {
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            exception.getMessage()
        );
    }

    @ExceptionHandler(TeamNameAlreadyExistsException.class)
    public ProblemDetail handleTeamNameAlreadyExists(
        TeamNameAlreadyExistsException exception
    ) {
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            exception.getMessage()
        );
    }

    @ExceptionHandler(MonitoredServiceNotFoundException.class)
    public ProblemDetail handleMonitoredServiceNotFound(
        MonitoredServiceNotFoundException exception
    ) {
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            exception.getMessage()
        );
    }
}
