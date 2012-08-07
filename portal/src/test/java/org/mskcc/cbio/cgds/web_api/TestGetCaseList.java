package org.mskcc.cbio.cgds.web_api;

import java.io.File;
import junit.framework.TestCase;
import org.mskcc.cbio.cgds.dao.DaoCancerStudy;
import org.mskcc.cbio.cgds.model.CancerStudy;
import org.mskcc.cbio.cgds.scripts.ImportCaseList;
import org.mskcc.cbio.cgds.scripts.ImportTypesOfCancers;
import org.mskcc.cbio.cgds.scripts.ResetDatabase;
import org.mskcc.cbio.cgds.util.ProgressMonitor;
import org.mskcc.cbio.cgds.web_api.GetCaseLists;

/**
 * JUnit test for GetCaseLists class.
 */
public class TestGetCaseList extends TestCase {

   public void testGetCaseList() throws Exception {

      ResetDatabase.resetDatabase();
      // load cancers
	  // TBD: change this to use getResourceAsStream()
      ImportTypesOfCancers.load(new ProgressMonitor(), new File("target/test-classes/cancers.txt"));

      // corresponds to cancer_study_identifier: gbm in
      // /case_list_test.txt
      CancerStudy cancerStudy = new CancerStudy( "GBM", "GBM Description", "gbm", "GBM", false);
      DaoCancerStudy.addCancerStudy(cancerStudy);

      ProgressMonitor pMonitor = new ProgressMonitor();
      pMonitor.setConsoleMode(false);
	  // TBD: change this to use getResourceAsStream()
      File file = new File("target/test-classes/case_list_test.txt");

      ImportCaseList.importCaseList(file, pMonitor);
      String[] caseList = GetCaseLists.getCaseLists("GBM").split("\n");
      assertTrue(caseList[1]
               .startsWith("gbm_91\tGBM 91\tGBM 91 Case List Description\t1\tTCGA-02-0001 TCGA-02-0003 TCGA-02-0006"));
   }
}
