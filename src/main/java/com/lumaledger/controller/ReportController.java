
package com.lumaledger.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.lumaledger.model.MonthlyReport;
import com.lumaledger.service.ReportService;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly")
    public MonthlyReport getMonthlyReport(
            @RequestParam int year,
            @RequestParam int month) {

        if (month < 1 || month > 12) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Month must be between 1 and 12"
            );
        }

        if (year < 2000 || year > 2100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Year must be between 2000 and 2100"
            );
        }

        return reportService.generateMonthlyReport(year, month);
    }
}
