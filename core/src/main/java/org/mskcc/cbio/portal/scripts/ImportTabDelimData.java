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

package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Code to Import Copy Number Alteration, MRNA Expression Data, Methylation, or protein RPPA data
 *
 * @author Ethan Cerami
 */
public class ImportTabDelimData {
    private HashSet<Long> importedGeneSet = new HashSet<Long>();

    private File mutationFile;
    private String targetLine;
    private int geneticProfileId;
    private GeneticProfile geneticProfile;
    private int entriesSkipped = 0;
    private int nrExtraRecords = 0;
    private Set<String> arrayIdSet = new HashSet<String>();

    /**
     * Constructor.
     *
     * @param dataFile         Data File containing Copy Number Alteration, MRNA Expression Data, or protein RPPA data
     * @param targetLine       The line we want to import.
     *                         If null, all lines are imported.
     * @param geneticProfileId GeneticProfile ID.
     * 
     * @deprecated : TODO shall we deprecate this feature (i.e. the targetLine)? 
     */
    public ImportTabDelimData(File dataFile, String targetLine, int geneticProfileId) {
        this.mutationFile = dataFile;
        this.targetLine = targetLine;
        this.geneticProfileId = geneticProfileId;
    }

    /**
     * Constructor.
     *
     * @param dataFile         Data File containing Copy Number Alteration, MRNA Expression Data, or protein RPPA data
     * @param geneticProfileId GeneticProfile ID.
     */
    public ImportTabDelimData(File dataFile, int geneticProfileId) {
        this.mutationFile = dataFile;
        this.geneticProfileId = geneticProfileId;
    }

