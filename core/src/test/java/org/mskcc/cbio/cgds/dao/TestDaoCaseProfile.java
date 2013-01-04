package org.mskcc.cbio.cgds.dao;

import java.util.ArrayList;
import junit.framework.TestCase;
import org.mskcc.cbio.cgds.scripts.ResetDatabase;

/**
 * JUnit test for DaoCase class
 */
public class TestDaoCaseProfile extends TestCase {

    public void testDaoCaseProfile() throws DaoException {
        ResetDatabase.resetDatabase();

        int num = DaoCaseProfile.addCaseProfile("TCGA-12345", 1);
        assertEquals(1, num);
        boolean exists = DaoCaseProfile.caseExistsInGeneticProfile("TCGA-12345", 1);
        assertTrue(exists);

        assertEquals(1, DaoCaseProfile.getProfileIdForCase( "TCGA-12345" ));
        
        num = DaoCaseProfile.addCaseProfile("TCGA-123456", 1);
        assertEquals(1, num);
        ArrayList<String> caseIds = DaoCaseProfile.getAllCaseIdsInProfile(1);
        assertEquals(2, caseIds.size());
        DaoCaseProfile.deleteAllRecords();
    }
}
