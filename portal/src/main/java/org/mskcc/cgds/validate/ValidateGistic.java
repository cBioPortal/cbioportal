package org.mskcc.cgds.validate;

import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.Gistic;
import sun.net.idn.StringPrep;

import java.io.File;
import java.util.ArrayList;

// todo: later this can be refactored into a factory method.
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
        double res_qValue = gistic.getRes_qValue();
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
            System.out.println("peaksize=" + gistic.peakSize());
//            throw new validationException("" +  " " + peakEnd +  " " + peakStart);
        }

        if (qValue < 0 || qValue > 1) {
            throw new validationException(qValue);
        }

        if (res_qValue < 0 || res_qValue > 1) {
            throw new validationException(res_qValue);
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