    /**
     * Import the Copy Number Alteration, MRNA Expression Data, or protein RPPA data
     *
     * @throws IOException  IO Error.
     * @throws DaoException Database Error.
     */
    public void importData(int numLines) throws IOException, DaoException {

        geneticProfile = DaoGeneticProfile.getGeneticProfileById(geneticProfileId);

        FileReader reader = new FileReader(mutationFile);
        BufferedReader buf = new BufferedReader(reader);
        String headerLine = buf.readLine();
        String parts[] = headerLine.split("\t");
        
        //Whether data regards CNA or RPPA:
        boolean discritizedCnaProfile = geneticProfile!=null
                                        && geneticProfile.getGeneticAlterationType() == GeneticAlterationType.COPY_NUMBER_ALTERATION
                                        && geneticProfile.showProfileInAnalysisTab();
        boolean rppaProfile = geneticProfile!=null
                                && geneticProfile.getGeneticAlterationType() == GeneticAlterationType.PROTEIN_LEVEL
                                && "Composite.Element.Ref".equalsIgnoreCase(parts[0]);
        int numRecordsToAdd = 0;
        int samplesSkipped = 0;
        try {
        	int hugoSymbolIndex = getHugoSymbolIndex(parts);
	        int entrezGeneIdIndex = getEntrezGeneIdIndex(parts);
	        int rppaGeneRefIndex = getRppaGeneRefIndex(parts);
	        int sampleStartIndex = getStartIndex(parts, hugoSymbolIndex, entrezGeneIdIndex, rppaGeneRefIndex);
	        if (rppaProfile) {
	        	if (rppaGeneRefIndex == -1)
	        		throw new RuntimeException("Error: the following column should be present for RPPA data: Composite.Element.Ref");
	        }	
	        else if (hugoSymbolIndex == -1 && entrezGeneIdIndex == -1)
	        	throw new RuntimeException("Error: at least one of the following columns should be present: Hugo_Symbol or Entrez_Gene_Id");
	        
	        String sampleIds[];
	        sampleIds = new String[parts.length - sampleStartIndex];
	        System.arraycopy(parts, sampleStartIndex, sampleIds, 0, parts.length - sampleStartIndex);

	        int nrUnknownSamplesAdded = 0;
	        ProgressMonitor.setCurrentMessage(" --> total number of samples: " + sampleIds.length);	        
	
	        // link Samples to the genetic profile
	        ArrayList <Integer> orderedSampleList = new ArrayList<Integer>();
	        ArrayList <Integer> filteredSampleIndices = new ArrayList<Integer>();
	        for (int i = 0; i < sampleIds.length; i++) {
	        	// backwards compatible part (i.e. in the new process, the sample should already be there. TODO - replace this workaround later with an exception:
	            Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(geneticProfile.getCancerStudyId(),
	                                                                       StableIdUtil.getSampleId(sampleIds[i]));
				if (sample == null ) {
					//TODO - as stated above, this part should be removed. Agreed with JJ to remove this as soon as MSK moves to new validation 
			        //procedure. In this new procedure, Patients and Samples should only be added 
			        //via the corresponding ImportClinicalData process. Furthermore, the code below is wrong as it assumes one 
			        //sample per patient, which is not always the case.
					ImportDataUtil.addPatients(new String[] { sampleIds[i] }, geneticProfileId);
	                // add the sample (except if it is a 'normal' sample):
					nrUnknownSamplesAdded += ImportDataUtil.addSamples(new String[] { sampleIds[i] }, geneticProfileId);
				}
		        // check again (repeated because of workaround above):
				sample = DaoSample.getSampleByCancerStudyAndSampleId(geneticProfile.getCancerStudyId(),
                                                                           StableIdUtil.getSampleId(sampleIds[i]));
		        // can be null in case of 'normal' sample:
	           if (sample == null) {
	                assert StableIdUtil.isNormal(sampleIds[i]);
	                filteredSampleIndices.add(i);
	                samplesSkipped++;
	                continue;
	           }
	           if (!DaoSampleProfile.sampleExistsInGeneticProfile(sample.getInternalId(), geneticProfileId)) {
	               DaoSampleProfile.addSampleProfile(sample.getInternalId(), geneticProfileId);
	           }
	           orderedSampleList.add(sample.getInternalId());
	        }
	        if (nrUnknownSamplesAdded > 0) {
	        	ProgressMonitor.logWarning("WARNING: Number of samples added on the fly because they were missing in clinical data:  " + nrUnknownSamplesAdded);
	        }
	        if (samplesSkipped > 0) {
	        	ProgressMonitor.setCurrentMessage(" --> total number of samples skipped (normal samples): " + samplesSkipped);
	        }
	        ProgressMonitor.setCurrentMessage(" --> total number of data lines:  " + (numLines-1));
	        
	        DaoGeneticProfileSamples.addGeneticProfileSamples(geneticProfileId, orderedSampleList);
	
	        //Gene cache:
	        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
	
	        //Object to insert records in the generic 'genetic_alteration' table: 
	        DaoGeneticAlteration daoGeneticAlteration = DaoGeneticAlteration.getInstance();
	        
	        //cache for data found in  cna_event' table:
	        Map<CnaEvent.Event, CnaEvent.Event> existingCnaEvents = null;	        
	        if (discritizedCnaProfile) {
	            existingCnaEvents = new HashMap<CnaEvent.Event, CnaEvent.Event>();
	            for (CnaEvent.Event event : DaoCnaEvent.getAllCnaEvents()) {
	                existingCnaEvents.put(event, event);
	            }
	            MySQLbulkLoader.bulkLoadOn();
	        }
	        
	        int lenParts = parts.length;
	        
	        String line = buf.readLine();
	        while (line != null) {
	            ProgressMonitor.incrementCurValue();
	            ConsoleUtil.showProgress();
	        	if (parseLine(line, lenParts, sampleStartIndex, 
	        			hugoSymbolIndex, entrezGeneIdIndex, rppaGeneRefIndex,
	        			rppaProfile, discritizedCnaProfile, 
	        			daoGene, 
	        			filteredSampleIndices, orderedSampleList, 
	        			existingCnaEvents, daoGeneticAlteration)) {
	        		numRecordsToAdd++;
	        	}
	        	else {
	        		entriesSkipped++;
	        	}
	            line = buf.readLine();
	        }
	        if (MySQLbulkLoader.isBulkLoad()) {
	           MySQLbulkLoader.flushAll();
	        }
        }
        finally {
	        buf.close();
	        if (rppaProfile) {
	        	ProgressMonitor.setCurrentMessage(" --> total number of extra records added because of multiple genes in one line:  " + nrExtraRecords);
	        }
	        if (entriesSkipped > 0) {
	        	ProgressMonitor.setCurrentMessage(" --> total number of data entries skipped (see table below):  " + entriesSkipped);
	        }

	        if (numRecordsToAdd == 0) {
	            throw new DaoException ("Something has gone wrong!  I did not save any records" +
	                    " to the database!");
	        }
        }
        
    }
    
