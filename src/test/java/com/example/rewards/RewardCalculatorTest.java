package com.example.rewards;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Verifies tier boundaries, fractional policy, and invalid amounts. */
class RewardCalculatorTest {
  private final RewardCalculator calculator = new RewardCalculator();

  /** Checks independent examples against the complete-dollar policy. */
  @ParameterizedTest
  @CsvSource({
    "0,0",
    "40,0",
    "49.99,0",
    "50,0",
    "50.99,0",
    "51,1",
    "75,25",
    "99.99,49",
    "100,50",
    "100.99,50",
    "101,52",
    "120,90",
    "150,150",
    "200,250",
    "9999999999.99,19999999848"
  })
  void calculatesPoints(String amount, long expected) {
    assertEquals(expected, calculator.calculate(new BigDecimal(amount)));
  }

  /** Rejects null amounts. */
  @Test
  void rejectsNull() {
    assertThrows(IllegalArgumentException.class, () -> calculator.calculate(null));
  }

  /** Rejects negatives and values exceeding the database amount limit. */
  @ParameterizedTest
  @CsvSource({"-0.01", "-20", "10000000000", "1E30"})
  void rejectsInvalidAmount(String amount) {
    assertThrows(
        IllegalArgumentException.class, () -> calculator.calculate(new BigDecimal(amount)));
  }
}
