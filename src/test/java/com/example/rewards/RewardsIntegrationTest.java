package com.example.rewards;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Exercises real controllers, services, JDBC, H2, and SQL initialization. */
@SpringBootTest
@AutoConfigureMockMvc
class RewardsIntegrationTest {
  @Autowired private MockMvc mvc;
  @Autowired private JdbcTemplate jdbc;

  /** Checks independent expected totals for all eight SQL-backed customers. */
  @Test
  void multipleCustomers() throws Exception {
    mvc.perform(get("/api/rewards").param("startMonth", "2026-01"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(8))
        .andExpect(jsonPath("$[0].monthlyRewards['2026-01']").value(120))
        .andExpect(jsonPath("$[0].totalRewards").value(360))
        .andExpect(jsonPath("$[1].totalRewards").value(570))
        .andExpect(jsonPath("$[2].totalRewards").value(825))
        .andExpect(jsonPath("$[3].monthlyRewards['2026-02']").value(0))
        .andExpect(jsonPath("$[3].totalRewards").value(100))
        .andExpect(jsonPath("$[4].totalRewards").value(150))
        .andExpect(jsonPath("$[5].totalRewards").value(6744))
        .andExpect(jsonPath("$[6].totalRewards").value(0))
        .andExpect(jsonPath("$[7].totalRewards").value(0));
    assertEquals(
        72, jdbc.queryForObject("SELECT COUNT(*) FROM purchase_transaction", Integer.class));
  }

  /** Confirms the window includes December and January without hardcoded months. */
  @Test
  void crossesYear() throws Exception {
    mvc.perform(get("/api/rewards/101").param("startMonth", "2025-12"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.monthlyRewards['2025-12']").value(90))
        .andExpect(jsonPath("$.monthlyRewards['2026-01']").value(120))
        .andExpect(jsonPath("$.monthlyRewards['2026-02']").value(120))
        .andExpect(jsonPath("$.totalRewards").value(330));
  }

  /** Confirms the interval ends before the fourth month. */
  @Test
  void defaultWindow() throws Exception {
    mvc.perform(get("/api/rewards/101"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.monthlyRewards['2026-02']").value(120))
        .andExpect(jsonPath("$.monthlyRewards['2026-04']").value(90))
        .andExpect(jsonPath("$.totalRewards").value(330));
  }

  /** Returns zero months for a period outside the fixture. */
  @Test
  void emptyWindow() throws Exception {
    mvc.perform(get("/api/rewards/101").param("startMonth", "2030-01"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.monthlyRewards['2030-03']").value(0))
        .andExpect(jsonPath("$.totalRewards").value(0));
  }

  /** Rejects malformed, impossible, and unsupported months. */
  @ParameterizedTest
  @ValueSource(
      strings = {"", "2026-1", "January", "2026-00", "2026-13", "0000-01", "9999-12", "2026-01-01"})
  void badMonth(String month) throws Exception {
    mvc.perform(get("/api/rewards").param("startMonth", month))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
  }

  /** Rejects invalid customer path values. */
  @ParameterizedTest
  @ValueSource(strings = {"0", "-1", "abc", "999999999999999999999"})
  void badId(String id) throws Exception {
    mvc.perform(get("/api/rewards/" + id)).andExpect(status().isBadRequest());
  }

  /** Maps unknown customer IDs to a useful HTTP 404. */
  @Test
  void missingCustomer() throws Exception {
    mvc.perform(get("/api/rewards/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("Customer not found: 999"));
  }

  /** Enforces the SQL check and foreign-key constraints on invalid fixtures. */
  @Test
  void databaseConstraints() {
    assertThrows(
        DataIntegrityViolationException.class,
        () ->
            jdbc.update("INSERT INTO purchase_transaction VALUES (1000,101,-1,DATE '2026-01-01')"));
    assertThrows(
        DataIntegrityViolationException.class,
        () ->
            jdbc.update(
                "INSERT INTO purchase_transaction VALUES (1001,999,120,DATE '2026-01-01')"));
  }

  /** Includes both edge dates and excludes the day before and the fourth month. */
  @Test
  @Transactional
  void exactDateBoundaries() throws Exception {
    jdbc.update("INSERT INTO purchase_transaction VALUES (2000,108,120,DATE '2025-12-31')");
    jdbc.update("INSERT INTO purchase_transaction VALUES (2001,108,120,DATE '2026-01-01')");
    jdbc.update("INSERT INTO purchase_transaction VALUES (2002,108,150,DATE '2026-03-31')");
    jdbc.update("INSERT INTO purchase_transaction VALUES (2003,108,200,DATE '2026-04-01')");
    mvc.perform(get("/api/rewards/108").param("startMonth", "2026-01"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.monthlyRewards['2026-01']").value(90))
        .andExpect(jsonPath("$.monthlyRewards['2026-03']").value(150))
        .andExpect(jsonPath("$.totalRewards").value(240));
  }
}
