package org.mskcc.cbio.portal.util;

/**
 * Derived from the web:  http://stackoverflow.com/questions/356807/java-double-comparison-epsilon
 */
public class DoubleUtil {

    private final static double EPSILON = 0.00001;
    /**
     * Returns true if two doubles are considered equal.  Tests if the absolute
     * difference between two doubles has a difference less then .00001.   This
     * should be fine when comparing prices, because prices have a precision of
     * .001.
     *
     * @param a double to compare.
     * @param b double to compare.
     * @return true true if two doubles are considered equal.
     */
    public static boolean equals(double a, double b){
            return a == b ? true : Math.abs(a - b) < EPSILON;
    }
    /**
     * Returns true if two doubles are considered equal. Tests if the absolute
     * difference between the two doubles has a difference less then a given
     * double (epsilon). Determining the given epsilon is highly dependant on the
     * precision of the doubles that are being compared.
     *
     * @param a double to compare.
     * @param b double to compare
     * @param epsilon double which is compared to the absolute difference of two
     * doubles to determine if they are equal.
     * @return true if a is considered equal to b.
     */
    public static boolean equals(double a, double b, double epsilon){
            return a == b ? true : Math.abs(a - b) < epsilon;
    }
}
