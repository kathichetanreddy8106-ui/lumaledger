
package com.lumaledger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.lumaledger.analytics.AnomalyDetector;
import com.lumaledger.analytics.CashFlowForecaster;
import com.lumaledger.analytics.FinancialSummary;
import com.lumaledger.analytics.WhatIfSimulator;
import com.lumaledger.model.Budget;
import com.lumaledger.model.Category;
import com.lumaledger.model.Transaction;
import com.lumaledger.model.TransactionType;
import com.lumaledger.repository.CategoryRepository;
import com.lumaledger.service.AnalyticsService;
import com.lumaledger.service.BudgetService;
import com.lumaledger.service.RecurringPaymentService;
import com.lumaledger.service.TransactionService;

class AnalyticsServiceTests {

    @Test
    void financialSummaryCalculatesCorrectly() {

        Transaction income = new Transaction();
        income.setType(TransactionType.INCOME);
        income.setAmount(new BigDecimal("30000.00"));

        Transaction expense = new Transaction();
        expense.setType(TransactionType.EXPENSE);
        expense.setAmount(new BigDecimal("5000.00"));

        FinancialSummary.Summary result =
                FinancialSummary.calculate(List.of(income, expense));

        assertEquals(
                0,
                new BigDecimal("30000.00").compareTo(result.getTotalIncome())
        );

        assertEquals(
                0,
                new BigDecimal("5000.00").compareTo(result.getTotalExpenses())
        );

        assertEquals(
                0,
                new BigDecimal("25000.00").compareTo(result.getBalance())
        );

        assertEquals(
                0,
                new BigDecimal("83.33").compareTo(result.getSavingsRate())
        );
    }

    @Test
    void anomalyDetectorIdentifiesUnusualSpending() {

        boolean result = AnomalyDetector.isUnusualSpending(
                new BigDecimal("7000"),
                new BigDecimal("4000")
        );

        assertTrue(result);
    }

    @Test
    void cashFlowForecasterCalculatesThreeMonthAverage() {

        BigDecimal[] expenses = {
                new BigDecimal("3000"),
                new BigDecimal("4000"),
                new BigDecimal("5000")
        };

        BigDecimal result =
                CashFlowForecaster.forecastNextMonth(expenses);

        assertEquals(
                0,
                new BigDecimal("4000.00").compareTo(result)
        );
    }

    @Test
void cashFlowForecasterCalculatesDifferentPeriods() {

    BigDecimal[] expenses = {
            new BigDecimal("9000"),
            new BigDecimal("12000"),
            new BigDecimal("15000")
    };

    assertEquals(
            0,
            new BigDecimal("2800.00").compareTo(
                    CashFlowForecaster.forecastNext7Days(expenses))
    );

    assertEquals(
            0,
            new BigDecimal("12000.00").compareTo(
                    CashFlowForecaster.forecastNext30Days(expenses))
    );

    assertEquals(
            0,
            new BigDecimal("36000.00").compareTo(
                    CashFlowForecaster.forecastNext90Days(expenses))
    );
}

@Test
void cashFlowForecasterRejectsNegativeExpenses() {

    BigDecimal[] expenses = {
            new BigDecimal("9000"),
            new BigDecimal("-2000"),
            new BigDecimal("15000")
    };

    org.junit.jupiter.api.Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> CashFlowForecaster.averageMonthlyExpenses(expenses)
    );
}