    private boolean parseLine(String line, int nrColumns, int sampleStartIndex, 
    		int hugoSymbolIndex, int entrezGeneIdIndex, int rppaGeneRefIndex,
    		boolean rppaProfile, boolean discritizedCnaProfile,
    		DaoGeneOptimized daoGene,
    		List <Integer> filteredSampleIndices, List <Integer> orderedSampleList,
    		Map<CnaEvent.Event, CnaEvent.Event> existingCnaEvents, DaoGeneticAlteration daoGeneticAlteration
    		) throws DaoException {
        
    	boolean recordStored = false; 
    	
        //  Ignore lines starting with #
        if (!line.startsWith("#") && line.trim().length() > 0) {
            String[] parts = line.split("\t",-1);
            
            if (parts.length>nrColumns) {
                if (line.split("\t").length>nrColumns) {
                    ProgressMonitor.logWarning("Ignoring line with more fields (" + parts.length
                            + ") than specified in the headers(" + nrColumns + "): \n"+parts[0]);
                    return false;
                }
            }
            String values[] = (String[]) ArrayUtils.subarray(parts, sampleStartIndex, parts.length>nrColumns?nrColumns:parts.length);
            values = filterOutNormalValues(filteredSampleIndices, values);

            String geneSymbol = null;
            if (hugoSymbolIndex != -1) {
            	geneSymbol = parts[hugoSymbolIndex];
            }
            //RPPA: //TODO - we should split up the RPPA scenario from this code...too many if/else because of this
            if (rppaGeneRefIndex != -1) {
            	geneSymbol = parts[rppaGeneRefIndex];
            }
            if (geneSymbol!=null && geneSymbol.isEmpty()) {
                geneSymbol = null;
            }
            if (rppaProfile && geneSymbol == null) {
            	ProgressMonitor.logWarning("Ignoring line with no Composite.Element.REF value");
            	return false;
            }
            //get entrez
            String entrez = null;
            if (entrezGeneIdIndex!=-1) {
                entrez = parts[entrezGeneIdIndex];
            }
            if (entrez!=null) {
            	if (entrez.isEmpty()) {
            		entrez = null;
            	}
            	else if (!entrez.matches("[0-9]+")) {
            		//TODO - would be better to give an exception in some cases, like negative Entrez values
            		ProgressMonitor.logWarning("Ignoring line with invalid Entrez_Id " + entrez);
                	return false;
            	}            	
            }
            
            //If all are empty, skip line:
            if (geneSymbol == null && entrez == null) {
            	ProgressMonitor.logWarning("Ignoring line with no Hugo_Symbol or Entrez_Id value");
            	return false;
            }
            else {
                if (geneSymbol != null && (geneSymbol.contains("///") || geneSymbol.contains("---"))) {
                    //  Ignore gene IDs separated by ///.  This indicates that
                    //  the line contains information regarding multiple genes, and
                    //  we cannot currently handle this.
                    //  Also, ignore gene IDs that are specified as ---.  This indicates
                    //  the line contains information regarding an unknown gene, and
                    //  we cannot currently handle this.
                    ProgressMonitor.logWarning("Ignoring gene ID:  " + geneSymbol);
                    return false;
                } else {
                	List<CanonicalGene> genes = null;
                	//If rppa, parse genes from "Composite.Element.REF" column:
                	if (rppaProfile) {
                        genes = parseRPPAGenes(geneSymbol);
                        if (genes == null) {
                        	//will be null when there is a parse error in this case, so we 
                        	//can return here and avoid duplicated messages:
                        	return false;
                        }	
                    }
                	else {
	                	//try entrez:
	                    if (entrez!=null) {
	                        CanonicalGene gene = daoGene.getGene(Long.parseLong(entrez));
	                        if (gene!=null) {
	                            genes = Arrays.asList(gene);
	                        }
	                        else {
	                        	ProgressMonitor.logWarning("Entrez_Id " + entrez + " not found. Record will be skipped for this gene.");
	                        	return false;
	                        }
	                    } 
	                    //no entrez, try hugo:
	                    if (genes==null && geneSymbol != null) {
	                        // deal with multiple symbols separate by |, use the first one
	                        int ix = geneSymbol.indexOf("|");
	                        if (ix>0) {
	                            geneSymbol = geneSymbol.substring(0, ix);
	                        }
	
	                        genes = daoGene.getGene(geneSymbol, true);
	                    }
                	}

                    if (genes == null || genes.isEmpty()) {
                        genes = Collections.emptyList();
                    }

                    //  If no target line is specified or we match the target, process.
                    if (targetLine == null || parts[0].equals(targetLine)) {
                        if (genes.isEmpty()) {
                            //  if gene is null, we might be dealing with a micro RNA ID
                            if (geneSymbol != null && geneSymbol.toLowerCase().contains("-mir-")) {
//                                if (microRnaIdSet.contains(geneId)) {
//                                    storeMicroRnaAlterations(values, daoMicroRnaAlteration, geneId);
//                                    numRecordsStored++;
//                                } else {
                                    ProgressMonitor.logWarning("microRNA is not known to me:  [" + geneSymbol
                                        + "]. Ignoring it "
                                        + "and all tab-delimited data associated with it!");
                                    return false;
//                                }
                            } else {
                                String gene = (geneSymbol != null) ? geneSymbol : entrez;
                                ProgressMonitor.logWarning("Gene not found for:  [" + gene
                                    + "]. Ignoring it "
                                    + "and all tab-delimited data associated with it!");
                                return false;
                            }
                        } else if (genes.size()==1) {
                        	List<CnaEvent> cnaEventsToAdd = new ArrayList<CnaEvent>();
                        	
                            if (discritizedCnaProfile) {
                                long entrezGeneId = genes.get(0).getEntrezGeneId();
                                int n = values.length;
                                if (n==0)
                                    System.out.println();
                                int i = values[0].equals(""+entrezGeneId) ? 1:0;
                                for (; i<n; i++) {
                                    
                                    // temporary solution -- change partial deletion back to full deletion.
                                    if (values[i].equals(GeneticAlterationType.PARTIAL_DELETION)) {
                                        values[i] = GeneticAlterationType.HOMOZYGOUS_DELETION;
                                    }
                                    
                                    if (values[i].equals(GeneticAlterationType.AMPLIFICATION) 
                                           // || values[i].equals(GeneticAlterationType.GAIN)  >> skipping GAIN, ZERO, HEMIZYGOUS_DELETION to minimize size of dataset in DB
                                           // || values[i].equals(GeneticAlterationType.ZERO)
                                           // || values[i].equals(GeneticAlterationType.HEMIZYGOUS_DELETION)
                                            || values[i].equals(GeneticAlterationType.HOMOZYGOUS_DELETION)) {
                                        CnaEvent cnaEvent = new CnaEvent(orderedSampleList.get(i), geneticProfileId, entrezGeneId, Short.parseShort(values[i]));
                                        //delayed add:
                                        cnaEventsToAdd.add(cnaEvent);
                                    }
                                }
                            }
                            recordStored = storeGeneticAlterations(values, daoGeneticAlteration, genes.get(0), geneSymbol);
                            //only add extra CNA related records if the step above worked, otherwise skip:
                            if (recordStored) {
	                            for (CnaEvent cnaEvent : cnaEventsToAdd) {
		                            if (existingCnaEvents.containsKey(cnaEvent.getEvent())) {
		                                cnaEvent.setEventId(existingCnaEvents.get(cnaEvent.getEvent()).getEventId());
		                                DaoCnaEvent.addCaseCnaEvent(cnaEvent, false);
		                            } else {
		                            	//cnaEvent.setEventId(++cnaEventId); not needed anymore, column now has AUTO_INCREMENT 
		                                DaoCnaEvent.addCaseCnaEvent(cnaEvent, true);
		                                existingCnaEvents.put(cnaEvent.getEvent(), cnaEvent.getEvent());
		                            }
	                            }
                            }                            
                        } else {
                        	//TODO - review: is this still correct?
                        	int otherCase = 0;
                            for (CanonicalGene gene : genes) {
                            	if (gene.isMicroRNA() || rppaProfile) { // for micro rna or protein data, duplicate the data
	                            	boolean result = storeGeneticAlterations(values, daoGeneticAlteration, gene, geneSymbol);
	                            	if (result == true) {
	                            		recordStored = true;
	                            		nrExtraRecords++;
	                            	}
                            	}
                            	else {
                            		otherCase++;
                            	}
                            }
                            if (recordStored) {
                            	//skip one, to avoid double counting:
                            	nrExtraRecords--;
                            }
                            if (!recordStored) {
		                        if (otherCase > 1) {
		                        	//this means that genes.size() > 1 and data was not rppa or microRNA, so it is not defined how to deal with 
		                        	//the ambiguous alias list. Report this:
		                        	ProgressMonitor.logWarning("Gene symbol " + geneSymbol + " found to be ambiguous. Record will be skipped for this gene.");
		                        }
		                        else { 
		                        	//should not occur:
		                        	throw new RuntimeException("Unexpected error: unable to process row with gene " + geneSymbol);
	                            }
                        	}
                        }
                    }
                }
            }
        }
        return recordStored;
	}

