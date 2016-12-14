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

package org.mskcc.cbio.portal.scripts;

import java.io.*;
import java.util.*;
import joptsimple.*;
import org.cbioportal.model.GeneSet;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.util.ProgressMonitor;

/**
 *
 * @author ochoaa
 */
public class ImportGeneSetData extends ConsoleRunnable {    
    
    @Override
    public void run() {
        try {
            String progName = "ImportGeneSetData";
            String description = "Import geneset data files.";
            // usage: --data <data_file.txt> --supp <supp_file.txt> --update [allow updates to existing geneset data]
            
            OptionParser parser = new OptionParser();
            OptionSpec<String> data = parser.accepts("data", "Geneset data file")
                    .withRequiredArg().ofType(String.class);
            OptionSpec<String> supp = parser.accepts("supp", "Option geneset supplemental data file")
                    .withRequiredArg().ofType(String.class);
            parser.accepts("update", "Permits updates to geneset data even if geneset is in use");
            
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
                        "'data' and/or 'supp' argument argument required");
            }
            
            // import geneset data file and/or supplemental geneset data file
            boolean allowUpdates = options.hasArgument("update");            
            if (options.hasArgument(data)) {
                File genesetFile = new File(options.valueOf(data));
                importData(genesetFile, allowUpdates);
            }            
            if (options.hasArgument(supp)) {
                File genesetSuppFile = new File(options.valueOf(supp));
                importSuppGeneSetData(genesetSuppFile);
            }
        }
        catch (Exception ex) {
            
        }
        

    }
    
    /**
     * Imports data from geneset file.
     * @param genesetFile
     * @param allowUpdates
     * @throws Exception 
     */
    private static void importData(File genesetFile, boolean allowUpdates) throws Exception {
        ProgressMonitor.setCurrentMessage("Reading data from: " + genesetFile.getCanonicalPath());
        DaoGeneSet daoGeneSet = DaoGeneSet.getInstance();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        
        // read geneset data file - note: this file does not contain headers
        FileReader reader = new FileReader(genesetFile);
        BufferedReader buf = new BufferedReader(reader);        
        String line = buf.readLine();
                
        while (line != null) {            
            String[] parts = line.split("\t");
                        
            // assumed that geneset id and ref link are the first two columns in file            
            GeneSet geneSet = new GeneSet();
            geneSet.setExternalId(parts[0]);
            geneSet.setRefLink(parts[1]);
            
            // parse entrez ids for geneset
            List<Integer> genesetGenes = new ArrayList();
            for (int i=2; i<parts.length; i++) {
                genesetGenes.add(Integer.valueOf(parts[i]));
            }
            geneSet.setGenesetGenes(genesetGenes);
            
            // check if geneset already exists by external id
            GeneSet existingGeneSet = daoGeneSet.getGeneSetByExternalId(geneSet.getExternalId());
            // if geneset exists then check usage
            if (existingGeneSet != null) {
                // if geneset in use by other studies and option to allow updates 
                // was not set then alert user and throw RuntimeException
                if (daoGeneSet.checkUsage(existingGeneSet.getGeneticEntityId()) && !allowUpdates) {
                    throw new RuntimeException("Geneset " + geneSet.getExternalId() + " exists and is already in use in DB. " +
                            "Set option 'update' to allow updates to existing genesets.");
                }                
                // assumed that ref link and geneset genes are updated
                existingGeneSet.setRefLink(geneSet.getRefLink());
                existingGeneSet.setGenesetGenes(geneSet.getGenesetGenes());
                
                // update geneset record and geneset genes in db
                daoGeneSet.updateGeneSet(geneSet, true);                
            }
            else {
                // import new geneset record
                daoGeneSet.addGeneSet(geneSet);
            }
            line = buf.readLine();
        }
        // close file
        reader.close();
    }
    
    /**
     * Imports supplemental geneset data from supp file.
     * @param suppFile
     * @throws Exception 
     */
    private static void importSuppGeneSetData(File suppFile) throws Exception {
        ProgressMonitor.setCurrentMessage("Reading data from: " + suppFile.getCanonicalPath());
        DaoGeneSet daoGeneSet = DaoGeneSet.getInstance();
        
        // read supplemental geneset data file - note: this file does not contain headers
        FileReader reader = new FileReader(suppFile);
        BufferedReader buf = new BufferedReader(reader);        
        String line = buf.readLine();
        
        while (line != null) {
            String[] parts = line.split("\t");
            
            // assumed that fields contain: geneset id, name, short name
            GeneSet geneSet = daoGeneSet.getGeneSetByExternalId(parts[0]);
            
            // if geneset does not already exist then alert user and skip record
            if (geneSet == null) {
                ProgressMonitor.logWarning("Could not find geneset " + parts[0] + " in DB. Record will be skipped.");
            }
            else {
                // update name and short name for geneset
                geneSet.setNameShort(parts[1]);
                geneSet.setName(parts[2]);
                
                // update geneset record in db without updating geneset genes
                daoGeneSet.updateGeneSet(geneSet, false);
            }
            
            line = buf.readLine();
        }
        // close file
        reader.close();
    }

    public ImportGeneSetData(String[] args) {
        super(args);
    }
    
    public static void main(String[] args) {
        ConsoleRunnable runner = new ImportGeneSetData(args);
        runner.runInConsole();        
    }


    
}
