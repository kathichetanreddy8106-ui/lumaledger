
package com.lumaledger.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import com.lumaledger.model.Category;
import com.lumaledger.model.TransactionType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class CategoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public CategoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Category> rowMapper = (rs, rowNum) -> {
        Timestamp timestamp = rs.getTimestamp("created_at");

        return new Category(
                rs.getLong("id"),
                rs.getString("name"),
                TransactionType.valueOf(rs.getString("type")),
                timestamp == null ? null : timestamp.toLocalDateTime()
        );
    };

    public Category create(Category category) {
        String sql = """
                INSERT INTO categories (name, type)
                VALUES (?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, category.getName().trim());
            ps.setString(2, category.getType().name());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new IllegalStateException(
                    "Creating category did not return a generated ID");
        }

        return findById(key.longValue())
                .orElseThrow(() -> new IllegalStateException(
                        "Category was created but could not be retrieved"));
    }

    public Optional<Category> findById(Long id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        List<Category> results = jdbcTemplate.query(sql, rowMapper, id);
        return results.stream().findFirst();
    }

    public List<Category> findAll() {
        String sql = """
                SELECT * FROM categories
                ORDER BY type ASC, name ASC, id ASC
                """;
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<Category> findByType(TransactionType type) {
        String sql = """
                SELECT * FROM categories
                WHERE type = ?
                ORDER BY name ASC, id ASC
                """;
        return jdbcTemplate.query(sql, rowMapper, type.name());
    }

    public Category update(Long id, Category category) {
        String sql = """
                UPDATE categories
                SET name = ?, type = ?
                WHERE id = ?
                """;

        int rows = jdbcTemplate.update(
                sql,
                category.getName().trim(),
                category.getType().name(),
                id
        );

        if (rows == 0) {
            return null;
        }

        return findById(id).orElse(null);
    }

    public boolean deleteById(Long id) {
        return jdbcTemplate.update(
                "DELETE FROM categories WHERE id = ?", id) > 0;
    }

    public boolean existsByNameAndType(
            String name, TransactionType type) {
        String sql = """
                SELECT COUNT(*) FROM categories
                WHERE LOWER(name) = LOWER(?) AND type = ?
                """;

        Integer count = jdbcTemplate.queryForObject(
                sql, Integer.class, name.trim(), type.name());

        return count != null && count > 0;
    }
}
