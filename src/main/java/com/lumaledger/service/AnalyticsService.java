
package com.lumaledger.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.lumaledger.analytics.AnomalyDetector;
import com.lumaledger.analytics.CashFlowForecaster;
import com.lumaledger.analytics.FinancialSummary;
import com.lumaledger.analytics.WhatIfSimulator;
import com.lumaledger.model.Budget;
import com.lumaledger.model.Category;
import com.lumaledger.model.Transaction;
import com.lumaledger.model.TransactionType;
import com.lumaledger.repository.CategoryRepository;

@Service
public class AnalyticsService {

    private final TransactionService transactionService;
    private final CategoryRepository categoryRepository;
    private final BudgetService budgetService;
    private final RecurringPaymentService recurringPaymentService;

   public AnalyticsService(
        TransactionService transactionService,
        CategoryRepository categoryRepository,
        BudgetService budgetService,
        RecurringPaymentService recurringPaymentService) {

    this.transactionService = transactionService;
    this.categoryRepository = categoryRepository;
    this.budgetService = budgetService;
    this.recurringPaymentService = recurringPaymentService;
}

    // 1. Financial Summary
    public FinancialSummary.Summary getFinancialSummary() {
        List<Transaction> transactions = transactionService.getAll();
        return FinancialSummary.calculate(transactions);
    }

    public FinancialSummary.Summary getMonthlyFinancialSummary(
        int year,
        int month) {

    YearMonth selectedMonth = YearMonth.of(year, month);

    List<Transaction> transactions = transactionService.getAll();

    return FinancialSummary.calculateForMonth(
            transactions,
            selectedMonth
    );
}

    // 2. Unusual Spending Detection
    // Compares current month's spending with the average
    // spending of the previous three complete months.
    public List<String> getSpendingAlerts() {

        YearMonth currentMonth = YearMonth.from(LocalDate.now());
        LocalDate currentMonthStart = currentMonth.atDay(1);
        LocalDate historyStart = currentMonth.minusMonths(3).atDay(1);

        List<Transaction> transactions = transactionService.getAll();
        List<Category> categories = categoryRepository.findAll();

        Map<Long, String> categoryNames = new HashMap<>();

        for (Category category : categories) {
            categoryNames.put(category.getId(), category.getName());
        }

        Map<Long, BigDecimal> currentSpending = new HashMap<>();
        Map<Long, BigDecimal> historicalSpending = new HashMap<>();

        BigDecimal currentUncategorized = BigDecimal.ZERO;
        BigDecimal historicalUncategorized = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {

            if (transaction.getType() != TransactionType.EXPENSE
                    || transaction.getAmount() == null
                    || transaction.getTransactionDate() == null) {
                continue;
            }

            LocalDate date = transaction.getTransactionDate();
            Long categoryId = transaction.getCategoryId();

            if (!date.isBefore(currentMonthStart)) {

                if (categoryId == null) {
                    currentUncategorized =
                            currentUncategorized.add(transaction.getAmount());
                } else {
                    currentSpending.merge(
                            categoryId,
                            transaction.getAmount(),
                            BigDecimal::add
                    );
                }

            } else if (!date.isBefore(historyStart)) {

                if (categoryId == null) {
                    historicalUncategorized =
                            historicalUncategorized.add(transaction.getAmount());
                } else {
                    historicalSpending.merge(
                            categoryId,
                            transaction.getAmount(),
                            BigDecimal::add
                    );
                }
            }
        }

        List<String> alerts = new ArrayList<>();

        for (Map.Entry<Long, BigDecimal> entry : currentSpending.entrySet()) {

            Long categoryId = entry.getKey();

            BigDecimal historicalAverage =
                    historicalSpending.getOrDefault(
                            categoryId,
                            BigDecimal.ZERO
                    ).divide(
                            BigDecimal.valueOf(3),
                            2,
                            RoundingMode.HALF_UP
                    );

            String categoryName = categoryNames.getOrDefault(
                    categoryId,
                    "Unknown category"
            );

            String result = AnomalyDetector.checkSpending(
                    categoryName,
                    entry.getValue(),
                    historicalAverage
            );

            if (result.startsWith("ALERT:")) {
                alerts.add(result);
            }
        }

        BigDecimal uncategorizedAverage =
                historicalUncategorized.divide(
                        BigDecimal.valueOf(3),
                        2,
                        RoundingMode.HALF_UP
                );

        String uncategorizedResult = AnomalyDetector.checkSpending(
                "Uncategorized",
                currentUncategorized,
                uncategorizedAverage
        );

        if (uncategorizedResult.startsWith("ALERT:")) {
            alerts.add(uncategorizedResult);
        }

        return alerts;
    }

