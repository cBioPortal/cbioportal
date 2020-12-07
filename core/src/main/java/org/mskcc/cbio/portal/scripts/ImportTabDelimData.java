/*
 * Copyright (c) 2015 - 2019 Memorial Sloan-Kettering Cancer Center.
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

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang.ArrayUtils;
import org.cbioportal.model.EntityType;
import org.cbioportal.model.GeneticEntity;
import org.cbioportal.model.meta.GenericAssayMeta;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;


/**
 * Code to Import Copy Number Alteration, MRNA Expression Data, Methylation, or protein RPPA data
 *
 * @author Ethan Cerami
 */
public class ImportTabDelimData {
    public static final String CNA_VALUE_AMPLIFICATION = "2";
    public static final String CNA_VALUE_GAIN = "1";
    public static final String CNA_VALUE_HEMIZYGOUS_DELETION = "-1";
    public static final String CNA_VALUE_HOMOZYGOUS_DELETION = "-2";
    public static final String CNA_VALUE_PARTIAL_DELETION = "-1.5";
    public static final String CNA_VALUE_ZERO = "0";
    private HashSet<Long> importSetOfGenes = new HashSet<Long>();
    private HashSet<Integer> importedGeneticEntitySet = new HashSet<>(); 
    private File dataFile;
    private String targetLine;
    private int geneticProfileId;
    private GeneticProfile geneticProfile;
    private int entriesSkipped = 0;
    private int nrExtraRecords = 0;
    private Set<String> arrayIdSet = new HashSet<String>();
    private String genePanel;
    private String genericEntityProperties;
    private File pdAnnotationsFile;
    private Map<Map.Entry<Integer, Long>, Map<String, String>> pdAnnotations;

    /**
     * Constructor.
     *
     * @param dataFile         Data File containing Copy Number Alteration, MRNA Expression Data, or protein RPPA data
     * @param targetLine       The line we want to import.
     *                         If null, all lines are imported.
     * @param geneticProfileId GeneticProfile ID.
     * @param genePanel        GenePanel
     * @param genericEntityProperties GeneticProfile ID.
     * 
     * @deprecated : TODO shall we deprecate this feature (i.e. the targetLine)? 
     */
    public ImportTabDelimData(File dataFile, String targetLine, int geneticProfileId, String genePanel, String genericEntityProperties) {
        this.dataFile = dataFile;
        this.targetLine = targetLine;
        this.geneticProfileId = geneticProfileId;
        this.genePanel = genePanel;
        this.genericEntityProperties = genericEntityProperties;
    }

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
    public ImportTabDelimData(File dataFile, String targetLine, int geneticProfileId, String genePanel) {
        this.dataFile = dataFile;
        this.targetLine = targetLine;
        this.geneticProfileId = geneticProfileId;
        this.genePanel = genePanel;
    }

    /**
     * Constructor.
     *
     * @param dataFile         Data File containing Copy Number Alteration, MRNA Expression Data, or protein RPPA data
     * @param geneticProfileId GeneticProfile ID.
     */
    public ImportTabDelimData(File dataFile, int geneticProfileId, String genePanel) {
        this.dataFile = dataFile;
        this.geneticProfileId = geneticProfileId;
        this.genePanel = genePanel;
    }

