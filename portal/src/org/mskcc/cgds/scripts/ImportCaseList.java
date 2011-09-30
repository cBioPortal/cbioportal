package org.mskcc.cgds.scripts;

import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoCaseList;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.CaseList;
import org.mskcc.cgds.util.ConsoleUtil;
import org.mskcc.cgds.util.ProgressMonitor;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.ArrayList;

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
      caseList.setCancerStudyId( theCancerStudy.getInternalId());
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
