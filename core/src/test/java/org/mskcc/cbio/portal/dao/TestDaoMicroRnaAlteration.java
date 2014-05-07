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

import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.scripts.ResetDatabase;

import java.util.*;
import junit.framework.TestCase;

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
        ArrayList<Integer> internalSampleIds = createSamples();

        //  Add the Case List
        int numRows = DaoGeneticProfileSamples.addGeneticProfileSamples(1, internalSampleIds);
        assertEquals (1, numRows);

        String data = "1.2:1.4:1.6:1.8";
        String values[] = data.split(":");

        DaoMicroRnaAlteration dao = DaoMicroRnaAlteration.getInstance();
        int num = dao.addMicroRnaAlterations(1, "hsa-123", values);

        // if bulkLoading, execute LOAD FILE
        if( MySQLbulkLoader.isBulkLoad()){
           MySQLbulkLoader.flushAll();
        }

        String value = dao.getMicroRnaAlteration(1, internalSampleIds.get(0), "hsa-123");
        assertEquals("1.2", value);
        value = dao.getMicroRnaAlteration(1, internalSampleIds.get(1), "hsa-123");
        assertEquals("1.4", value);

        HashMap<Integer, String> map = dao.getMicroRnaAlterationMap(1, "hsa-123");
        assertEquals (4, map.size());
        assertTrue (map.containsKey(internalSampleIds.get(1)));
        assertTrue (map.containsKey(internalSampleIds.get(2)));

        Set<String> microRnaSet = dao.getGenesInProfile(1);
        assertEquals (1, microRnaSet.size());
        dao.deleteAllRecords();
    }

    private ArrayList<Integer> createSamples() throws DaoException {
        ArrayList<Integer> toReturn = new ArrayList<Integer>();
        CancerStudy study = new CancerStudy("study", "description", "id", "brca", true);
        Patient p = new Patient(study, "TCGA-1");
        int pId = DaoPatient.addPatient(p);
        Sample s = new Sample("TCGA-1-1-01", pId, "type");
        toReturn.add(DaoSample.addSample(s));
        s = new Sample("TCGA-1-2-01", pId, "type");
        toReturn.add(DaoSample.addSample(s));
        s = new Sample("TCGA-1-3-01", pId, "type");
        toReturn.add(DaoSample.addSample(s));
        s = new Sample("TCGA-1-4-01", pId, "type");
        toReturn.add(DaoSample.addSample(s));
        return toReturn;
    }
}