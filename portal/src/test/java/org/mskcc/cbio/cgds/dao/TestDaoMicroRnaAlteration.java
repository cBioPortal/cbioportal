package org.mskcc.cbio.cgds.test.dao;

import junit.framework.TestCase;
import org.mskcc.cbio.cgds.dao.*;
import org.mskcc.cbio.cgds.scripts.ResetDatabase;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;

/**
 * Junit tests for DaoMicroRnaAlteration class.
 */
public class TestDaoMicroRnaAlteration extends TestCase {

    public void testDaoMicroRnaAlteration() throws DaoException {
        
        // test with both values of MySQLbulkLoader.isBulkLoad()
        MySQLbulkLoader.bulkLoadOff();
        runTheTest();
        MySQLbulkLoader.bulkLoadOn();
        runTheTest();
    }
    
    private void runTheTest() throws DaoException{
        ResetDatabase.resetDatabase();

        //  Add the Case List
        ArrayList<String> orderedCaseList = new ArrayList<String>();
        orderedCaseList.add("TCGA-1");
        orderedCaseList.add("TCGA-2");
        orderedCaseList.add("TCGA-3");
        orderedCaseList.add("TCGA-4");

        DaoGeneticProfileCases daoGeneticProfileCases = new DaoGeneticProfileCases();
        int numRows = daoGeneticProfileCases.addGeneticProfileCases(1, orderedCaseList);
        assertEquals (1, numRows);

        String data = "1.2:1.4:1.6:1.8";
        String values[] = data.split(":");

        DaoMicroRnaAlteration dao = DaoMicroRnaAlteration.getInstance();
        int num = dao.addMicroRnaAlterations(1, "hsa-123", values);

        // if bulkLoading, execute LOAD FILE
        if( MySQLbulkLoader.isBulkLoad()){
            dao.flushMicroRnaAlteration();
        }

        String value = dao.getMicroRnaAlteration(1, "TCGA-1", "hsa-123");
        assertEquals("1.2", value);
        value = dao.getMicroRnaAlteration(1, "TCGA-2", "hsa-123");
        assertEquals("1.4", value);

        HashMap<String, String> map = dao.getMicroRnaAlterationMap(1, "hsa-123");
        assertEquals (4, map.size());
        assertTrue (map.containsKey("TCGA-1"));
        assertTrue (map.containsKey("TCGA-2"));

        Set<String> microRnaSet = dao.getGenesInProfile(1);
        assertEquals (1, microRnaSet.size());
        dao.deleteAllRecords();
    }

}