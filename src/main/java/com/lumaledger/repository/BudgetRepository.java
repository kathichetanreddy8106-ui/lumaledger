
package com.lumaledger.repository;

import com.lumaledger.model.Budget;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class BudgetRepository {

    private final JdbcTemplate jdbcTemplate;

    public BudgetRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Budget> budgetRowMapper = (rs, rowNum) -> {
        Budget budget = new Budget();
        budget.setId(rs.getLong("id"));
        budget.setCategoryId(rs.getLong("category_id"));
        budget.setBudgetMonth(
                rs.getDate("budget_month").toLocalDate());
        budget.setAmount(rs.getBigDecimal("amount"));

        Date createdDate = rs.getDate("created_at");
        if (createdDate != null) {
            budget.setCreatedAt(
                    rs.getTimestamp("created_at").toLocalDateTime());
        }

        return budget;
    };

    public Budget create(Budget budget) {
        String sql = """
                INSERT INTO budgets (category_id, budget_month, amount)
                VALUES (?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, budget.getCategoryId());
            ps.setDate(2, Date.valueOf(budget.getBudgetMonth()));
            ps.setBigDecimal(3, budget.getAmount());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() == null) {
            throw new IllegalStateException(
                    "Failed to retrieve generated budget ID");
        }

        budget.setId(keyHolder.getKey().longValue());
        return findById(budget.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Created budget could not be retrieved"));
    }

    public Optional<Budget> findById(Long id) {
        String sql = "SELECT * FROM budgets WHERE id = ?";
        List<Budget> results = jdbcTemplate.query(
                sql, budgetRowMapper, id);
        return results.stream().findFirst();
    }

    public List<Budget> findAll() {
        String sql = """
                SELECT * FROM budgets
                ORDER BY budget_month DESC, id DESC
                """;
        return jdbcTemplate.query(sql, budgetRowMapper);
    }

    public List<Budget> findByMonth(Date month) {
        String sql = """
                SELECT * FROM budgets
                WHERE budget_month = ?
                ORDER BY category_id
                """;
        return jdbcTemplate.query(sql, budgetRowMapper, month);
    }

    public List<Budget> findByCategoryId(Long categoryId) {
        String sql = """
                SELECT * FROM budgets
                WHERE category_id = ?
                ORDER BY budget_month DESC
                """;
        return jdbcTemplate.query(
                sql, budgetRowMapper, categoryId);
    }

    public boolean update(Budget budget) {
        String sql = """
                UPDATE budgets
                SET category_id = ?, budget_month = ?, amount = ?
                WHERE id = ?
                """;

        int rows = jdbcTemplate.update(
                sql,
                budget.getCategoryId(),
                Date.valueOf(budget.getBudgetMonth()),
                budget.getAmount(),
                budget.getId());

        return rows > 0;
    }

    public boolean deleteById(Long id) {
        String sql = "DELETE FROM budgets WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }
}
