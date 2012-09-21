package org.mskcc.cbio.cgds.util;

import org.mskcc.cbio.cgds.dao.*;
import org.mskcc.cbio.cgds.model.CancerStudy;
import org.mskcc.cbio.cgds.model.CanonicalGene;
import org.mskcc.cbio.cgds.model.Gistic;
import org.mskcc.cbio.cgds.validate.ValidateGistic;
import org.mskcc.cbio.cgds.validate.validationException;
import org.springframework.ui.context.Theme;

import java.io.*;
import java.lang.System;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/**
 * Utility for importing Gistic data from a file
 */
public class GisticReader {

    /**
     * Extracts find the database's internal Id for the record
     * associated with the Cancer Study string
     * @param cancerStudy_str   String (e.g. "tcga_gbm")
     * @return                  CancerStudyId
     * @throws DaoException
     */

    public int getCancerStudyInternalId(String cancerStudy_str)
            throws DaoException {
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudy_str);

        if (cancerStudy == null) {
            throw new DaoException(cancerStudy_str);
        }
        return cancerStudy.getInternalId();
    }

    public ArrayList<Gistic> parse(File gistic_f, int cancerStudyId) throws IOException, DaoException {

        ArrayList<Gistic> gistics = new ArrayList<Gistic>();

        FileReader reader = new FileReader(gistic_f);
        BufferedReader buf = new BufferedReader(reader);

        String line = buf.readLine();

        // -- parse field names --
        // todo: would it be better to use <enums>?

        int chromosomeField = -1;
        int peakStartField = -1;
        int peakEndField = -1;
        int genesField = -1;
        int qvalField = -1;
        int ampField = -1;
        int cytobandField = -1;

        String[] fields = line.split("\t");
        int num_fields = fields.length;

        for (int i = 0 ; i < num_fields; i+=1) {
            if (fields[i].equals("chromosome")) {
                chromosomeField = i;
            }

            else if (fields[i].equals("peak_start")) {
                peakStartField = i;
            }

            else if (fields[i].equals("peak_end")) {
                peakEndField = i;
            }

            else if (fields[i].equals("genes_in_region")) {
                genesField = i;
            }

            else if (fields[i].equals("q_value")) {
                qvalField = i;
            }

            else if (fields[i].equals("cytoband")) {
                cytobandField = i;
            }

            else if (fields[i].equals("amp")) {
                ampField = i;
            }
        }

        if (chromosomeField == -1) {
            System.out.println("The field: chromosome, is missing");
            System.exit(1);
        }

        if (peakStartField == -1) {
            System.out.println("The field: peak start, is missing");
            System.exit(1);
        }

        if (peakEndField == -1) {
            System.out.println("The field: peak end, is missing");
            System.exit(1);
        }

        if (genesField == -1) {
            System.out.println("The field: genes, is missing");
            System.exit(1);
        }

        if (qvalField == -1) {
            System.out.println("The field: q_value, is missing");
            System.exit(1);
        }

        if (cytobandField == -1) {
            System.out.println("The field: cytoband, is missing");
            System.exit(1);
        }

        if (ampField == -1) {
            System.out.println("The field: amp, is missing");
            System.exit(1);
        }

        line = buf.readLine();
        while (line != null) {

            fields = line.split("\t");

            Gistic gistic = new Gistic();
            gistic.setCancerStudyId(cancerStudyId);

            gistic.setChromosome(Integer.parseInt(fields[chromosomeField]));
            gistic.setPeakStart(Integer.parseInt(fields[peakStartField]));
            gistic.setPeakEnd(Integer.parseInt(fields[peakEndField]));

            int amp = Integer.parseInt(fields[ampField]);
            gistic.setAmp(amp == 1);

            gistic.setCytoband(fields[cytobandField]);
            gistic.setqValue((Float.parseFloat(fields[qvalField])));

            // -- parse genes --

            // parse out '[' and ']' chars and         ** Do these brackets have meaning? **
            String[] _genes = fields[genesField].replace("[","")
                    .replace("]", "")
                    .split(",");

            // map _genes to list of CanonicalGenes
            ArrayList<CanonicalGene> genes = new ArrayList<CanonicalGene>();
            DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
            for (String gene : _genes) {

                // TODO: when gene mapping has implemented, map miRNA appropriately, for now miRNA is silently ignored
                if (gene.contains("hsa")) {
                    //System.out.println("ignoring miRNA : " + gene);
                    continue;
                }

                CanonicalGene canonicalGene = daoGene.getNonAmbiguousGene(gene);

                if (canonicalGene == null) {
                    canonicalGene = new CanonicalGene(gene);

//                    System.out.println("gene not found, skipping: " + gene);
//                    throw new DaoException("gene not found: " + gene);
                }

                genes.add(canonicalGene);
            }
            // -- end parse genes --

            gistic.setGenes_in_ROI(genes);

            gistics.add(gistic);
            line = buf.readLine();
        }

        buf.close();
        reader.close();
        return gistics;
    }
}
