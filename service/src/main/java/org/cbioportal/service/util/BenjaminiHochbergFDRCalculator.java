package org.cbioportal.service.util;

import org.springframework.stereotype.Component;

@Component
public class BenjaminiHochbergFDRCalculator {

    public double[] calculate(double[] pValues) {

        int length = pValues.length;
        double[] adjustedPValues = new double[length];
        
        for (int i = length - 1; i >= 0; i--) {
            
            if (i == length - 1) {
                adjustedPValues[i] = pValues[i];
            } else {
                double unadjustedPValue = pValues[i];
                double left = adjustedPValues[i + 1];
                double right = (length / (double) (i + 1)) * unadjustedPValue;
                adjustedPValues[i] = Math.min(left, right);
            }
        }
        
        return adjustedPValues;
    }
}
