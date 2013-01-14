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

package org.mskcc.cbio.cgds.scripts;

import junit.framework.TestCase;
import org.mskcc.cbio.cgds.dao.DaoClinicalTrial;
import org.mskcc.cbio.cgds.model.ClinicalTrial;

import java.io.File;
import java.net.URL;

/**
 * Tests @link org.mskcc.cbio.cgds.scripts.ImportClinicalTrialData.
 *
 * @author Arman
 *
 */
public class TestImportClinicalTrialData extends TestCase {
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

        ClinicalTrial trial2 = daoClinicalTrial.getClinicalTrialById("NCT00000002");
        assertNotNull(trial2);
        assertEquals("Phase II", trial2.getPhase());
        assertEquals("Active", trial2.getStatus());
        assertTrue(trial2.isActive());
        assertEquals("Lorem ipsum 2 brief", trial2.getTitle());
        assertEquals("Lorem City, L.I.S.", trial2.getLocation());
    }
}
