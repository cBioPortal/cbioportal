package org.cbioportal.legacy.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BigDecimalStats {
    public static BigDecimal calculateSampleSD(List<BigDecimal> data) {
        int n = data.size();
        if (n < 2) {
            throw new IllegalArgumentException("Sample size must be at least 2");
        }

        // Compute the mean
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal num : data) {
            sum = sum.add(num);
        }
        BigDecimal mean = sum.divide(BigDecimal.valueOf(n), MathContext.DECIMAL128);

        // Compute the sum of squared differences
        BigDecimal squaredDiffSum = BigDecimal.ZERO;
        for (BigDecimal num : data) {
            BigDecimal diff = num.subtract(mean);
            squaredDiffSum = squaredDiffSum.add(diff.pow(2));
        }

        // Divide by (n-1) for sample standard deviation
        BigDecimal variance = squaredDiffSum.divide(BigDecimal.valueOf(n - 1), MathContext.DECIMAL128);

        // Compute square root (since BigDecimal has no built-in sqrt)
        double sqrtValue = Math.sqrt(variance.doubleValue());

        return BigDecimal.valueOf(sqrtValue);
    }

    public static List<BigDecimal> calculateZScores(List<BigDecimal> data) {
        int n = data.size();
        if (n < 2) {
            throw new IllegalArgumentException("Sample size must be at least 2");
        }

        // Compute mean
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal num : data) {
            sum = sum.add(num);
        }
        BigDecimal mean = sum.divide(BigDecimal.valueOf(n), MathContext.DECIMAL128);

        // Compute sample standard deviation
        BigDecimal sampleSD = calculateSampleSD(data);

        if (sampleSD.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Standard deviation is zero, Z-score cannot be calculated.");
        }

        // Compute Z-scores: (x - mean) / standard deviation
        return data.stream()
                .map(num -> (num.subtract(mean)).divide(sampleSD, MathContext.DECIMAL128)
                    .setScale(3, RoundingMode.HALF_UP))
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        List<BigDecimal> sampleData = Arrays.asList(
                new BigDecimal("10"), new BigDecimal("12"),
                new BigDecimal("23"), new BigDecimal("23"),
                new BigDecimal("16"), new BigDecimal("23"),
                new BigDecimal("21"), new BigDecimal("16")
        );

        // Calculate Sample Standard Deviation
        BigDecimal sampleSD = calculateSampleSD(sampleData);
        System.out.println("Sample Standard Deviation: " + sampleSD);

        // Calculate Z-scores
        List<BigDecimal> zScores = calculateZScores(sampleData);
        System.out.println("Z-scores: " + zScores);
    }
}