
package com.lumaledger.analytics;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CashFlowForecaster {

    public static BigDecimal averageMonthlyExpenses(
            BigDecimal[] monthlyExpenses) {

        if (monthlyExpenses == null || monthlyExpenses.length == 0) {
            throw new IllegalArgumentException(
                    "Monthly expenses cannot be empty."
            );
        }

        BigDecimal total = BigDecimal.ZERO;

        for (BigDecimal expense : monthlyExpenses) {

            if (expense == null ||
                    expense.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "Expenses must be non-negative."
                );
            }

            total = total.add(expense);
        }

        return total.divide(
                BigDecimal.valueOf(monthlyExpenses.length),
                2,
                RoundingMode.HALF_UP
        );
    }

    public static BigDecimal forecastNextMonth(
            BigDecimal[] monthlyExpenses) {

        return averageMonthlyExpenses(monthlyExpenses);
    }

    // Forecast expenses for any number of days.
    public static BigDecimal forecastExpenses(
            BigDecimal[] monthlyExpenses,
            int days) {

        if (days <= 0) {
            throw new IllegalArgumentException(
                    "Forecast days must be greater than zero."
            );
        }

        BigDecimal monthlyAverage =
                averageMonthlyExpenses(monthlyExpenses);

        BigDecimal dailyAverage = monthlyAverage.divide(
                BigDecimal.valueOf(30),
                10,
                RoundingMode.HALF_UP
        );

        return dailyAverage.multiply(BigDecimal.valueOf(days))
                .setScale(2, RoundingMode.HALF_UP);
    }

    // Forecast for the next 7 days.
    public static BigDecimal forecastNext7Days(
            BigDecimal[] monthlyExpenses) {

        return forecastExpenses(monthlyExpenses, 7);
    }

    // Forecast for the next 30 days.
    public static BigDecimal forecastNext30Days(
            BigDecimal[] monthlyExpenses) {

        return forecastExpenses(monthlyExpenses, 30);
    }

    // Forecast for the next 90 days.
    public static BigDecimal forecastNext90Days(
            BigDecimal[] monthlyExpenses) {

        return forecastExpenses(monthlyExpenses, 90);
    }
}
