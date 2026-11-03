/*
 * Copyright (c) 2021 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.dao.BackupUtil;
import org.mskcc.cbio.portal.dao.ClickHouseBulkLoader;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneticAlteration;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoGeneticProfileSamples;
import org.mskcc.cbio.portal.dao.DaoPatient;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.dao.DaoSampleProfile;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.Patient;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.model.shared.EntityType;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.FileUtil;
import org.mskcc.cbio.portal.util.GeneticProfileUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.util.StableIdUtil;
import org.mskcc.cbio.portal.util.TsvUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ImportGenericAssayPatientLevelData {
    private HashSet<Integer> importedGeneticEntitySet = new HashSet<>(); 
    private File dataFile;
    private String targetLine;
    private int geneticProfileId;
    private GeneticProfile geneticProfile;
    private int entriesSkipped = 0;
    private String genePanel;
    private String genericEntityProperties;
    private boolean isIncrementalUpdateMode;

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
     * @param isIncrementalUpdateMode if true, update/append data to the existing one
     *
     * @deprecated : TODO shall we deprecate this feature (i.e. the targetLine)?
     */
    public ImportGenericAssayPatientLevelData(File dataFile, String targetLine, int geneticProfileId, String genePanel, String genericEntityProperties, boolean isIncrementalUpdateMode) {
        this.dataFile = dataFile;
        this.targetLine = targetLine;
        this.geneticProfileId = geneticProfileId;
        this.genePanel = genePanel;
        this.genericEntityProperties = genericEntityProperties;
        this.isIncrementalUpdateMode = isIncrementalUpdateMode;
    }

    public ImportGenericAssayPatientLevelData(File dataFile, String targetLine, int geneticProfileId, String genePanel, String genericEntityProperties) {
        this(dataFile, targetLine, geneticProfileId, genePanel, genericEntityProperties, false);
    }

    /**
     * Import the patient level Generic Assay data
     *
     * @throws IOException  IO Error.
     * @throws DaoException Database Error.
     */
    public void importData() throws Exception {
        BackupUtil.conditionalBackup(isIncrementalUpdateMode, List.of("genetic_alteration", "genetic_profile_samples"), this::importDataInternal);
    }

    void importDataInternal() throws Exception {
        int numLines = FileUtil.getNumLines(dataFile);

        geneticProfile = DaoGeneticProfile.getGeneticProfileById(geneticProfileId);

        //Object to insert records in the generic 'genetic_alteration' table:
        DaoGeneticAlteration daoGeneticAlteration = DaoGeneticAlteration.getInstance();

        int numRecordsToAdd = 0;
        try (
                FileReader reader = new FileReader(dataFile);
                BufferedReader buf = new BufferedReader(reader);
        ) {
            ProgressMonitor.setCurrentMessage("Importing Generic Assay Patient Level data from file: " + dataFile.getCanonicalPath());

            String headerLine = buf.readLine();
            String parts[] = headerLine.split("\t");

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
                    samples.forEach(sample -> orderedSampleList.add(sample.getInternalId()));
                    numSamplesInPatient[i][0] = samples.size();
                    sampleCount += samples.size();
                }
            }
            Integer genePanelID = (genePanel == null) ? null : GeneticProfileUtil.getGenePanelId(genePanel);
            DaoSampleProfile.upsertSampleToProfileMapping(orderedSampleList, geneticProfileId, genePanelID);

            ProgressMonitor.setCurrentMessage(" --> total number of data lines:  " + (numLines-1));
            
            DaoGeneticProfileSamples.addGeneticProfileSamples(geneticProfileId, orderedSampleList);
    
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
            if (ClickHouseBulkLoader.isBulkLoad()) {
               ClickHouseBulkLoader.flushAll();
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
        
        if (TsvUtil.isDataLine(line)) {
            String[] parts = TsvUtil.splitTsvLine(line);

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
