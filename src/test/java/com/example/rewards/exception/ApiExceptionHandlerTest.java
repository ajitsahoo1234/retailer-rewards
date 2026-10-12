package com.example.rewards.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Tests error response details produced by the global exception handler. */
@ExtendWith(MockitoExtension.class)
public class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Mock
    private MethodArgumentTypeMismatchException typeMismatchException;

    /** Returns a 404 problem containing the missing-customer message. */
    @Test
    void notFoundShouldReturn404WithCustomerMessage() {
        CustomerNotFoundException exception =
                new CustomerNotFoundException(999L);

        ProblemDetail result = handler.notFound(exception);

        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatus());
        assertEquals("Not Found", result.getTitle());
        assertEquals("Customer not found: 999", result.getDetail());
    }

    /** Returns a 400 problem containing the input-validation message. */
    @Test
    void invalidShouldReturn400WithValidationMessage() {
        IllegalArgumentException exception =
                new IllegalArgumentException("startMonth must use YYYY-MM");

        ProblemDetail result = handler.invalid(exception);

        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatus());
        assertEquals("Bad Request", result.getTitle());
        assertEquals("startMonth must use YYYY-MM", result.getDetail());
    }

    /** Returns a clear 400 problem for a customer-ID conversion failure. */
    @Test
    void wrongTypeShouldReturn400WithFriendlyMessage() {
        ProblemDetail result = handler.wrongType(typeMismatchException);

        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatus());
        assertEquals("Bad Request", result.getTitle());
        assertEquals(
                "Customer ID must be a positive integer",
                result.getDetail());
    }

    /** Uses the same safe message regardless of framework diagnostics. */
    @Test
    void wrongTypeShouldNotExposeExceptionDetails() {
        when(typeMismatchException.getMessage())
                .thenReturn("Internal conversion diagnostic");

        // Read the mocked message to compare it with the public response.
        String internalMessage = typeMismatchException.getMessage();

        ProblemDetail result = handler.wrongType(typeMismatchException);

        assertNotNull(result.getDetail());
        assertEquals(
                "Customer ID must be a positive integer",
                result.getDetail());
        assertFalse(result.getDetail().contains(internalMessage));
    }

    /** Returns a generic 500 problem when an unexpected failure occurs. */
    @Test
    void unexpectedShouldReturn500WithGenericMessage() {
        Exception exception =
                new RuntimeException("Database connection failed");

        ProblemDetail result = handler.unexpected(exception);

        assertNotNull(result);
        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                result.getStatus());
        assertEquals("Internal Server Error", result.getTitle());
        assertEquals("Unable to calculate rewards", result.getDetail());
        assertFalse(result.getDetail().contains(exception.getMessage()));
    }
}