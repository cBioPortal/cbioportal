package org.mskcc.cgds.test.scripts;

import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoClinicalData;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.scripts.ImportClinicalData;
import org.mskcc.cgds.scripts.ResetDatabase;
import org.mskcc.cgds.util.ProgressMonitor;
import org.mskcc.cgds.model.ClinicalData;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.ArrayList;

/**
 * Tests Import of Clinical Data.
 *
 * @author Ethan Cerami.
 */
public class TestImportClinicalData extends TestCase {

    /**
     * Test importing of Clinical Data File.
     *
     * @throws DaoException Database Access Error.
     * @throws IOException  IO Error.
     */
    public void testImportClinicalData() throws DaoException, IOException {
        ResetDatabase.resetDatabase();
        ProgressMonitor pMonitor = new ProgressMonitor();
        File file = new File("testData/clinical_test.txt");
        ImportClinicalData importClinicalData = new ImportClinicalData(file, pMonitor);
        importClinicalData.importData();

        DaoClinicalData dao = new DaoClinicalData();

        HashSet <String> caseSet = new HashSet<String>();
        caseSet.add("TCGA-04-1331");
        caseSet.add("TCGA-24-2030");
        caseSet.add("TCGA-24-2261");

        ArrayList<ClinicalData> clinicalCaseList = dao.getCases(caseSet);
        assertEquals (3, clinicalCaseList.size());

        ClinicalData clinical0 = clinicalCaseList.get(0);
        assertEquals (new Double(79.04), clinical0.getAgeAtDiagnosis());
        assertEquals ("DECEASED", clinical0.getOverallSurvivalStatus());
        assertEquals ("Recurred/Progressed", clinical0.getDiseaseFreeSurvivalStatus());
        assertEquals (new Double(43.8), clinical0.getOverallSurvivalMonths());
        assertEquals (new Double(15.05), clinical0.getDiseaseFreeSurvivalMonths());

        ClinicalData clinical1 = clinicalCaseList.get(1);
        assertEquals (null, clinical1.getAgeAtDiagnosis());
        assertEquals (null, clinical1.getOverallSurvivalStatus());
        assertEquals ("Recurred/Progressed", clinical1.getDiseaseFreeSurvivalStatus());
        assertEquals (null, clinical1.getOverallSurvivalMonths());
        assertEquals (new Double(21.18), clinical1.getDiseaseFreeSurvivalMonths());

        ClinicalData clinical2 = clinicalCaseList.get(2);
        assertEquals (null, clinical2.getDiseaseFreeSurvivalMonths());
    }
}