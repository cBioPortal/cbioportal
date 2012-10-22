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
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoCase;
import org.mskcc.cbio.cgds.scripts.ResetDatabase;

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