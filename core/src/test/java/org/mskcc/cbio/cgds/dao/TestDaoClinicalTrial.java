/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
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
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package org.mskcc.cbio.cgds.dao;

import junit.framework.TestCase;
import org.mskcc.cbio.cgds.model.ClinicalTrial;
import org.mskcc.cbio.cgds.scripts.ResetDatabase;

import java.util.HashSet;

public class TestDaoClinicalTrial extends TestCase {
    public void testDaoClinicalTrial() throws DaoException {
        ResetDatabase.resetDatabase();

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
