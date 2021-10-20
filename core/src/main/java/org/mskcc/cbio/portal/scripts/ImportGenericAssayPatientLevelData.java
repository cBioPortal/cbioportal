/*
 * Copyright (c) 2021 Memorial Sloan-Kettering Cancer Center.
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
import java.util.stream.*;

import org.cbioportal.model.EntityType;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

public class ImportGenericAssayPatientLevelData {
    private HashSet<Integer> importedGeneticEntitySet = new HashSet<>(); 
    private File dataFile;
    private String targetLine;
    private int geneticProfileId;
    private GeneticProfile geneticProfile;
    private int entriesSkipped = 0;
    private String genePanel;
    private String genericEntityProperties;

    private static final String ENTITY_STABLE_ID_COLUMN_NAME = "ENTITY_STABLE_ID";

    /**
     * Constructor.
     *
     * @param dataFile         Generic Assay Patient Level data file
     * @param targetLine       The line we want to import.
     *                         If null, all lines are imported.
     * @param geneticProfileId GeneticProfile ID.
     * @param genePanel        GenePanel
     * @param genericEntityProperties Generic Assay Entities.
     * 
     * @deprecated : TODO shall we deprecate this feature (i.e. the targetLine)? 
     */
    public ImportGenericAssayPatientLevelData(File dataFile, String targetLine, int geneticProfileId, String genePanel, String genericEntityProperties) {
        this.dataFile = dataFile;
        this.targetLine = targetLine;
        this.geneticProfileId = geneticProfileId;
        this.genePanel = genePanel;
        this.genericEntityProperties = genericEntityProperties;
    }

    /**
     * Import the patient level Generic Assay data
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
        
        int numRecordsToAdd = 0;
        int patientsSkipped = 0;
        try {
            int patientStartIndex = getPatientIdStartColumnIndex(parts);
            int genericAssayIdIndex = getGenericAssayIdIndex(parts);
            if (genericAssayIdIndex == -1) {
                throw new RuntimeException("Error: the following column should be present for this type of data: " + ENTITY_STABLE_ID_COLUMN_NAME);
            }
            
            String patientIds[];
            patientIds = new String[parts.length - patientStartIndex];
            System.arraycopy(parts, patientStartIndex, patientIds, 0, parts.length - patientStartIndex);

            ProgressMonitor.setCurrentMessage(" --> total number of patients: " + patientIds.length);

            // link Samples associated with patients to the genetic profile
            // 1. find samples associated with each patient
            // 2. link samples to the genetic profile
            ArrayList <Integer> orderedSampleList = new ArrayList<Integer>();
            int[][] numSamplesInPatient = new int[patientIds.length][1];
            int sampleCount = 0;
            for (int i = 0; i < patientIds.length; i++) {
                Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(geneticProfile.getCancerStudyId(), patientIds[i]);
                if (patient == null) {
                    throw new RuntimeException("Unknown patient id '" + StableIdUtil.getPatientId(patientIds[i]) + "' found in tab-delimited file: " + this.dataFile.getCanonicalPath());
                } else {
                    List<Sample> samples = DaoSample.getSamplesByPatientId(patient.getInternalId());
                    List<Integer> sampleInternalIds = samples.stream().map(sample -> sample.getInternalId()).collect(Collectors.toList());
                    for (int j = 0; j < sampleInternalIds.size(); j++) {
                        if (!DaoSampleProfile.sampleExistsInGeneticProfile(sampleInternalIds.get(j), geneticProfileId)) {
                            Integer genePanelID = (genePanel == null) ? null : GeneticProfileUtil.getGenePanelId(genePanel);
                            DaoSampleProfile.addSampleProfile(sampleInternalIds.get(j), geneticProfileId, genePanelID);
                        }
                        orderedSampleList.add(sampleInternalIds.get(j));
                    }
                    numSamplesInPatient[i][0] = samples.size();
                    sampleCount += samples.size();
                }
            }

            ProgressMonitor.setCurrentMessage(" --> total number of data lines:  " + (numLines-1));
            
            DaoGeneticProfileSamples.addGeneticProfileSamples(geneticProfileId, orderedSampleList);
    
            //Object to insert records in the generic 'genetic_alteration' table: 
            DaoGeneticAlteration daoGeneticAlteration = DaoGeneticAlteration.getInstance();

            // load entities map from database
            Map<String, Integer> genericAssayStableIdToEntityIdMap = GenericAssayMetaUtils.buildGenericAssayStableIdToEntityIdMap();
            
            String line = buf.readLine();
            while (line != null) {
                ProgressMonitor.incrementCurValue();
                ConsoleUtil.showProgress();
                boolean recordAdded = false;
                
                recordAdded = parseGenericAssayLine(line, parts.length, patientStartIndex, genericAssayIdIndex, numSamplesInPatient, sampleCount, daoGeneticAlteration, genericAssayStableIdToEntityIdMap);
                
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

    /**
     * Parses line for generic assay profile record and stores record in 'genetic_alteration' table.
     * @param line  row from the separated-text that contains one or more values on a single patient
     * @param nrColumns
     * @param patientStartIndex  index of the first patient column
     * @param genericAssayIdIndex  index of the column that uniquely identifies a patient
     * @param daoGeneticAlteration
     * @return
     * @throws DaoException 
     */

    private boolean parseGenericAssayLine(String line, int nrColumns, int patientStartIndex, int genericAssayIdIndex, int[][] numSamplesInPatient, int sampleCount, DaoGeneticAlteration daoGeneticAlteration, Map<String, Integer> genericAssayStableIdToEntityIdMap) throws DaoException {

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
            
            String[] values = new String[sampleCount];
            int maxColumns = parts.length>nrColumns?nrColumns:parts.length;
            int currentIndex = 0;
            for (int i = patientStartIndex; i < maxColumns; i++) {
                int patientIndex = i - patientStartIndex;
                if (patientIndex < numSamplesInPatient.length) {
                    for (int j = 0; j < numSamplesInPatient[patientIndex][0]; j++) {
                        values[currentIndex] = parts[i];
                        currentIndex++;
                    }
                }
            }

            // trim whitespace from values
            values = Stream.of(values).map(String::trim).toArray(String[]::new);

            String stableId = parts[genericAssayIdIndex];
            Integer entityId = genericAssayStableIdToEntityIdMap.getOrDefault(stableId, null);
            
            if (entityId ==  null) {
                ProgressMonitor.logWarning("Generic Assay entity " + parts[genericAssayIdIndex] + " not found in DB. Record will be skipped.");
            } else {
                recordIsStored = storeGeneticEntityGeneticAlterations(values, daoGeneticAlteration, entityId, stableId);
            }

            return recordIsStored;
        }

        return recordIsStored;
    }
    
    /**
     * Stores genetic alteration data for a genetic entity. 
     * @param values
     * @param daoGeneticAlteration
     * @param geneticEntityId - internal id for genetic entity
     * @param geneticEntityName - entity name for Generic Assay
     * @return boolean indicating if record was stored successfully or not
     */
    private boolean storeGeneticEntityGeneticAlterations(String[] values, DaoGeneticAlteration daoGeneticAlteration,
        Integer geneticEntityId, String geneticEntityName) {
        try {
            if (importedGeneticEntitySet.add(geneticEntityId)) {
                daoGeneticAlteration.addGeneticAlterationsForGeneticEntity(geneticProfile.getGeneticProfileId(), geneticEntityId, values);
                return true;
            }
            else {
                ProgressMonitor.logWarning("Data for genetic entity " + geneticEntityName 
                    + " [" + EntityType.GENERIC_ASSAY +"] already imported from file. Record will be skipped.");
                return false;
            }
        }
        catch (Exception ex) {
            throw new RuntimeException("Aborted: Error found for row starting with " + geneticEntityName + ": " + ex.getMessage());
        }
    }

    private int getGenericAssayIdIndex(String[] headers) {
        return getColIndexByName(headers, ENTITY_STABLE_ID_COLUMN_NAME);
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

    private int getPatientIdStartColumnIndex(String[] headers) {

        // one stable feature column ENTITY_STABLE_ID defined
        Integer startFeatureCol = 1;
        
        // list the names of feature columns here
        List<String> featureColNames = new ArrayList<String>();
        featureColNames.add(ENTITY_STABLE_ID_COLUMN_NAME);

        // add genericEntityProperties as feature colums
        if (genericEntityProperties != null && genericEntityProperties.trim().length() != 0) {
            String[] propertyNames = genericEntityProperties.trim().split(",");
            featureColNames.addAll(Arrays.asList(propertyNames));
        }

        int startIndex = -1;
        
        for (int i=0; i<headers.length; i++) {
            String h = headers[i];
            //if the column is not one of the pre-sample columns:
            // and the column is found after all non value columns that are passed in
            if ( featureColNames.stream().noneMatch(e -> e.equalsIgnoreCase(h))
                && i > startFeatureCol) {
                //then we consider this the start of the patient columns:
                startIndex = i;
                break;
            }
        }
        if (startIndex == -1)
            throw new RuntimeException("Could not find a patient column in the file");
        
        return startIndex;
    }
}
