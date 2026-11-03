
package com.lumaledger.repository;

import com.lumaledger.model.RecurringPayment;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Repository
public class RecurringPaymentRepository {

    private final JdbcTemplate jdbcTemplate;

    public RecurringPaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<RecurringPayment> rowMapper = (rs, rowNum) -> {
        RecurringPayment payment = new RecurringPayment();

        payment.setId(rs.getLong("id"));

        long categoryId = rs.getLong("category_id");
        payment.setCategoryId(rs.wasNull() ? null : categoryId);

        payment.setTitle(rs.getString("title"));
        payment.setAmount(rs.getBigDecimal("amount"));
        payment.setFrequency(rs.getString("frequency"));

        Date dueDate = rs.getDate("next_due_date");
        payment.setNextDueDate(
                dueDate == null ? null : dueDate.toLocalDate());

        payment.setActive(rs.getBoolean("active"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        payment.setCreatedAt(
                createdAt == null ? null : createdAt.toLocalDateTime());

        return payment;
    };

    public RecurringPayment create(RecurringPayment payment) {
        String sql = """
                INSERT INTO recurring_payments
                    (category_id, title, amount, frequency,
                     next_due_date, active)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);

            if (payment.getCategoryId() == null) {
                ps.setNull(1, Types.BIGINT);
            } else {
                ps.setLong(1, payment.getCategoryId());
            }

            ps.setString(2, payment.getTitle());
            ps.setBigDecimal(3, payment.getAmount());
            ps.setString(4, payment.getFrequency());
            ps.setDate(5, Date.valueOf(payment.getNextDueDate()));
            ps.setBoolean(6, payment.isActive());

            return ps;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId == null) {
            throw new IllegalStateException(
                    "Database did not return a recurring payment ID");
        }

        payment.setId(generatedId.longValue());
        return findById(payment.getId()).orElseThrow(
                () -> new IllegalStateException(
                        "Created recurring payment could not be retrieved"));
    }

    public Optional<RecurringPayment> findById(Long id) {
        String sql = """
                SELECT * FROM recurring_payments WHERE id = ?
                """;

        try {
            return Optional.ofNullable(
                    jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public List<RecurringPayment> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM recurring_payments ORDER BY id",
                rowMapper);
    }

    public List<RecurringPayment> findActive() {
        return jdbcTemplate.query(
                """
                SELECT * FROM recurring_payments
                WHERE active = TRUE
                ORDER BY next_due_date, id
                """,
                rowMapper);
    }

    public List<RecurringPayment> findDueOnOrBefore(Date date) {
        return jdbcTemplate.query(
                """
                SELECT * FROM recurring_payments
                WHERE active = TRUE AND next_due_date <= ?
                ORDER BY next_due_date, id
                """,
                rowMapper,
                date);
    }

    public boolean update(RecurringPayment payment) {
        String sql = """
                UPDATE recurring_payments
                SET category_id = ?, title = ?, amount = ?,
                    frequency = ?, next_due_date = ?, active = ?
                WHERE id = ?
                """;

        int rows = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);

            if (payment.getCategoryId() == null) {
                ps.setNull(1, Types.BIGINT);
            } else {
                ps.setLong(1, payment.getCategoryId());
            }

            ps.setString(2, payment.getTitle());
            ps.setBigDecimal(3, payment.getAmount());
            ps.setString(4, payment.getFrequency());
            ps.setDate(5, Date.valueOf(payment.getNextDueDate()));
            ps.setBoolean(6, payment.isActive());
            ps.setLong(7, payment.getId());

            return ps;
        });

        return rows > 0;
    }

    public boolean deleteById(Long id) {
        return jdbcTemplate.update(
                "DELETE FROM recurring_payments WHERE id = ?",
                id) > 0;
    }
}