    // 3. Next Month Expense Forecast
    // Uses the previous three complete months.
    public BigDecimal forecastNextMonthExpenses() {

        YearMonth currentMonth = YearMonth.from(LocalDate.now());
        BigDecimal[] monthlyExpenses = new BigDecimal[3];

        for (int i = 1; i <= 3; i++) {

            YearMonth month = currentMonth.minusMonths(i);

            monthlyExpenses[i - 1] =
                    transactionService.getMonthlyTotal(
                            TransactionType.EXPENSE,
                            month.getYear(),
                            month.getMonthValue()
                    );
        }

        return CashFlowForecaster.forecastNextMonth(monthlyExpenses);
    }


public BigDecimal forecastExpenses(int days) {

    if (days != 7 && days != 30 && days != 90) {
        throw new IllegalArgumentException(
                "Forecast period must be 7, 30, or 90 days."
        );
    }

    YearMonth currentMonth = YearMonth.from(LocalDate.now());

    BigDecimal[] monthlyExpenses = new BigDecimal[3];

    for (int i = 1; i <= 3; i++) {

        YearMonth month = currentMonth.minusMonths(i);

        monthlyExpenses[i - 1] =
                transactionService.getMonthlyTotal(
                        TransactionType.EXPENSE,
                        month.getYear(),
                        month.getMonthValue()
                );
    }

    return CashFlowForecaster.forecastExpenses(
            monthlyExpenses,
            days
    );
}

    // 4. What-If Savings Simulator
    public BigDecimal simulateSavings(
            BigDecimal income,
            BigDecimal currentExpenses,
            BigDecimal expenseReductionPercent,
            BigDecimal extraPurchase) {

        return WhatIfSimulator.simulateSavings(
                income,
                currentExpenses,
                expenseReductionPercent,
                extraPurchase
        );
    }


