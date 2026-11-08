
package com.lumaledger.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.lumaledger.repository.TransactionRepository;
import com.lumaledger.model.TransactionType;
import com.lumaledger.model.MonthlyReport;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;

    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public BigDecimal getMonthlyIncome(int year, int month) {
        return transactionRepository.getMonthlyTotal(
                TransactionType.INCOME, year, month
        );
    }

    public BigDecimal getMonthlyExpenses(int year, int month) {
        return transactionRepository.getMonthlyTotal(
                TransactionType.EXPENSE, year, month
        );
    }

    public BigDecimal getMonthlySavings(int year, int month) {
        BigDecimal income = getMonthlyIncome(year, month);
        BigDecimal expenses = getMonthlyExpenses(year, month);

        return income.subtract(expenses);
    }

    public MonthlyReport generateMonthlyReport(int year, int month) {

        BigDecimal income = getMonthlyIncome(year, month);

        BigDecimal expenses = getMonthlyExpenses(year, month);

        BigDecimal savings = income.subtract(expenses);

        return new MonthlyReport(
                year,
                month,
                income,
                expenses,
                savings
        );
    }
}
