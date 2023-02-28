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

package org.mskcc.cbio.portal.web_api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * JUnit test for GetProfileData class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestGetProfileData {
	
	int geneticProfileId;
	
	@Before
	public void setUp() throws DaoException {
		int studyId = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub").getInternalId();
		
		DaoPatient.reCache();
		DaoSample.reCache();
		DaoGeneticProfile.reCache();
		
		GeneticProfile newGeneticProfile = new GeneticProfile();
		newGeneticProfile.setCancerStudyId(studyId);
		newGeneticProfile.setGeneticAlterationType(GeneticAlterationType.COPY_NUMBER_ALTERATION);
		newGeneticProfile.setStableId("study_tcga_pub_test");
		newGeneticProfile.setProfileName("Barry CNA Results");
		newGeneticProfile.setDatatype("test");
		DaoGeneticProfile.addGeneticProfile(newGeneticProfile);
		
		geneticProfileId =  DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_test").getGeneticProfileId();
	}

    @Test
    public void testGetProfileData() throws DaoException, IOException {
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        daoGene.addGene(new CanonicalGene(207, "AKT1"));
        daoGene.addGene(new CanonicalGene(208, "AKT2"));
        daoGene.addGene(new CanonicalGene(10000, "AKT3"));
        daoGene.addGene(new CanonicalGene(369, "ARAF"));
        daoGene.addGene(new CanonicalGene(472, "ATM"));
        daoGene.addGene(new CanonicalGene(673, "BRAF"));
        daoGene.addGene(new CanonicalGene(672, "BRCA1"));
        daoGene.addGene(new CanonicalGene(675, "BRCA2"));

        ArrayList <String> targetGeneList = new ArrayList<String> ();
        targetGeneList.add("AKT1");
        targetGeneList.add("AKT2");
        targetGeneList.add("AKT3");
        targetGeneList.add("ATM");
        targetGeneList.add("BRCA1");

        ArrayList <String> geneticProfileIdList = new ArrayList<String>();
        geneticProfileIdList.add("study_tcga_pub_gistic");

        ArrayList <String> sampleIdList = new ArrayList <String>();
        sampleIdList.add("TCGA-A1-A0SB-01");
        sampleIdList.add("TCGA-A1-A0SD-01");
        sampleIdList.add("TCGA-A1-A0SE-01");

        GetProfileData getProfileData = new GetProfileData(geneticProfileIdList, targetGeneList,
                sampleIdList, Boolean.FALSE);
        String out = getProfileData.getRawContent();
        String lines[] = out.split("\n");
        assertEquals("# DATA_TYPE\t Putative copy-number alterations from GISTIC" , lines[0]);
        assertEquals("# COLOR_GRADIENT_SETTINGS\t COPY_NUMBER_ALTERATION", lines[1]);
        assertTrue(lines[2].startsWith("GENE_ID\tCOMMON\tTCGA-A1-A0SB-01\t" +
                "TCGA-A1-A0SD-01\tTCGA-A1-A0SE-01"));
        assertTrue(lines[3].startsWith("207\tAKT1\t0\t0\t0"));
        assertTrue(lines[4].startsWith("208\tAKT2\t0\t0\t0"));
    }
}
