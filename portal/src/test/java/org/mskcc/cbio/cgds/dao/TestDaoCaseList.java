package org.mskcc.cbio.cgds.test.dao;

import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoCase;
import org.mskcc.cgds.dao.DaoCaseList;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.model.CaseList;
import org.mskcc.cgds.model.CaseListCategory;
import org.mskcc.cgds.scripts.ResetDatabase;

import java.util.ArrayList;

/**
 * JUnit test for DaoCase List.
 */
public class TestDaoCaseList extends TestCase {

    public void testDaoCaseList() throws DaoException {
        ResetDatabase.resetDatabase();
        DaoCaseList daoCaseList = new DaoCaseList();

        CaseList caseList = new CaseList();
        caseList.setName("Name0");
        caseList.setDescription("Description0");
        caseList.setStableId("stable_0");
        caseList.setCancerStudyId(2);
        caseList.setCaseListCategory(CaseListCategory.ALL_CASES_WITH_CNA_DATA);
        ArrayList<String> cases = new ArrayList<String>();
        cases.add("TCGA-1");
        cases.add("TCGA-2");
        caseList.setCaseList(cases);
        daoCaseList.addCaseList(caseList);
        
        CaseList caseListFromDb = daoCaseList.getCaseListByStableId("stable_0");
        assertEquals("Name0", caseListFromDb.getName());
        assertEquals("Description0", caseListFromDb.getDescription());
        assertEquals(CaseListCategory.ALL_CASES_WITH_CNA_DATA, caseListFromDb.getCaseListCategory());
        assertEquals("stable_0", caseListFromDb.getStableId());
        assertEquals(2, caseListFromDb.getCaseList().size());
    }
}