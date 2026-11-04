
package com.lumaledger.analytics;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AnomalyDetector {

    private static final BigDecimal THRESHOLD =
            new BigDecimal("1.5");

    public static boolean isUnusualSpending(
            BigDecimal currentSpending,
            BigDecimal historicalAverage) {

        if (currentSpending == null ||
            historicalAverage == null ||
            currentSpending.compareTo(BigDecimal.ZERO) < 0 ||
            historicalAverage.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Spending values must be non-negative."
            );
        }

        if (historicalAverage.compareTo(BigDecimal.ZERO) == 0) {
            return currentSpending.compareTo(BigDecimal.ZERO) > 0;
        }

        BigDecimal limit = historicalAverage.multiply(THRESHOLD);

        return currentSpending.compareTo(limit) > 0;
    }

    public static String checkSpending(
            String categoryName,
            BigDecimal currentSpending,
            BigDecimal historicalAverage) {

        if (categoryName == null || categoryName.isBlank()) {
            throw new IllegalArgumentException(
                    "Category name is required."
            );
        }

        boolean unusual = isUnusualSpending(
                currentSpending, historicalAverage
        );

        if (unusual) {
            return "ALERT: " + categoryName
                    + " spending is unusually high! Current: Rs."
                    + currentSpending.setScale(2, RoundingMode.HALF_UP)
                    + ", usual average: Rs."
                    + historicalAverage.setScale(2, RoundingMode.HALF_UP);
        }

        return "Normal spending in category: " + categoryName;
    }
}