@Test
void analyticsServiceRejectsInvalidForecastPeriod() {

    TransactionService transactionService =
            mock(TransactionService.class);

    CategoryRepository categoryRepository =
            mock(CategoryRepository.class);

    BudgetService budgetService =
            mock(BudgetService.class);

    RecurringPaymentService recurringPaymentService =
            mock(RecurringPaymentService.class);

    AnalyticsService analyticsService = new AnalyticsService(
            transactionService,
            categoryRepository,
            budgetService,
            recurringPaymentService
    );

    org.junit.jupiter.api.Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> analyticsService.forecastExpenses(15)
    );
}
@Test
void spendingAlertsDetectUnusualSpending() {

    TransactionService transactionService =
            mock(TransactionService.class);

    CategoryRepository categoryRepository =
            mock(CategoryRepository.class);

    BudgetService budgetService =
            mock(BudgetService.class);

    RecurringPaymentService recurringPaymentService =
            mock(RecurringPaymentService.class);

    Transaction historicalExpense = new Transaction();
    historicalExpense.setType(TransactionType.EXPENSE);
    historicalExpense.setAmount(new BigDecimal("3000"));
    historicalExpense.setCategoryId(1L);
    historicalExpense.setTransactionDate(
            LocalDate.now().minusMonths(1)
    );

    Transaction currentExpense = new Transaction();
    currentExpense.setType(TransactionType.EXPENSE);
    currentExpense.setAmount(new BigDecimal("10000"));
    currentExpense.setCategoryId(1L);
    currentExpense.setTransactionDate(LocalDate.now());

    Category category = new Category();
    category.setId(1L);
    category.setName("Groceries");

    when(transactionService.getAll())
            .thenReturn(List.of(historicalExpense, currentExpense));

    when(categoryRepository.findAll())
            .thenReturn(List.of(category));

    AnalyticsService analyticsService = new AnalyticsService(
            transactionService,
            categoryRepository,
            budgetService,
            recurringPaymentService
    );

    List<String> alerts = analyticsService.getSpendingAlerts();

    assertEquals(1, alerts.size());
    assertTrue(alerts.get(0).contains("Groceries"));
    assertTrue(alerts.get(0).startsWith("ALERT:"));
}

@Test
void budgetAlertsDetectSpendingAbove80Percent() {

    TransactionService transactionService =
            mock(TransactionService.class);

    CategoryRepository categoryRepository =
            mock(CategoryRepository.class);

    BudgetService budgetService =
            mock(BudgetService.class);

    RecurringPaymentService recurringPaymentService =
            mock(RecurringPaymentService.class);

    Budget budget = new Budget();
    budget.setCategoryId(1L);
    budget.setBudgetMonth(LocalDate.now().withDayOfMonth(1));
    budget.setAmount(new BigDecimal("10000"));

    Transaction expense = new Transaction();
    expense.setType(TransactionType.EXPENSE);
    expense.setAmount(new BigDecimal("8500"));
    expense.setCategoryId(1L);
    expense.setTransactionDate(LocalDate.now());

    Category category = new Category();
    category.setId(1L);
    category.setName("Groceries");

    when(budgetService.getBudgetsByMonth(any(YearMonth.class)))
            .thenReturn(List.of(budget));

    when(transactionService.getAll())
            .thenReturn(List.of(expense));

    when(categoryRepository.findAll())
            .thenReturn(List.of(category));

    AnalyticsService analyticsService = new AnalyticsService(
            transactionService,
            categoryRepository,
            budgetService,
            recurringPaymentService
    );

    List<String> alerts = analyticsService.getBudgetAlerts();

    assertEquals(1, alerts.size());
    assertTrue(alerts.get(0).contains("Groceries"));
    assertTrue(alerts.get(0).startsWith("BUDGET WARNING:"));
    assertTrue(alerts.get(0).contains("85.00%"));
}

    @Test
    void whatIfSimulatorCalculatesProjectedBalance() {

        BigDecimal result = WhatIfSimulator.simulateSavings(
                new BigDecimal("30000"),
                new BigDecimal("10000"),
                new BigDecimal("20"),
                new BigDecimal("2000")
        );

        assertEquals(
                0,
                new BigDecimal("20000.00").compareTo(result)
        );
    }
    @Test
void safeToSpendCalculatesCorrectly() {

    TransactionService transactionService =
            mock(TransactionService.class);

    CategoryRepository categoryRepository =
            mock(CategoryRepository.class);

    BudgetService budgetService =
            mock(BudgetService.class);

    RecurringPaymentService recurringPaymentService =
            mock(RecurringPaymentService.class);

    when(transactionService.getAll()).thenReturn(List.of());

    when(budgetService.getBudgetsByMonth(any(YearMonth.class)))
            .thenReturn(List.of());

    when(recurringPaymentService.getUpcomingPaymentTotal(30))
            .thenReturn(new BigDecimal("5000"));

    AnalyticsService analyticsService = new AnalyticsService(
            transactionService,
            categoryRepository,
            budgetService,
            recurringPaymentService
    );

    BigDecimal result = analyticsService.calculateSafeToSpend(
            new BigDecimal("50000"),
            new BigDecimal("5000"),
            new BigDecimal("3000")
    );

    assertEquals(
            0,
            new BigDecimal("37000.00").compareTo(result)
    );
}

