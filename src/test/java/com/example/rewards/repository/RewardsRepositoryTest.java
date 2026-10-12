package com.example.rewards.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

import com.example.rewards.model.Customer;
import com.example.rewards.model.Purchase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/** Tests repository queries, parameter binding, and JDBC row mapping. */
@ExtendWith(MockitoExtension.class)
public class RewardsRepositoryTest {

    private static final String CUSTOMERS_SQL =
            "SELECT id, name FROM customer ORDER BY id";

    private static final String PURCHASES_SQL =
            "SELECT customer_id, amount, transaction_date FROM purchase_transaction "
                    + "WHERE transaction_date >= ? AND transaction_date < ?";

    private static final String LATEST_DATE_SQL =
            "SELECT MAX(transaction_date) FROM purchase_transaction";

    @Mock
    private JdbcTemplate jdbc;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private RewardsRepository repository;

    /** Converts multiple customer rows into Customer records. */
    @Test
    void customersShouldMapDatabaseRows() throws Exception {
        when(resultSet.getLong("id")).thenReturn(101L, 102L);
        when(resultSet.getString("name")).thenReturn("Amit", "Rahul");

        when(jdbc.query(
                eq(CUSTOMERS_SQL),
                ArgumentMatchers.<RowMapper<Customer>>any()))
                .thenAnswer(invocation -> {
                    RowMapper<Customer> mapper = invocation.getArgument(1);

                    return List.of(
                            mapper.mapRow(resultSet, 0),
                            mapper.mapRow(resultSet, 1));
                });

        List<Customer> actual = repository.customers();

        assertEquals(
                List.of(
                        new Customer(101L, "Amit"),
                        new Customer(102L, "Rahul")),
                actual);

        verify(jdbc).query(
                eq(CUSTOMERS_SQL),
                ArgumentMatchers.<RowMapper<Customer>>any());
    }

    /** Returns an empty list when the customer query finds no rows. */
    @Test
    void customersShouldReturnEmptyList() {
        when(jdbc.query(
                eq(CUSTOMERS_SQL),
                ArgumentMatchers.<RowMapper<Customer>>any()))
                .thenReturn(List.of());

        List<Customer> actual = repository.customers();

        assertEquals(List.of(), actual);
    }

    /** Binds both date parameters and maps purchases for all customers. */
    @Test
    void purchasesShouldMapRowsWithoutCustomerFilter() throws Exception {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 4, 1);
        LocalDate purchaseDate = LocalDate.of(2026, 1, 5);
        BigDecimal amount = new BigDecimal("120.50");

        when(resultSet.getLong("customer_id")).thenReturn(101L);
        when(resultSet.getBigDecimal("amount")).thenReturn(amount);
        when(resultSet.getDate("transaction_date"))
                .thenReturn(Date.valueOf(purchaseDate));

        when(jdbc.query(
                eq(PURCHASES_SQL),
                ArgumentMatchers.<RowMapper<Purchase>>any(),
                eq(start),
                eq(end)))
                .thenAnswer(invocation -> {
                    RowMapper<Purchase> mapper = invocation.getArgument(1);

                    return List.of(mapper.mapRow(resultSet, 0));
                });

        List<Purchase> actual = repository.purchases(start, end, null);

        assertEquals(
                List.of(new Purchase(101L, amount, purchaseDate)),
                actual);

        verify(jdbc).query(
                eq(PURCHASES_SQL),
                ArgumentMatchers.<RowMapper<Purchase>>any(),
                eq(start),
                eq(end));
    }

    /** Adds the customer filter and binds its ID after the date parameters. */
    @Test
    void purchasesShouldFilterByCustomerId() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 4, 1);
        String sql = PURCHASES_SQL + " AND customer_id = ?";

        List<Purchase> expected = List.of(
                new Purchase(
                        101L,
                        new BigDecimal("120.00"),
                        LocalDate.of(2026, 1, 5)),
                new Purchase(
                        101L,
                        new BigDecimal("80.00"),
                        LocalDate.of(2026, 2, 10)));

        when(jdbc.query(
                eq(sql),
                ArgumentMatchers.<RowMapper<Purchase>>any(),
                eq(start),
                eq(end),
                eq(101L)))
                .thenReturn(expected);

        List<Purchase> actual = repository.purchases(start, end, 101L);

        assertEquals(expected, actual);

        verify(jdbc).query(
                eq(sql),
                ArgumentMatchers.<RowMapper<Purchase>>any(),
                eq(start),
                eq(end),
                eq(101L));
    }

    /** Returns an empty list when no purchases match the query. */
    @Test
    void purchasesShouldReturnEmptyList() {
        LocalDate start = LocalDate.of(2030, 1, 1);
        LocalDate end = LocalDate.of(2030, 4, 1);

        when(jdbc.query(
                eq(PURCHASES_SQL),
                ArgumentMatchers.<RowMapper<Purchase>>any(),
                eq(start),
                eq(end)))
                .thenReturn(List.of());

        List<Purchase> actual = repository.purchases(start, end, null);

        assertEquals(List.of(), actual);
    }

    /** Returns the latest transaction date supplied by the aggregate query. */
    @Test
    void latestDateShouldReturnMaximumDate() {
        LocalDate expected = LocalDate.of(2026, 4, 5);

        when(jdbc.queryForObject(LATEST_DATE_SQL, LocalDate.class))
                .thenReturn(expected);

        LocalDate actual = repository.latestDate();

        assertEquals(expected, actual);
        verify(jdbc).queryForObject(LATEST_DATE_SQL, LocalDate.class);
    }

    /** Returns null when MAX(transaction_date) produces SQL NULL. */
    @Test
    void latestDateShouldReturnNullWhenNoPurchasesExist() {
        when(jdbc.queryForObject(LATEST_DATE_SQL, LocalDate.class))
                .thenReturn(null);

        LocalDate actual = repository.latestDate();

        assertNull(actual);
    }

    /** Allows database failures to propagate to the caller. */
    @Test
    void purchasesShouldPropagateDatabaseFailure() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 4, 1);

        DataAccessResourceFailureException expected =
                new DataAccessResourceFailureException(
                        "Database connection unavailable");

        when(jdbc.query(
                eq(PURCHASES_SQL),
                ArgumentMatchers.<RowMapper<Purchase>>any(),
                eq(start),
                eq(end)))
                .thenThrow(expected);

        DataAccessResourceFailureException actual = assertThrows(
                DataAccessResourceFailureException.class,
                () -> repository.purchases(start, end, null));

        assertSame(expected, actual);
    }
}