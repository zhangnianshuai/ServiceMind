package com.zhangnianshuai.servicemind.api;

import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail>
            handleResponseStatusException(
                ResponseStatusException exception) {

        HttpStatusCode status =
            exception.getStatusCode();

        String detail =
            exception.getReason() == null
                ? "Request could not be completed."
                : exception.getReason();

        ProblemDetail problem =
            ProblemDetail.forStatusAndDetail(
                status,
                detail
            );

        problem.setTitle(
            titleFor(status)
        );

        return ResponseEntity
            .status(status)
            .body(problem);
    }

    @ExceptionHandler(
        MethodArgumentNotValidException.class
    )
    public ResponseEntity<ProblemDetail>
            handleValidation(
                MethodArgumentNotValidException exception) {

        ProblemDetail problem =
            ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Request validation failed."
            );

        problem.setTitle(
            "Invalid request"
        );

        problem.setProperty(
            "errors",
            exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                    error.getField()
                        + ": "
                        + error.getDefaultMessage()
                )
                .toList()
        );

        return ResponseEntity
            .badRequest()
            .body(problem);
    }

    @ExceptionHandler(
        ConstraintViolationException.class
    )
    public ResponseEntity<ProblemDetail>
            handleConstraintViolation(
                ConstraintViolationException exception) {

        ProblemDetail problem =
            ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Request parameter validation failed."
            );

        problem.setTitle(
            "Invalid request"
        );

        return ResponseEntity
            .badRequest()
            .body(problem);
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ProblemDetail>
            handleMalformedRequest(
                Exception exception) {

        ProblemDetail problem =
            ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "The request contains invalid data."
            );

        problem.setTitle(
            "Invalid request"
        );

        return ResponseEntity
            .badRequest()
            .body(problem);
    }

    private static String titleFor(
            HttpStatusCode status) {

        HttpStatus resolved =
            HttpStatus.resolve(
                status.value()
            );

        if (resolved == null) {
            return "Request failed";
        }

        return switch (resolved) {
            case BAD_REQUEST ->
                "Invalid request";
            case UNAUTHORIZED ->
                "Authentication failed";
            case FORBIDDEN ->
                "Access denied";
            case NOT_FOUND ->
                "Resource not found";
            case PAYLOAD_TOO_LARGE ->
                "Payload too large";
            case TOO_MANY_REQUESTS ->
                "Too many requests";
            case BAD_GATEWAY ->
                "Upstream service unavailable";
            default ->
                "Request failed";
        };
    }
}