    // 5. Monthly Budget Alerts
    public List<String> getBudgetAlerts() {

        YearMonth currentMonth = YearMonth.from(LocalDate.now());

        List<Budget> budgets =
                budgetService.getBudgetsByMonth(currentMonth);

        List<Transaction> transactions =
                transactionService.getAll();

        List<Category> categories =
                categoryRepository.findAll();

        Map<Long, String> categoryNames = new HashMap<>();

        for (Category category : categories) {
            categoryNames.put(category.getId(), category.getName());
        }

        Map<Long, BigDecimal> spendingByCategory = new HashMap<>();

        for (Transaction transaction : transactions) {

            if (transaction.getType() != TransactionType.EXPENSE
                    || transaction.getAmount() == null
                    || transaction.getTransactionDate() == null
                    || transaction.getCategoryId() == null) {
                continue;
            }

            YearMonth transactionMonth =
                    YearMonth.from(transaction.getTransactionDate());

            if (transactionMonth.equals(currentMonth)) {
                spendingByCategory.merge(
                    transaction.getCategoryId(),
                    transaction.getAmount(),
                    BigDecimal::add
                    );
                }
            }
    

        List<String> alerts = new ArrayList<>();

        for (Budget budget : budgets) {

            BigDecimal spent = spendingByCategory.getOrDefault(
                budget.getCategoryId(),
                BigDecimal.ZERO
            );

            BigDecimal limit = budget.getAmount();

            BigDecimal percentage = spent.multiply(
                    BigDecimal.valueOf(100)
            ).divide(limit, 2, RoundingMode.HALF_UP);

            String categoryName = categoryNames.getOrDefault(
                    budget.getCategoryId(),
                   "Unknown category"
            );

            if (percentage.compareTo(new BigDecimal("100")) >= 0) {

                alerts.add(
                        "OVER BUDGET: " + categoryName
                        + " has used " + percentage
                        + "% of its monthly budget."
                );

            } else if (percentage.compareTo(new BigDecimal("80")) >= 0) {

                alerts.add(
                        "BUDGET WARNING: " + categoryName
                        + " has used " + percentage
                        + "% of its monthly budget."
                );
            }
        }

        return alerts;
    }


public SafeToSpendBreakdown calculateSafeToSpendBreakdown(
        BigDecimal availableBalance,
        BigDecimal savingsContribution,
        BigDecimal emergencyBuffer) {

    if (availableBalance == null
            || savingsContribution == null
            || emergencyBuffer == null) {
        throw new IllegalArgumentException(
                "Financial amounts cannot be null.");
    }

    if (availableBalance.compareTo(BigDecimal.ZERO) < 0
            || savingsContribution.compareTo(BigDecimal.ZERO) < 0
            || emergencyBuffer.compareTo(BigDecimal.ZERO) < 0) {
        throw new IllegalArgumentException(
                "Financial amounts cannot be negative.");
    }

    // 1. Upcoming recurring bills
    BigDecimal upcomingBills =
            recurringPaymentService.getUpcomingPaymentTotal(30);

    // 2. Calculate remaining monthly budget
    YearMonth currentMonth = YearMonth.from(LocalDate.now());

    List<Budget> budgets =
            budgetService.getBudgetsByMonth(currentMonth);

    List<Transaction> transactions =
            transactionService.getAll();

    Map<Long, BigDecimal> spendingByCategory = new HashMap<>();

    for (Transaction transaction : transactions) {

        if (transaction.getType() != TransactionType.EXPENSE
                || transaction.getAmount() == null
                || transaction.getTransactionDate() == null
                || transaction.getCategoryId() == null) {
            continue;
        }

        YearMonth transactionMonth =
                YearMonth.from(transaction.getTransactionDate());

        if (transactionMonth.equals(currentMonth)) {
            spendingByCategory.merge(
                    transaction.getCategoryId(),
                    transaction.getAmount(),
                    BigDecimal::add);
        }
    }

    BigDecimal remainingBudget = BigDecimal.ZERO;

    for (Budget budget : budgets) {

        BigDecimal spent = spendingByCategory.getOrDefault(
                budget.getCategoryId(),
                BigDecimal.ZERO);

        BigDecimal unused = budget.getAmount().subtract(spent);

        if (unused.compareTo(BigDecimal.ZERO) > 0) {
            remainingBudget = remainingBudget.add(unused);
        }
    }

    // 3. Calculate final safe-to-spend amount
    BigDecimal safeToSpend = availableBalance
            .subtract(upcomingBills)
            .subtract(remainingBudget)
            .subtract(savingsContribution)
            .subtract(emergencyBuffer)
            .max(BigDecimal.ZERO)
            .setScale(2, RoundingMode.HALF_UP);

    return new SafeToSpendBreakdown(
            availableBalance,
            upcomingBills,
            remainingBudget,
            savingsContribution,
            emergencyBuffer,
            safeToSpend
    );
}

// Preserve the existing method for compatibility.
public BigDecimal calculateSafeToSpend(
        BigDecimal availableBalance,
        BigDecimal savingsContribution,
        BigDecimal emergencyBuffer) {

    return calculateSafeToSpendBreakdown(
            availableBalance,
            savingsContribution,
            emergencyBuffer
    ).getSafeToSpend();
}

// Detailed safe-to-spend result
public static class SafeToSpendBreakdown {

    private final BigDecimal availableBalance;
    private final BigDecimal upcomingBills;
    private final BigDecimal remainingBudget;
    private final BigDecimal savingsContribution;
    private final BigDecimal emergencyBuffer;
    private final BigDecimal safeToSpend;

    public SafeToSpendBreakdown(
            BigDecimal availableBalance,
            BigDecimal upcomingBills,
            BigDecimal remainingBudget,
            BigDecimal savingsContribution,
            BigDecimal emergencyBuffer,
            BigDecimal safeToSpend) {

        this.availableBalance = availableBalance;
        this.upcomingBills = upcomingBills;
        this.remainingBudget = remainingBudget;
        this.savingsContribution = savingsContribution;
        this.emergencyBuffer = emergencyBuffer;
        this.safeToSpend = safeToSpend;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public BigDecimal getUpcomingBills() {
        return upcomingBills;
    }

    public BigDecimal getRemainingBudget() {
        return remainingBudget;
    }

    public BigDecimal getSavingsContribution() {
        return savingsContribution;
    }

    public BigDecimal getEmergencyBuffer() {
        return emergencyBuffer;
    }

    public BigDecimal getSafeToSpend() {
        return safeToSpend;
    }
}

}
