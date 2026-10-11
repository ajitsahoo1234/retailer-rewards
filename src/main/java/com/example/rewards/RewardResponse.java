package com.example.rewards;

import java.util.Map;

/** Contains three ordered YYYY-MM totals and their sum for one customer. */
public record RewardResponse(
    long customerId, String customerName, Map<String, Long> monthlyRewards, long totalRewards) {}
