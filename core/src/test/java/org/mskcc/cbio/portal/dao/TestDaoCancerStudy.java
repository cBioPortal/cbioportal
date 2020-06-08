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
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.model.CancerStudy;

import java.io.IOException;
import java.util.ArrayList;

import org.mskcc.cbio.portal.model.ReferenceGenome;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

/**
 * JUnit Tests for DaoCancer Study.
 *
 * @author Arman Aksoy, Ethan Cerami, Arthur Goldberg, Ersin Ciftci.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestDaoCancerStudy {

    /**
     * Tests DaoCancer Study #1.
     * @throws DaoException Database Error.
     * @throws IOException IO Error.
     */
	@Test
    public void testDaoCancerStudy() throws DaoException, IOException {

		assertEquals("breast,breast invasive", DaoTypeOfCancer.getTypeOfCancerById("BRCA").getClinicalTrialKeywords());

        CancerStudy cancerStudy = new CancerStudy("GBM", "GBM Description", "gbm", "brca", false);
        cancerStudy.setReferenceGenome("hg19");
        DaoCancerStudy.addCancerStudy(cancerStudy);

        // Removed testing that depends on internal ids
        //   `CANCER_STUDY_ID` auto_increment counts from 1
        // assertEquals(1, cancerStudy.getInternalId());
        
        cancerStudy.setDescription("Glioblastoma");
        DaoCancerStudy.addCancerStudy(cancerStudy,true);
        // Removed testing that depends on internal ids
        // assertEquals(2, cancerStudy.getInternalId());
        
        cancerStudy = DaoCancerStudy.getCancerStudyByStableId("gbm");
        assertEquals("gbm", cancerStudy.getCancerStudyStableId());
        assertEquals("GBM", cancerStudy.getName());
        assertEquals("Glioblastoma", cancerStudy.getDescription());

        CancerStudy cancerStudy2 = new CancerStudy("Breast", "Breast Description", "breast", "brca", false);
        cancerStudy2.setReferenceGenome("hg19");
        DaoCancerStudy.addCancerStudy(cancerStudy2);
        
        // Removed testing that depends on internal ids
        // assertEquals(3, cancerStudy.getInternalId());
        int testInternalId = cancerStudy2.getInternalId();

        cancerStudy2 = DaoCancerStudy.getCancerStudyByInternalId(testInternalId);
        assertEquals("Breast Description", cancerStudy2.getDescription());
        assertEquals("Breast", cancerStudy2.getName());

        ArrayList<CancerStudy> list = DaoCancerStudy.getAllCancerStudies();
        assertEquals(3, list.size());

        assertEquals(null, DaoCancerStudy.getCancerStudyByStableId("no such study"));
        assertTrue(DaoCancerStudy.doesCancerStudyExistByStableId
                (cancerStudy.getCancerStudyStableId()));
        assertFalse(DaoCancerStudy.doesCancerStudyExistByStableId("no such study"));

        assertTrue(DaoCancerStudy.doesCancerStudyExistByInternalId(cancerStudy.getInternalId()));
        assertFalse(DaoCancerStudy.doesCancerStudyExistByInternalId(-1));

        DaoCancerStudy.deleteCancerStudy(cancerStudy2.getInternalId());

        list = DaoCancerStudy.getAllCancerStudies();
        assertEquals(2, list.size());

        cancerStudy = DaoCancerStudy.getCancerStudyByStableId("gbm");
        DaoCancerStudy.deleteCancerStudy(cancerStudy.getInternalId());

        list = DaoCancerStudy.getAllCancerStudies();
        assertEquals(1, list.size());
    }

    /**
     * Tests DaoCancer Study #2.
     * @throws DaoException Database Error.
     * @throws IOException IO Error.
     */
	@Test
    public void testDaoCancerStudy2() throws DaoException, IOException {

        CancerStudy cancerStudy1 = new CancerStudy("GBM public study x", "GBM Description",
                "tcga_gbm1", "brca", true);
        cancerStudy1.setReferenceGenome(ReferenceGenome.HOMO_SAPIENS_DEFAULT_GENOME_NAME);
        DaoCancerStudy.addCancerStudy(cancerStudy1);

        CancerStudy cancerStudy2 = new CancerStudy("GBM private study x", "GBM Description 2",
                "tcga_gbm2", "brca", false);
        cancerStudy2.setReferenceGenome(ReferenceGenome.HOMO_SAPIENS_DEFAULT_GENOME_NAME);
        DaoCancerStudy.addCancerStudy(cancerStudy2);

        CancerStudy cancerStudy3 = new CancerStudy("Breast", "Breast Description",
                "tcga_gbm3", "brca", false);
        cancerStudy3.setReferenceGenome(ReferenceGenome.HOMO_SAPIENS_DEFAULT_GENOME_NAME);
        DaoCancerStudy.addCancerStudy(cancerStudy3);

        ArrayList<CancerStudy> list = DaoCancerStudy.getAllCancerStudies();
        assertEquals(4, list.size());
        
        int cancerStudy1Id = cancerStudy1.getInternalId();
        int cancerStudy2Id = cancerStudy2.getInternalId();
        int cancerStudy3Id = cancerStudy3.getInternalId();

        CancerStudy readCancerStudy1 = DaoCancerStudy.getCancerStudyByInternalId(cancerStudy1Id);
        // Removed testing that depends on internal ids
        // assertEquals(1, cancerStudy1.getInternalId());
        assertEquals("GBM Description", readCancerStudy1.getDescription());
        assertEquals("GBM public study x", readCancerStudy1.getName());
        assertEquals(true, readCancerStudy1.isPublicStudy());

        CancerStudy readCancerStudy2 = DaoCancerStudy.getCancerStudyByInternalId(cancerStudy2Id);
        // Removed testing that depends on internal ids
        // assertEquals(2, cancerStudy1.getInternalId());
        assertEquals("GBM private study x", readCancerStudy2.getName());
        assertEquals("GBM Description 2", readCancerStudy2.getDescription());
        assertEquals(false, readCancerStudy2.isPublicStudy());

        CancerStudy readCancerStudy3 = DaoCancerStudy.getCancerStudyByInternalId(cancerStudy3Id);
        // Removed testing that depends on internal ids
        // assertEquals(3, cancerStudy1.getInternalId());
        assertEquals("Breast", readCancerStudy3.getName());
        assertEquals("Breast Description", readCancerStudy3.getDescription());
        assertEquals(false, readCancerStudy3.isPublicStudy());

        assertEquals(4, DaoCancerStudy.getCount());
        DaoCancerStudy.deleteCancerStudy(readCancerStudy1.getInternalId());
        assertEquals(3, DaoCancerStudy.getCount());
        DaoCancerStudy.deleteCancerStudy(readCancerStudy2.getInternalId());
        assertEquals(2, DaoCancerStudy.getCount());
        DaoCancerStudy.deleteCancerStudy(readCancerStudy3.getInternalId());
        assertEquals(1, DaoCancerStudy.getCount());
	}
}
