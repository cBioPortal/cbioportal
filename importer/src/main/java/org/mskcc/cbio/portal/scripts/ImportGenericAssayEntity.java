/*
 * Copyright (c) 2019 The Hyve B.Vs.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

/**
* @author Pim van Nierop, pim@thehyve.nl
*/

package org.mskcc.cbio.portal.scripts;

import java.io.*;
import java.util.*;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.mskcc.cbio.portal.dao.DaoGenericAssay;
import org.mskcc.cbio.portal.dao.DaoGeneticEntity;
import org.mskcc.cbio.portal.model.shared.EntityType;
import org.mskcc.cbio.portal.model.shared.GenericEntityProperty;
import org.mskcc.cbio.portal.model.shared.GeneticAlterationType;
import org.mskcc.cbio.portal.model.shared.GeneticEntity;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.util.TsvUtil;

/**
* Note; Imports genetic entities from generic assay files. Has been written for treatment response data
* but is intended to be used for generic assay data in the future. The importer adds data to the treatment
* table. In later rounds of implementation of generic assay types this table should be renamed to fi. 
* `assay`. Also all remaining references to 'treatment(s)' should be removed from the importer code.  
*
* @author Pim van Nierop, pim@thehyve.nl
*/
public class ImportGenericAssayEntity extends ConsoleRunnable {

    public ImportGenericAssayEntity(String[] args) {
        super(args);
    }

    public ImportGenericAssayEntity(File dataFile, EntityType entityType, String additionalProperties, boolean updateInfo) {
        // fake the console arguments required by the ConsoleRunnable class
        super( new String[]{"--data", dataFile.getAbsolutePath(), "--entity-type", entityType.name(), "--additional-properties", additionalProperties, "--update-info", updateInfo?"1":"0"});
	}

