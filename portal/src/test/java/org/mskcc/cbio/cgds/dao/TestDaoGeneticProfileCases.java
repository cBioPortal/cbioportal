package org.mskcc.cbio.cgds.test.dao;

import junit.framework.TestCase;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoGeneticProfileCases;
import org.mskcc.cbio.cgds.scripts.ResetDatabase;

import java.util.ArrayList;

/**
 * JUnit Tests for the Dao Genetic Profile Cases Class.
 *
 * @author Ethan Cerami.
 */
public class TestDaoGeneticProfileCases extends TestCase {

    /**
     * Tests the Dao Genetic Profile Cases Class.
     * @throws DaoException Database Exception.
     */
    public void testDaoGeneticProfileCases() throws DaoException {
        ResetDatabase.resetDatabase();
        DaoGeneticProfileCases daoGeneticProfileCases = new DaoGeneticProfileCases();

        ArrayList<String> orderedCaseList = new ArrayList<String>();
        orderedCaseList.add("TCGA-1");
        orderedCaseList.add("TCGA-2");
        orderedCaseList.add("TCGA-3");
        orderedCaseList.add("TCGA-4");
        int numRows = daoGeneticProfileCases.addGeneticProfileCases(1, orderedCaseList);

        assertEquals (1, numRows);

        orderedCaseList = daoGeneticProfileCases.getOrderedCaseList(1);
        assertEquals (4, orderedCaseList.size());

        //  Test the Delete method
        daoGeneticProfileCases.deleteAllCasesInGeneticProfile(1);
        orderedCaseList = daoGeneticProfileCases.getOrderedCaseList(1);
        assertEquals (0, orderedCaseList.size());

    }

}