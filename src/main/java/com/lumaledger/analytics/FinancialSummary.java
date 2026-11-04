package com.lumaledger.analytics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import com.lumaledger.model.Transaction;
import com.lumaledger.model.TransactionType;

public class FinancialSummary {

    public static class Summary {
        private final BigDecimal totalIncome;
        private final BigDecimal totalExpenses;
        private final BigDecimal balance;
        private final BigDecimal savingsRate;

        public Summary(BigDecimal totalIncome,
                       BigDecimal totalExpenses,
                       BigDecimal balance,
                       BigDecimal savingsRate) {
            this.totalIncome = totalIncome;
            this.totalExpenses = totalExpenses;
            this.balance = balance;
            this.savingsRate = savingsRate;
        }

        public BigDecimal getTotalIncome() {
            return totalIncome;
        }

        public BigDecimal getTotalExpenses() {
            return totalExpenses;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public BigDecimal getSavingsRate() {
            return savingsRate;
        }
    }

    public static Summary calculate(List<Transaction> transactions) {

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        if (transactions == null) {
            throw new IllegalArgumentException(
                "Transaction list cannot be null."
            );
        }

        for (Transaction transaction : transactions) {

            if (transaction == null ||
                transaction.getAmount() == null ||
                transaction.getType() == null) {
                continue;
            }

            if (transaction.getType() == TransactionType.INCOME) {
                totalIncome = totalIncome.add(transaction.getAmount());

            } else if (transaction.getType() == TransactionType.EXPENSE) {
                totalExpenses = totalExpenses.add(transaction.getAmount());
            }
        }

        BigDecimal balance = totalIncome.subtract(totalExpenses);

        BigDecimal savingsRate = BigDecimal.ZERO;

        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            savingsRate = balance
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalIncome, 2, RoundingMode.HALF_UP);
        }

        return new Summary(
            totalIncome,
            totalExpenses,
            balance,
            savingsRate
        );
    }

public static Summary calculateForMonth(
        List<Transaction> transactions,
        YearMonth month) {

    if (transactions == null) {
        throw new IllegalArgumentException(
                "Transaction list cannot be null."
        );
    }

    if (month == null) {
        throw new IllegalArgumentException(
                "Month cannot be null."
        );
    }

    List<Transaction> monthlyTransactions = new ArrayList<>();

    for (Transaction transaction : transactions) {

        if (transaction == null ||
                transaction.getTransactionDate() == null) {
            continue;
        }

        YearMonth transactionMonth =
                YearMonth.from(transaction.getTransactionDate());

        if (transactionMonth.equals(month)) {
            monthlyTransactions.add(transaction);
        }
    }

    return calculate(monthlyTransactions);
}

}
