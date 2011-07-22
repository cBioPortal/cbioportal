package org.mskcc.cgds.test.web_api;

import java.io.File;
import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.scripts.ImportCaseList;
import org.mskcc.cgds.scripts.ImportTypesOfCancers;
import org.mskcc.cgds.scripts.ResetDatabase;
import org.mskcc.cgds.util.ProgressMonitor;
import org.mskcc.cgds.web_api.GetCaseLists;

public class TestGetCaseList extends TestCase {

   public void testGetCaseList() throws Exception {

      ResetDatabase.resetDatabase();
      // load cancers
      String[] args = { "testData/cancers.txt" };
      ImportTypesOfCancers.main(args);

      // corresponds to cancer_study_identifier: gbm in
      // testData/case_list_test.txt
      CancerStudy cancerStudy = new CancerStudy( "GBM", "GBM Description", "gbm", "GBM", false);
      DaoCancerStudy.addCancerStudy(cancerStudy);

      ProgressMonitor pMonitor = new ProgressMonitor();
      pMonitor.setConsoleMode(false);
      File file = new File("testData/case_list_test.txt");

      ImportCaseList.importCaseList(file, pMonitor);
      String[] caseList = GetCaseLists.getCaseLists(1).split("\n");
      assertTrue(caseList[1]
               .startsWith("gbm_91\tGBM 91\tGBM 91 Case List Description\t1\tTCGA-02-0001 TCGA-02-0003 TCGA-02-0006"));
   }
}
