package com.example.rewards.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import com.example.rewards.RewardResponse;
import com.example.rewards.exception.CustomerNotFoundException;
import com.example.rewards.service.RewardsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Tests controller delegation and month validation without starting Spring. */
@ExtendWith(MockitoExtension.class)
public class RewardsControllerTest {

    @Mock
    private RewardsService service;

    @InjectMocks
    private RewardsController controller;

    /** Returns reports for multiple customers for a supplied start month. */
    @Test
    void allShouldReturnRewardsForAllCustomers() {
        YearMonth start = YearMonth.of(2026, 1);

        RewardResponse amit = new RewardResponse(
                101L,
                "Amit",
                Map.of("2026-01", 120L, "2026-02", 120L, "2026-03", 120L),
                360L);

        RewardResponse rahul = new RewardResponse(
                102L,
                "Rahul",
                Map.of("2026-01", 190L, "2026-02", 190L, "2026-03", 190L),
                570L);

        List<RewardResponse> expected = List.of(amit, rahul);

        when(service.rewards(start, null)).thenReturn(expected);

        List<RewardResponse> actual = controller.all("2026-01");

        assertEquals(expected, actual);
        verify(service).rewards(start, null);
    }

    /** Passes a null month so the service can select the default period. */
    @Test
    void allShouldDelegateDefaultPeriodToService() {
        when(service.rewards(null, null)).thenReturn(List.of());

        List<RewardResponse> actual = controller.all(null);

        assertEquals(List.of(), actual);
        verify(service).rewards(null, null);
    }

    /** Returns an empty list when the service finds no customers. */
    @Test
    void allShouldReturnEmptyListWhenNoCustomersExist() {
        YearMonth start = YearMonth.of(2026, 1);

        when(service.rewards(start, null)).thenReturn(List.of());

        List<RewardResponse> actual = controller.all("2026-01");

        assertEquals(List.of(), actual);
        verify(service).rewards(start, null);
    }

    /** Returns the requested customer's report for the supplied period. */
    @Test
    void oneShouldReturnRewardsForRequestedCustomer() {
        YearMonth start = YearMonth.of(2026, 1);
        RewardResponse expected = new RewardResponse(
                101L,
                "Amit",
                Map.of("2026-01", 120L, "2026-02", 120L, "2026-03", 120L),
                360L);

        when(service.rewards(start, 101L)).thenReturn(List.of(expected));

        RewardResponse actual = controller.one(101L, "2026-01");

        assertEquals(expected, actual);
        verify(service).rewards(start, 101L);
    }

    /** Allows the service to choose the default period for one customer. */
    @Test
    void oneShouldDelegateDefaultPeriodToService() {
        RewardResponse expected = new RewardResponse(
                101L,
                "Amit",
                Map.of("2026-02", 120L, "2026-03", 120L, "2026-04", 90L),
                330L);

        when(service.rewards(null, 101L)).thenReturn(List.of(expected));

        RewardResponse actual = controller.one(101L, null);

        assertEquals(expected, actual);
        verify(service).rewards(null, 101L);
    }

    /** Rejects incorrectly formatted months before invoking the service. */
    @ParameterizedTest
    @ValueSource(strings = {
            "",
            " ",
            "2026-1",
            "26-01",
            "January",
            "2026/01",
            "2026-01-01"
    })
    void allShouldRejectInvalidMonthFormat(String startMonth) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> controller.all(startMonth));

        assertEquals("startMonth must use YYYY-MM", exception.getMessage());
        verifyNoInteractions(service);
    }

    /** Rejects nonexistent months even when the input matches YYYY-MM. */
    @ParameterizedTest
    @ValueSource(strings = {"2026-00", "2026-13"})
    void allShouldRejectInvalidCalendarMonth(String startMonth) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> controller.all(startMonth));

        assertEquals(
                "startMonth must be a valid YYYY-MM",
                exception.getMessage());
        assertEquals(
                DateTimeParseException.class,
                exception.getCause().getClass());
        verifyNoInteractions(service);
    }

    /** Applies month validation to the single-customer endpoint as well. */
    @Test
    void oneShouldRejectInvalidMonthBeforeCallingService() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> controller.one(101L, "2026-13"));

        assertEquals(
                "startMonth must be a valid YYYY-MM",
                exception.getMessage());
        verifyNoInteractions(service);
    }

    /** Propagates the missing-customer exception to the exception handler. */
    @Test
    void oneShouldPropagateCustomerNotFoundException() {
        YearMonth start = YearMonth.of(2026, 1);
        CustomerNotFoundException expected =
                new CustomerNotFoundException(999L);

        when(service.rewards(start, 999L)).thenThrow(expected);

        CustomerNotFoundException actual = assertThrows(
                CustomerNotFoundException.class,
                () -> controller.one(999L, "2026-01"));

        assertSame(expected, actual);
        verify(service).rewards(start, 999L);
    }

    /** Propagates customer-ID validation failures raised by the service. */
    @ParameterizedTest
    @ValueSource(longs = {0L, -1L})
    void oneShouldPropagateInvalidCustomerId(long customerId) {
        YearMonth start = YearMonth.of(2026, 1);
        IllegalArgumentException expected =
                new IllegalArgumentException("Customer ID must be positive");

        when(service.rewards(start, customerId)).thenThrow(expected);

        IllegalArgumentException actual = assertThrows(
                IllegalArgumentException.class,
                () -> controller.one(customerId, "2026-01"));

        assertSame(expected, actual);
        verify(service).rewards(start, customerId);
    }
}