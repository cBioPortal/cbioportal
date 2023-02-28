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

package org.mskcc.cbio.portal.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.GeneticAlterationType;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * JUnit test for GeneticProfileReader class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestGeneticProfileReader {

    @Test
    public void testGeneticProfileReader() throws Exception {
        // load cancers
        // TBD: change this to use getResourceAsStream()
        // TBD: change this to use getResourceAsStream()

        File file = new File("target/test-classes/genetic_profile_test.txt");
        GeneticProfile geneticProfile = GeneticProfileReader.loadGeneticProfile(file);
        assertEquals("Barry", geneticProfile.getTargetLine());
        assertEquals("Blah Blah.", geneticProfile.getProfileDescription());

        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub");
        ArrayList<GeneticProfile> list = DaoGeneticProfile.getAllGeneticProfiles
                (cancerStudy.getInternalId());
        geneticProfile = list.get(0);

        assertEquals(cancerStudy.getInternalId(), geneticProfile.getCancerStudyId());
        assertEquals("Putative copy-number alterations from GISTIC", geneticProfile.getProfileName());
        assertEquals(GeneticAlterationType.COPY_NUMBER_ALTERATION,
                geneticProfile.getGeneticAlterationType());
    }

    @Test(expected = RuntimeException.class)
    public void testTreatmentResponseMissingPivotField() throws IOException, DaoException {
        File file = new File("target/test-classes/test_meta_treatment_missing_pivot.txt");
        GeneticProfileReader.loadGeneticProfile(file);
    }

    @Test(expected = RuntimeException.class)
    public void testTreatmentResponseMissingSortOrderField() throws IOException, DaoException {
        File file = new File("target/test-classes/test_meta_treatment_missing_sortorder.txt");
        GeneticProfileReader.loadGeneticProfile(file);
    }

}
