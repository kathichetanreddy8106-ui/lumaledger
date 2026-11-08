package com.lumaledger.repository;

import com.lumaledger.model.Transaction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;

@Repository
public class AuditLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public AuditLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(String action, Transaction transaction) {

        String sql = """
                INSERT INTO audit_logs
                    (transaction_id, action, amount, type,
                     category_id, description, transaction_date)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                transaction.getId(),
                action,
                transaction.getAmount(),
                transaction.getType().name(),
                transaction.getCategoryId(),
                transaction.getDescription(),
                Date.valueOf(transaction.getTransactionDate())
        );
    }
}