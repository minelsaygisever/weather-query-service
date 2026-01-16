package com.minelsaygisever.weatherqueryservice.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleValidationException(ConstraintViolationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Validation Error");
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        return problemDetail;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParams(MissingServletRequestParameterException ex) {
        String detail = "Required parameter '" + ex.getParameterName() + "' is missing.";

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problemDetail.setTitle("Missing Parameter");
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        return problemDetail;
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ProblemDetail handleExternalServiceException(ExternalServiceException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, ex.getMessage());
        problemDetail.setTitle("External Service Error");
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        return problemDetail;
    }
}
