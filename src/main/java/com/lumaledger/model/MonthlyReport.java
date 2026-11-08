
package com.lumaledger.model;

import java.math.BigDecimal;

public class MonthlyReport {

    private int year;
    private int month;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal totalSavings;

    public MonthlyReport(int year, int month,
                         BigDecimal totalIncome,
                         BigDecimal totalExpenses,
                         BigDecimal totalSavings) {
        this.year = year;
        this.month = month;
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.totalSavings = totalSavings;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public BigDecimal getTotalSavings() {
        return totalSavings;
    }
}
