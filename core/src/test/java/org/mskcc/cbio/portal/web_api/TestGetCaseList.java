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

package org.mskcc.cbio.portal.web_api;

import java.io.File;
import junit.framework.TestCase;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.scripts.ImportCaseList;
import org.mskcc.cbio.portal.scripts.ImportTypesOfCancers;
import org.mskcc.cbio.portal.scripts.ResetDatabase;
import org.mskcc.cbio.portal.util.ProgressMonitor;

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
      String[] caseList = GetCaseLists.getCaseListsAsTable("gbm").split("\n");
      assertTrue(caseList[1]
               .startsWith("gbm_91\tGBM 91\tGBM 91 Case List Description\t1\tTCGA-02-0001 TCGA-02-0003 TCGA-02-0006"));
   }
}