	private boolean storeGeneticAlterations(String[] values, DaoGeneticAlteration daoGeneticAlteration,
            CanonicalGene gene, String geneSymbol) throws DaoException {
		//  Check that we have not already imported information regarding this gene.
        //  This is an important check, because a GISTIC or RAE file may contain
        //  multiple rows for the same gene, and we only want to import the first row.
		try {
	        if (!importedGeneSet.contains(gene.getEntrezGeneId())) {
	            daoGeneticAlteration.addGeneticAlterations(geneticProfileId, gene.getEntrezGeneId(), values);
	            importedGeneSet.add(gene.getEntrezGeneId());
	            return true;
	        }
	        else {
	        	//TODO - review this part - maybe it should be an Exception instead of just a warning.
	        	String geneSymbolMessage = "";
	        	if (geneSymbol != null && !geneSymbol.equalsIgnoreCase(gene.getHugoGeneSymbolAllCaps()))
	        		geneSymbolMessage = "(given as alias in your file as: " + geneSymbol + ") ";
	        	ProgressMonitor.logWarning("Gene " + gene.getHugoGeneSymbolAllCaps() + " (" + gene.getEntrezGeneId() + ")" + geneSymbolMessage + " found to be duplicated in your file. Duplicated row will be ignored!");
	        	return false;
	        }
		}
		catch (Exception e)
		{
			throw new RuntimeException("Aborted: Error found for row starting with " + geneSymbol + ": " + e.getMessage());
		}
    }
    
