/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
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
 * @author ochoaa
 * @author Sander Tan
 */

package org.mskcc.cbio.portal.scripts;

import java.io.*;
import java.util.*;
import joptsimple.*;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.Geneset;
import org.mskcc.cbio.portal.util.ProgressMonitor;


public class ImportGenesetData extends ConsoleRunnable {
    
	public static int skippedGenes;
    
    @Override
    public void run() {
        try {
            String progName = "ImportGenesetData";
            String description = "Import gene set data files.";
            // usage:     --data <data_file.txt> --supp <supp_file.txt>
            // optional:  --new-version <version>
            //            --update-info

            OptionParser parser = new OptionParser();
            OptionSpec<String> data = parser.accepts("data", "Geneset data file")
                    .withRequiredArg().ofType(String.class);
            OptionSpec<String> supp = parser.accepts("supp", "Option gene set supplemental data file")
                    .withRequiredArg().ofType(String.class);
            OptionSpec<String> newVersionArgument = parser.accepts("new-version", "New version of gene sets. This will REMOVE GSVA data and hierarchy.")
            	.withRequiredArg().ofType(String.class);
            parser.accepts("update-info", "Only update the info of gene sets (Ref Link, name, description)");

            // For testing purposes, we don't want the prompt, so make extra argument.
            parser.accepts("confirm-delete-all-genesets-hierarchy-genesetprofiles", "This will avoid the command prompt being shown.");

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
            if (!options.has(data) && !options.has(supp)) {
                throw new UsageException(
                        progName, description, parser,
                        "'data' and/or 'supp' argument required");
            }
            
            // Check options
            boolean updateInfo = options.has("update-info");
            boolean newVersion = options.has("new-version");
            boolean confirmEmptyGenesets = options.has("confirm-delete-all-genesets-hierarchy-genesetprofiles");
            
            // Check current version
            String databaseGenesetVersion = DaoInfo.getGenesetVersion();
            
            // Check new version
            String newGenesetVersion = options.valueOf(newVersionArgument);
            
        	// Required to specify if we update info or add new version
            if ((!updateInfo && !newVersion) || (updateInfo && newVersion)) {      
                throw new UsageException(
                        progName, description, parser,
                        "Please specify 1 of 2 options:\n" +
                        "--update-info             Only update gene info (Ref Link, name, description). Genes will not be updated.\n" +
                		"--new-version <Version>   Import new gene set version. This will REMOVE all previous loaded gene sets, gene set genetic profiles and gene set hierarchy.\n");
    
        	// In case of updating info only
            } else if (updateInfo) {

            	// Check if there is something in the database before updating
            	if (databaseGenesetVersion == null) {
	        		throw new UsageException(
	                    progName, description, parser,
	    				"Attempted to update gene sets, but gene set tables are empty. You can import with --new-version <Version>");
            	}
            	
            	ProgressMonitor.setCurrentMessage("Updating gene set information.\n");

            	// Only update info
            	startImport(options, data, supp, updateInfo, newVersion);
            	
        	// In case of new version
            } else {

                // Check if gene set tables are empty. In that case, it's not necessary to ask user to remove previous data.
            	if (databaseGenesetVersion == null) {
	            	ProgressMonitor.setCurrentMessage("New version of gene sets specified. Loading to empty database.");
            		DaoGeneset.deleteAllRecords();
	            	
	            	ProgressMonitor.setCurrentMessage("Adding new gene sets.\n");
	            	startImport(options, data, supp, updateInfo, newVersion);
	            	DaoInfo.setGenesetVersion(newGenesetVersion);

            	} else {
            		// If automatic confirm is not set, prompt the user with question to confirm.
            		if (!confirmEmptyGenesets) {
            			ProgressMonitor.setCurrentMessage("Are you sure you want to remove all previous gene sets, gene set hierarchy and gene set genetic profiles?");
		                ProgressMonitor.setCurrentMessage("Type `yes` to continue or anything else to abort.");
		            	
		                try (Scanner scanner = new Scanner(System.in)) {
		
			            	String confirmEmptyingGenesets = scanner.next().toLowerCase();
			            	if (confirmEmptyingGenesets.equals("yes")) {
			            		confirmEmptyGenesets = true;
                            } else {
                                ProgressMonitor.setCurrentMessage("Replacing existing gene sets not confirmed; aborting.");
                                return;
                            }
		                }
            		} else {
            			ProgressMonitor.setCurrentMessage("Auto confirm to empty gene sets, gene set hierarchy and gene set genetic profiles is set.");
            		}
            		
            		// Empty gene set tables  and import new gene sets
	            	ProgressMonitor.setCurrentMessage("New version of gene sets specified.\nRemoving old gene sets, gene set hierarchy and gene set genetic profiles.");
	            		            	
	            	DaoGeneset.deleteAllRecords();
	            	ProgressMonitor.setCurrentMessage("Adding new gene sets.");
	            	startImport(options, data, supp, updateInfo, newVersion);
            		DaoInfo.setGenesetVersion(newGenesetVersion);
            	}
        		ProgressMonitor.setCurrentMessage("It is now possible to import gene set hierarchy data and gene set genetic profiles such as GSVA scores.");
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Start import process for gene set file and supplementary file.
     */ 
    // TODO Exception should be handled by the callling code. Remove 
    // handling of exception and replace with `throw` statement.
    public static void startImport(OptionSet options, OptionSpec<String> data, OptionSpec<String> supp, boolean updateInfo, boolean newVersion){
    	try {
       	 	if (options.hasArgument(data)) {
	             File genesetFile = new File(options.valueOf(data));
	             importData(genesetFile, updateInfo, newVersion);
       	 	}            
	         if (options.hasArgument(supp)) {
	             File genesetSuppFile = new File(options.valueOf(supp));
	             importSuppGenesetData(genesetSuppFile);
	         }
    	}
        catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    
    /**
     * Imports data from gene set file.
     * @param genesetFile
     * @param allowUpdates
     * @param version 
     * @throws Exception 
     */
    public static int importData(File genesetFile,  boolean updateInfo, boolean newVersion) throws Exception {
    	
    	ProgressMonitor.setCurrentMessage("Reading data from: " + genesetFile.getCanonicalPath());
   
        // read gene set data file - note: this file does not contain headers
        FileReader reader = new FileReader(genesetFile);
        BufferedReader buf = new BufferedReader(reader);        
        String line = buf.readLine();


        while (line != null) {            
            String[] parts = line.split("\t");
                        
            // assumed that gene set id and ref link are the first two columns in file            
            Geneset geneset = new Geneset();
            geneset.setExternalId(parts[0]);
            geneset.setRefLink(parts[1]);
            
            // by default name and desciption are the same as external id, and can be overriden in importSuppGenesetData:
            geneset.setName(geneset.getExternalId());
            geneset.setDescription(geneset.getExternalId());
                
            // parse entrez ids for gene set
            Set<Long> genesetGenes = new HashSet<Long>();
            for (int i=2; i<parts.length; i++) {
                genesetGenes.add(Long.valueOf(parts[i]));
            }
            geneset.setGenesetGenes(genesetGenes);
                     
            // check if gene set already exists by external id
            Geneset existingGeneset = DaoGeneset.getGenesetByExternalId(geneset.getExternalId());
            
            // Update only reflink
        	if (updateInfo) {
                if ( existingGeneset == null) {
            		ProgressMonitor.logWarning("Could not find gene set " + parts[0] + " in DB. Record will be skipped.");
            	} else {
            		ProgressMonitor.setCurrentMessage("Updating gene set: " + geneset.getExternalId());
            		
            		// Get gene set id for the already existing gene
            		geneset.setId(existingGeneset.getId());
                    DaoGeneset.updateGeneset(geneset, true);
            	}
                
            // Add a new gene
        	} else {
        		ProgressMonitor.setCurrentMessage("Adding gene set: " + geneset.getExternalId());
                Geneset newGeneset = DaoGeneset.addGeneset(geneset);
                // add geneset genes (adding to bulkloader for performance reasons):
                DaoGeneset.addGenesetGenesToBulkLoader(newGeneset);
        	}

            line = buf.readLine();
        }
        //flush bulkloader to commit geneset genes:
        MySQLbulkLoader.flushAll();
        // close file
        reader.close();

        // print warnings message with skipped genes
        if (skippedGenes > 0) {
            ProgressMonitor.setCurrentMessage(
                    "\n" + skippedGenes + " times a gene was not found in local gene table. Possible reasons:\n\n" +
                    "1. The Entrez gene IDs are relatively new. Consider adding them to database.\n" +
                    "2. The Entrez gene IDs are deprecated. Consider updating gene sets and recalculating GSVA scores.\n" +
                    "3. Invalid Entrez gene IDs. Please check .gmt file to verify genes are in Entrez gene ID format.\n\n");
        }

    	ProgressMonitor.setCurrentMessage("Finished loading gene sets.\n");
  
        return skippedGenes;
    }

    /**
     * Imports supplemental gene set data from supp file.
     * @param suppFile
     * @throws Exception 
     */
    static void importSuppGenesetData(File suppFile) throws Exception {
    	
        ProgressMonitor.setCurrentMessage("Reading data from: " + suppFile.getCanonicalPath());
        
        // read supplemental gene set data file - note: this file does not contain headers
        FileReader reader = new FileReader(suppFile);
        BufferedReader buf = new BufferedReader(reader);        
        String line = buf.readLine();
        
        while (line != null) {
            String[] parts = line.split("\t");
            
            // assumed that fields contain: geneset id, name, short name
            Geneset geneset = DaoGeneset.getGenesetByExternalId(parts[0]);
            
            // if gene set does not already exist then alert user and skip record
            if (geneset == null) {
                ProgressMonitor.logWarning("Could not find gene set " + parts[0] + " in DB. Record will be skipped.");
            }
            else {
                // update name and short name for gene set
                geneset.setName(parts[1]);
                geneset.setDescription(parts[2]);
                
                // update gene set record in db without updating gene set genes
                DaoGeneset.updateGeneset(geneset, false);
            }
            
            line = buf.readLine();
        }
        // close file
        reader.close();
        
    	ProgressMonitor.setCurrentMessage("Finished loading supplementary gene set info.\n");
    }

    public ImportGenesetData(String[] args) {
        super(args);
    }
    
    public static void main(String[] args) {
        ConsoleRunnable runner = new ImportGenesetData(args);
        runner.runInConsole();        
    }   
}
