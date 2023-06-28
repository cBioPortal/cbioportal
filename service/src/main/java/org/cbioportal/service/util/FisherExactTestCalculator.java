package org.cbioportal.service.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FisherExactTestCalculator {

    private double getPValue(int a, int b, int c, int d, double[] f) {
        
        int n = a + b + c + d;
        double p = (f[a + b] + f[c + d] + f[a + c] + f[b + d]) - (f[a] + f[b] + f[c] + f[d] + f[n]);
        return Math.exp(p);
    }

    public double getCumulativePValue(int a, int b, int c, int d) {
        
        int min, i;
        int n = a + b + c + d;
        double p = 0;
        double[] f = new double[n + 1];
        f[0] = 0.0;
        
        for (int j = 1; j <= n; j++) {
            f[j] = f[j - 1] + Math.log(j);
        }

        p += getPValue(a, b, c, d, f);
        if ((a * d) >= (b * c)) {
            min = (c < b) ? c : b;
            for (i = 0; i < min; i++) {
                p += getPValue(++a, --b, --c, ++d, f);
            }
        }
        
        if ((a * d) < (b * c)) {
            min = (a < d) ? a : d;
            for (i = 0; i < min; i++) {
                p += getPValue(--a, ++b, ++c, --d, f);
            }
        }
        return p;
    }

    public BigDecimal[] calcqValue(BigDecimal[] pValuesInIncreasingOrder) {
        BigDecimal cachedElement = BigDecimal.valueOf(0.0);
        int dataLength = pValuesInIncreasingOrder.length;
        BigDecimal[]  reversedQValues = new BigDecimal[dataLength];

        reverseValues(dataLength, pValuesInIncreasingOrder);

        for (int i = 0; i < dataLength; i++) {
            if (i > 0) {
                BigDecimal calculatedValue = cachedElement.min(
                    (pValuesInIncreasingOrder[i].multiply(new BigDecimal(dataLength))).divide(new BigDecimal(dataLength - i))
                );
                cachedElement = calculatedValue;
                reversedQValues[i] = calculatedValue;
            } else {
                cachedElement = pValuesInIncreasingOrder[i];
                reversedQValues[i] = pValuesInIncreasingOrder[i];
            }
        }

        reverseValues(dataLength, reversedQValues);

        return reversedQValues;
    }

    private void reverseValues(int dataLength, BigDecimal[]  reversedQValues) {
        for (int i = 0; i < dataLength / 2; i++) {
            BigDecimal temp = reversedQValues[i];
            reversedQValues[i] = reversedQValues[dataLength - i - 1];
            reversedQValues[dataLength - i - 1] = temp;
        }
    }
}
