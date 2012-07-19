package org.mskcc.cbio.cgds.test.dao;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoClinicalData;
import org.mskcc.cgds.scripts.ResetDatabase;
import org.mskcc.cgds.model.ClinicalData;

import java.util.ArrayList;
import java.util.HashSet;

import junit.framework.TestCase;

/**
 * Tests the DaoClinicalData Class.
 *
 * @author Ethan Cerami
 */
public class TestDaoClinical extends TestCase {

    /**
     * Basic Unit Tests.
     * @throws DaoException Database Access Error.
     */
    public void testDaoCase() throws DaoException {
        ResetDatabase.resetDatabase();
        DaoClinicalData daoClinical = new DaoClinicalData();

        daoClinical.addCase("TCGA-12-1234", new Double(0.5), "ALIVE", null, null, null);
        daoClinical.addCase("TCGA-12-1235", new Double(0.7), "ALIVE", new Double(0.9),
                "RECURRED", new Double(65));

        HashSet<String> caseSet = new HashSet<String>();
        caseSet.add("TCGA-12-1234");
        caseSet.add("TCGA-12-1235");
        ArrayList <ClinicalData> caseList = daoClinical.getCases(caseSet);

        assertEquals (2, caseList.size());
        ClinicalData caseSurvival = caseList.get(0);
        assertEquals ("TCGA-12-1234", caseSurvival.getCaseId());
        assertEquals (new Double(0.5), caseSurvival.getOverallSurvivalMonths());
        assertEquals ("ALIVE", caseSurvival.getOverallSurvivalStatus());
        assertNull (caseSurvival.getDiseaseFreeSurvivalMonths());
        assertNull (caseSurvival.getDiseaseFreeSurvivalStatus());
        assertNull (caseSurvival.getAgeAtDiagnosis());

        caseSurvival = caseList.get(1);
        assertEquals ("TCGA-12-1235", caseSurvival.getCaseId());
        assertEquals (new Double(0.7), caseSurvival.getOverallSurvivalMonths());
        assertEquals ("ALIVE", caseSurvival.getOverallSurvivalStatus());
        assertEquals (new Double(0.9), caseSurvival.getDiseaseFreeSurvivalMonths());
        assertEquals ("RECURRED", caseSurvival.getDiseaseFreeSurvivalStatus());
        assertEquals (new Double(65), caseSurvival.getAgeAtDiagnosis());
    }
}
