/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.cgds.dao;

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