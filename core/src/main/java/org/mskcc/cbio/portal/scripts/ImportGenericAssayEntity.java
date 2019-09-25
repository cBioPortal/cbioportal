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

import org.mskcc.cbio.portal.dao.DaoTreatment;
import org.mskcc.cbio.portal.model.Treatment;
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

    public ImportGenericAssayEntity(File dataFile, boolean updateInfo) {
        // fake the console arguments required by the ConsoleRunnable class
        super( new String[]{"--data", dataFile.getAbsolutePath(), "--update-info", updateInfo?"1":"0"});
	}

	@Override
    public void run() {
        try {
            String progName = "ImportTreatments";
            String description = "Import treatment records from treatment response files.";
            
            OptionParser parser = new OptionParser();
            OptionSpec<String> data = parser.accepts("data", "Treatment data file")
            .withRequiredArg().ofType(String.class);
            
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
            
            // Check options
            boolean updateInfo = options.has("update-info");
            
            ProgressMonitor.setCurrentMessage("Adding new treatments to the database\n");
            startImport(options, data, updateInfo);
            
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
    public static void startImport(OptionSet options, OptionSpec<String> data, boolean updateInfo) throws Exception {
        if (options.hasArgument(data)) {
            File treatmentFile = new File(options.valueOf(data));
            importData(treatmentFile);
        }
    }
    
    /**
    * Imports feature columns from treatment file.
    *
    * @param treatmentDataFile
    * @param updateInfo boolean that indicates wether additional fields
    * ("Description, Name, URL") of existing records should be overwritten
    * @throws Exception
    */
    public static void importData(File treatmentDataFile) throws Exception {
        
        ProgressMonitor.setCurrentMessage("Reading data from: " + treatmentDataFile.getCanonicalPath());
        
        // read gene set data file - note: this file does not contain headers
        FileReader reader = new FileReader(treatmentDataFile);
        BufferedReader buf = new BufferedReader(reader);
        String currentLine = buf.readLine();
        String[] headerNames = currentLine.split("\t");
        
        int indexStableIdField = getTreatmentStableIdIndex(headerNames);
        int indexNameField = getTreatmentNameIndex(headerNames);
        int indexDescField = getTreatmentDescIndex(headerNames);
        int indexUrlField = getTreatmentUrlIndex(headerNames);
        
        currentLine = buf.readLine();
        
        while (currentLine != null) {
            
            String[] parts = currentLine.split("\t");
            
            // assumed that fields contain: treat id, name, short name
            String treatmentStableId = parts[indexStableIdField];
            Treatment treatment = DaoTreatment.getTreatmentByStableId(treatmentStableId);
            
            // treatments are always updated to based on the current import;
            // also when present in db a new record is created.
                
            // extract fields; replace optional fields with the Stable ID when not set
            String stableId = parts[indexStableIdField];
            String name = indexNameField == -1?stableId:parts[indexNameField];
            String desc = indexNameField == -1?stableId:parts[indexDescField];
            String url = indexNameField == -1?stableId:parts[indexUrlField];

            if (treatment == null) {
            
                // create a new treatment and add to the database
                Treatment newTreatment = new Treatment(stableId, name, desc, url);
                ProgressMonitor.setCurrentMessage("Adding treatment: " + newTreatment.getStableId());
                DaoTreatment.addTreatment(newTreatment);

            }
            // update the meta-information fields of the treatment
            else {

                ProgressMonitor.setCurrentMessage("Updating treatment: " + treatment.getStableId());
                treatment.setName(name);
                treatment.setDescription(desc);
                treatment.setRefLink(url);
                DaoTreatment.updateTreatment(treatment);

            }

            currentLine = buf.readLine();
        }
        
        reader.close();
        
        ProgressMonitor.setCurrentMessage("Finished loading treatments.\n");
        
        return;
    }
    
    // returns index for entity_stable_id column
    private static int getTreatmentStableIdIndex(String[] headers) {
        return getColIndexByName(headers, "entity_stable_id");
    }
    
    // returns index for treatment name column
    private static  int getTreatmentNameIndex(String[] headers) {
        return getColIndexByName(headers, ImportUtils.metaFieldTag+"name");
    }
    
    // returns index for treatment description column
    private static  int getTreatmentDescIndex(String[] headers) {
        return getColIndexByName(headers, ImportUtils.metaFieldTag+"description");
    }
    
    // returns index for treatment linkout url column
    private static  int getTreatmentUrlIndex(String[] headers) {
        return getColIndexByName(headers, ImportUtils.metaFieldTag+"url");
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