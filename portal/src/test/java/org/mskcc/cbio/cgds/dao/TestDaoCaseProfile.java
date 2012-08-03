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
        DaoCaseProfile daoCaseProfile = new DaoCaseProfile();

        int num = daoCaseProfile.addCaseProfile("TCGA-12345", 1);
        assertEquals(1, num);
        boolean exists = daoCaseProfile.caseExistsInGeneticProfile("TCGA-12345", 1);
        assertTrue(exists);

        assertEquals(1, daoCaseProfile.getProfileIdForCase( "TCGA-12345" ));
        
        num = daoCaseProfile.addCaseProfile("TCGA-123456", 1);
        assertEquals(1, num);
        ArrayList<String> caseIds = daoCaseProfile.getAllCaseIdsInProfile(1);
        assertEquals(2, caseIds.size());
        daoCaseProfile.deleteAllRecords();
    }
}