	/**
	 * Tries to parse the genes and look them up in DaoGeneOptimized
	 * 
	 * @param antibodyWithGene
	 * @return returns null if something was wrong, e.g. could not parse the antibodyWithGene string; returns 
	 * a list with 0 or more elements otherwise.
	 * @throws DaoException
	 */
	private List<CanonicalGene> parseRPPAGenes(String antibodyWithGene) throws DaoException {
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        String[] parts = antibodyWithGene.split("\\|");
        //validate:
        if (parts.length < 2) {
        	ProgressMonitor.logWarning("Could not parse Composite.Element.Ref value " + antibodyWithGene + ". Record will be skipped.");
        	//return null when there was a parse error:
        	return null;
        }
        String[] symbols = parts[0].split(" ");
        String arrayId = parts[1];
        //validate arrayId: if arrayId if duplicated, warn:
        if (!arrayIdSet.add(arrayId)) {
        	ProgressMonitor.logWarning("Id " + arrayId + " in [" + antibodyWithGene + "] found to be duplicated. Record will be skipped.");
        	return null;
        }
        List<String> symbolsNotFound = new ArrayList<String>();
        List<CanonicalGene> genes = new ArrayList<CanonicalGene>();
        for (String symbol : symbols) {
        	if (symbol.equalsIgnoreCase("NA")) {
        		//workaround because of bug in firehose. See https://github.com/cBioPortal/cbioportal/issues/839#issuecomment-203523078
        		ProgressMonitor.logWarning("Gene " + symbol + " will be interpreted as 'Not Available' in this case. Record will be skipped for this gene.");
        	}
        	else {
	            CanonicalGene gene = daoGene.getNonAmbiguousGene(symbol, null);
	            if (gene!=null) {
	                genes.add(gene);
	            }
	            else {
	            	symbolsNotFound.add(symbol);
	            }
        	}
        }
        if (genes.size() == 0) {
        	//return empty list:
        	return genes;
        }
        //So one or more genes were found, but maybe some were not found. If any 
        //is not found, report it here:
        for (String symbol : symbolsNotFound) {
        	ProgressMonitor.logWarning("Gene " + symbol + " not found in DB. Record will be skipped for this gene.");
        }
        
        Pattern p = Pattern.compile("(p[STY][0-9]+)");
        Matcher m = p.matcher(arrayId);
        String residue;
        if (!m.find()) {
            //type is "protein_level":
            return genes;
        } else {
            //type is "phosphorylation":
            residue = m.group(1);
            return importPhosphoGene(genes, residue);
        }
    }
    