	@Override
    public void run() {
        try {
            String progName = "ImportGenericAssay";
            String description = "Import generic assay records from generic assay response files.";
            
            OptionParser parser = new OptionParser();
            OptionSpec<String> data = parser.accepts("data", "Generic assay data file")
            .withRequiredArg().ofType(String.class);

            // require entity type
            OptionSpec<String> entityType = parser.accepts("entity-type", "Entity type")
            .withRequiredArg().ofType(String.class);

            // don't require additional properties
            OptionSpec<String> additionalProperties = parser.accepts("additional-properties", "Additional properties need to be imported")
            .withOptionalArg().ofType(String.class);

            // don't require updateInfo, default as true
            OptionSpec<String> updateInfoArg = parser.accepts("update-info", "Update information for existing entities in the database")
            .withOptionalArg().ofType(String.class);
            
            OptionSet options = null;
            try {
                options = parser.parse(args);
            }
            catch (Exception ex) {
                throw new UsageException(
                progName, description, parser,
                ex.getMessage());
            }
            
            // if neither option was set then throw UsageException
            if (!options.has(data)) {
                throw new UsageException(
                progName, description, parser,
                "'data' argument required");
            }

            // if no entity type then throw UsageException
            if (!options.has(entityType)) {
                throw new UsageException(
                progName, description, parser,
                "'entityType' argument required");
            }
            
            // Check options, set default as false
            boolean updateInfo = false;
            if (options.has("update-info") && (options.valueOf(updateInfoArg).equalsIgnoreCase("true") || options.valueOf(updateInfoArg).equals("1"))) {
                updateInfo = true;
            }
            
            ProgressMonitor.setCurrentMessage("Adding new generic assay to the database\n");
            startImport(options, data, entityType, additionalProperties, updateInfo);
            
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
    * Start import process for gene set file and supplementary file.
    *
    * @param updateInfo
    */
    public static void startImport(OptionSet options, OptionSpec<String> data, OptionSpec<String> geneticAlterationType, OptionSpec<String> additionalProperties, boolean updateInfo) throws Exception {
        if (options.hasArgument(data) && options.hasArgument(geneticAlterationType)) {
            File genericAssayFile = new File(options.valueOf(data));
            GeneticAlterationType geneticAlterationTypeArg = GeneticAlterationType.valueOf(options.valueOf(geneticAlterationType));
            String additionalPropertiesArg = options.valueOf(additionalProperties);
            importData(genericAssayFile, geneticAlterationTypeArg, additionalPropertiesArg, updateInfo);
        }
    }
    
    /**
    * Imports feature columns from generic assay file.
    *
    * @param dataFile
    * @param geneticAlterationType
    * @param additionalProperties
    * @throws Exception
    */
    public static void importData(File dataFile, GeneticAlterationType geneticAlterationType, String additionalProperties, boolean updateInfo) throws Exception {
        ProgressMonitor.setCurrentMessage("Reading data from: " + dataFile.getCanonicalPath());
        
        // read generic assay data file
        FileReader reader = new FileReader(dataFile);
        BufferedReader buf = new BufferedReader(reader);
        String currentLine = buf.readLine();
        String[] headerNames = currentLine.split("\t");
        
        // read generic assay data
        int indexStableIdField = getStableIdIndex(headerNames);

        // entities have been overriden
        List<String> updatedEntities = new ArrayList<>();
        List<String> notUpdatedEntities = new ArrayList<>();
        List<String> newEntities = new ArrayList<>();
        
        // list of generic entity properties to be updated
        List<GenericEntityProperty> updateProperties = new ArrayList<>();

        // load entities map from database
        Map<String, Integer> genericAssayStableIdToEntityIdMap = GenericAssayMetaUtils.buildGenericAssayStableIdToEntityIdMap();
        
        currentLine = buf.readLine();
        
        while (currentLine != null) {
            if (!TsvUtil.isDataLine(currentLine)) {
                currentLine = buf.readLine();
                continue;
            }
            
            String[] parts = currentLine.split("\t");
            
            // get stableId and try to find related Entity ID from genericAssayStableIdToEntityIdMap
            String stableId = parts[indexStableIdField];
            Integer entityId = genericAssayStableIdToEntityIdMap.getOrDefault(stableId, null);
            
            // generic assay meta are always updated to based on the current import;
            // also when present in db a new record is created.
                
            // extract fields; replace optional fields with the Stable ID when not set
            HashMap<String, String> propertiesMap = new HashMap<>();
            if (additionalProperties != null) {
                String[] columnNameList = additionalProperties.trim().split(",");
                for (String columnName : columnNameList) {
                    int indexAdditionalField = getColIndexByName(headerNames, columnName);
                    if (indexAdditionalField != -1) {
                        propertiesMap.put(columnName, parts[indexAdditionalField]);
                    }
                }
            }

            // log for the existing entities
            if (entityId != null) {
                if (updateInfo) {
                    updatedEntities.add(stableId);
                    DaoGenericAssay.deleteGenericEntityPropertiesByEntityId(entityId);
                    propertiesMap.forEach((k, v) -> {
                        updateProperties.add(new GenericEntityProperty(k, v, entityId));
                    });
                } else {
                    notUpdatedEntities.add(stableId);
                }
            }
            // create a new generic assay meta and add to the database
            else {
                newEntities.add(stableId);
                GeneticEntity newGeneticEntity = new GeneticEntity(geneticAlterationType.name(), stableId);
                GeneticEntity createdGeneticEntity = DaoGeneticEntity.addNewGeneticEntity(newGeneticEntity);
                propertiesMap.forEach((k, v) -> {
                    updateProperties.add(new GenericEntityProperty(k, v, createdGeneticEntity.getId()));
                });
            }
            
            currentLine = buf.readLine();
        }

        // Add new properties
        if (updateProperties.size() > 0) {
            DaoGenericAssay.setGenericEntityPropertiesUsingBatch(updateProperties);
        }
        
        // show import result message
        if (updatedEntities.size() > 0) {
            ProgressMonitor.setCurrentMessage("--> Entities updated: " + updatedEntities.size() + " generic entities existing in the database that were overridden during import.");
        }
        if (notUpdatedEntities.size() > 0) {
            ProgressMonitor.setCurrentMessage("--> Entities not updated: " + notUpdatedEntities.size() + " generic entities existing in the database that were not overridden during import.");
        }
        if (newEntities.size() > 0) {
            ProgressMonitor.setCurrentMessage("--> New Entities: " + newEntities.size() + " generic entities have been imported into database during import.");
        }
        
        reader.close();
        
        ProgressMonitor.setCurrentMessage("Finished loading generic assay.\n");
    }
    
    // returns index for ENTITY_STABLE_ID column
    private static int getStableIdIndex(String[] headers) {
        return getColIndexByName(headers, "ENTITY_STABLE_ID");
    }
    
    // helper function for finding the index of a column by name
    private static  int getColIndexByName(String[] headers, String colName) {
        for (int i=0; i<headers.length; i++) {
            if (headers[i].equalsIgnoreCase(colName)) {
                return i;
            }
        }
        return -1;
    }
    
    
    /**
     * usage:   --data <data_file.txt>
     *          --update-info [0:1]
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new ImportGenericAssayEntity(args);
        runner.runInConsole();
    }
}
