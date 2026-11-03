
package com.lumaledger;

import com.lumaledger.model.Transaction;
import com.lumaledger.model.TransactionType;
import com.lumaledger.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TransactionRepositoryTests {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void transactionCrudSearchAndMonthlyTotalsWork() {
        Long categoryId = null;
        Long firstId = null;
        Long secondId = null;
        Long previousMonthId = null;

        String categoryName = "Repository Test " + UUID.randomUUID();
        LocalDate today = LocalDate.now();
        LocalDate previousMonth = today.minusMonths(1);

        try {
            // Create a unique expense category for this test.
            jdbcTemplate.update(
                    "INSERT INTO categories (name, type) VALUES (?, ?)",
                    categoryName, "EXPENSE"
            );

            categoryId = jdbcTemplate.queryForObject(
                    "SELECT id FROM categories WHERE name = ? AND type = ?",
                    Long.class, categoryName, "EXPENSE"
            );

            assertNotNull(categoryId);

            // Create the first transaction.
            Transaction first = new Transaction(
                    null,
                    categoryId,
                    TransactionType.EXPENSE,
                    new BigDecimal("125.50"),
                    "Repository test groceries",
                    today,
                    null
            );

            Transaction createdFirst = transactionRepository.create(first);
            firstId = createdFirst.getId();

            assertNotNull(firstId);
            assertEquals(
                    0,
                    createdFirst.getAmount().compareTo(new BigDecimal("125.50"))
            );
            assertEquals(
                    "Repository test groceries",
                    createdFirst.getDescription()
            );

            // Create the second transaction.
            Transaction second = new Transaction(
                    null,
                    categoryId,
                    TransactionType.EXPENSE,
                    new BigDecimal("74.50"),
                    "Repository test transport",
                    today,
                    null
            );

            Transaction createdSecond = transactionRepository.create(second);
            secondId = createdSecond.getId();

            assertNotNull(secondId);

            // Use final copies in lambdas because the cleanup variables
            // are reassigned later in this test.
            final Long testFirstId = firstId;
            final Long testSecondId = secondId;

            // Verify read operations.
            assertTrue(
                    transactionRepository.findById(testFirstId).isPresent()
            );

            assertTrue(
                    transactionRepository.findAll().stream()
                            .anyMatch(t -> testFirstId.equals(t.getId()))
            );

            // Verify inclusive date boundaries and description filtering.
            List<Transaction> searchResults = transactionRepository.search(
                    today,
                    today,
                    TransactionType.EXPENSE,
                    "test groceries"
            );

            assertTrue(
                    searchResults.stream()
                            .anyMatch(t -> testFirstId.equals(t.getId()))
            );

            assertFalse(
                    searchResults.stream()
                            .anyMatch(t -> testSecondId.equals(t.getId()))
            );

            // Verify updating a transaction.
            Transaction updated = new Transaction(
                    testFirstId,
                    categoryId,
                    TransactionType.EXPENSE,
                    new BigDecimal("150.00"),
                    "Repository test updated",
                    today,
                    null
            );

            assertTrue(
                    transactionRepository.update(testFirstId, updated)
            );

            Transaction retrieved = transactionRepository
                    .findById(testFirstId)
                    .orElseThrow();

            assertEquals(
                    0,
                    retrieved.getAmount().compareTo(new BigDecimal("150.00"))
            );
            assertEquals(
                    "Repository test updated",
                    retrieved.getDescription()
            );

            // Create a transaction in the previous month.
            Transaction older = new Transaction(
                    null,
                    categoryId,
                    TransactionType.EXPENSE,
                    new BigDecimal("900.00"),
                    "Repository test previous month",
                    previousMonth,
                    null
            );

            previousMonthId = transactionRepository.create(older).getId();
            assertNotNull(previousMonthId);

            // Verify the monthly total for the test's own transactions.
            // This avoids relying on unrelated transactions in the database.
            BigDecimal monthlyTotal = jdbcTemplate.queryForObject(
                    """
                    SELECT COALESCE(SUM(amount), 0)
                    FROM transactions
                    WHERE id IN (?, ?)
                      AND type = ?
                      AND transaction_date >= ?
                      AND transaction_date < ?
                    """,
                    BigDecimal.class,
                    testFirstId,
                    testSecondId,
                    TransactionType.EXPENSE.name(),
                    java.sql.Date.valueOf(today.withDayOfMonth(1)),
                    java.sql.Date.valueOf(
                            today.withDayOfMonth(1).plusMonths(1)
                    )
            );

            assertNotNull(monthlyTotal);

            BigDecimal repositoryMonthlyTotal =
                    transactionRepository.getMonthlyTotal(
                            TransactionType.EXPENSE,
                            today.getYear(),
                            today.getMonthValue()
                    );

            assertEquals(
                    0,
                    repositoryMonthlyTotal.compareTo(
                            monthlyTotal.add(
                                    jdbcTemplate.queryForObject(
                                            """
                                            SELECT COALESCE(SUM(amount), 0)
                                            FROM transactions
                                            WHERE type = ?
                                              AND transaction_date >= ?
                                              AND transaction_date < ?
                                              AND id NOT IN (?, ?)
                                            """,
                                            BigDecimal.class,
                                            TransactionType.EXPENSE.name(),
                                            java.sql.Date.valueOf(
                                                    today.withDayOfMonth(1)
                                            ),
                                            java.sql.Date.valueOf(
                                                    today.withDayOfMonth(1)
                                                            .plusMonths(1)
                                            ),
                                            testFirstId,
                                            testSecondId
                                    )
                            )
                    ),
                    "Monthly total should include the test transactions and other current-month expenses"
            );

            // Verify deletion and repeated deletion behavior.
            assertTrue(
                    transactionRepository.deleteById(testSecondId)
            );

            assertFalse(
                    transactionRepository.findById(testSecondId).isPresent()
            );

            assertFalse(
                    transactionRepository.deleteById(testSecondId)
            );

            secondId = null;

        } finally {
            // Clean up test data even when an assertion fails.
            if (firstId != null) {
                transactionRepository.deleteById(firstId);
            }

            if (secondId != null) {
                transactionRepository.deleteById(secondId);
            }

            if (previousMonthId != null) {
                transactionRepository.deleteById(previousMonthId);
            }

            if (categoryId != null) {
                jdbcTemplate.update(
                        "DELETE FROM categories WHERE id = ?",
                        categoryId
                );
            }
        }
    }
}
