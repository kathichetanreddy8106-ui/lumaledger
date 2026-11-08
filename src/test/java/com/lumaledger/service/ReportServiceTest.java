package com.lumaledger.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lumaledger.model.MonthlyReport;
import com.lumaledger.model.TransactionType;
import com.lumaledger.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void testGetMonthlyIncome() {
        when(transactionRepository.getMonthlyTotal(
                TransactionType.INCOME, 2026, 11))
                .thenReturn(new BigDecimal("50000"));

        BigDecimal income = reportService.getMonthlyIncome(2026, 11);

        assertEquals(new BigDecimal("50000"), income);
    }

    @Test
    void testGetMonthlyExpenses() {
        when(transactionRepository.getMonthlyTotal(
                TransactionType.EXPENSE, 2026, 11))
                .thenReturn(new BigDecimal("20000"));

        BigDecimal expenses = reportService.getMonthlyExpenses(2026, 11);

        assertEquals(new BigDecimal("20000"), expenses);
    }

    @Test
    void testGetMonthlySavings() {
        when(transactionRepository.getMonthlyTotal(
                TransactionType.INCOME, 2026, 11))
                .thenReturn(new BigDecimal("50000"));

        when(transactionRepository.getMonthlyTotal(
                TransactionType.EXPENSE, 2026, 11))
                .thenReturn(new BigDecimal("20000"));

        BigDecimal savings = reportService.getMonthlySavings(2026, 11);

        assertEquals(new BigDecimal("30000"), savings);
    }

    @Test
    void testGenerateMonthlyReport() {
        when(transactionRepository.getMonthlyTotal(
                TransactionType.INCOME, 2026, 11))
                .thenReturn(new BigDecimal("50000"));

        when(transactionRepository.getMonthlyTotal(
                TransactionType.EXPENSE, 2026, 11))
                .thenReturn(new BigDecimal("20000"));

        MonthlyReport report =
                reportService.generateMonthlyReport(2026, 11);

        assertEquals(2026, report.getYear());
        assertEquals(11, report.getMonth());
        assertEquals(new BigDecimal("50000"), report.getTotalIncome());
        assertEquals(new BigDecimal("20000"), report.getTotalExpenses());
        assertEquals(new BigDecimal("30000"), report.getTotalSavings());
    }
}