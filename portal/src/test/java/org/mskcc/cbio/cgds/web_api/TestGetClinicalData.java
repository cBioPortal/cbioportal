package org.mskcc.cbio.cgds.web_api;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import junit.framework.TestCase;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.scripts.ImportClinicalData;
import org.mskcc.cbio.cgds.scripts.ResetDatabase;
import org.mskcc.cbio.cgds.util.ProgressMonitor;

/**
 * Tests Get Clinical Data.
 *
 * @author Ethan Cerami.
 */
public class TestGetClinicalData extends TestCase {

    /**
     * Tests Get Clinical Data.
     *
     * @throws DaoException Database Access Error.
     * @throws IOException IO Error.
     */
    public void testGetClinicalData() throws DaoException, IOException {

        ResetDatabase.resetDatabase();
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(false);
        // TBD: change this to use getResourceAsStream()
//        File file = new File("target/test-classes/clinical_test.txt");
//        ImportClinicalData importClinicalData = new ImportClinicalData(file, pMonitor);
//        importClinicalData.importData();
//		
//        HashSet<String> caseSet = new HashSet<String>();
//        caseSet.add("TCGA-04-1331");
//        caseSet.add("TCGA-24-2030");
//        caseSet.add("TCGA-24-2261");
//        String clinicalDataOut = GetClinicalData.getClinicalData(caseSet, false);
//        String lines[] = clinicalDataOut.split("\n");
//
//        assertTrue(lines[2].startsWith("TCGA-24-2030\tNA\tNA\t21.18\tRecurred/Progressed\tNA"));
//        assertTrue(lines[3].startsWith("TCGA-24-2261\t0.79\tDECEASED\tNA" +
//                "\tRecurred/Progressed\t76.43"));
    }
}
