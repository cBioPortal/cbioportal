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

import org.mskcc.cbio.portal.model.*;

import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import java.util.*;

/**
 * JUnit test for DaoSample class
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestDaoSampleProfile {

	CancerStudy study;
    GenePanel genePanel;
	ArrayList<Integer> internalSampleIds;
	int geneticProfileId;

	@Before
	public void setUp() throws DaoException {
        CanonicalGene brca1 = DaoGeneOptimized.getInstance().getGene("BRCA1");
        CanonicalGene brca2 = DaoGeneOptimized.getInstance().getGene("BRCA2");
        CanonicalGene kras = DaoGeneOptimized.getInstance().getGene("KRAS");
        HashSet<CanonicalGene> canonicalGenes = new HashSet<CanonicalGene>();
        canonicalGenes.add(brca1);
        canonicalGenes.add(brca2);
        canonicalGenes.add(kras);
        DaoGenePanel.addGenePanel("testGenePanel", "Test gene panel description", canonicalGenes);
        genePanel = DaoGenePanel.getGenePanelByStableId("testGenePanel");

		study = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub");
		geneticProfileId = DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_mutations").getGeneticProfileId();

		internalSampleIds = new ArrayList<Integer>();
        Patient p = new Patient(study, "TCGA-12345");
        int pId = DaoPatient.addPatient(p);

        DaoSample.reCache();
        Sample s = new Sample("TCGA-12345-01", pId, "brca");
        internalSampleIds.add(DaoSample.addSample(s));
        s = new Sample("TCGA-123456-01", pId, "brca");
        internalSampleIds.add(DaoSample.addSample(s));
	}

	@Test
    public void testDaoSampleProfile() throws DaoException {

        Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(study.getInternalId(), "TCGA-12345");
        Sample sample = DaoSample.getSampleByPatientAndSampleId(patient.getInternalId(), "TCGA-12345-01");

        int num = DaoSampleProfile.addSampleProfile(sample.getInternalId(), geneticProfileId, null);
        assertEquals(1, num);

        boolean exists = DaoSampleProfile.sampleExistsInGeneticProfile(sample.getInternalId(), geneticProfileId);
        assertTrue(exists);

        assertEquals(geneticProfileId, DaoSampleProfile.getProfileIdForSample(sample.getInternalId()));

        sample = DaoSample.getSampleByPatientAndSampleId(patient.getInternalId(), "TCGA-123456-01");
        num = DaoSampleProfile.addSampleProfile(sample.getInternalId(), geneticProfileId, genePanel.getInternalId());
        assertEquals(1, num);

        boolean existsByPanelId = DaoSampleProfile.sampleProfileMappingExistsByPanel(genePanel.getInternalId());
        assertTrue(existsByPanelId);

        ArrayList<Integer> sampleIds = DaoSampleProfile.getAllSampleIdsInProfile(geneticProfileId);
        assertEquals(9, sampleIds.size());
    }

}
