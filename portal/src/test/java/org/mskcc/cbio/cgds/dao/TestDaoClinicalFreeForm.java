package org.mskcc.cbio.cgds.test.dao;

import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoClinicalFreeForm;
import org.mskcc.cbio.cgds.scripts.ResetDatabase;
import org.mskcc.cbio.cgds.model.ClinicalParameterMap;

import junit.framework.TestCase;

import java.util.HashSet;

/**
 * Tests the DaoClinicalData Class.
 *
 * @author Ethan Cerami
 */
public class TestDaoClinicalFreeForm extends TestCase {
    private static final int DUMMY_CANCER_STUDY_ID = 1;

    /**
     * Basic Unit Tests.
     * @throws org.mskcc.cgds.dao.DaoException Database Access Error.
     */
    public void testDaoCase() throws DaoException {
        ResetDatabase.resetDatabase();
        DaoClinicalFreeForm daoClinicalFreeForm = new DaoClinicalFreeForm();
        daoClinicalFreeForm.addDatum(DUMMY_CANCER_STUDY_ID, "TCGA-1", "CNA_CLUSTER", "1");
        daoClinicalFreeForm.addDatum(DUMMY_CANCER_STUDY_ID, "TCGA-2", "CNA_CLUSTER", "2");
        daoClinicalFreeForm.addDatum(DUMMY_CANCER_STUDY_ID, "TCGA-3", "CNA_CLUSTER", "2");
        daoClinicalFreeForm.addDatum(DUMMY_CANCER_STUDY_ID, "TCGA-4", "CNA_CLUSTER", "1");
        daoClinicalFreeForm.addDatum(DUMMY_CANCER_STUDY_ID, "TCGA-1", "HYPER_MUTATED", "YES");
        daoClinicalFreeForm.addDatum(DUMMY_CANCER_STUDY_ID, "TCGA-2", "HYPER_MUTATED", "YES");
        daoClinicalFreeForm.addDatum(DUMMY_CANCER_STUDY_ID, "TCGA-3", "HYPER_MUTATED", "NO");
        daoClinicalFreeForm.addDatum(DUMMY_CANCER_STUDY_ID, "TCGA-4", "HYPER_MUTATED", "NO");

        ClinicalParameterMap paramMap = daoClinicalFreeForm.getDataSlice(DUMMY_CANCER_STUDY_ID, "CNA_CLUSTER");
        assertEquals ("CNA_CLUSTER", paramMap.getName());
        assertEquals("1", paramMap.getValue("TCGA-1"));
        assertEquals("2", paramMap.getValue("TCGA-3"));
        assertEquals(2, paramMap.getDistinctCategories().size());

        HashSet<String> paramSet = daoClinicalFreeForm.getDistinctParameters(DUMMY_CANCER_STUDY_ID);
        assertEquals (2, paramSet.size());
    }
}