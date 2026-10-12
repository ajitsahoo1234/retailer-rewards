package com.example.rewards.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/** Verifies Purchase record values and equality behavior. */
public class PurchaseTest {

    /** Returns the customer ID, amount, and date supplied at construction. */
    @Test
    void shouldReturnPurchaseDetails() {
        Purchase purchase = new Purchase(
                101L,
                new BigDecimal("120.50"),
                LocalDate.of(2026, 1, 5));

        assertEquals(101L, purchase.customerId());
        assertEquals(new BigDecimal("120.50"), purchase.amount());
        assertEquals(LocalDate.of(2026, 1, 5), purchase.date());
    }

    /** Treats purchases with identical component values as equal. */
    @Test
    void shouldBeEqualWhenAllValuesMatch() {
        Purchase first = new Purchase(
                101L,
                new BigDecimal("120.00"),
                LocalDate.of(2026, 1, 5));

        Purchase second = new Purchase(
                101L,
                new BigDecimal("120.00"),
                LocalDate.of(2026, 1, 5));

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    /** Includes the customer ID when comparing purchases. */
    @Test
    void shouldNotBeEqualWhenCustomerIdsDiffer() {
        LocalDate date = LocalDate.of(2026, 1, 5);
        BigDecimal amount = new BigDecimal("120.00");

        Purchase first = new Purchase(101L, amount, date);
        Purchase second = new Purchase(102L, amount, date);

        assertNotEquals(first, second);
    }

    /** Includes the purchase amount when comparing purchases. */
    @Test
    void shouldNotBeEqualWhenAmountsDiffer() {
        LocalDate date = LocalDate.of(2026, 1, 5);

        Purchase first = new Purchase(
                101L, new BigDecimal("120.00"), date);

        Purchase second = new Purchase(
                101L, new BigDecimal("150.00"), date);

        assertNotEquals(first, second);
    }

    /** Includes the transaction date when comparing purchases. */
    @Test
    void shouldNotBeEqualWhenDatesDiffer() {
        BigDecimal amount = new BigDecimal("120.00");

        Purchase first = new Purchase(
                101L, amount, LocalDate.of(2026, 1, 5));

        Purchase second = new Purchase(
                101L, amount, LocalDate.of(2026, 1, 6));

        assertNotEquals(first, second);
    }

    /** Documents that BigDecimal equality includes its decimal scale. */
    @Test
    void shouldNotBeEqualWhenAmountScalesDiffer() {
        LocalDate date = LocalDate.of(2026, 1, 5);

        Purchase first = new Purchase(
                101L, new BigDecimal("120.0"), date);

        Purchase second = new Purchase(
                101L, new BigDecimal("120.00"), date);

        assertNotEquals(first, second);
    }
}