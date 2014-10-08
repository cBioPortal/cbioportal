/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.util.*;

import java.io.*;
import java.util.*;

/**
 * Command Line tool to Import Patient Lists.
 */
public class ImportPatientList {

   public static void importPatientList(File dataFile, ProgressMonitor pMonitor) throws Exception {
      pMonitor.setCurrentMessage("Read data from:  " + dataFile.getAbsolutePath());
      Properties properties = new Properties();
      properties.load(new FileInputStream(dataFile));

      String stableId = properties.getProperty("stable_id").trim();

      if (stableId.contains(" ")) {
         throw new IllegalArgumentException("stable_id cannot contain spaces:  " + stableId);
      }

      if (stableId == null || stableId.length() == 0) {
         throw new IllegalArgumentException("stable_id is not specified.");
      }

      String cancerStudyIdentifier = properties.getProperty("cancer_study_identifier");
      if (cancerStudyIdentifier == null) {
         throw new IllegalArgumentException("cancer_study_identifier is not specified.");
      }
      CancerStudy theCancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyIdentifier);
      if (theCancerStudy == null) {
         throw new IllegalArgumentException("cancer study identified by cancer_study_identifier '"
                  + cancerStudyIdentifier + "' not found in dbms or inaccessible to user.");
      }

      String patientListName = properties.getProperty("case_list_name");
       
      String patientListCategoryStr = properties.getProperty("case_list_category");
      if (patientListCategoryStr  == null || patientListCategoryStr.length() == 0) {
          patientListCategoryStr = "other";
      }
      PatientListCategory patientListCategory = PatientListCategory.get(patientListCategoryStr); 
       
      String patientListDescription = properties.getProperty("case_list_description");
      String patientListStr = properties.getProperty("case_list_ids");
      if (patientListName == null) {
         throw new IllegalArgumentException("case_list_name is not specified.");
      } else if (patientListDescription == null) {
         throw new IllegalArgumentException("case_list_description is not specified.");
      }

      // construct patient id list
      ArrayList<String> patientIDsList = new ArrayList<String>();
      String[] patientIds = patientListStr.split("\\s");
      for (String patientId : patientIds) {
         Patient p = DaoPatient.getPatientByCancerStudyAndPatientId(theCancerStudy.getInternalId(), patientId);
         if (p != null && !patientIDsList.contains(p.getStableId())) {
            patientIDsList.add(p.getStableId());
         }
         else {
            Sample s = DaoSample.getSampleByCancerStudyAndSampleId(theCancerStudy.getInternalId(), patientId);
            if (s != null) {
               p = DaoPatient.getPatientById(s.getInternalPatientId());
               if (!patientIDsList.contains(p.getStableId())) {
                  patientIDsList.add(p.getStableId());
               }
            }
         }
      }

      DaoPatientList daoPatientList = new DaoPatientList();
      PatientList patientList = daoPatientList.getPatientListByStableId(stableId);
      if (patientList != null) {
         throw new IllegalArgumentException("Patient list with this stable Id already exists:  " + stableId);
      }

      patientList = new PatientList();
      patientList.setStableId(stableId);
      int cancerStudyId = theCancerStudy.getInternalId();
      patientList.setCancerStudyId(cancerStudyId);
      patientList.setPatientListCategory(patientListCategory);
      patientList.setName(patientListName);
      patientList.setDescription(patientListDescription);
      patientList.setPatientList(patientIDsList);
      daoPatientList.addPatientList(patientList);

      patientList = daoPatientList.getPatientListByStableId(stableId);

      pMonitor.setCurrentMessage(" --> stable ID:  " + patientList.getStableId());
      pMonitor.setCurrentMessage(" --> patient list name:  " + patientList.getName());
      pMonitor.setCurrentMessage(" --> number of patients:  " + patientIDsList.size());
   }

   public static void main(String[] args) throws Exception {

      // check args
      if (args.length < 1) {
         System.out.println("command line usage:  importCaseListData.pl " + "<data_file.txt or directory>");
            return;
      }
      ProgressMonitor pMonitor = new ProgressMonitor();
      pMonitor.setConsoleMode(true);
      File dataFile = new File(args[0]);
      if (dataFile.isDirectory()) {
         File files[] = dataFile.listFiles();
         for (File file : files) {
            if (file.getName().endsWith("txt")) {
               ImportPatientList.importPatientList(file, pMonitor);
            }
         }
         if (files.length == 0) {
             pMonitor.setCurrentMessage("No patient lists found in directory, skipping import: " + dataFile.getCanonicalPath());
         }
      } else {
         ImportPatientList.importPatientList(dataFile, pMonitor);
      }
      ConsoleUtil.showWarnings(pMonitor);
   }
}