    /**
     * Import the Copy Number Alteration, mRNA Expression, protein RPPA, GSVA or generic_assay data
     *
     * @throws IOException  IO Error.
     * @throws DaoException Database Error.
     */
    public void importData(int numLines) throws IOException, DaoException {

        geneticProfile = DaoGeneticProfile.getGeneticProfileById(geneticProfileId);

        FileReader reader = new FileReader(dataFile);
        BufferedReader buf = new BufferedReader(reader);
        String headerLine = buf.readLine();
        String parts[] = headerLine.split("\t");
        
        //Whether data regards CNA or RPPA:
        boolean isDiscretizedCnaProfile = geneticProfile!=null
                                        && geneticProfile.getGeneticAlterationType() == GeneticAlterationType.COPY_NUMBER_ALTERATION
                                        && geneticProfile.showProfileInAnalysisTab();
        boolean isRppaProfile = geneticProfile!=null
                                && geneticProfile.getGeneticAlterationType() == GeneticAlterationType.PROTEIN_LEVEL
                                && "Composite.Element.Ref".equalsIgnoreCase(parts[0]);
        boolean isGsvaProfile = geneticProfile!=null
                                && geneticProfile.getGeneticAlterationType() == GeneticAlterationType.GENESET_SCORE
                                && parts[0].equalsIgnoreCase("geneset_id");
        boolean isGenericAssayProfile = geneticProfile!=null
                                && geneticProfile.getGeneticAlterationType() == GeneticAlterationType.GENERIC_ASSAY
                                && parts[0].equalsIgnoreCase("ENTITY_STABLE_ID");
        
        int numRecordsToAdd = 0;
        int samplesSkipped = 0;
        try {
            int hugoSymbolIndex = getHugoSymbolIndex(parts);
            int entrezGeneIdIndex = getEntrezGeneIdIndex(parts);
            int rppaGeneRefIndex = getRppaGeneRefIndex(parts);
            int genesetIdIndex = getGenesetIdIndex(parts);
            int sampleStartIndex = getStartIndex(parts, hugoSymbolIndex, entrezGeneIdIndex, rppaGeneRefIndex, genesetIdIndex);
            int genericAssayIdIndex = getGenericAssayIdIndex(parts);
            if (isRppaProfile) {
                if (rppaGeneRefIndex == -1) {
                    throw new RuntimeException("Error: the following column should be present for RPPA data: Composite.Element.Ref");
                }
            } else if (isGsvaProfile) {
                if (genesetIdIndex == -1) {
                    throw new RuntimeException("Error: the following column should be present for gene set score data: geneset_id");
                }
            } else if (isGenericAssayProfile) {
                if (genericAssayIdIndex == -1) {
                    throw new RuntimeException("Error: the following column should be present for this type of data: ENTITY_STABLE_ID");
                }
            } else if (hugoSymbolIndex == -1 && entrezGeneIdIndex == -1) {
                throw new RuntimeException("Error: at least one of the following columns should be present: Hugo_Symbol or Entrez_Gene_Id");
            }
            
            String sampleIds[];
            sampleIds = new String[parts.length - sampleStartIndex];
            System.arraycopy(parts, sampleStartIndex, sampleIds, 0, parts.length - sampleStartIndex);

            int nrUnknownSamplesAdded = 0;
            ProgressMonitor.setCurrentMessage(" --> total number of samples: " + sampleIds.length);

            Map<Map.Entry<String, Long>, Map<String, String>> pdAnnotationsForStableSampleIds = null;
            if (this.pdAnnotationsFile != null) {
                pdAnnotationsForStableSampleIds = readPdAnnotations(this.pdAnnotationsFile);
            }
            // link Samples to the genetic profile
            ArrayList <Integer> orderedSampleList = new ArrayList<Integer>();
            ArrayList <Integer> filteredSampleIndices = new ArrayList<Integer>();
            this.pdAnnotations = new HashMap<>();
            for (int i = 0; i < sampleIds.length; i++) {
                Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(geneticProfile.getCancerStudyId(),
                                                                           StableIdUtil.getSampleId(sampleIds[i]));
                // can be null in case of 'normal' sample, throw exception if not 'normal' and sample not found in db
                if (sample == null) {
                    if (StableIdUtil.isNormal(sampleIds[i])) {
                        filteredSampleIndices.add(i);
                        samplesSkipped++;
                        continue;
                    }
                    else {
                        throw new RuntimeException("Unknown sample id '" + StableIdUtil.getSampleId(sampleIds[i]) + "' found in tab-delimited file: " + this.dataFile.getCanonicalPath());
                    }
                }
                if (!DaoSampleProfile.sampleExistsInGeneticProfile(sample.getInternalId(), geneticProfileId)) {
                    Integer genePanelID = (genePanel == null) ? null : GeneticProfileUtil.getGenePanelId(genePanel);
                    DaoSampleProfile.addSampleProfile(sample.getInternalId(), geneticProfileId, genePanelID);
                }
                orderedSampleList.add(sample.getInternalId());
                if (pdAnnotationsForStableSampleIds != null) {
                    Set<Map.Entry<String, Long>> keys = new HashSet<>(pdAnnotationsForStableSampleIds.keySet());
                    for (Map.Entry<String, Long> stableSampleIdToGeneKey: keys) {
                        if (stableSampleIdToGeneKey.getKey().equals(sample.getStableId())) {
                            Long entrezGeneId = stableSampleIdToGeneKey.getValue();
                            Map<String, String> pdAnnotationsDetails = pdAnnotationsForStableSampleIds.get(stableSampleIdToGeneKey);
                            this.pdAnnotations.put(new AbstractMap.SimpleEntry<>(sample.getInternalId(), entrezGeneId), pdAnnotationsDetails);
                            pdAnnotationsForStableSampleIds.remove(stableSampleIdToGeneKey);
                        }
                    }
                }
            }
            if (pdAnnotationsForStableSampleIds != null && !pdAnnotationsForStableSampleIds.keySet().isEmpty()) {
                ProgressMonitor.logWarning("WARNING: Following pd annotation sample-entrezId pairs newer used in the data file:  " + pdAnnotationsForStableSampleIds.keySet());
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
            if (isDiscretizedCnaProfile) {
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
                boolean recordAdded = false;
                
                // either parse line as geneset or gene for importing into 'genetic_alteration' table
                if (isGsvaProfile) {
                    recordAdded = parseGenesetLine(line, lenParts, sampleStartIndex, genesetIdIndex, 
                            filteredSampleIndices, daoGeneticAlteration);
                } else if (isGenericAssayProfile) {
                    recordAdded = parseGenericAssayLine(line, lenParts, sampleStartIndex, genericAssayIdIndex, 
                            filteredSampleIndices, daoGeneticAlteration);
                } else {
                    recordAdded = parseLine(line, lenParts, sampleStartIndex, 
                            hugoSymbolIndex, entrezGeneIdIndex, rppaGeneRefIndex, 
                            isRppaProfile, isDiscretizedCnaProfile,
                            daoGene, 
                            filteredSampleIndices, orderedSampleList, 
                            existingCnaEvents, daoGeneticAlteration);
                }
                
                // increment number of records added or entries skipped
                if (recordAdded) {
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
            
            if (isRppaProfile) {
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
        finally {
            buf.close();
        }                
    }

    private Map<Map.Entry<String, Long>, Map<String, String>> readPdAnnotations(File pdAnnotationsFile) {
        Map<Map.Entry<String, Long>, Map<String, String>> pdAnnotations = new HashMap<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(pdAnnotationsFile));
            List<String> header = Arrays.asList(reader.readLine().toLowerCase().split("\t"));
            int sampleIdIndx = header.indexOf("sample_id");
            if (sampleIdIndx < 0) {
                throw new RuntimeException("SAMPLE_ID column is not found in " + pdAnnotationsFile.getAbsolutePath());
            }
            int entrezGeneIdIndx = header.indexOf("entrez_gene_id");
            if (entrezGeneIdIndx < 0) {
                throw new RuntimeException("Entrez_Gene_Id column is not found in " + pdAnnotationsFile.getAbsolutePath());
            }
            int driverFilterIndx = header.indexOf("cbp_driver");
            if (driverFilterIndx < 0) {
                throw new RuntimeException("cbp_driver column is not found in " + pdAnnotationsFile.getAbsolutePath());
            }
            int driverFilterAnnotationIndx = header.indexOf("cbp_driver_annotation");
            if (driverFilterAnnotationIndx < 0) {
                throw new RuntimeException("cbp_driver_annotation column is not found in " + pdAnnotationsFile.getAbsolutePath());
            }
            int driverTiersFilterIndx = header.indexOf("cbp_driver_tiers");
            if (driverTiersFilterIndx < 0) {
                throw new RuntimeException("cbp_driver_tiers column is not found in " + pdAnnotationsFile.getAbsolutePath());
            }
            int driverTiersFilterAnnotationIndx = header.indexOf("cbp_driver_tiers_annotation");
            if (driverTiersFilterAnnotationIndx < 0) {
                throw new RuntimeException("cbp_driver_tiers_annotation column is not found in " + pdAnnotationsFile.getAbsolutePath());
            }
            String line = reader.readLine();

            while (line != null) {
                String[] row = line.split("\t", -1);
                if (row.length < 6) {
                    throw new RuntimeException("Mis-formatted row: " + String.join(", ", row));
                }
                String stableSampleId = row[sampleIdIndx];
                Long entrezGeneId = Long.valueOf(row[entrezGeneIdIndx]);
                Map.Entry<String, Long> sampleGeneKey = new AbstractMap.SimpleEntry<>(stableSampleId, entrezGeneId);
                if (pdAnnotations.containsKey(sampleGeneKey)) {
                    throw new RuntimeException("There is more then one row with SAMPLE_ID=" + stableSampleId + " and Entrez_Gene_Id=" + entrezGeneId);
                }
                Map<String, String> driverInfo = new HashMap<>();
                driverInfo.put("DRIVER_FILTER", row[driverFilterIndx]);
                driverInfo.put("DRIVER_FILTER_ANNOTATION", row[driverFilterAnnotationIndx]);
                driverInfo.put("DRIVER_TIERS_FILTER", row[driverTiersFilterIndx]);
                driverInfo.put("DRIVER_TIERS_FILTER_ANNOTATION", row[driverTiersFilterAnnotationIndx]);

                pdAnnotations.put(sampleGeneKey, driverInfo);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
           throw new RuntimeException("Can't read PD annotation file", e);
        }
        return pdAnnotations;
    }

    /**
    * Attempt to create a genetic_alteration record based on the current line read from a profile data file.
    * <ol>
    *   <li>Commented out lines and blank lines are always skipped (returns false)
    *   <li>The line is split into columns by the tab delimiter
    *   <li>The involved genes (list of entrez_gene_ids) are determined:
    *     <ol>
    *       <li>Hugo_Symbol and Entrez_Gene_Id column indices are read and validated
    *       <li>if neither are available, the line is skipped
    *       <li>if Hugo_Symbol contains '///' or '---', the line is skipped
    *       <li>rppaProfile parsing has special rules for determining the involved genes
    *       <li>if Entrez_Gene_Id is available, use that to determine the involved genes
    *       <li>if Hugo_Symbol is available, use that to determine the involved genes (truncate symbols with '|' in them)
    *       <li>if the involved genes list is still empty, the line is skipped (returns false)
    *     </ol>
    *   <li>Both gene_alias and gene records are examined to see how many genes of type 'miRNA' are matched
    *   <li>If any matched record is of type 'miRNA':
    *     <ul>
    *       <li>Loop through each gene or gene_alias of type 'miRNA' and attempt to store the record under that gene in genetic_alteration
    *       <li>If no records were successfully stored in genetic_alteration, log the failure
    *     </ul>
    *   <li>If no matched record is of type 'miRNA':
    *       <li>if there is exactly 1 involved gene (using only the gene table if sufficient, or gene_alias if neccessary):
    *         <ol>
    *           <li>if this is a 'discretizedCnaProfile', normalize the CNA values and create a list of cnaEvents to be added
    *           <li>attempt to store the record in genetic_alteration
    *           <li>if the record is successfully stored (not duplicated), create (or update) records in sample_cna_event for the created list of cnaEvents (if any)
    *         </ol>
    *       <li>if there are several involved genes and the profile is an rppaProfile, loop through the genes; for each one:
    *         <ol>
    *           <li>attempt to store the record under that gene in genetic_alteration
    *           <li>count the number of successfully imported records (for logging)
    *         </ol>
    *         <ul>
    *           <li>after looping through all involved genes, check whether any records were successfully stored in genetic_alteration - if not log the failure
    *         </ul>
    *       <li>if there are several involved genes and the profile is not an rppaProfile, log a failure to import the current line due to ambiguous gene symbol
    *     </ol>
    *   <li>If a record was (or more than one were) successfully stored in genetic_alteration, return true ; else false
    * </ol>
    * <p>
    * During the import of any single profile data file, at most one record per Entrez_Gene_Id will be successfuly imported to genetic_alteration.
    * Each attempt to import is done through a call to the function storeGeneticAlterations().
    * That function will check an instance variable importSetOfGenes, and if the gene has been previously imported, no new attempt is made (failure).
    * Each time a gene is successfully imported, it is added to importSetOfGenes.
    * <p>
    * MicroRNA are treated specially because of the possible presence of constructed combination forms (such as 'MIR-100/100*' and 'MIR-100/100').
    * In these cases a Hugo_Symbol such as 'hsa-mir-100' may be expected to match the (fake) Entrez_Gene_Id for both of these combination forms.
    * In that case, we want to import several copies of the genetic alteration profile line .. one for each matched gene of type 'miRNA'.
    * This allows the visualization of both CNA event profiles for the microRNA precursor with expression profiles for the microRNA mature form.
    * <p>
    * The current implementation of this code does not attempt to "merge" / "unify" lines in the profile data file which have duplicated Entrez_Gene_Id.
    * Instead, the first encountered line which maps to the Entrez_Gene_Id will be stored as a record in genetic_alteration (returns true).
    * Later lines which attempt to store a record with that Entrez_Gene_Id will not be stored as a record in genetic_alteration (returns false).
    * For microRNA gene aliases it is possible that complex interactions will occur, where an earlier line in the data file stores a record under several Entrez_Gene_Ids, and a later line in the file fails to store records under some of those previously 'used' Entrez_Gene_Ids, but succeeds in storing a record under one or more not previously used Entrez_Gene_Ids. So a microRNA line from the file may be imported "partially successfully" (returns true).
    * <p>
    * Examples Cases:<br>
    * Gene records are P1, P2, P3, P4 (protein coding), M1, M2, M3 (microRNA).
    * Gene_Symbol AMA is gene_alias for M1 and M2, Gene_Symbol AMB is gene_alias for M2 and M3, Gene_Symbol AAMBIG is gene_alias for P3 and P4. Gene_Symbol AMIXED is gene_alias for P1 and M3.
    * <p>
    * Case_1 (the last two lines will be skipped and logged like "Gene P1 (#) found to be duplicated in your file. Duplicated row will be ignored!")<br>
    * <table>
    * <tr><th>Hugo_Symbol<th>Sample1<th>...
    * <tr><td>P1<td>0<td>...
    * <tr><td>P2<td>0<td>...
    * <tr><td>P1<td>0<td>...
    * <tr><td>P1<td>0<td>...
    * </table>
    * <p>
    * Case_2 (the last line will be skipped and logged like "Gene M1 (#) (given as alias in your file as: AMA) found to be duplicated in your file. Duplicated row will be ignored!" , "Gene M2 (#) (given as alias in your file as: AMA) found to be duplicated in your file. Duplicated row will be ignored!" , "Could not store microRNA or RPPA data" )<br>
    * <table>
    * <tr><th>Hugo_Symbol<th>Sample1<th>...
    * <tr><td>AMA<td>0<td>...
    * <tr><td>AMA<td>0<td>...
    * </table>
    * <p>
    * Case_3 (the last line in the file will fail with log messages like "Gene symbol AAMBIG found to be ambiguous. Record will be skipped for this gene.")<br>
    * <table>
    * <tr><th>Hugo_Symbol<th>Sample1<th>...
    * <tr><td>P1<td>0<td>...
    * <tr><td>P2<td>0<td>...
    * <tr><td>AAMBIG<td>0<td>...
    * </table>
    * <p>
    * Case_4 (the second to last line will partially succeed, storing a record in genetic_alteration for gene M3 but failing for M2 with a log message like "Gene M2 (#) (given as alias in your file as: AMB) found to be duplicated in your file. Duplicated row will be ignored!" ; the last line in the file will fail with log messages like "Gene M3 (#) (given as alias in your file as: AMIXED) found to be duplicated in your file. Duplicated row will be ignored!" , "Gene symbol AMIXED found to be ambiguous (a mixture of microRNA and other types). Record will be skipped for this gene.")<br>
    * <table>
    * <tr><th>Hugo_Symbol<th>Sample1<th>...
    * <tr><td>AMA<td>0<td>...
    * <tr><td>AMB<td>0<td>...
    * <tr><td>AMIXED<td>0<td>...
    * </table>
    *
    * @param  line                      the line from the profile data file to be parsed
    * @param  nrColumns                 the number of columns, defined by the header line
    * @param  sampleStartIndex          the index of the first column with a sample name in the header field
    * @param  hugoSymbolIndex           the index of the column Hugo_Symbol
    * @param  entrezGeneIdIndex         the index of the column Entrez_Gene_Id
    * @param  rppaGeneRefIndex          the index of the column Composite.Element.Ref
    * @param  isRppaProfile               true if this is an rppa profile (i.e. alteration type is PROTEIN_LEVEL and the first column is Composite.Element.Ref)
    * @param  isDiscretizedCnaProfile     true if this is a discretized CNA profile (i.e. alteration type COPY_NUMBER_ALTERATION and showProfileInAnalysisTab is true)
    * @param  daoGene                   an instance of DaoGeneOptimized ... for use in resolving gene symbols
    * @param  filteredSampleIndicesList not used (dead code)
    * @param  orderedSampleList         a list of the internal sample ids corresponding to the sample names in the header line
    * @param  existingCnaEvents         a collection of CnaEvents, to be added to or updated during parsing of individual lines
    * @param  daoGeneticAlteration      in instance of DaoGeneticAlteration ... for use in storing records in the genetic_alteration table
    * @return                           true if any record was stored in genetic_alteration, else false
    * @throws DaoException              if any DaoException is thrown while using daoGene or daoGeneticAlteration
    */
    private boolean parseLine(String line, int nrColumns, int sampleStartIndex, 
            int hugoSymbolIndex, int entrezGeneIdIndex, int rppaGeneRefIndex,
            boolean isRppaProfile, boolean isDiscretizedCnaProfile,
            DaoGeneOptimized daoGene,
            List <Integer> filteredSampleIndices, List <Integer> orderedSampleList,
            Map<CnaEvent.Event, CnaEvent.Event> existingCnaEvents, DaoGeneticAlteration daoGeneticAlteration
            ) throws DaoException {
        
        //TODO: refactor this entire function - split functionality into smaller units / subroutines

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
            if (isRppaProfile && geneSymbol == null) {
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
            } else {
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
                    if (isRppaProfile) {
                        genes = parseRPPAGenes(geneSymbol);
                        if (genes == null) {
                            //will be null when there is a parse error in this case, so we
                            //can return here and avoid duplicated messages:
                            return false;
                        }    
                        if (genes.isEmpty()) {
                            String gene = (geneSymbol != null) ? geneSymbol : entrez;
                            ProgressMonitor.logWarning("Gene not found for:  [" + gene
                                + "]. Ignoring it "
                                + "and all tab-delimited data associated with it!");
                            return false;
                        }
                    } else {
                        //try entrez:
                        if (entrez != null) {
                            CanonicalGene gene = daoGene.getGene(Long.parseLong(entrez));
                            if (gene != null) {
                                genes = Arrays.asList(gene);
                            }
                        }
                        //no entrez or could not resolve by entrez, try hugo:
                        if ((genes == null || genes.isEmpty()) && geneSymbol != null) {
                            // deal with multiple symbols separate by |, use the first one
                            int ix = geneSymbol.indexOf("|");
                            if (ix>0) {
                                geneSymbol = geneSymbol.substring(0, ix);
                            }
                            genes = daoGene.getGene(geneSymbol, true);
                        }
                        //if genes still null, skip current record
                        if (genes == null || genes.isEmpty()) {
                            ProgressMonitor.logWarning("Entrez_Id " + entrez + " not found. Record will be skipped for this gene.");
                            return false;
                        }
                    }

                    //  If targetLine is specified and does not match the current line, skip the current line.
                    if (targetLine != null && !(parts[0].equals(targetLine))) {
                        return false;
                    }

                    List<CanonicalGene> genesMatchingAnAlias = Collections.emptyList();
                    if (geneSymbol != null) {
                        genesMatchingAnAlias = daoGene.getGenesForAlias(geneSymbol);
                    }

                    Set<CanonicalGene> microRNAGenes = new HashSet<>();
                    Set<CanonicalGene> nonMicroRNAGenes = new HashSet<>();
                    Iterator<CanonicalGene> geneIterator = Stream.concat(genes.stream(), genesMatchingAnAlias.stream()).iterator();
                    while (geneIterator.hasNext()) {
                        CanonicalGene g = geneIterator.next();
                        if ("miRNA".equals(g.getType())) {
                            microRNAGenes.add(g);
                        } else {
                            nonMicroRNAGenes.add(g);
                        }
                    }
                    if (!microRNAGenes.isEmpty()) {
                        // for micro rna, duplicate the data
                        for (CanonicalGene gene : microRNAGenes) {
                            boolean result = storeGeneticAlterations(values, daoGeneticAlteration, gene, geneSymbol);
                            if (result == true) {
                                recordStored = true;
                            }
                        }
                        if (!recordStored) {
                            if (nonMicroRNAGenes.isEmpty()) {
                                // this means that no microRNA records could not be stored
                                ProgressMonitor.logWarning("Could not store microRNA data");
                            } else {
                                // this case :
                                //      - at least one of the entrez-gene-ids was not a microRNA
                                //      - all of the matched microRNA ids (if any) failed to be imported (presumably already imported on a prior line)
                                ProgressMonitor.logWarning("Gene symbol " + geneSymbol + " found to be ambiguous (a mixture of microRNA and other types). Record will be skipped for this gene.");
                            }
                            return false;
                        }
                    } else {
                        // none of the matched genes are type "miRNA"
                        if (genes.size() == 1) {
                            List<CnaEvent> cnaEventsToAdd = new ArrayList<CnaEvent>();
                        
                            if (isDiscretizedCnaProfile) {
                                long entrezGeneId = genes.get(0).getEntrezGeneId();
                                for (int i = 0; i < values.length; i++) {
                                 
                                    // temporary solution -- change partial deletion back to full deletion.
                                    if (values[i].equals(CNA_VALUE_PARTIAL_DELETION)) {
                                        values[i] = CNA_VALUE_HOMOZYGOUS_DELETION;
                                    }
                                    if (values[i].equals(CNA_VALUE_AMPLIFICATION) 
                                        // || values[i].equals(CNA_VALUE_GAIN)  >> skipping GAIN, ZERO, HEMIZYGOUS_DELETION to minimize size of dataset in DB
                                        // || values[i].equals(CNA_VALUE_ZERO)
                                        // || values[i].equals(CNA_VALUE_HEMIZYGOUS_DELETION)
                                            || values[i].equals(CNA_VALUE_HOMOZYGOUS_DELETION)) {
                                        Integer sampleId = orderedSampleList.get(i);
                                        CnaEvent cnaEvent = new CnaEvent(sampleId, geneticProfileId, entrezGeneId, Short.parseShort(values[i]));
                                        //delayed add:
                                        AbstractMap.SimpleEntry<Integer, Long> sampleGenePair = new AbstractMap.SimpleEntry<>(sampleId, entrezGeneId);
                                        Map<String, String> pdAnnotationDetails = this.pdAnnotations.get(sampleGenePair);
                                        if (pdAnnotationDetails != null) {
                                            cnaEvent.setDriverFilter(pdAnnotationDetails.get("DRIVER_FILTER"));
                                            cnaEvent.setDriverFilterAnnotation(pdAnnotationDetails.get("DRIVER_FILTER_ANNOTATION"));
                                            cnaEvent.setDriverTiersFilter(pdAnnotationDetails.get("DRIVER_TIERS_FILTER"));
                                            cnaEvent.setDriverTiersFilterAnnotation(pdAnnotationDetails.get("DRIVER_TIERS_FILTER_ANNOTATION"));
                                        }
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
                            if (isRppaProfile) { // for protein data, duplicate the data
                                for (CanonicalGene gene : genes) {
                                    boolean result = storeGeneticAlterations(values, daoGeneticAlteration, gene, geneSymbol);
                                    if (result == true) {
                                        recordStored = true;
                                        nrExtraRecords++;
                                    }
                                }
                                if (recordStored) {
                                    //skip one, to avoid double counting:
                                    nrExtraRecords--;
                                } else {
                                    // this means that RPPA could not be stored
                                    ProgressMonitor.logWarning("Could not store RPPA data");
                                }
                            } else {
                                if (!recordStored) {
                                    // this case :
                                    //      - the hugo gene symbol was ambiguous (matched multiple entrez-gene-ids)
                                    ProgressMonitor.logWarning("Gene symbol " + geneSymbol + " found to be ambiguous. Record will be skipped for this gene.");
                                }
                            }
                        }
                    }
                }
            }
        }
        return recordStored;
    }
    
    /**
     * Parses line for gene set record and stores record in 'genetic_alteration' table.
     * @param line
     * @param nrColumns
     * @param sampleStartIndex
     * @param genesetIdIndex
     * @param filteredSampleIndices
     * @param daoGeneticAlteration
     * @return
     * @throws DaoException 
     */
    private boolean parseGenesetLine(String line, int nrColumns, int sampleStartIndex, int genesetIdIndex,
             List<Integer> filteredSampleIndices, DaoGeneticAlteration daoGeneticAlteration) throws DaoException {
        boolean storedRecord = false;
        
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

            // trim whitespace from values
            values = Stream.of(values).map(String::trim).toArray(String[]::new);
            values = filterOutNormalValues(filteredSampleIndices, values);
            
            Geneset geneset = DaoGeneset.getGenesetByExternalId(parts[genesetIdIndex]);
            if (geneset !=  null) {
                storedRecord = storeGeneticEntityGeneticAlterations(values, daoGeneticAlteration, geneset.getGeneticEntityId(), 
                        EntityType.GENESET, geneset.getExternalId());
            }
            else {
                ProgressMonitor.logWarning("Geneset " + parts[genesetIdIndex] + " not found in DB. Record will be skipped.");
            }
        }
        return storedRecord;
    }

    /**
     * Parses line for generic assay profile record and stores record in 'genetic_alteration' table.
     * @param line  row from the separated-text that contains one or more values on a single sample
     * @param nrColumns
     * @param sampleStartIndex  index of the first sample column
     * @param genericAssayIdIndex  index of the column that uniquely identifies a sample
     * @param filteredSampleIndices
     * @param daoGeneticAlteration
     * @return
     * @throws DaoException 
     */

    private boolean parseGenericAssayLine(String line, int nrColumns, int sampleStartIndex, int genericAssayIdIndex,
             List<Integer> filteredSampleIndices, DaoGeneticAlteration daoGeneticAlteration) throws DaoException {

        boolean recordIsStored = false;
        
        if (!line.startsWith("#") && line.trim().length() > 0) {
            String[] parts = line.split("\t", -1);

            if (parts.length > nrColumns) {
                if (line.split("\t").length > nrColumns) {
                    ProgressMonitor.logWarning("Ignoring line with more fields (" + parts.length
                                        + ") than specified in the headers(" + nrColumns + "): \n"+parts[0]);
                    return false;
                }
            }
            
            String values[] = (String[]) ArrayUtils.subarray(parts, sampleStartIndex, parts.length>nrColumns?nrColumns:parts.length);

            // trim whitespace from values
            values = Stream.of(values).map(String::trim).toArray(String[]::new);
            values = filterOutNormalValues(filteredSampleIndices, values);
            
            GenericAssayMeta genericAssayMeta = DaoGenericAssay.getGenericAssayMetaByStableId(parts[genericAssayIdIndex]);
            
            if (genericAssayMeta ==  null) {
                ProgressMonitor.logWarning("Mutational Signature " + parts[genericAssayIdIndex] + " not found in DB. Record will be skipped.");
            } else {
                GeneticEntity geneticEntity = DaoGeneticEntity.getGeneticEntityByStableId(genericAssayMeta.getStableId());
                if (geneticEntity == null) {
                    ProgressMonitor.logWarning("Mutational Signature " + parts[genericAssayIdIndex] + " not found in DB. Record will be skipped.");
                }
                recordIsStored = storeGeneticEntityGeneticAlterations(values, daoGeneticAlteration, geneticEntity.getId(), 
                                    EntityType.GENERIC_ASSAY, genericAssayMeta.getStableId());
            }

            return recordIsStored;
        }

        return recordIsStored;
    }

    private boolean storeGeneticAlterations(String[] values, DaoGeneticAlteration daoGeneticAlteration,
            CanonicalGene gene, String geneSymbol) throws DaoException {
        //  Check that we have not already imported information regarding this gene.
        //  This is an important check, because a GISTIC or RAE file may contain
        //  multiple rows for the same gene, and we only want to import the first row.
        try {
            if (!importSetOfGenes.contains(gene.getEntrezGeneId())) {
                daoGeneticAlteration.addGeneticAlterations(geneticProfileId, gene.getEntrezGeneId(), values);
                importSetOfGenes.add(gene.getEntrezGeneId());
                return true;
            }
            else {
                //TODO - review this part - maybe it should be an Exception instead of just a warning.
                String geneSymbolMessage = "";
                if (geneSymbol != null && !geneSymbol.equalsIgnoreCase(gene.getHugoGeneSymbolAllCaps()))
                    geneSymbolMessage = " (given as alias in your file as: " + geneSymbol + ")";
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
     * Stores genetic alteration data for a genetic entity. 
     * @param values
     * @param daoGeneticAlteration
     * @param geneticEntityId - internal id for genetic entity
     * @param geneticEntityType - "GENE", "GENESET", "PHOSPHOPROTEIN"
     * @param geneticEntityName - hugo symbol for "GENE", external id for "GENESET", phospho gene name for "PHOSPHOPROTEIN"
     * @return boolean indicating if record was stored successfully or not
     */
    private boolean storeGeneticEntityGeneticAlterations(String[] values, DaoGeneticAlteration daoGeneticAlteration,
        Integer geneticEntityId, EntityType geneticEntityType, String geneticEntityName) {
        try {
            if (importedGeneticEntitySet.add(geneticEntityId)) {
                daoGeneticAlteration.addGeneticAlterationsForGeneticEntity(geneticProfile.getGeneticProfileId(), geneticEntityId, values);
                return true;
            }
            else {
                ProgressMonitor.logWarning("Data for genetic entity " + geneticEntityName 
                    + " [" + geneticEntityType +"] already imported from file. Record will be skipped.");
                return false;
            }
        }
        catch (Exception ex) {
            throw new RuntimeException("Aborted: Error found for row starting with " + geneticEntityName + ": " + ex.getMessage());
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
                CanonicalGene gene = daoGene.getNonAmbiguousGene(symbol, true);
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
        
        Pattern p = Pattern.compile("(p[STY][0-9]+(?:_[STY][0-9]+)*)");
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
                daoGene.addGene(phosphoGene);
            }
            phosphoGenes.add(phosphoGene);
        }
        return phosphoGenes;
    }

    
    // returns index for geneset id column
    private int getGenesetIdIndex(String[] headers) {
        return getColIndexByName(headers, "geneset_id");
    }

    private int getGenericAssayIdIndex(String[] headers) {
        return getColIndexByName(headers, "ENTITY_STABLE_ID");
    }
    
    private int getHugoSymbolIndex(String[] headers) {
        return getColIndexByName(headers, "Hugo_Symbol");
    }
    
    private int getEntrezGeneIdIndex(String[] headers) {
        return getColIndexByName(headers, "Entrez_Gene_Id");
    }
    
    private int getRppaGeneRefIndex(String[] headers) {
        return getColIndexByName(headers, "Composite.Element.Ref");
    }
    
    // helper function for finding the index of a column by name
    private int getColIndexByName(String[] headers, String colName) {
        for (int i=0; i<headers.length; i++) {
            if (headers[i].equalsIgnoreCase(colName)) {
                return i;
            }
        }
        return -1;
    }

    private int getStartIndex(String[] headers, int ...featureColIds) {

        // get the feature column index with the highest value
        Integer lastFeatureCol = IntStream.of(featureColIds).max().orElse(-1);
        
        // list the names of feature columns here
        List<String> featureColNames = new ArrayList<String>();
        featureColNames.add("Gene Symbol");
        featureColNames.add("Hugo_Symbol");
        featureColNames.add("Entrez_Gene_Id");
        featureColNames.add("Locus ID");
        featureColNames.add("Cytoband");
        featureColNames.add("Composite.Element.Ref");
        featureColNames.add("geneset_id");
        featureColNames.add("entity_stable_id");
        featureColNames.add("ENTITY_STABLE_ID");

        // add genericEntityProperties as the feature colum
        if (genericEntityProperties != null && genericEntityProperties.trim().length() != 0) {
            String[] propertyNames = genericEntityProperties.trim().split(",");
            featureColNames.addAll(Arrays.asList(propertyNames));
        }

        int startIndex = -1;
        
        for (int i=0; i<headers.length; i++) {
            String h = headers[i];
            //if the column is not one of the gene symbol/gene id columns or other pre-sample columns:
            // and the column is found after all non value columns that are passed in
            if ( featureColNames.stream().noneMatch(e -> e.equalsIgnoreCase(h))
                && i > lastFeatureCol) {
                //then we consider this the start of the sample columns:
                startIndex = i;
                break;
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

    public File getPdAnnotationsFile() {
        return pdAnnotationsFile;
    }

    public void setPdAnnotationsFile(File pdAnnotationsFile) {
        this.pdAnnotationsFile = pdAnnotationsFile;
    }
}
