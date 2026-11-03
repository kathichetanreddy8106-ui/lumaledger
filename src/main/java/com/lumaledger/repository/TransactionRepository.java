package com.lumaledger.repository;

import com.lumaledger.model.Transaction;
import com.lumaledger.model.TransactionType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class TransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Transaction> rowMapper = (rs, rowNum) -> {
        Transaction transaction = new Transaction();
        transaction.setId(rs.getLong("id"));

        long categoryId = rs.getLong("category_id");
        transaction.setCategoryId(rs.wasNull() ? null : categoryId);

        transaction.setType(
                TransactionType.valueOf(rs.getString("type"))
        );
        transaction.setAmount(rs.getBigDecimal("amount"));
        transaction.setDescription(rs.getString("description"));

        Date transactionDate = rs.getDate("transaction_date");
        transaction.setTransactionDate(
                transactionDate != null ? transactionDate.toLocalDate() : null
        );

        Timestamp createdAt = rs.getTimestamp("created_at");
        transaction.setCreatedAt(
                createdAt != null ? createdAt.toLocalDateTime() : null
        );

        return transaction;
    };

    public TransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Transaction create(Transaction transaction) {
        String sql = """
                INSERT INTO transactions
                    (category_id, type, amount, description, transaction_date)
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS
            );

            if (transaction.getCategoryId() == null) {
                ps.setNull(1, java.sql.Types.BIGINT);
            } else {
                ps.setLong(1, transaction.getCategoryId());
            }

            ps.setString(2, transaction.getType().name());
            ps.setBigDecimal(3, transaction.getAmount());
            ps.setString(4, transaction.getDescription());
            ps.setDate(5, Date.valueOf(transaction.getTransactionDate()));

            return ps;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId == null) {
            throw new IllegalStateException(
                    "Database did not return the transaction ID"
            );
        }

        transaction.setId(generatedId.longValue());
        return findById(transaction.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Created transaction could not be retrieved"
                ));
    }

    public Optional<Transaction> findById(Long id) {
        String sql = "SELECT * FROM transactions WHERE id = ?";

        List<Transaction> results = jdbcTemplate.query(
                sql, rowMapper, id
        );

        return results.stream().findFirst();
    }

    public List<Transaction> findAll() {
        String sql = """
                SELECT * FROM transactions
                ORDER BY transaction_date DESC, id DESC
                """;

        return jdbcTemplate.query(sql, rowMapper);
    }

    public boolean update(Long id, Transaction transaction) {
        String sql = """
                UPDATE transactions
                SET category_id = ?, type = ?, amount = ?,
                    description = ?, transaction_date = ?
                WHERE id = ?
                """;

        int updated = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);

            if (transaction.getCategoryId() == null) {
                ps.setNull(1, java.sql.Types.BIGINT);
            } else {
                ps.setLong(1, transaction.getCategoryId());
            }

            ps.setString(2, transaction.getType().name());
            ps.setBigDecimal(3, transaction.getAmount());
            ps.setString(4, transaction.getDescription());
            ps.setDate(5, Date.valueOf(transaction.getTransactionDate()));
            ps.setLong(6, id);

            return ps;
        });

        return updated > 0;
    }

    public boolean deleteById(Long id) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    public List<Transaction> search(
            LocalDate from,
            LocalDate to,
            TransactionType type,
            String description
    ) {
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM transactions WHERE 1 = 1"
        );

        java.util.List<Object> params = new java.util.ArrayList<>();

        if (from != null) {
            sql.append(" AND transaction_date >= ?");
            params.add(Date.valueOf(from));
        }

        if (to != null) {
            sql.append(" AND transaction_date <= ?");
            params.add(Date.valueOf(to));
        }

        if (type != null) {
            sql.append(" AND type = ?");
            params.add(type.name());
        }

        if (description != null && !description.isBlank()) {
            sql.append(" AND description LIKE ?");
            params.add("%" + description.trim() + "%");
        }

        sql.append(" ORDER BY transaction_date DESC, id DESC");

        return jdbcTemplate.query(
                sql.toString(), rowMapper, params.toArray()
        );
    }

    public BigDecimal getMonthlyTotal(
            TransactionType type,
            int year,
            int month
    ) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate endExclusive = start.plusMonths(1);

        String sql = """
                SELECT COALESCE(SUM(amount), 0)
                FROM transactions
                WHERE type = ?
                  AND transaction_date >= ?
                  AND transaction_date < ?
                """;

        return jdbcTemplate.queryForObject(
                sql,
                BigDecimal.class,
                type.name(),
                Date.valueOf(start),
                Date.valueOf(endExclusive)
        );
    }
}
