/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.cgds.scripts;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;
import org.mskcc.cbio.cgds.dao.DaoCancerStudy;
import org.mskcc.cbio.cgds.dao.DaoCaseList;
import org.mskcc.cbio.cgds.model.CancerStudy;
import org.mskcc.cbio.cgds.model.CaseList;
import org.mskcc.cbio.cgds.model.CaseListCategory;
import org.mskcc.cbio.cgds.util.ConsoleUtil;
import org.mskcc.cbio.cgds.util.ProgressMonitor;

/**
 * Command Line tool to Import Case Lists.
 */
public class ImportCaseList {

   public static void importCaseList(File dataFile, ProgressMonitor pMonitor) throws Exception {
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

      String caseListName = properties.getProperty("case_list_name");
       
      String caseListCategoryStr = properties.getProperty("case_list_category");
      if (caseListCategoryStr  == null || caseListCategoryStr.length() == 0) {
          throw new IllegalArgumentException("case_list_category is not specified");
      }
      CaseListCategory caseListCategory = CaseListCategory.get(caseListCategoryStr); 
       
      String caseListDescription = properties.getProperty("case_list_description");
      String caseListStr = properties.getProperty("case_list_ids");
      if (caseListName == null) {
         throw new IllegalArgumentException("case_list_name is not specified.");
      } else if (caseListDescription == null) {
         throw new IllegalArgumentException("case_list_description is not specified.");
      }

      // construct case id list
      ArrayList<String> caseIDsList = new ArrayList<String>();
      String[] caseIds = caseListStr.split("\\s");
      for (String caseId : caseIds) {
         caseIDsList.add(caseId);
      }

      DaoCaseList daoCaseList = new DaoCaseList();
      CaseList caseList = daoCaseList.getCaseListByStableId(stableId);
      if (caseList != null) {
         throw new IllegalArgumentException("Case list with this stable Id already exists:  " + stableId);
      }

      caseList = new CaseList();
      caseList.setStableId(stableId);
      int cancerStudyId = theCancerStudy.getInternalId();
      caseList.setCancerStudyId(cancerStudyId);
      caseList.setCaseListCategory(caseListCategory);
      caseList.setName(caseListName);
      caseList.setDescription(caseListDescription);
      caseList.setCaseList(caseIDsList);
      daoCaseList.addCaseList(caseList);

      caseList = daoCaseList.getCaseListByStableId(stableId);

      pMonitor.setCurrentMessage(" --> stable ID:  " + caseList.getStableId());
      pMonitor.setCurrentMessage(" --> case list name:  " + caseList.getName());
      pMonitor.setCurrentMessage(" --> number of cases:  " + caseIDsList.size());
   }

   public static void main(String[] args) throws Exception {

      // check args
      if (args.length < 1) {
         System.out.println("command line usage:  importCaseListData.pl " + "<data_file.txt or directory>");
         System.exit(1);
      }
      ProgressMonitor pMonitor = new ProgressMonitor();
      pMonitor.setConsoleMode(true);
      File dataFile = new File(args[0]);
      if (dataFile.isDirectory()) {
         File files[] = dataFile.listFiles();
         for (File file : files) {
            if (file.getName().endsWith("txt")) {
               ImportCaseList.importCaseList(file, pMonitor);
            }
         }
      } else {
         ImportCaseList.importCaseList(dataFile, pMonitor);
      }
      ConsoleUtil.showWarnings(pMonitor);
   }
}
