package com.example.rewards.repository;

import java.time.LocalDate;
import java.util.List;

import com.example.rewards.model.Customer;
import com.example.rewards.model.Purchase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Reads SQL-backed customers and purchases through parameterized queries. */
@Repository
public class RewardsRepository {
  private final JdbcTemplate jdbc;

  /** Receives the application-managed JDBC template. */
  public RewardsRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  /** Returns all customers in stable ID order. */
  public List<Customer> customers() {
    return jdbc.query(
        "SELECT id, name FROM customer ORDER BY id",
        (rs, row) -> new Customer(rs.getLong("id"), rs.getString("name")));
  }

  /** Returns purchases in the half-open date interval, optionally for one customer. */
  public List<Purchase> purchases(LocalDate start, LocalDate end, Long customerId) {
    String sql =
        "SELECT customer_id, amount, transaction_date FROM purchase_transaction "
            + "WHERE transaction_date >= ? AND transaction_date < ?";
    Object[] parameters =
        customerId == null ? new Object[] {start, end} : new Object[] {start, end, customerId};
    if (customerId != null) {
      sql += " AND customer_id = ?";
    }
    return jdbc.query(
        sql,
        (rs, row) ->
            new Purchase(
                rs.getLong("customer_id"),
                rs.getBigDecimal("amount"),
                rs.getDate("transaction_date").toLocalDate()),
        parameters);
  }

  /** Finds the latest available transaction date for the default reporting window. */
  public LocalDate latestDate() {
    return jdbc.queryForObject(
        "SELECT MAX(transaction_date) FROM purchase_transaction", LocalDate.class);
  }
}
