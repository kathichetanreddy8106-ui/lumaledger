
package com.lumaledger.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final JdbcTemplate jdbcTemplate;

    public AuditLogController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public List<Map<String, Object>> getAllAuditLogs() {

        String sql = """
                SELECT *
                FROM audit_logs
                ORDER BY changed_at DESC
                """;

        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/check")
    public Map<String, Object> checkAuditTable() {

        String sql = """
                SELECT COUNT(*) AS total_logs
                FROM audit_logs
                """;

        Integer count = jdbcTemplate.queryForObject(
                sql, Integer.class
        );

        return Map.of(
                "tableExists", true,
                "totalLogs", count
        );
    }
}
