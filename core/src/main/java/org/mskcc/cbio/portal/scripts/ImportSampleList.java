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
 * Command Line tool to Import Sample Lists.
 */
public class ImportSampleList {

   public static void importSampleList(File dataFile) throws Exception {
      ProgressMonitor.setCurrentMessage("Read data from:  " + dataFile.getAbsolutePath());
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

      String sampleListName = properties.getProperty("case_list_name");
       
      String sampleListCategoryStr = properties.getProperty("case_list_category");
      if (sampleListCategoryStr  == null || sampleListCategoryStr.length() == 0) {
          sampleListCategoryStr = "other";
      }
      SampleListCategory sampleListCategory = SampleListCategory.get(sampleListCategoryStr); 
       
      String sampleListDescription = properties.getProperty("case_list_description");
      String sampleListStr = properties.getProperty("case_list_ids");
      if (sampleListName == null) {
         throw new IllegalArgumentException("case_list_name is not specified.");
      } else if (sampleListDescription == null) {
         throw new IllegalArgumentException("case_list_description is not specified.");
      }

      // construct sample id list
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
             ProgressMonitor.logWarning("Warning: duplicated sample ID "+s.getStableId()+" in case list "+stableId);
         }
      }

      DaoSampleList daoSampleList = new DaoSampleList();
      SampleList sampleList = daoSampleList.getSampleListByStableId(stableId);
      if (sampleList != null) {
         throw new IllegalArgumentException("Patient list with this stable Id already exists:  " + stableId);
      }

      sampleList = new SampleList();
      sampleList.setStableId(stableId);
      int cancerStudyId = theCancerStudy.getInternalId();
      sampleList.setCancerStudyId(cancerStudyId);
      sampleList.setSampleListCategory(sampleListCategory);
      sampleList.setName(sampleListName);
      sampleList.setDescription(sampleListDescription);
      sampleList.setSampleList(sampleIDsList);
      daoSampleList.addSampleList(sampleList);

      sampleList = daoSampleList.getSampleListByStableId(stableId);

      ProgressMonitor.setCurrentMessage(" --> stable ID:  " + sampleList.getStableId());
      ProgressMonitor.setCurrentMessage(" --> sample list name:  " + sampleList.getName());
      ProgressMonitor.setCurrentMessage(" --> number of samples:  " + sampleIDsList.size());
   }

   public static void main(String[] args) throws Exception {

      // check args
      if (args.length < 1) {
         System.out.println("command line usage:  importCaseListData.pl " + "<data_file.txt or directory>");
            return;
      }
      ProgressMonitor.setConsoleMode(true);
      File dataFile = new File(args[0]);
      if (dataFile.isDirectory()) {
         File files[] = dataFile.listFiles();
         for (File file : files) {
            if (file.getName().endsWith("txt")) {
               ImportSampleList.importSampleList(file);
            }
         }
         if (files.length == 0) {
             ProgressMonitor.setCurrentMessage("No patient lists found in directory, skipping import: " + dataFile.getCanonicalPath());
         }
      } else {
         ImportSampleList.importSampleList(dataFile);
      }
      ConsoleUtil.showWarnings();
   }
}
