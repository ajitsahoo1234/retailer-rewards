package com.example.rewards;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Tests the values exposed by the RewardResponse record. */
public class RewardResponseTest {

    /** Returns customer details, monthly points, and the supplied total. */
    @Test
    void shouldReturnRewardDetails() {
        Map<String, Long> monthlyRewards = new LinkedHashMap<>();
        monthlyRewards.put("2026-01", 120L);
        monthlyRewards.put("2026-02", 150L);
        monthlyRewards.put("2026-03", 0L);

        RewardResponse response =
                new RewardResponse(101L, "Amit", monthlyRewards, 270L);

        assertEquals(101L, response.customerId());
        assertEquals("Amit", response.customerName());
        assertEquals(monthlyRewards, response.monthlyRewards());
        assertEquals(270L, response.totalRewards());
    }

    /** Exposes month order when the supplied map preserves insertion order. */
    @Test
    void shouldPreserveOrderOfSuppliedLinkedHashMap() {
        Map<String, Long> monthlyRewards = new LinkedHashMap<>();
        monthlyRewards.put("2025-12", 90L);
        monthlyRewards.put("2026-01", 120L);
        monthlyRewards.put("2026-02", 150L);

        RewardResponse response =
                new RewardResponse(101L, "Amit", monthlyRewards, 360L);

        assertEquals(
                List.of("2025-12", "2026-01", "2026-02"),
                List.copyOf(response.monthlyRewards().keySet()));
    }

    /** Represents a customer whose monthly and overall points are zero. */
    @Test
    void shouldRepresentZeroRewards() {
        Map<String, Long> monthlyRewards = Map.of(
                "2026-01", 0L,
                "2026-02", 0L,
                "2026-03", 0L);

        RewardResponse response =
                new RewardResponse(108L, "No Purchases", monthlyRewards, 0L);

        assertEquals(monthlyRewards, response.monthlyRewards());
        assertEquals(0L, response.totalRewards());
    }

    /** Treats responses with identical component values as equal. */
    @Test
    void shouldBeEqualWhenAllValuesMatch() {
        RewardResponse first = new RewardResponse(
                101L,
                "Amit",
                Map.of("2026-01", 120L, "2026-02", 150L, "2026-03", 0L),
                270L);

        RewardResponse second = new RewardResponse(
                101L,
                "Amit",
                Map.of("2026-01", 120L, "2026-02", 150L, "2026-03", 0L),
                270L);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    /** Distinguishes responses containing different reward values. */
    @Test
    void shouldNotBeEqualWhenRewardValuesDiffer() {
        RewardResponse first = new RewardResponse(
                101L,
                "Amit",
                Map.of("2026-01", 120L, "2026-02", 150L, "2026-03", 0L),
                270L);

        RewardResponse second = new RewardResponse(
                101L,
                "Amit",
                Map.of("2026-01", 120L, "2026-02", 150L, "2026-03", 90L),
                360L);

        assertNotEquals(first, second);
    }
}