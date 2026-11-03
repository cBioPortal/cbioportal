/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.scripts;

import java.io.*;
import java.util.*;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoPatient;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.dao.DaoSampleList;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.Patient;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.model.SampleList;
import org.mskcc.cbio.portal.model.SampleListCategory;
import org.mskcc.cbio.portal.util.CaseList;
import org.mskcc.cbio.portal.util.CaseListReader;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.util.StableIdUtil;
import org.mskcc.cbio.portal.validate.CaseListValidator;

/**
 * Command Line tool to Import Sample Lists.
 */
public class ImportSampleList extends ConsoleRunnable {

   public static void importSampleList(File dataFile) throws IOException, DaoException {
      ProgressMonitor.setCurrentMessage("Read data from:  " + dataFile.getAbsolutePath());
      CaseList caseList = CaseListReader.readFile(dataFile);
      CaseListValidator.validateAll(caseList);

      CancerStudy theCancerStudy = DaoCancerStudy.getCancerStudyByStableId(caseList.getCancerStudyIdentifier());
      if (theCancerStudy == null) {
         throw new IllegalArgumentException("cancer study identified by cancer_study_identifier '"
                  + caseList.getCancerStudyIdentifier() + "' not found in dbms or inaccessible to user.");
      }

      String sampleListCategoryStr = caseList.getCategory();
      if (sampleListCategoryStr  == null || sampleListCategoryStr.length() == 0) {
          sampleListCategoryStr = "other";
      }
      SampleListCategory sampleListCategory = SampleListCategory.get(sampleListCategoryStr); 
       
      boolean itemsAddedViaPatientLink = false;
      // construct sample id list
      ArrayList<String> sampleIDsList = new ArrayList<String>();
      for (String sampleId : caseList.getSampleIds()) {
         sampleId = StableIdUtil.getSampleId(sampleId);
         Sample s = DaoSample.getSampleByCancerStudyAndSampleId(theCancerStudy.getInternalId(), sampleId);
         if (s==null) {
             String warningMessage = "Error: could not find sample "+sampleId;
             Patient p = DaoPatient.getPatientByCancerStudyAndPatientId(theCancerStudy.getInternalId(), sampleId);
             if (p!=null) {
                warningMessage += ". But found a patient with this ID. Will use its samples in the sample list.";
                List<Sample> samples = DaoSample.getSamplesByPatientId(p.getInternalId());
                for (Sample sa : samples) {
                      if (!sampleIDsList.contains(sa.getStableId())) {
                          sampleIDsList.add(sa.getStableId());
                          itemsAddedViaPatientLink = true;
                      }
                }
             } 
             ProgressMonitor.logWarning(warningMessage);
         } else if (!sampleIDsList.contains(s.getStableId())) {
            sampleIDsList.add(s.getStableId());
         } else {
             ProgressMonitor.logWarning("Warning: duplicated sample ID " + s.getStableId() + " in case list " + caseList.getStableId());
         }
      }

      DaoSampleList daoSampleList = new DaoSampleList();
      SampleList sampleList = daoSampleList.getSampleListByStableId(caseList.getStableId());
      if (sampleList != null) {
         throw new IllegalArgumentException("Patient list with this stable Id already exists:  " + caseList.getStableId());
      }

      sampleList = new SampleList();
      sampleList.setStableId(caseList.getStableId());
      int cancerStudyId = theCancerStudy.getInternalId();
      sampleList.setCancerStudyId(cancerStudyId);
      sampleList.setSampleListCategory(sampleListCategory);
      sampleList.setName(caseList.getName());
      sampleList.setDescription(caseList.getDescription());
      sampleList.setSampleList(sampleIDsList);
      daoSampleList.addSampleList(sampleList);
      // Keep the populated instance. ClickHouse Cloud may not expose an INSERT to a
      // SELECT on another connection immediately (SharedMergeTree stale snapshot).

      ProgressMonitor.setCurrentMessage(" --> stable ID:  " + sampleList.getStableId());
      ProgressMonitor.setCurrentMessage(" --> sample list name:  " + sampleList.getName());
      ProgressMonitor.setCurrentMessage(" --> number of samples in file:  " + caseList.getSampleIds().size());
      String warningSamplesViaPatientLink = (itemsAddedViaPatientLink? "(nb: can be higher if samples were added via patient link)" : "");
      ProgressMonitor.setCurrentMessage(" --> number of samples stored in final sample list " + warningSamplesViaPatientLink + ":  " + sampleIDsList.size());
   }

   public void run () {
      try {
    	  // check args
	      if (args.length < 1) {
	         // an extra --noprogress option can be given to avoid the messages regarding memory usage and % complete
             throw new UsageException(
                     "importCaseListData.pl ",
                     null,
                     "<data_file.txt or directory>");
	      }
	      File dataFile = new File(args[0]);
	      if (dataFile.isDirectory()) {
	         File files[] = dataFile.listFiles();
	         for (File file : files) {
	            if (!file.getName().startsWith(".") && !file.getName().endsWith("~")) {
	               ImportSampleList.importSampleList(file);
	            }
	         }
	         if (files.length == 0) {
	             ProgressMonitor.logWarning("No patient lists found in directory, skipping import: " + dataFile.getCanonicalPath());
	         }
	      } else {
	    	  if (!dataFile.getName().startsWith(".") && !dataFile.getName().endsWith("~")) {
	    		  ImportSampleList.importSampleList(dataFile);
	    	  }
	    	  else {
	    		  ProgressMonitor.logWarning("File name starts with '.' or ends with '~', so it was skipped: " + dataFile.getCanonicalPath());
	    	  }
	      }
      } catch (RuntimeException e) {
          throw e;
      } catch (IOException|DaoException e) {
          throw new RuntimeException(e);
      }
   }

    /**
     * Makes an instance to run with the given command line arguments.
     *
     * @param args  the command line arguments to be used
     */
    public ImportSampleList(String[] args) {
        super(args);
    }

    /**
     * Runs the command as a script and exits with an appropriate exit code.
     *
     * @param args  the arguments given on the command line
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new ImportSampleList(args);
        runner.runInConsole();
    }
}
