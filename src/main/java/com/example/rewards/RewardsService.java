package com.example.rewards;

import java.time.YearMonth;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Aggregates transaction-level points over three consecutive calendar months. */
@Service
@Transactional(readOnly = true)
public class RewardsService {
  private final RewardsRepository repository;
  private final RewardCalculator calculator;

  /** Receives SQL access and the independently testable reward calculator. */
  public RewardsService(RewardsRepository repository, RewardCalculator calculator) {
    this.repository = repository;
    this.calculator = calculator;
  }

  /** Returns all customers, or one existing customer, with zero-filled months. */
  public List<RewardResponse> rewards(YearMonth requestedStart, Long customerId) {
    if (customerId != null && customerId <= 0) {
      throw new IllegalArgumentException("Customer ID must be positive");
    }
    YearMonth start = requestedStart;
    if (start == null) {
      var latest = repository.latestDate();
      start = (latest == null ? YearMonth.now() : YearMonth.from(latest)).minusMonths(2);
    }
    if (start.getYear() < 1 || start.getYear() > 9998) {
      throw new IllegalArgumentException("startMonth year must be between 0001 and 9998");
    }
    List<Customer> customers = repository.customers();
    if (customerId != null) {
      customers = customers.stream().filter(c -> c.id() == customerId).toList();
      if (customers.isEmpty()) {
        throw new CustomerNotFoundException(customerId);
      }
    }
    Map<Long, List<Purchase>> grouped =
        repository.purchases(start.atDay(1), start.plusMonths(3).atDay(1), customerId).stream()
            .collect(Collectors.groupingBy(Purchase::customerId));
    YearMonth window = start;
    return customers.stream()
        .map(
            customer -> summarize(customer, window, grouped.getOrDefault(customer.id(), List.of())))
        .toList();
  }

  /** Adds each purchase's points to its month, then sums the monthly totals. */
  private RewardResponse summarize(Customer customer, YearMonth start, List<Purchase> purchases) {
    Map<String, Long> months = new LinkedHashMap<>();
    for (int index = 0; index < 3; index++) {
      months.put(start.plusMonths(index).toString(), 0L);
    }
    for (Purchase purchase : purchases) {
      months.merge(
          YearMonth.from(purchase.date()).toString(),
          calculator.calculate(purchase.amount()),
          Math::addExact);
    }
    long total = months.values().stream().reduce(0L, Math::addExact);
    return new RewardResponse(
        customer.id(), customer.name(), Collections.unmodifiableMap(months), total);
  }
}
