package org.cbioportal.service.util;

import org.springframework.stereotype.Component;

@Component
public class FisherExactTestCalculator {

    private double getPValue(int a, int b, int c, int d, double[] f) {
        int n = a + b + c + d;
        double p =
            (f[a + b] + f[c + d] + f[a + c] + f[b + d]) -
            (f[a] + f[b] + f[c] + f[d] + f[n]);
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
}
