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
 * @author jj
 */
public class ImportCopyNumberSegmentData {
    private int cancerStudyId;
    private File file;
    private int entriesSkipped = 0;
    
    public ImportCopyNumberSegmentData(File file, int cancerStudyId)
    {
        this.file = file;
        this.cancerStudyId = cancerStudyId;
    }
    
    public void importData() throws Exception
    {
        MySQLbulkLoader.bulkLoadOn();
        FileReader reader = new FileReader(file);
        BufferedReader buf = new BufferedReader(reader);
        try {
	        String line = buf.readLine(); // skip header line
	        long segId = DaoCopyNumberSegment.getLargestId();
	        while ((line=buf.readLine()) != null) {
	            ProgressMonitor.incrementCurValue();
	            ConsoleUtil.showProgress();
	            
	            String[] strs = line.split("\t");
	            if (strs.length<6) {
	                System.err.println("wrong format: "+line);
	            }
	
	            CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(cancerStudyId);
	            //TODO - lines below should be removed. Agreed with JJ to remove this as soon as MSK moves to new validation 
		        //procedure. In this new procedure, Patients and Samples should only be added 
		        //via the corresponding ImportClinicalData process. Furthermore, the code below is wrong as it assumes one 
		        //sample per patient, which is not always the case.
	            ImportDataUtil.addPatients(new String[] { strs[0] }, cancerStudy);
		        int nrUnknownSamplesAdded = ImportDataUtil.addSamples(new String[] { strs[0] }, cancerStudy);
		        if (nrUnknownSamplesAdded > 0) {
		        	ProgressMonitor.logWarning("WARNING: Number of samples added on the fly because they were missing in clinical data:  " + nrUnknownSamplesAdded);
		        }
	
	            String sampleId = StableIdUtil.getSampleId(strs[0]);
	            String chrom = strs[1].trim(); 
	            //validate in same way as GistitReader:
	            ValidationUtils.validateChromosome(chrom);
	            
	            long start = Double.valueOf(strs[2]).longValue();
	            long end = Double.valueOf(strs[3]).longValue();
	            if (start >= end) {
	            	//workaround to skip with warning, according to https://github.com/cBioPortal/cbioportal/issues/839#issuecomment-203452415
	            	ProgressMonitor.logWarning("Start position of segment is not lower than end position. Skipping this entry.");
	            	entriesSkipped++;
	            	continue;
	            }            
	            int numProbes = new BigDecimal((strs[4])).intValue();
	            double segMean = Double.parseDouble(strs[5]);
	            
	            Sample s = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudyId, sampleId);
	            if (s == null) {
	                assert StableIdUtil.isNormal(sampleId);
	                entriesSkipped++;
	                continue;
	            }
	            CopyNumberSegment cns = new CopyNumberSegment(cancerStudyId, s.getInternalId(), chrom, start, end, numProbes, segMean);
	            cns.setSegId(++segId);
	            DaoCopyNumberSegment.addCopyNumberSegment(cns);
	        }
	        MySQLbulkLoader.flushAll();
        }
        finally {
        	buf.close();
        }
    }
    
    public static void main(String[] args) throws Exception
    {
    	try {    		
 		    ProgressMonitor.setConsoleModeAndParseShowProgress(args);
		    String description = "Import 'segment data' files";
	    	
		    OptionSet options = ConsoleUtil.parseStandardDataAndMetaOptions(args, description, true);
		    String dataFile = (String) options.valueOf("data");
		    File descriptorFile = new File((String) options.valueOf("meta"));
        
		    Properties properties = new Properties();
		    properties.load(new FileInputStream(descriptorFile));

		    System.out.println("Reading data from:  " + dataFile);
		    
			SpringUtil.initDataSource();
		    CancerStudy cancerStudy = getCancerStudy(properties);
		    
		    if (segmentDataExistsForCancerStudy(cancerStudy)) {
			     throw new IllegalArgumentException("Ignoring this file since seg data for cancer study " + cancerStudy.getCancerStudyStableId() + " has already been imported: " + dataFile);
		    }
		
		    importCopyNumberSegmentFileMetadata(cancerStudy, properties);
		    ProgressMonitor.setConsoleModeAndParseShowProgress(args);
		    importCopyNumberSegmentFileData(cancerStudy, dataFile);
		    
		    System.out.println("Done.");
		    ConsoleUtil.showMessages();
	    } catch (Exception e) {
	    	ConsoleUtil.showWarnings();
	    	//exit with error status:
	    	System.err.println ("\nABORTED! Error:  " + e.getMessage());
	    	System.exit(1);
	    }
    }


    private static CancerStudy getCancerStudy(Properties properties) throws Exception
    {
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(properties.getProperty("cancer_study_identifier").trim());
        if (cancerStudy == null) {
            throw new Exception("Unknown cancer study: " + properties.getProperty("cancer_study_identifier").trim());
        }
        return cancerStudy;
    }

    private static boolean segmentDataExistsForCancerStudy(CancerStudy cancerStudy) throws Exception
    {
        return (DaoCopyNumberSegment.segmentDataExistForCancerStudy(cancerStudy.getInternalId()));
    }

    private static void importCopyNumberSegmentFileMetadata(CancerStudy cancerStudy, Properties properties) throws Exception
    {
        CopyNumberSegmentFile copyNumSegFile = new CopyNumberSegmentFile();
        copyNumSegFile.cancerStudyId = cancerStudy.getInternalId();
        copyNumSegFile.referenceGenomeId = getRefGenId(properties.getProperty("reference_genome_id").trim()); 
        copyNumSegFile.description = properties.getProperty("description").trim();
        copyNumSegFile.filename = properties.getProperty("data_filename").trim();
        DaoCopyNumberSegmentFile.addCopyNumberSegmentFile(copyNumSegFile);
    }

    private static void importCopyNumberSegmentFileData(CancerStudy cancerStudy, String dataFilename) throws Exception
    {
        File file = new File(dataFilename);
        int numLines = FileUtil.getNumLines(file);
        ProgressMonitor.setCurrentMessage(" --> total number of data lines:  " + (numLines-1));
        ProgressMonitor.setMaxValue(numLines);
        ImportCopyNumberSegmentData parser = new ImportCopyNumberSegmentData(file, cancerStudy.getInternalId());
        parser.importData();
        ProgressMonitor.setCurrentMessage(" --> total number of entries skipped:  " + parser.entriesSkipped);
    }

    private static CopyNumberSegmentFile.ReferenceGenomeId getRefGenId(String potentialRefGenId) throws Exception
    {
        if (CopyNumberSegmentFile.ReferenceGenomeId.has(potentialRefGenId)) {
            return CopyNumberSegmentFile.ReferenceGenomeId.valueOf(potentialRefGenId);
        }
        else {
            throw new Exception ("Unknown reference genome id: " + potentialRefGenId);
        }
    }
}
