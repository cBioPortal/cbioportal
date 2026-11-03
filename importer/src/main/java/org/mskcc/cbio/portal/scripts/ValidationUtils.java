/*
 * Copyright (c) 2016 The Hyve B.V.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.scripts;

import java.io.*;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.model.CancerStudy;

/**
 * Utility validation methods shared by different importers.
 * 
 *  @author pieterlukasse
 */
public class ValidationUtils {

    /**
     * Utility method to validate the chromosome. Should be 1-22 or X, Y. 
     * It also makes a small exception for 23 and 24: 23 is interpreted as X and 24 is interpreted as Y
     * 
     * @param chrom
     * 
     * @throws IllegalArgumentException when chromosome is invalid, or even NumberFormatException 
     * when not a number and other than X or Y.
     */
	public static int validateChromosome(String chrom) {
        if (chrom.equalsIgnoreCase("X"))
        	chrom = "23";
        if (chrom.equalsIgnoreCase("Y"))
        	chrom = "24";
        int chromNr = Integer.parseInt(chrom);
        if (chromNr > 24) {
        	throw new IllegalArgumentException("Error: Invalid chromosome [" + chrom + "]");
        }
        return chromNr;
	}

    /**
     * Gets internal cancer study id by stable id and throws an 
     * exception when study is not found.
     * 
     * @param cancerStudy String (e.g. "tcga_gbm")
     * @return the cancer study internal id
     * @throws IOException
     * @throws DaoException when study cannot be found
     */
    public static int getInternalStudyId(final String cancerStudyIdentifier) throws IOException, DaoException
    {
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyIdentifier);

        if (cancerStudy == null) {
            throw new DaoException("no CancerStudy associated with \""
                    + cancerStudyIdentifier + "\" cancer_study_identifier");
        }

        return cancerStudy.getInternalId();
	}
}
