/*
 * Copyright (c) 2015 - 2016 Memorial Sloan-Kettering Cancer Center.
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

import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.model.*;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.Assert.*;

/**
 * JUnit tests for DaoGeneticProfile class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestDaoGeneticProfile {
	
	int studyId;
	
	@Before 
	public void setUp() throws DaoException
	{
		studyId = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub").getInternalId();
		DaoGeneticProfile.reCache();
	}

	@Test
	public void testDaoGetAllGeneticProfiles() throws DaoException {

		ArrayList<GeneticProfile> list = DaoGeneticProfile.getAllGeneticProfiles(studyId);
		assertEquals(6, list.size());
	}
		
	@Test
	public void testDaoCheckGeneticProfiles() throws DaoException {

		ArrayList<GeneticProfile> list = DaoGeneticProfile.getAllGeneticProfiles(studyId);
		GeneticProfile geneticProfile = list.get(0);
		assertEquals(studyId, geneticProfile.getCancerStudyId());
		assertEquals("Putative copy-number alterations from GISTIC", geneticProfile.getProfileName());
		assertEquals(GeneticAlterationType.COPY_NUMBER_ALTERATION, geneticProfile.getGeneticAlterationType());
		assertEquals("Putative copy-number from GISTIC 2.0. Values: -2 = homozygous deletion; -1 = hemizygous deletion; 0 = neutral / no change; 1 = gain; 2 = high level amplification.", 
				geneticProfile.getProfileDescription());

		geneticProfile = list.get(1);
		assertEquals(studyId, geneticProfile.getCancerStudyId());
		assertEquals("mRNA expression (microarray)", geneticProfile.getProfileName());
		assertEquals(GeneticAlterationType.MRNA_EXPRESSION, geneticProfile.getGeneticAlterationType());
		assertEquals(false, geneticProfile.showProfileInAnalysisTab());
	}
	
	@Test
	public void testDaoCreateGeneticProfile() throws DaoException {

		GeneticProfile geneticProfile = new GeneticProfile();
		geneticProfile.setCancerStudyId(studyId);
		geneticProfile.setProfileName("test profile");
		geneticProfile.setStableId("test");
		geneticProfile.setGeneticAlterationType(GeneticAlterationType.FUSION);
		geneticProfile.setDatatype("test");
		DaoGeneticProfile.addGeneticProfile(geneticProfile);
		
		GeneticProfile readGeneticProfile = DaoGeneticProfile.getGeneticProfileByStableId("test");
		assertEquals(studyId, readGeneticProfile.getCancerStudyId());
		assertEquals("test", readGeneticProfile.getStableId());
		assertEquals("test profile", readGeneticProfile.getProfileName());
		assertEquals(GeneticAlterationType.FUSION, readGeneticProfile.getGeneticAlterationType());
	}

	@Test
	public void testDaoGetGeneticProfileByStableId() throws DaoException {

		GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_gistic");
		assertEquals(studyId, geneticProfile.getCancerStudyId());
		assertEquals("Putative copy-number alterations from GISTIC", geneticProfile.getProfileName());
		assertEquals(GeneticAlterationType.COPY_NUMBER_ALTERATION, geneticProfile.getGeneticAlterationType());
	}
	
	@Test
	public void testDaoGetGeneticProfileByInternalId() throws DaoException {

		GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileById(2);
		assertEquals(studyId, geneticProfile.getCancerStudyId());
		assertEquals("Putative copy-number alterations from GISTIC", geneticProfile.getProfileName());
		assertEquals(GeneticAlterationType.COPY_NUMBER_ALTERATION, geneticProfile.getGeneticAlterationType());
	}
	
	@Test
	public void testDaoDeleteGeneticProfile() throws DaoException {

		GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileById(2);

		assertEquals(6, DaoGeneticProfile.getCount());
		DaoGeneticProfile.deleteGeneticProfile(geneticProfile);
		assertEquals(5, DaoGeneticProfile.getCount());
		
		ArrayList<GeneticProfile> list = DaoGeneticProfile.getAllGeneticProfiles(studyId);
		assertEquals(5, list.size());
		geneticProfile = list.get(0);
		assertEquals(studyId, geneticProfile.getCancerStudyId());
		assertEquals("mRNA expression (microarray)", geneticProfile.getProfileName());
		assertEquals(GeneticAlterationType.MRNA_EXPRESSION, geneticProfile.getGeneticAlterationType());
	}

	@Test
	public void testDaoUpdateGeneticProfile() throws DaoException {

		GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_gistic");

		assertTrue(DaoGeneticProfile.updateNameAndDescription(
				geneticProfile.getGeneticProfileId(), "Updated Name",
				"Updated Description"));
		ArrayList<GeneticProfile> list = DaoGeneticProfile.getAllGeneticProfiles(studyId);
		assertEquals(6, list.size());
		geneticProfile = list.get(0);
		assertEquals(studyId, geneticProfile.getCancerStudyId());
		assertEquals("Updated Name", geneticProfile.getProfileName());
		assertEquals(GeneticAlterationType.COPY_NUMBER_ALTERATION, geneticProfile.getGeneticAlterationType());
		assertEquals("Updated Description", geneticProfile.getProfileDescription());
	}
}
