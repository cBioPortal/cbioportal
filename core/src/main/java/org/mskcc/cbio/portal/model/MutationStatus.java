/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.model;

/**
 * Class for mutation status
 */
public class MutationStatus {
    public static final int MUTATED = 1;
    public static final int NOT_MUTATED = 0;
    public static final int NO_DATA = -9999;

    public static int getMutationStatus(String value) {
        //if (value < -1 || value > 1) {
        //    throw new IllegalArgumentException("Can't handle mutation value of:  " + value);
        //} else {
		return (value.equals("0")) ? NOT_MUTATED : MUTATED;
		//}
    }
}
