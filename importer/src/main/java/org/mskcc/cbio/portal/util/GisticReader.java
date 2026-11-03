/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.util;

import java.io.*;
import java.util.*;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.Gistic;
import org.mskcc.cbio.portal.scripts.ValidationUtils;

/**
 * Utility for importing Gistic data from a file
 */
public class GisticReader {

    public ArrayList<Gistic> parse(File gistic_f, int cancerStudyId) throws IOException, DaoException {

        ArrayList<Gistic> gistics = new ArrayList<Gistic>();

        FileReader reader = new FileReader(gistic_f);
        BufferedReader buf = new BufferedReader(reader);
        try {
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
	                throw e;
	            }
	            line = buf.readLine();
	        }
	
	        buf.close();
	        reader.close();
	        return gistics;
        }
        finally {
        	buf.close();
        }
    }
    
    private Gistic parseLine(String line, int cancerStudyId, int chromosomeField, int peakStartField, int peakEndField, int genesField, int qvalField, int ampField, int cytobandField) {
        String[] fields = line.split("\t");

        Gistic gistic = new Gistic();
        gistic.setCancerStudyId(cancerStudyId);

        int chromosomeNumber = ValidationUtils.validateChromosome(fields[chromosomeField]);
        gistic.setChromosome(chromosomeNumber);

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
            if (alreadyProcessedGenes.contains(canonicalGene)) {
            	String geneSymbolMessage = "";
	        	if (!gene.equalsIgnoreCase(canonicalGene.getHugoGeneSymbolAllCaps()))
	        		geneSymbolMessage = "(given as alias in your file as: " + gene + ") ";
	        	ProgressMonitor.logWarning("Gene " + canonicalGene.getHugoGeneSymbolAllCaps() + " (" + canonicalGene.getEntrezGeneId() + ")" + geneSymbolMessage + " found to be duplicated in your file. Skipping this duplicated entry of the gene.");
                continue;
            }
            if (canonicalGene != null) {
                if (canonicalGene.isMicroRNA()) {
                	ProgressMonitor.logWarning("ignoring miRNA: " + canonicalGene.getHugoGeneSymbolAllCaps());
                    continue;
                }

                genes.add(canonicalGene);
                alreadyProcessedGenes.add(canonicalGene);
            } 
            else {
            	ProgressMonitor.logWarning("Gene " + gene + " not found or was ambiguous. Skipping this gene.");
            }
            
        }
        // -- end parse genes --
        
        if (genes.size() == 0) {
        	ProgressMonitor.logWarning("No genes found in database for " + fields[genesField] + ". Skipping gistic event");
            return null;
        }
        gistic.setGenes_in_ROI(genes);

        return gistic;
    }
}
