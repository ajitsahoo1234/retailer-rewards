package com.example.rewards;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Immutable purchase loaded from SQL; points are calculated per purchase. */
public record Purchase(long customerId, BigDecimal amount, LocalDate date) {}
