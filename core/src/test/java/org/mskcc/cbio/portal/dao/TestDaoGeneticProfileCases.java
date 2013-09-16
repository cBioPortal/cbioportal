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

package org.mskcc.cbio.portal.dao;

import junit.framework.TestCase;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneticProfileCases;
import org.mskcc.cbio.portal.scripts.ResetDatabase;

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