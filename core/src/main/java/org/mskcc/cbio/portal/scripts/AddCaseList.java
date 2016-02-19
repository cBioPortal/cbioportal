/*
 * Copyright (c) 2016 The Hyve B.V.
 * This code is licensed under the GNU Affero General Public License (AGPL),
 * version 3, or (at your option) any later version.
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

import java.util.ArrayList;
import java.util.List;

import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoPatientList;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.PatientList;
import org.mskcc.cbio.portal.model.PatientListCategory;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.util.SpringUtil;

/**
 * Command Line tool to Add new case lists by generating them based on some rules.
 */
public class AddCaseList {

	
	/**
	 * Add case list of type "all" 
	 * 
	 * @param theCancerStudy
	 * @param pMonitor
	 * @throws Exception
	 */
	private static void addAllCasesList(CancerStudy theCancerStudy) throws Exception {
		String cancerStudyIdentifier = theCancerStudy.getCancerStudyStableId();
		String stableId = cancerStudyIdentifier + "_all";
		ProgressMonitor.setCurrentMessage("Adding case list:  " + stableId + "..."); 
		
		String patientListCategoryStr = "other"; //TODO : check if this is important...
		PatientListCategory patientListCategory = PatientListCategory.get(patientListCategoryStr); 
		   
		String patientListDescription = "All cases in study";
		String patientListName = patientListDescription;
				
		// construct sample id list
		ArrayList<String> sampleIDsList = new ArrayList<String>();
		  
		List<String> sampleIds = DaoSample.getSampleStableIdsByCancerStudy(theCancerStudy.getInternalId());
		for (String sampleId : sampleIds) {
		   Sample s = DaoSample.getSampleByCancerStudyAndSampleId(theCancerStudy.getInternalId(), sampleId);
		   if (s==null) {
		       System.err.println("Error: could not find sample "+sampleId);
		   } else {
		      sampleIDsList.add(s.getStableId());
		   }
		}
		addCaseList(stableId, theCancerStudy, patientListCategory, patientListName, patientListDescription,  sampleIDsList);
	}
   
	/**
	 * Generic method to add a case list
	 * 
	 * @param stableId
	 * @param theCancerStudy
	 * @param patientListCategory
	 * @param patientListName
	 * @param patientListDescription
	 * @param sampleIDsList
	 * @param pMonitor
	 * @throws Exception
	 */
   public static void addCaseList(String stableId, CancerStudy theCancerStudy, 
		   PatientListCategory patientListCategory, String patientListName, String patientListDescription, 
		   ArrayList<String> sampleIDsList) throws Exception {

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

      ProgressMonitor.setCurrentMessage(" --> stable ID:  " + patientList.getStableId());
      ProgressMonitor.setCurrentMessage(" --> patient list name:  " + patientList.getName());
      ProgressMonitor.setCurrentMessage(" --> number of patients:  " + sampleIDsList.size());
   }

   public static void main(String[] args) throws Exception {

      // check args
      if (args.length < 2) {
         System.out.println("command line usage:  addCaseList.pl " + "<study identifier> <case list type>");
         // an extra --noprogress option can be given to avoid the messages regarding memory usage and % complete
         return;
      }
      ProgressMonitor.setConsoleModeAndParseShowProgress(args);
      
      String cancerStudyIdentifier = args[0];
      String caseListType = args[1];
      if (cancerStudyIdentifier == null) {
          throw new IllegalArgumentException("cancer_study_identifier is not specified.");
      }
 	  SpringUtil.initDataSource();
      CancerStudy theCancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyIdentifier);
      if (theCancerStudy == null) {
          throw new IllegalArgumentException("cancer study identified by cancer_study_identifier '"
                   + cancerStudyIdentifier + "' not found in dbms or inaccessible to user.");
      }
      
      if (caseListType.equals("all")) {
	      //Add "all" case list:
	      AddCaseList.addAllCasesList(theCancerStudy);
      }
      
      ConsoleUtil.showWarnings();
      System.err.println("Done.");
   }
}
