
package com.lumaledger.analytics;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class WhatIfSimulator {

    public static BigDecimal simulateSavings(
            BigDecimal income,
            BigDecimal currentExpenses,
            BigDecimal expenseReductionPercent,
            BigDecimal extraPurchase) {

        if (income == null ||
            currentExpenses == null ||
            expenseReductionPercent == null ||
            extraPurchase == null) {
            throw new IllegalArgumentException(
                    "Financial inputs cannot be null."
            );
        }

        if (income.compareTo(BigDecimal.ZERO) < 0 ||
            currentExpenses.compareTo(BigDecimal.ZERO) < 0 ||
            extraPurchase.compareTo(BigDecimal.ZERO) < 0 ||
            expenseReductionPercent.compareTo(BigDecimal.ZERO) < 0 ||
            expenseReductionPercent.compareTo(
                    new BigDecimal("100")) > 0) {

            throw new IllegalArgumentException(
                    "Invalid financial input."
            );
        }

        BigDecimal remainingPercent =
                new BigDecimal("100").subtract(
                        expenseReductionPercent
                );

        BigDecimal reducedExpenses =
                currentExpenses.multiply(remainingPercent)
                        .divide(
                                new BigDecimal("100"),
                                2,
                                RoundingMode.HALF_UP
                        );

        return income.subtract(reducedExpenses)
                .subtract(extraPurchase);
    }
}
