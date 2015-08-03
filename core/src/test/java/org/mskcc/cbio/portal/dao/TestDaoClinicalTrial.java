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
import org.mskcc.cbio.portal.model.ClinicalTrial;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

import java.util.HashSet;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class TestDaoClinicalTrial {
	
	@Test
    public void testDaoClinicalTrial() throws DaoException {

        DaoClinicalTrial instance = DaoClinicalTrial.getInstance();
        ClinicalTrial clinicalTrial = new ClinicalTrial();
        clinicalTrial.setId("AID");
        clinicalTrial.setStatus("Active");
        clinicalTrial.setPhase("Phase III");
        clinicalTrial.setLocation("Location X");
        clinicalTrial.setTitle("A clinical trial");
        clinicalTrial.setSecondaryId("AID2");
        HashSet<String> keywords1 = new HashSet<String>();
        keywords1.add("k1");
        keywords1.add("k12");
        instance.addClinicalTrial(clinicalTrial, keywords1);

        ClinicalTrial clinicalTrial2 = new ClinicalTrial();
        clinicalTrial2.setId("BID");
        clinicalTrial2.setStatus("Closed");
        clinicalTrial2.setPhase("Phase II");
        clinicalTrial2.setLocation("Location Y");
        clinicalTrial2.setTitle("Another clinical trial");
        clinicalTrial2.setSecondaryId("BID2");
        HashSet<String> keywords2 = new HashSet<String>();
        keywords2.add("k2");
        keywords2.add("k12");
        instance.addClinicalTrial(clinicalTrial2, keywords2);

        assertEquals(2, instance.countClinicalStudies());
        assertEquals(1, instance.searchClinicalTrials("k1").size());
        assertEquals(1, instance.searchClinicalTrials("k2").size());
        assertEquals(2, instance.searchClinicalTrials("k12").size());
        assertEquals(2, instance.fuzzySearchClinicalTrials("k1").size());
        ClinicalTrial aid = instance.getClinicalTrialById("AID");
        assertNotNull(aid);
        assertEquals("AID2", aid.getSecondaryId());
        ClinicalTrial bid = instance.getClinicalTrialById("BID");
        assertNotNull(bid);
        assertEquals("BID2", bid.getSecondaryId());
        assertNull(instance.getClinicalTrialById("SID"));
        assertTrue(instance.fuzzySearchClinicalTrials("k123").isEmpty());
    }
}
