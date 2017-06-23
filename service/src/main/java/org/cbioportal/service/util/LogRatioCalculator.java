package org.cbioportal.service.util;

import org.springframework.stereotype.Component;

@Component
public class LogRatioCalculator {

    private static final double LOG2 = Math.log(2);

    public double getLogRatio(double a, double b) {
        
        if (a == 0 && b == 0) {
            return Double.NaN;  
        } else if (a == 0) {
            return Double.NEGATIVE_INFINITY;
        } else if (b == 0) {
            return Double.POSITIVE_INFINITY;
        } else {
            return Math.log(a / b) / LOG2;
        }
    }
}
