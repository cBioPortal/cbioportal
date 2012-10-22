/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

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
