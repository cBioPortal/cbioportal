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

package org.mskcc.cbio.portal.validate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.Gistic;

import java.util.ArrayList;

// todo: later this can be refactored into a factory method.
public class ValidateGistic {
    private static final Log logger = LogFactory.getLog(ValidateGistic.class);

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
            throw new validationException(chromosome);
        }

        if (peakStart <= 0) {
            throw new validationException(peakStart);
        }

        if (peakEnd <= 0) {
            throw new validationException(peakEnd);
        }

        if (peakEnd <= peakStart) {
            throw new validationException("" +  " " + peakEnd +  " " + peakStart);
//            System.out.println("peaksize=" + gistic.peakSize());
        }

        if (qValue < 0 || qValue > 1) {
            throw new validationException("qValue=" + qValue);
        }

        if (genes_in_ROI.isEmpty()){
            throw new validationException(genes_in_ROI);
        }

        // todo: how do you validate ampdel?
    }

    /**
     * Checks to make sure that we know all the fields that we are getting.
     * @param fields
     * @throws validationException
     */

    public static void validateFieldNames_tabularFile(String[] fields) throws validationException {
        int fields_len = fields.length;

        for (int i = 0; i < fields_len; i+=1) {
            // each field should be one of the following
            if (!(fields[i].equals("chromosome")
                    || fields[i].equals("peak_start")
                    || fields[i].equals("peak_end")
                    || fields[i].equals("genes_in_region")
                    || fields[i].equals("genes_in_peak")
                    || fields[i].equals("n_genes_on_chip")
                    || fields[i].equals("genes_on_chip")
                    || fields[i].equals("top 3")
                    || fields[i].equals("n_genes_in_region")
                    || fields[i].equals("n_genes_in_peak")
                    || fields[i].equals("region_start")
                    || fields[i].equals("region_end")
                    || fields[i].equals("enlarged_peak_start")
                    || fields[i].equals("enlarged_peak_end")
                    || fields[i].equals("index")))
            {
//                System.out.println(fields.toString());
                throw new validationException(fields[i]);
            }
        }
    }

    /**
     * Checks to make sure that we know all the fields that we are getting.
     * @param cols
     * @throws validationException
     */
    public static void validateNonTabularRow(String[] cols) throws validationException {
        int cols_len = cols.length;

        String field_name = cols[0];

        for (int i = 0; i < cols_len; i+=1) {
            // each field should be one of the following
            if (!((field_name.equals("q value")
                    || field_name.equals("residual q value"))
                    || field_name.equals("genes in wide peak")
                    || field_name.equals("cytoband")
                    || field_name.equals("")                    // likely to actually contain a gene
                    || field_name.equals("wide peak boundaries"))) {
                {
                    throw new validationException(field_name);
                }
            }
        }
    }

    /**
     * Validates that two files are either both amplified for both deleted
     * @param ampdel1       ampdel from one gistic file
     * @param ampdel2       ampdel from another gistic file
     * @throws validationException
     */
    public static void validateAmpdels(boolean ampdel1, boolean ampdel2) throws validationException {

        if (ampdel1 != ampdel2) {
            String x = ampdel1 == Gistic.AMPLIFIED ? "Amplified" : "Deleted";
            String y = ampdel2 == Gistic.AMPLIFIED ? "Amplified" : "Deleted";
            throw new validationException(x + " " + y);
        }
    }
}
