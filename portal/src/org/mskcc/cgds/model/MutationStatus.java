package org.mskcc.cgds.model;

public class MutationStatus {
    public final static int MUTATED = 1;
    public final static int NOT_MUTATED = 0;
    public final static int NO_DATA = -9999;

    public static int getMutationStatus(String value) {
        //if (value < -1 || value > 1) {
        //    throw new IllegalArgumentException("Can't handle mutation value of:  " + value);
        //} else {
		return (value.equals("0")) ? NOT_MUTATED : MUTATED;
		//}
    }
}
