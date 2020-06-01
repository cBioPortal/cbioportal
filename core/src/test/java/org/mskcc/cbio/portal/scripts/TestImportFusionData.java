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
 * Test class to test functionality of ImportFusionData
 *
 * @author Selcuk Onur Sumer
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestImportFusionData
{
	
	int studyId;
	int geneticProfileId;
	
	@Before
	public void setUp() throws DaoException
	{
		studyId = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub").getInternalId();
		geneticProfileId = DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_mutations").getGeneticProfileId();

	}
	
	@Test
	public void testImportFusionData()
	{
        try {

            MySQLbulkLoader.bulkLoadOn();

            ProgressMonitor.setConsoleMode(false);

            // TODO change this to use getResourceAsStream()
            File file = new File("target/test-classes/data_fusions.txt");
            ImportFusionData parser = new ImportFusionData(file, geneticProfileId, null);

			loadGenes();
			parser.importData();
			MySQLbulkLoader.flushAll();

			checkImportedData();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (DaoException e)
		{
			e.printStackTrace();
		}
	}       

	private void checkImportedData() throws DaoException
	{
		ArrayList<ExtendedMutation> list = DaoMutation.getAllMutations();
		
		ArrayList<ExtendedMutation> fusions = new ArrayList<ExtendedMutation>();
		for(ExtendedMutation mut : list) {
			if (mut.getEvent().getMutationType().equals("Fusion")) {
				fusions.add(mut);
			}
		}

		assertEquals(2, fusions.size()); // all except "FAKE"

		list = DaoMutation.getMutations(geneticProfileId, DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-A1-A0SB-01").getInternalId());

		assertEquals(1, list.size());
		assertEquals("saturn", list.get(0).getSequencingCenter());
		assertEquals("FGFR3", list.get(0).getGeneSymbol());
		assertEquals("Fusion", list.get(0).getMutationType());
		assertEquals("Fusion1", list.get(0).getProteinChange());

		list = DaoMutation.getMutations(geneticProfileId, DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-A1-A0SD-01").getInternalId());

		assertEquals(1, list.size());
		assertEquals("jupiter", list.get(0).getSequencingCenter());
		assertEquals("ERBB2", list.get(0).getGeneSymbol());
		assertEquals("Fusion", list.get(0).getMutationType());
		assertEquals("Fusion2", list.get(0).getProteinChange());
	}

	private void loadGenes() throws DaoException
	{
		DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

		// genes for "data_fusions.txt"
		daoGene.addGene(new CanonicalGene(2261L, "FGFR3"));
		daoGene.addGene(new CanonicalGene(2064L, "ERBB2"));

		MySQLbulkLoader.flushAll();
	}
}
