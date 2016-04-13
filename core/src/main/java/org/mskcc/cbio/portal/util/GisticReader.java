/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.util;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.Gistic;
import org.mskcc.cbio.portal.validate.ValidateGistic;
import org.mskcc.cbio.portal.validate.validationException;
import org.springframework.ui.context.Theme;

import java.io.*;
import java.lang.System;
import java.sql.SQLException;
import java.util.*;

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
    
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(GisticReader.class);

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
            throw new IllegalStateException("The field: chromosome, is missing");
        }

        if (peakStartField == -1) {
            throw new IllegalStateException("The field: peak start, is missing");
        }

        if (peakEndField == -1) {
            throw new IllegalStateException("The field: peak end, is missing");
        }

        if (genesField == -1) {
            throw new IllegalStateException("The field: genes, is missing");
        }

        if (qvalField == -1) {
            throw new IllegalStateException("The field: q_value, is missing");
        }

        if (cytobandField == -1) {
            throw new IllegalStateException("The field: cytoband, is missing");
        }

        if (ampField == -1) {
            throw new IllegalStateException("The field: amp, is missing");
        }

        line = buf.readLine();
        while (line != null) {
            Gistic gistic;
            try {
                gistic = this.parseLine(line, cancerStudyId, chromosomeField, peakStartField, peakEndField, genesField, qvalField, ampField, cytobandField);
                if (gistic != null) {
                    gistics.add(gistic);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            line = buf.readLine();
        }

        buf.close();
        reader.close();
        return gistics;
    }
    
    private Gistic parseLine(String line, int cancerStudyId, int chromosomeField, int peakStartField, int peakEndField, int genesField, int qvalField, int ampField, int cytobandField) {
        String[] fields = line.split("\t");

        Gistic gistic = new Gistic();
        gistic.setCancerStudyId(cancerStudyId);

        if (fields[chromosomeField].equalsIgnoreCase("X"))
            fields[chromosomeField] = "23";
        if (fields[chromosomeField].equalsIgnoreCase("Y"))
            fields[chromosomeField] = "24";
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
        ArrayList<CanonicalGene> alreadyProcessedGenes = new ArrayList<CanonicalGene>();
        for (String gene : _genes) {
            
            gene = gene.split("\\|")[0];

            CanonicalGene canonicalGene = daoGene.getNonAmbiguousGene(gene);

            if (canonicalGene != null && !alreadyProcessedGenes.contains(canonicalGene)) {
                if (canonicalGene.isMicroRNA()) {
                    System.err.println("ignoring miRNA: " + canonicalGene.getHugoGeneSymbolAllCaps());
                    continue;
                }

                genes.add(canonicalGene);
                alreadyProcessedGenes.add(canonicalGene);
            }
        }
        // -- end parse genes --
        
        if (genes.size() == 0) {
            logger.info("No genes found in database - skipping gistic event");
            return null;
        }
        gistic.setGenes_in_ROI(genes);

        return gistic;
    }
}
