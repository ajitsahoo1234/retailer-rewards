package com.example.rewards;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

/** Calculates tiered rewards using complete dollars from each purchase. */
@Component
public class RewardCalculator {
  /** Returns points; rejects missing, negative, or out-of-range amounts. */
  public long calculate(BigDecimal amount) {
    if (amount == null
        || amount.signum() < 0
        || amount.compareTo(new BigDecimal("9999999999.99")) > 0) {
      throw new IllegalArgumentException("Amount must be between 0 and 9999999999.99");
    }
    long dollars = amount.setScale(0, RoundingMode.DOWN).longValueExact();
    if (dollars <= 50) {
      return 0;
    }
    return dollars <= 100 ? dollars - 50 : 50 + (dollars - 100) * 2;
  }
}
