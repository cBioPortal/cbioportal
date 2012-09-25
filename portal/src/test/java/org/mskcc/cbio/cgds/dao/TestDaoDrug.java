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
import org.mskcc.cbio.cgds.dao.DaoDrug;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.model.Drug;
import org.mskcc.cbio.cgds.scripts.ResetDatabase;
import org.mskcc.cbio.cgds.dao.MySQLbulkLoader;

public class TestDaoDrug extends TestCase {
    public void testDaoDrug() throws DaoException {

        ResetDatabase.resetDatabase();

		// save bulkload setting before turning off
		boolean isBulkLoad = MySQLbulkLoader.isBulkLoad();
		MySQLbulkLoader.bulkLoadOff();

        DaoDrug daoDrug = DaoDrug.getInstance();
        Drug drug = new Drug("Dummy:1", "MyDrug", "description",
                "synonym,synonym2", "this is an xref", "DUMMY", true, "B01AE02");
        Drug drug2 = new Drug("Dummy:2", "MyDrug2", "description2",
                "synonym", "this is an xref2", "BLA", false, "L01XX29");

        assertEquals(daoDrug.addDrug(drug), 1);
        assertEquals(daoDrug.addDrug(drug2), 1);

        Drug tmpDrug = daoDrug.getDrug("Dummy:1");
        assertNotNull(tmpDrug);
        assertEquals(tmpDrug.getName(), "MyDrug");
        assertEquals(tmpDrug.getDescription(), "description");
        assertEquals(tmpDrug.getSynonyms(), "synonym,synonym2");
        assertEquals(tmpDrug.getResource(), "DUMMY");
        assertTrue(tmpDrug.isApprovedFDA());
        assertEquals("B01AE02", drug.getATCCode());

        Drug tmpDrug2 = daoDrug.getDrug("Dummy:2");
        assertNotNull(tmpDrug2);
        assertEquals(tmpDrug2.getName(), "MyDrug2");
        assertFalse(tmpDrug2.isApprovedFDA());

        assertNull(daoDrug.getDrug("Dummy:BLABLA"));

        assertEquals(2, daoDrug.getAllDrugs().size());

		// restore bulk setting
		if (isBulkLoad) {
			MySQLbulkLoader.bulkLoadOn();
		}
    }
}
