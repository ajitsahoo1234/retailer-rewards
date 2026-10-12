package com.example.rewards.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Translates failures into consistent HTTP problem responses. */
@RestControllerAdvice
public class ApiExceptionHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

  /** Maps unknown customers to HTTP 404. */
  @ExceptionHandler(CustomerNotFoundException.class)
  public ProblemDetail notFound(CustomerNotFoundException exception) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
  }

  /** Maps invalid request values to HTTP 400. */
  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail invalid(IllegalArgumentException exception) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
  }

  /** Reports nonnumeric IDs without exposing framework diagnostics. */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ProblemDetail wrongType(MethodArgumentTypeMismatchException exception) {
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST, "Customer ID must be a positive integer");
  }

  /** Logs unexpected failures and hides internal details from clients. */
  @ExceptionHandler(Exception.class)
  public ProblemDetail unexpected(Exception exception) {
    LOGGER.error("Reward report failed", exception);
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.INTERNAL_SERVER_ERROR, "Unable to calculate rewards");
  }
}
