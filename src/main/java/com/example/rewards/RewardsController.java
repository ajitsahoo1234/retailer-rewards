package com.example.rewards;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Exposes read-only reward reports through JSON REST endpoints. */
@RestController
@RequestMapping("/api/rewards")
public class RewardsController {
  private final RewardsService service;

  /** Receives the reward reporting service. */
  public RewardsController(RewardsService service) {
    this.service = service;
  }

  /** Lists rewards for every customer for the requested or default window. */
  @GetMapping
  public List<RewardResponse> all(@RequestParam(required = false) String startMonth) {
    return service.rewards(parse(startMonth), null);
  }

  /** Returns a single customer's report, or a 404 for an unknown customer. */
  @GetMapping("/{customerId}")
  public RewardResponse one(
      @PathVariable long customerId, @RequestParam(required = false) String startMonth) {
    return service.rewards(parse(startMonth), customerId).get(0);
  }

  /** Validates the exact YYYY-MM input format before parsing it. */
  private YearMonth parse(String value) {
    if (value == null) {
      return null;
    }
    try {
      if (!value.matches("[0-9]{4}-[0-9]{2}")) {
        throw new IllegalArgumentException("startMonth must use YYYY-MM");
      }
      return YearMonth.parse(value);
    } catch (DateTimeParseException exception) {
      throw new IllegalArgumentException("startMonth must be a valid YYYY-MM", exception);
    }
  }
}