@Test
void monthlyFinancialSummaryCalculatesCorrectly() {

    // November income
    Transaction income = new Transaction();
    income.setType(TransactionType.INCOME);
    income.setAmount(new BigDecimal("30000"));
    income.setTransactionDate(LocalDate.of(2026, 11, 5));

    // November expense
    Transaction expense = new Transaction();
    expense.setType(TransactionType.EXPENSE);
    expense.setAmount(new BigDecimal("10000"));
    expense.setTransactionDate(LocalDate.of(2026, 11, 10));

    // December expense - should not be included
    Transaction nextMonthExpense = new Transaction();
    nextMonthExpense.setType(TransactionType.EXPENSE);
    nextMonthExpense.setAmount(new BigDecimal("5000"));
    nextMonthExpense.setTransactionDate(LocalDate.of(2026, 12, 2));

    FinancialSummary.Summary result =
            FinancialSummary.calculateForMonth(
                    List.of(income, expense, nextMonthExpense),
                    YearMonth.of(2026, 11)
            );

    assertEquals(
            0,
            new BigDecimal("30000.00").compareTo(result.getTotalIncome())
    );

    assertEquals(
            0,
            new BigDecimal("10000.00").compareTo(result.getTotalExpenses())
    );

    assertEquals(
            0,
            new BigDecimal("20000.00").compareTo(result.getBalance())
    );

    assertEquals(
            0,
            new BigDecimal("66.67").compareTo(result.getSavingsRate())
    );
}

@Test
void safeToSpendBreakdownCalculatesCorrectly() {

    TransactionService transactionService =
            mock(TransactionService.class);

    CategoryRepository categoryRepository =
            mock(CategoryRepository.class);

    BudgetService budgetService =
            mock(BudgetService.class);

    RecurringPaymentService recurringPaymentService =
            mock(RecurringPaymentService.class);

    when(transactionService.getAll()).thenReturn(List.of());

    when(budgetService.getBudgetsByMonth(any(YearMonth.class)))
            .thenReturn(List.of());

    when(recurringPaymentService.getUpcomingPaymentTotal(30))
            .thenReturn(new BigDecimal("5000"));

    AnalyticsService analyticsService = new AnalyticsService(
            transactionService,
            categoryRepository,
            budgetService,
            recurringPaymentService
    );

    AnalyticsService.SafeToSpendBreakdown result =
            analyticsService.calculateSafeToSpendBreakdown(
                    new BigDecimal("50000"),
                    new BigDecimal("5000"),
                    new BigDecimal("3000")
            );

    assertEquals(0, new BigDecimal("50000.00")
            .compareTo(result.getAvailableBalance()));

    assertEquals(0, new BigDecimal("5000.00")
            .compareTo(result.getUpcomingBills()));

    assertEquals(0, BigDecimal.ZERO
            .compareTo(result.getRemainingBudget()));

    assertEquals(0, new BigDecimal("5000.00")
            .compareTo(result.getSavingsContribution()));

    assertEquals(0, new BigDecimal("3000.00")
            .compareTo(result.getEmergencyBuffer()));

    assertEquals(0, new BigDecimal("37000.00")
            .compareTo(result.getSafeToSpend()));
}

@Test
void safeToSpendRejectsNegativeAmounts() {

    TransactionService transactionService =
            mock(TransactionService.class);

    CategoryRepository categoryRepository =
            mock(CategoryRepository.class);

    BudgetService budgetService =
            mock(BudgetService.class);

    RecurringPaymentService recurringPaymentService =
            mock(RecurringPaymentService.class);

    AnalyticsService analyticsService = new AnalyticsService(
            transactionService,
            categoryRepository,
            budgetService,
            recurringPaymentService
    );

    org.junit.jupiter.api.Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> analyticsService.calculateSafeToSpendBreakdown(
                    new BigDecimal("-1000"),
                    new BigDecimal("500"),
                    new BigDecimal("200")
            )
    );
}

@Test
void safeToSpendRejectsNullAmounts() {

    TransactionService transactionService =
            mock(TransactionService.class);

    CategoryRepository categoryRepository =
            mock(CategoryRepository.class);

    BudgetService budgetService =
            mock(BudgetService.class);

    RecurringPaymentService recurringPaymentService =
            mock(RecurringPaymentService.class);

    AnalyticsService analyticsService = new AnalyticsService(
            transactionService,
            categoryRepository,
            budgetService,
            recurringPaymentService
    );

    org.junit.jupiter.api.Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> analyticsService.calculateSafeToSpendBreakdown(
                    null,
                    new BigDecimal("500"),
                    new BigDecimal("200")
            )
    );
}

}