package org.mskcc.portal.test.remote;

import junit.framework.TestCase;
import org.mskcc.portal.model.CaseSet;
import org.mskcc.portal.remote.GetCaseSets;
import org.mskcc.portal.util.XDebug;

import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * JUnit test for GetCaseSets class.
 */
public class TestGetCaseSets extends TestCase {
    private static final int EXPECTED_PLIST_SIZE = 138;

    /**
     * Tests the GetCaseSets class.
     *
     * @throws RemoteException Remote IO Error.
     */
    public void testGetCaseSets() throws RemoteException {
        ArrayList<CaseSet> caseList = GetCaseSets.getCaseSets("gbm", new XDebug());
        assertEquals(10, caseList.size());
        CaseSet p0 = caseList.get(0);
        assertEquals("gbm_3way_complete", p0.getId());
        assertEquals("All Complete Tumors (seq, mRNA, CNA)", p0.getName());
        ArrayList<String> pList = p0.getCaseList();
        assertEquals(EXPECTED_PLIST_SIZE, pList.size());
    }
}
