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
	  SpringUtil.initDataSource();
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
      String sampleListStr = properties.getProperty("case_list_ids");
      if (patientListName == null) {
         throw new IllegalArgumentException("case_list_name is not specified.");
      } else if (patientListDescription == null) {
         throw new IllegalArgumentException("case_list_description is not specified.");
      }

      // construct patient id list
      ArrayList<String> sampleIDsList = new ArrayList<String>();
      String[] sampleIds = sampleListStr.split("\t");
      for (String sampleId : sampleIds) {
         Sample s = DaoSample.getSampleByCancerStudyAndSampleId(theCancerStudy.getInternalId(), sampleId);
         if (s==null) {
//            throw new RuntimeException("Sample does not exist: "+sampleId);
             System.err.println("Error: could not find sample "+sampleId);
             Patient p = DaoPatient.getPatientByCancerStudyAndPatientId(theCancerStudy.getInternalId(), sampleId);
             if (p!=null) {
                System.err.println("Error: but found a patient with this ID. Will use it in the sample list.");
                List<Sample> samples = DaoSample.getSamplesByPatientId(p.getInternalId());
                for (Sample sa : samples) {
                      if (!sampleIDsList.contains(sa.getStableId())) {
                          sampleIDsList.add(sa.getStableId());
                      }
                }
             } else {
                 //throw new RuntimeException("Sample does not exist: "+sampleId);
             }
         } else if (!sampleIDsList.contains(s.getStableId())) {
            sampleIDsList.add(s.getStableId());
         } else {
             System.err.println("Warning: duplicated sample ID "+s.getStableId()+" in case list "+stableId);
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
      patientList.setPatientList(sampleIDsList);
      daoPatientList.addPatientList(patientList);

      patientList = daoPatientList.getPatientListByStableId(stableId);

      pMonitor.setCurrentMessage(" --> stable ID:  " + patientList.getStableId());
      pMonitor.setCurrentMessage(" --> patient list name:  " + patientList.getName());
      pMonitor.setCurrentMessage(" --> number of patients:  " + sampleIDsList.size());
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
