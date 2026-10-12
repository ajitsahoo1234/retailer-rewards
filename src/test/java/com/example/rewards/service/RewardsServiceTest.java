package com.example.rewards.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import com.example.rewards.RewardCalculator;
import com.example.rewards.exception.CustomerNotFoundException;
import com.example.rewards.model.Customer;
import com.example.rewards.model.Purchase;
import com.example.rewards.repository.RewardsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/** Tests aggregation independently of HTTP and the database. */
@ExtendWith(MockitoExtension.class)
public class RewardsServiceTest {
  @Mock private RewardsRepository repository;
  @Spy private RewardCalculator calculator = new RewardCalculator();
  @InjectMocks private RewardsService service;

  /** Verifies separate transactions, multiple customers, and zero-filled months. */
  @Test
  void aggregatesWithoutCombiningSpending() {
    when(repository.customers()).thenReturn(List.of(new Customer(1, "A"), new Customer(2, "B")));
    when(repository.purchases(LocalDate.of(2025, 12, 1), LocalDate.of(2026, 3, 1), null))
        .thenReturn(
            List.of(
                new Purchase(1, new BigDecimal("50"), LocalDate.of(2025, 12, 5)),
                new Purchase(1, new BigDecimal("50"), LocalDate.of(2025, 12, 6)),
                new Purchase(1, new BigDecimal("120"), LocalDate.of(2026, 1, 5)),
                new Purchase(2, new BigDecimal("150"), LocalDate.of(2026, 2, 5))));
    var result = service.rewards(YearMonth.of(2025, 12), null);
    assertEquals(0L, result.get(0).monthlyRewards().get("2025-12"));
    assertEquals(90, result.get(0).totalRewards());
    assertEquals(150, result.get(1).totalRewards());
    assertEquals(
        List.of("2025-12", "2026-01", "2026-02"),
        List.copyOf(result.get(0).monthlyRewards().keySet()));
  }

  /** Anchors the default window to the latest stored purchase. */
  @Test
  void defaultsToLatestData() {
    when(repository.latestDate()).thenReturn(LocalDate.of(2026, 4, 5));
    when(repository.customers()).thenReturn(List.of(new Customer(1, "A")));
    when(repository.purchases(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 5, 1), null))
        .thenReturn(List.of());
    assertEquals(0, service.rewards(null, null).get(0).totalRewards());
    verify(repository).purchases(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 5, 1), null);
  }

  /** Handles an empty database without failing. */
  @Test
  void emptyDatabase() {
    when(repository.latestDate()).thenReturn(null);
    when(repository.customers()).thenReturn(List.of());
    assertEquals(List.of(), service.rewards(null, null));
  }

  /** Rejects a customer absent from the customer table. */
  @Test
  void unknownCustomer() {
    when(repository.customers()).thenReturn(List.of());
    assertThrows(
        CustomerNotFoundException.class, () -> service.rewards(YearMonth.of(2026, 1), 999L));
  }

  /** Rejects nonpositive customer IDs. */
  @Test
  void invalidCustomer() {
    assertThrows(IllegalArgumentException.class, () -> service.rewards(YearMonth.of(2026, 1), 0L));
  }
}
