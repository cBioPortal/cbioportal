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
    
    public double getTwoTailedPValue(int a, int b, int c, int d) {

        int min, i;
        int n = a + b + c + d;
        double p = 0;
        double[] f = new double[n + 1];
        f[0] = 0.0;

        for (int j = 1; j <= n; j++) {
            f[j] = f[j - 1] + Math.log(j);
        }

        double baseP = getPValue(a, b, c, d, f);
//         in order for a table under consideration to have its p-value included
//         in the final result, it must have a p-value less than the baseP, i.e.
//         Fisher's exact test computes the probability, given the observed marginal
//         frequencies, of obtaining exactly the frequencies observed and any configuration more extreme.
//         By "more extreme," we mean any configuration (given observed marginals) with a smaller probability of
//         occurrence in the same direction (one-tailed) or in both directions (two-tailed).

        int initialA = a, initialB = b, initialC = c, initialD = d;
        p += baseP;

        min = (c < b) ? c : b;
        for (i = 0; i < min; i++) {
            double tempP = getPValue(++a, --b, --c, ++d, f);
            if (tempP <= baseP) {
                p += tempP;
            }
        }

        // reset the values to their original so we can repeat this process for the other side
        a = initialA;
        b = initialB;
        c = initialC;
        d = initialD;

        min = (a < d) ? a : d;
        for (i = 0; i < min; i++) {
            double pTemp = getPValue(--a, ++b, ++c, --d, f);
            if (pTemp <= baseP) {
                p += pTemp;
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
                    (pValuesInIncreasingOrder[i].multiply(new BigDecimal(dataLength))).divide(new BigDecimal(dataLength - i), BigDecimal.ROUND_HALF_UP)
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
