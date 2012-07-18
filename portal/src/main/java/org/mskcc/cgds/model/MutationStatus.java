package org.mskcc.cgds.model;

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
