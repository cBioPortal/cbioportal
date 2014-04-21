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

package org.mskcc.cbio.portal.web_api;

import java.io.File;
import junit.framework.TestCase;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.scripts.*;
import org.mskcc.cbio.portal.util.*;

/**
 * JUnit test for GetPatientLists class.
 */
public class TestGetPatientList extends TestCase {

   public void testGetPatientList() throws Exception {

       createSmallDbms();

      ProgressMonitor pMonitor = new ProgressMonitor();
      pMonitor.setConsoleMode(false);
	  // TBD: change this to use getResourceAsStream()
      File file = new File("target/test-classes/case_list_test.txt");
      
      ImportPatientList.importPatientList(file, pMonitor);
      String[] patientList = GetPatientLists.getPatientListsAsTable("gbm").split("\n");
      assertTrue(patientList[1].startsWith("gbm_6\tGBM 6\tGBM 6 Case List Description"));
   }

    private void createSmallDbms() throws DaoException
    {
        TestImportUtil.createSmallDbms(true);

        CancerStudy study = DaoCancerStudy.getCancerStudyByStableId("gbm");

        Patient p = new Patient(study, "TCGA-02-0001");
        int pId = DaoPatient.addPatient(p);
        Sample s = new Sample("TCGA-02-0001-01", pId, "type");
        DaoSample.addSample(s);

        p = new Patient(study, "TCGA-02-0003");
        pId = DaoPatient.addPatient(p);
        s = new Sample("TCGA-02-0003-01", pId, "type");
        DaoSample.addSample(s);

        p = new Patient(study, "TCGA-02-0006");
        pId = DaoPatient.addPatient(p);
        s = new Sample("TCGA-02-0006-01", pId, "type");
        DaoSample.addSample(s);

        p = new Patient(study, "TCGA-02-0007");
        pId = DaoPatient.addPatient(p);
        s = new Sample("TCGA-02-0007-01", pId, "type");
        DaoSample.addSample(s);

        p = new Patient(study, "TCGA-02-0009");
        pId = DaoPatient.addPatient(p);
        s = new Sample("TCGA-02-0009-01", pId, "type");
        DaoSample.addSample(s);

        p = new Patient(study, "TCGA-02-0010");
        pId = DaoPatient.addPatient(p);
        s = new Sample("TCGA-02-0010-01", pId, "type");
        DaoSample.addSample(s);
    }
}
