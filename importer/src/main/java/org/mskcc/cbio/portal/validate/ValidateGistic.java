/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.validate;

import java.util.*;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.Gistic;

public class ValidateGistic {

    /**
     * Validates a gistic bean object according to some basic "business logic".
     * @param gistic
     * @throws validationException
     */
    public static void validateBean(Gistic gistic) throws validationException {
        
        int chromosome = gistic.getChromosome();
        int peakStart = gistic.getPeakStart();
        int peakEnd = gistic.getPeakEnd();
        double qValue = gistic.getqValue();
        ArrayList<CanonicalGene> genes_in_ROI = gistic.getGenes_in_ROI();

        if (chromosome < 1 || chromosome > 22) {
            throw new validationException("Invalid chromosome: " + chromosome);
        }

        if (peakStart <= 0) {
            throw new validationException("Invalid peak start: " + peakStart);
        }

        if (peakEnd <= 0) {
            throw new validationException("Invalid peak end: " + peakEnd);
        }

        if (peakEnd <= peakStart) {
            throw new validationException("Peak end is <= peak start: (start=" + peakStart + ", end=" + peakEnd + ")");
        }

        if (qValue < 0 || qValue > 1) {
            throw new validationException("Invalid qValue=" + qValue);
        }

        if (genes_in_ROI.isEmpty()){
            throw new validationException("No genes in ROI");
        }

        // todo: how do you validate ampdel?
    }
}
