/*
* Copyright (c) 2019 The Hyve B.Vs.
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

/**
* @author Pim van Nierop, pim@thehyve.nl
*/

package org.mskcc.cbio.portal.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.cbioportal.model.EntityType;
import org.cbioportal.model.GeneticEntity;
import org.cbioportal.model.meta.GenericAssayMeta;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGenericAssay;
import org.mskcc.cbio.portal.dao.DaoGeneticEntity;
import org.mskcc.cbio.portal.model.GeneticAlterationType;
import org.mskcc.cbio.portal.util.ProgressMonitor;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

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
        
        currentLine = buf.readLine();
        
        while (currentLine != null) {
            
            String[] parts = currentLine.split("\t");
            
            // get stableId and get the meta by the stableId
            String genericAssayMetaStableId = parts[indexStableIdField];
            GenericAssayMeta genericAssayMeta = DaoGenericAssay.getGenericAssayMetaByStableId(genericAssayMetaStableId);
            GeneticEntity genericAssayEntity = DaoGeneticEntity.getGeneticEntityByStableId(genericAssayMetaStableId);
            
            // generic assay meta are always updated to based on the current import;
            // also when present in db a new record is created.
                
            // extract fields; replace optional fields with the Stable ID when not set
            String stableId = parts[indexStableIdField];
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
            if (genericAssayMeta != null) {
                if (updateInfo) {
                    updatedEntities.add(stableId);
                    DaoGenericAssay.deleteGenericEntityPropertiesByStableId(stableId);
                    propertiesMap.forEach((k, v) -> {	
                        try {	
                            DaoGenericAssay.setGenericEntityProperty(genericAssayEntity.getId(), k, v);	
                        } catch (DaoException e) {	
                            e.printStackTrace();	
                        }	
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
                    try {
                        DaoGenericAssay.setGenericEntityProperty(createdGeneticEntity.getId(), k, v);
                    } catch (DaoException e) {
                        e.printStackTrace();
                    }
                });
            }

            currentLine = buf.readLine();
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
        
        return;
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