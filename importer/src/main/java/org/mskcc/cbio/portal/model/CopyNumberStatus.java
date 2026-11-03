/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.model;

/**
 * Class for copy number status
 */
public class CopyNumberStatus {
    public static final int COPY_NUMBER_AMPLIFICATION = 2;
    public static final int COPY_NUMBER_GAIN = 1;
    public static final int NO_CHANGE = 0;
    public static final int HEMIZYGOUS_DELETION = -1;
    public static final int HOMOZYGOUS_DELETION = -2;
    public static final int NO_DATA = -9999;

    public static int getCNAStatus(int entrezGeneId, String caseId, String valueStr) {

		try {
			Double value = Double.valueOf(valueStr);
			if (value < -2 || value > 2) {
				throw new IllegalArgumentException("Can't handle CNA value of:  " + value
                                        + " Case ID:  " + caseId + ", Entrez Gene ID:  " + entrezGeneId);
			} else {
				if (value > 2) {
					return COPY_NUMBER_AMPLIFICATION;
				} else {
					return value.intValue();
				}
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Can't handle CNA value of:  " + valueStr
                                + " Case ID:  " + caseId + ", Entrez Gene ID:  " + entrezGeneId);
		}
		
	}
}