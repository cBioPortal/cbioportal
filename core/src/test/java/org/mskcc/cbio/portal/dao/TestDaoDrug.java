/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.model.Drug;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestDaoDrug {
	
	@Test
    public void testDaoDrug() throws DaoException {

		// save bulkload setting before turning off
		boolean isBulkLoad = MySQLbulkLoader.isBulkLoad();
		MySQLbulkLoader.bulkLoadOff();

        DaoDrug daoDrug = DaoDrug.getInstance();
        Drug drug = new Drug("Dummy:1", "MyDrug", "description",
                "synonym,synonym2", "this is an xref", "DUMMY", "B01AE02", true, true, false, 25);
        Drug drug2 = new Drug("Dummy:2", "MyDrug2", "description2",
                "synonym", "this is an xref2", "BLA", "L01XX29", false, false, true, -1);

        assertEquals(daoDrug.addDrug(drug), 1);
        assertEquals(daoDrug.addDrug(drug2), 1);

        Drug tmpDrug = daoDrug.getDrug("Dummy:1");
        assertNotNull(tmpDrug);
        assertEquals(tmpDrug.getName(), "MyDrug");
        assertEquals(tmpDrug.getDescription(), "description");
        assertEquals(tmpDrug.getSynonyms(), "synonym,synonym2");
        assertEquals(tmpDrug.getResource(), "DUMMY");
        assertEquals("B01AE02", drug.getATCCode());
        assertTrue(tmpDrug.isApprovedFDA());
        assertTrue(tmpDrug.isCancerDrug());
        assertFalse(tmpDrug.isNutraceuitical());
        assertEquals(Integer.valueOf(25), tmpDrug.getNumberOfClinicalTrials());

        Drug tmpDrug2 = daoDrug.getDrug("Dummy:2");
        assertNotNull(tmpDrug2);
        assertEquals(tmpDrug2.getName(), "MyDrug2");
        assertFalse(tmpDrug2.isApprovedFDA());
        assertFalse(tmpDrug2.isCancerDrug());
        assertTrue(tmpDrug2.isNutraceuitical());
        assertEquals(Integer.valueOf(-1), tmpDrug2.getNumberOfClinicalTrials());

        assertNull(daoDrug.getDrug("Dummy:BLABLA"));

        assertEquals(2, daoDrug.getAllDrugs().size());

		// restore bulk setting
		if (isBulkLoad) {
			MySQLbulkLoader.bulkLoadOn();
		}
    }
}
