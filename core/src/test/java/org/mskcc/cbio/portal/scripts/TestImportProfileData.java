/*
 * Copyright (c) 2016 The Hyve B.V.
 * This code is licensed under the GNU Affero General Public License (AGPL),
 * version 3, or (at your option) any later version.
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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoMutation;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.dao.MySQLbulkLoader;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.ExtendedMutation;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.util.SpringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Pieter Lukasse pieter@thehyve.nl
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class TestImportProfileData {

	int studyId;
	int geneticProfileId;
	@Autowired
	ApplicationContext applicationContext;
	
	@Before
	public void setUp() throws DaoException {

		//set it, to avoid this being set to the runtime application context (instead of the test application context):
		SpringUtil.setApplicationContext(applicationContext);
		ProgressMonitor.setConsoleMode(false);

        loadGenes();
	}
	
	@Rule
	public ExpectedException exception = ExpectedException.none();

	
	@Test
	public void testImportFile() throws Exception {
        String[] args = {
        		"--data","target/test-classes/data_mutations_extended.txt",
        		"--meta","target/test-classes/meta_mutations_extended.txt"        		
        		};
        
        ImportProfileData.main(args);
        //This test is to check if the ImportProfileData class indeed adds the study stable Id in front of the 
        //dataset study id (e.g. studyStableId + "_breast_mutations"):
        String studyStableId = "study_tcga_pub";
		studyId = DaoCancerStudy.getCancerStudyByStableId(studyStableId).getInternalId();
		int sampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-AA-3664-01").getInternalId();

        geneticProfileId = DaoGeneticProfile.getGeneticProfileByStableId(studyStableId + "_breast_mutations").getGeneticProfileId();
        validateMutationAminoAcid (geneticProfileId, sampleId, 54407, "T433A");  
        
	}
	
	
	@Test
	public void testException() throws Exception {

        String[] args = {
        		"--data","target/test-classes/data_mutations_extended.txt",
        		"--meta","target/test-classes/meta_mutations_extended_wrong_stable_id.txt"        		
        		};
        
        exception.expect(IllegalArgumentException.class);
        ImportProfileData.main(args);
        
        
	}
	
	
    private void validateMutationAminoAcid (int geneticProfileId, Integer sampleId, long entrezGeneId,
            String expectedAminoAcidChange) throws DaoException {
        ArrayList<ExtendedMutation> mutationList = DaoMutation.getMutations
                (geneticProfileId, sampleId, entrezGeneId);
        assertEquals(1, mutationList.size());
        assertEquals(expectedAminoAcidChange, mutationList.get(0).getProteinChange());
    }



    private void loadGenes() throws DaoException {
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

	    // genes for "data_mutations_extended.txt"
        daoGene.addGene(new CanonicalGene(114548L, "NLRP3"));
        daoGene.addGene(new CanonicalGene(3339L, "HSPG2"));
        daoGene.addGene(new CanonicalGene(282770L, "OR10AG1"));
        daoGene.addGene(new CanonicalGene(51806L, "CALML5"));
        daoGene.addGene(new CanonicalGene(343035L, "RD3"));
        daoGene.addGene(new CanonicalGene(80114L, "BICC1"));
        daoGene.addGene(new CanonicalGene(219736L, "STOX1"));
        daoGene.addGene(new CanonicalGene(6609L, "SMPD1"));
        daoGene.addGene(new CanonicalGene(51259L, "TMEM216"));
        daoGene.addGene(new CanonicalGene(89L, "ACTN3"));
        daoGene.addGene(new CanonicalGene(64581L, "CLEC7A"));
        daoGene.addGene(new CanonicalGene(50839L, "TAS2R10"));
        daoGene.addGene(new CanonicalGene(54407L, "SLC38A2"));
        daoGene.addGene(new CanonicalGene(6667L, "SP1"));
        daoGene.addGene(new CanonicalGene(2842L, "GPR19"));

	    // additional genes for "data_mutations_oncotated.txt"
	    daoGene.addGene(new CanonicalGene(55138L, "FAM90A1"));
	    daoGene.addGene(new CanonicalGene(10628L, "TXNIP"));
	    daoGene.addGene(new CanonicalGene(80343, "SEL1L2"));
	    daoGene.addGene(new CanonicalGene(29102L, "DROSHA"));
	    daoGene.addGene(new CanonicalGene(7204L, "TRIO"));
	    daoGene.addGene(new CanonicalGene(57111L, "RAB25"));
	    daoGene.addGene(new CanonicalGene(773L, "CACNA1A"));
	    daoGene.addGene(new CanonicalGene(100132025L, "LOC100132025"));
	    daoGene.addGene(new CanonicalGene(1769L, "DNAH8"));
	    daoGene.addGene(new CanonicalGene(343171L, "OR2W3"));
	    daoGene.addGene(new CanonicalGene(2901L, "GRIK5"));
	    daoGene.addGene(new CanonicalGene(10568L, "SLC34A2"));
	    daoGene.addGene(new CanonicalGene(140738L, "TMEM37"));
	    daoGene.addGene(new CanonicalGene(94025L, "MUC16"));
	    daoGene.addGene(new CanonicalGene(1915L, "EEF1A1"));
	    daoGene.addGene(new CanonicalGene(65083L, "NOL6"));
	    daoGene.addGene(new CanonicalGene(7094L, "TLN1"));
	    daoGene.addGene(new CanonicalGene(51196L, "PLCE1"));
	    daoGene.addGene(new CanonicalGene(1952L, "CELSR2"));
	    daoGene.addGene(new CanonicalGene(2322L, "FLT3"));
	    daoGene.addGene(new CanonicalGene(867L, "CBL"));
            
            MySQLbulkLoader.flushAll();
    }
}
