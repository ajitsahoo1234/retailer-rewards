package com.example.rewards.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

/** Verifies the Customer record's accessors and value-based equality. */
public class CustomerTest {

    /** Returns the ID and name supplied during construction. */
    @Test
    void shouldReturnCustomerDetails() {
        Customer customer = new Customer(101L, "Amit");

        assertEquals(101L, customer.id());
        assertEquals("Amit", customer.name());
    }

    /** Treats customers with identical component values as equal. */
    @Test
    void shouldBeEqualWhenIdAndNameMatch() {
        Customer first = new Customer(101L, "Amit");
        Customer second = new Customer(101L, "Amit");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    /** Treats customers with different IDs as unequal. */
    @Test
    void shouldNotBeEqualWhenIdsDiffer() {
        Customer first = new Customer(101L, "Amit");
        Customer second = new Customer(102L, "Amit");

        assertNotEquals(first, second);
    }

    /** Treats customers with different names as unequal. */
    @Test
    void shouldNotBeEqualWhenNamesDiffer() {
        Customer first = new Customer(101L, "Amit");
        Customer second = new Customer(101L, "Rahul");

        assertNotEquals(first, second);
    }
}