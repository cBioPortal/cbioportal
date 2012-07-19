package org.mskcc.cgds.test.dao;

import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoCase;
import org.mskcc.cgds.scripts.ResetDatabase;

import java.util.ArrayList;

/**
 * JUnit test for DaoCase class
 */
public class TestDaoCase extends TestCase {

    public void testDaoCase() throws DaoException {
        ResetDatabase.resetDatabase();
        DaoCase daoCase = new DaoCase();

        int num = daoCase.addCase("TCGA-12345", 1);
        assertEquals(1, num);
        boolean exists = daoCase.caseExistsInGeneticProfile("TCGA-12345", 1);
        assertTrue(exists);

        assertEquals(1, daoCase.getProfileIdForCase( "TCGA-12345" ));
        
        num = daoCase.addCase("TCGA-123456", 1);
        assertEquals(1, num);
        ArrayList<String> caseIds = daoCase.getAllCaseIdsInProfile(1);
        assertEquals(2, caseIds.size());
        daoCase.deleteAllRecords();
    }
}