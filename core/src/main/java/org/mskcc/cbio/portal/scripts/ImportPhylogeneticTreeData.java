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
import org.mskcc.cbio.portal.util.*;
import org.mskcc.cbio.portal.model.*;

import joptsimple.*;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * Import Segment data into database.
 * @author us
 */
public class ImportPhylogeneticTreeData extends ConsoleRunnable {
    private int entriesSkipped;
    
    private void importData(File file, int cancerStudyId) throws IOException, DaoException {
        MySQLbulkLoader.bulkLoadOn();
        FileReader reader = new FileReader(file);
        BufferedReader buf = new BufferedReader(reader);
        try {
	        String line = buf.readLine(); // skip header line
	        long internalID = DaoPhylogeneticTree.getLargestId();
	        while ((line=buf.readLine()) != null) {
	            ProgressMonitor.incrementCurValue();
	            ConsoleUtil.showProgress();
	            
	            String[] strs = line.split("\t");
	            if (strs.length<3) {
	                System.err.println("wrong format: "+line);
	            }
	
	            CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(cancerStudyId);
	            //TODO - lines below should be removed. Agreed with JJ to remove this as soon as MSK moves to new validation 
		        //procedure. In this new procedure, Patients and Samples should only be added 
		        //via the corresponding ImportClinicalData process. Furthermore, the code below is wrong as it assumes one 
		        //sample per patient, which is not always the case.
	            String barCode = strs[0];
	            Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudyId,
                StableIdUtil.getPatientId(barCode));
                // if (sample == null ) {
    	        //     ImportDataUtil.addPatients(new String[] { barCode }, cancerStudy);
    		    //     ImportDataUtil.addSamples(new String[] { barCode }, cancerStudy);
    		    //     ProgressMonitor.logWarning("WARNING: Sample added on the fly because it was missing in clinical data");
    		    // }
	
	            String patientId = StableIdUtil.getPatientId(barCode);
	            // String chrom = strs[1].trim(); 
                String ancestorClone = strs[1].trim();
                String descendantClone = strs[2].trim();

	            //validate in same way as GistitReader:
	            //ValidationUtils.validateChromosome(chrom);
	            
	            // long start = Double.valueOf(strs[2]).longValue();
	            // long end = Double.valueOf(strs[3]).longValue();
	            // if (start >= end) {
	            // 	//workaround to skip with warning, according to https://github.com/cBioPortal/cbioportal/issues/839#issuecomment-203452415
	            // 	ProgressMonitor.logWarning("Start position of segment is not lower than end position. Skipping this entry.");
	            // 	entriesSkipped++;
	            // 	continue;
	            // }            
	            // int numProbes = new BigDecimal((strs[4])).intValue();
	            // double segMean = Double.parseDouble(strs[5]);
	            
	            Patient s = DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudyId, patientId);
	            if (s == null) {
	                // assert StableIdUtil.isNormal(sampleId);
	                entriesSkipped++;
	                continue;
	            }
	            PhylogeneticTree tree = new PhylogeneticTree(cancerStudyId, s.getInternalId(), ancestorClone, descendantClone);
	            tree.setInternalId(++internalID);
	            DaoPhylogeneticTree.addPhylogeneticTree(tree);
	        }
	        MySQLbulkLoader.flushAll();
        }
        finally {
        	buf.close();
        }
    }
    
    public void run() {
        try {
		    String description = "Import 'tree data' files";
	    	
		    OptionSet options = ConsoleUtil.parseStandardDataAndMetaOptions(args, description, true);
		    String dataFile = (String) options.valueOf("data");
		    File descriptorFile = new File((String) options.valueOf("meta"));
        
		    Properties properties = new Properties();
		    properties.load(new FileInputStream(descriptorFile));
            
            ProgressMonitor.setCurrentMessage("Reading data from:  " + dataFile);
            
			SpringUtil.initDataSource();
		    CancerStudy cancerStudy = getCancerStudy(properties);
		    
		    if (treeDataExistsForCancerStudy(cancerStudy)) {
			     throw new IllegalArgumentException("Tree for cancer study " + cancerStudy.getCancerStudyStableId() + " has already been imported: " + dataFile);
		    }
		
		    //importPhylogeneticTreeFileMetadata(cancerStudy, properties);
		    importPhylogeneticTreeFileData(cancerStudy, dataFile);
        } catch (RuntimeException e) {
            throw e;
        } catch (IOException|DaoException e) {
            throw new RuntimeException(e);
        }
    }


    private static CancerStudy getCancerStudy(Properties properties) throws DaoException {
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(properties.getProperty("cancer_study_identifier").trim());
        if (cancerStudy == null) {
            throw new RuntimeException("Unknown cancer study: " + properties.getProperty("cancer_study_identifier").trim());
        }
        return cancerStudy;
    }

    private static boolean treeDataExistsForCancerStudy(CancerStudy cancerStudy) throws DaoException {
        return (DaoPhylogeneticTree.treeDataExistForCancerStudy(cancerStudy.getInternalId()));
    }

    // private static void importPhylogeneticTreeFileMetadata(CancerStudy cancerStudy, Properties properties) throws DaoException {
    //     PhylogeneticTreeFile treeFile = new PhylogeneticTreeFile();
    //     treeFile.cancerStudyId = cancerStudy.getInternalId();
    //     //treeFile.referenceGenomeId = getRefGenId(properties.getProperty("reference_genome_id").trim()); 
    //     treeFile.description = properties.getProperty("description").trim();
    //     treeFile.filename = properties.getProperty("data_filename").trim();
    //     DaoPhylogeneticTreeFile.addPhylogeneticTreeFile(treeFile);
    // }

    private void importPhylogeneticTreeFileData(CancerStudy cancerStudy, String dataFilename) throws IOException, DaoException {
        File file = new File(dataFilename);
        int numLines = FileUtil.getNumLines(file);
        ProgressMonitor.setCurrentMessage(" --> total number of data lines:  " + (numLines-1));
        ProgressMonitor.setMaxValue(numLines);
        entriesSkipped = 0;
        importData(file, cancerStudy.getInternalId());
        ProgressMonitor.setCurrentMessage(" --> total number of entries skipped:  " + entriesSkipped);
    }

    // private static PhylogeneticTreeFile.ReferenceGenomeId getRefGenId(String potentialRefGenId) {
    //     if (PhylogeneticTreeFile.ReferenceGenomeId.has(potentialRefGenId)) {
    //         return PhylogeneticTreeFile.ReferenceGenomeId.valueOf(potentialRefGenId);
    //     }
    //     else {
    //         throw new RuntimeException ("Unknown reference genome id: " + potentialRefGenId);
    //     }
    // }

    /**
     * Makes an instance to run with the given command line arguments.
     *
     * @param args  the command line arguments to be used
     */
    public ImportPhylogeneticTreeData(String[] args) {
        super(args);
    }

    /**
     * Runs the command as a script and exits with an appropriate exit code.
     *
     * @param args  the arguments given on the command line
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new ImportPhylogeneticTreeData(args);
        runner.runInConsole();
    }
}
