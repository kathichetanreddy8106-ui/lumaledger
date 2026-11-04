
package com.lumaledger.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lumaledger.analytics.FinancialSummary;
import com.lumaledger.service.AnalyticsService;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public FinancialSummary.Summary getSummary() {
        return analyticsService.getFinancialSummary();
    }

    @GetMapping("/forecast/7-days")
    public BigDecimal forecast7Days() {
        return analyticsService.forecastExpenses(7);
    }

    @GetMapping("/forecast/30-days")
    public BigDecimal forecast30Days() {
        return analyticsService.forecastExpenses(30);
    }

    @GetMapping("/forecast/90-days")
    public BigDecimal forecast90Days() {
        return analyticsService.forecastExpenses(90);
    }

    @GetMapping("/monthly-summary")
    public FinancialSummary.Summary getMonthlySummary(
            @RequestParam int year,
            @RequestParam int month) {
        return analyticsService.getMonthlyFinancialSummary(year, month);
    }

    @GetMapping("/safe-to-spend")
    public AnalyticsService.SafeToSpendBreakdown getSafeToSpend(
            @RequestParam BigDecimal availableBalance,
            @RequestParam BigDecimal savingsContribution,
            @RequestParam BigDecimal emergencyBuffer) {
        return analyticsService.calculateSafeToSpendBreakdown(
                availableBalance,
                savingsContribution,
                emergencyBuffer
        );
    }

    @GetMapping("/spending-alerts")
    public List<String> getSpendingAlerts() {
        return analyticsService.getSpendingAlerts();
    }
   
    @GetMapping("/budget-alerts")
    public List<String> getBudgetAlerts() {
        return analyticsService.getBudgetAlerts();
    }
    @GetMapping("/what-if")
public BigDecimal simulateSavings(
        @RequestParam BigDecimal income,
        @RequestParam BigDecimal currentExpenses,
        @RequestParam BigDecimal expenseReductionPercent,
        @RequestParam BigDecimal extraPurchase) {

    return analyticsService.simulateSavings(
            income,
            currentExpenses,
            expenseReductionPercent,
            extraPurchase
    );
}
}
