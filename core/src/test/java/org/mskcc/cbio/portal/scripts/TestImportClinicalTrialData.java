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

package org.mskcc.cbio.portal.scripts;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.DaoClinicalTrial;
import org.mskcc.cbio.portal.model.ClinicalTrial;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;

/**
 * Tests @link org.mskcc.cbio.portal.scripts.ImportClinicalTrialData.
 *
 * @author Arman
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class TestImportClinicalTrialData {
	
	@Test
    public void testClinicalTrialDataImport() throws Exception {
        URL trialsURL = TestImportClinicalTrialData.class.getClassLoader().getResource("clinicalTrials");
        assertNotNull(trialsURL);

        File clinicalTrials
                = new File(trialsURL.getFile());
        ImportClinicalTrialData.importFilesFromFolder(clinicalTrials);

        DaoClinicalTrial daoClinicalTrial = DaoClinicalTrial.getInstance();
        assertEquals(2, daoClinicalTrial.countClinicalStudies());


        // Search by drug names
        assertEquals(1, daoClinicalTrial.searchClinicalTrials("drug11").size());
        assertEquals(1, daoClinicalTrial.searchClinicalTrials("drug12").size());
        assertEquals(1, daoClinicalTrial.searchClinicalTrials("drug13").size());
        assertEquals(1, daoClinicalTrial.searchClinicalTrials("drug14").size());

        assertEquals(1, daoClinicalTrial.searchClinicalTrials("drug21").size());
        assertEquals(1, daoClinicalTrial.searchClinicalTrials("drug22").size());
        assertEquals(1, daoClinicalTrial.searchClinicalTrials("drug23").size());

        // Search by terms
        // trial 1
        assertEquals(1, daoClinicalTrial.searchClinicalTrials("primary systemic amyloidosis").size());
        assertEquals(1, daoClinicalTrial.searchClinicalTrials("adult Burkitt lymphoma").size());
        assertEquals(1, daoClinicalTrial.fuzzySearchClinicalTrials("amyloidosis").size());
        // trial 2
        assertEquals(1,
                daoClinicalTrial.searchClinicalTrials("multiple myeloma and other plasma cell neoplasms").size());
        assertEquals(2, daoClinicalTrial.searchClinicalTrials("malignant neoplasm").size());
        assertEquals(1, daoClinicalTrial.fuzzySearchClinicalTrials("myeloma").size());

        // Get By id and check fields
        ClinicalTrial trial1 = daoClinicalTrial.getClinicalTrialById("NCT00000001");
        assertNotNull(trial1);
        assertEquals("Phase III", trial1.getPhase());
        assertEquals("Completed", trial1.getStatus());
        assertEquals("Lorem ipsum 1 brief", trial1.getTitle());
        assertEquals("1", trial1.getSecondaryId());

        ClinicalTrial trial2 = daoClinicalTrial.getClinicalTrialById("NCT00000002");
        assertNotNull(trial2);
        assertEquals("Phase II", trial2.getPhase());
        assertEquals("Active", trial2.getStatus());
        assertTrue(trial2.isActive());
        assertEquals("Lorem ipsum 2 brief", trial2.getTitle());
        assertEquals("Lorem City, L.I.S.", trial2.getLocation());
        assertEquals("2", trial2.getSecondaryId());
    }
}