    private List<CanonicalGene> importPhosphoGene(List<CanonicalGene> genes, String residue) throws DaoException {
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        List<CanonicalGene> phosphoGenes = new ArrayList<CanonicalGene>();
        for (CanonicalGene gene : genes) {
            Set<String> aliases = new HashSet<String>();
            aliases.add("rppa-phospho");
            aliases.add("phosphoprotein");
            aliases.add("phospho"+gene.getStandardSymbol());
            String phosphoSymbol = gene.getStandardSymbol()+"_"+residue;
            CanonicalGene phosphoGene = daoGene.getGene(phosphoSymbol);
            if (phosphoGene==null) {
            	ProgressMonitor.logWarning("Phosphoprotein " + phosphoSymbol + " not yet known in DB. Adding it to `gene` table with 3 aliases in `gene_alias` table.");
                phosphoGene = new CanonicalGene(phosphoSymbol, aliases);
                phosphoGene.setType(CanonicalGene.PHOSPHOPROTEIN_TYPE);
                phosphoGene.setCytoband(gene.getCytoband());
                daoGene.addGene(phosphoGene);
            }
            phosphoGenes.add(phosphoGene);
        }
        return phosphoGenes;
    }
    
    private int getHugoSymbolIndex(String[] headers) {
    	for (int i = 0; i<headers.length; i++) {
            if (headers[i].equalsIgnoreCase("Hugo_Symbol")) {
                return i;
            }
        }
        return -1;
    }
    
    private int getEntrezGeneIdIndex(String[] headers) {
        for (int i = 0; i<headers.length; i++) {
            if (headers[i].equalsIgnoreCase("Entrez_Gene_Id")) {
                return i;
            }
        }
        return -1;
    }

    private int getRppaGeneRefIndex(String[] headers) {
        for (int i = 0; i<headers.length; i++) {
            if (headers[i].equalsIgnoreCase("Composite.Element.Ref")) {
                return i;
            }
        }
        return -1;
    }
    
    private int getStartIndex(String[] headers, int hugoSymbolIndex, int entrezGeneIdIndex, int rppaGeneRefIndex) {
        int startIndex = -1;
        
        for (int i=0; i<headers.length; i++) {
            String h = headers[i];
            //if the column is not one of the gene symbol/gene ide columns or other pre-sample columns:
            if (!h.equalsIgnoreCase("Gene Symbol") &&
                    !h.equalsIgnoreCase("Hugo_Symbol") &&
                    !h.equalsIgnoreCase("Entrez_Gene_Id") &&
                    !h.equalsIgnoreCase("Locus ID") &&
                    !h.equalsIgnoreCase("Cytoband") &&
                    !h.equalsIgnoreCase("Composite.Element.Ref")) {
            	//and the column is found after  hugoSymbolIndex and entrezGeneIdIndex: 
            	if (i > hugoSymbolIndex && i > entrezGeneIdIndex && i > rppaGeneRefIndex) {
            		//then we consider this the start of the sample columns:
                	startIndex = i;
                	break;
            	}
            }
        }
        if (startIndex == -1)
        	throw new RuntimeException("Could not find a sample column in the file");
        
        return startIndex;
    }

    private String[] filterOutNormalValues(List <Integer> filteredSampleIndices, String[] values)
    {
        ArrayList<String> filteredValues = new ArrayList<String>();
        for (int lc = 0; lc < values.length; lc++) {
            if (!filteredSampleIndices.contains(lc)) {
                filteredValues.add(values[lc]);
            }
        }
        return filteredValues.toArray(new String[filteredValues.size()]);
    }
}
