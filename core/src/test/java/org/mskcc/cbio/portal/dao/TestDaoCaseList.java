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

package org.mskcc.cbio.portal.dao;

import java.util.ArrayList;
import junit.framework.TestCase;
import org.mskcc.cbio.portal.model.CaseList;
import org.mskcc.cbio.portal.model.CaseListCategory;
import org.mskcc.cbio.portal.scripts.ResetDatabase;

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
