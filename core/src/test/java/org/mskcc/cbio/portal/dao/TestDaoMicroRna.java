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
import org.mskcc.cbio.portal.scripts.ResetDatabase;

/**
 * JUnit tests for DaoMicroRna class.
 */
public class TestDaoMicroRna extends TestCase {

    public void testDaoMicroRna() throws DaoException {

        // test with both values of MySQLbulkLoader.isBulkLoad()
        MySQLbulkLoader.bulkLoadOff();
        runTheTest();
        MySQLbulkLoader.bulkLoadOn();
        runTheTest();
    }

    private void runTheTest() throws DaoException{
        
        ResetDatabase.resetDatabase();
        DaoMicroRna daoMicroRna = new DaoMicroRna();
        daoMicroRna.addMicroRna("hsa-let-7a", "hsa-let-7a-1");
        daoMicroRna.addMicroRna("hsa-let-7a", "hsa-let-7a-2");

        // if bulkLoading, execute LOAD FILE
        if( MySQLbulkLoader.isBulkLoad()){
           MySQLbulkLoader.flushAll();
        }
        daoMicroRna.addMicroRna("hsa-let-7a", "hsa-let-7a-3");

        // if bulkLoading, execute LOAD FILE
        if( MySQLbulkLoader.isBulkLoad()){
           MySQLbulkLoader.flushAll();
        }
        ArrayList<String> variantIdList = daoMicroRna.getVariantIds("hsa-let-7a");
        assertEquals (3, variantIdList.size());
    }
}
