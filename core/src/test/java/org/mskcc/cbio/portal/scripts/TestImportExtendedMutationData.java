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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class TestImportExtendedMutationData {

	int studyId;
	int geneticProfileId;
	@Before
	public void setUp() throws DaoException {
		studyId = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub").getInternalId();
		
		DaoGeneticProfile.reCache();
		DaoSample.reCache();
		DaoPatient.reCache();
		GeneticProfile geneticProfile = new GeneticProfile();
		geneticProfile.setCancerStudyId(studyId);
		geneticProfile.setProfileName("test profile");
		geneticProfile.setStableId("test");
		geneticProfile.setGeneticAlterationType(GeneticAlterationType.MUTATION_EXTENDED);
		geneticProfile.setDatatype("test");
		geneticProfile.setReferenceGenomeId(1);
		DaoGeneticProfile.addGeneticProfile(geneticProfile);
		geneticProfileId = DaoGeneticProfile.getGeneticProfileByStableId("test").getGeneticProfileId();

        ProgressMonitor.setConsoleMode(false);

        loadGenes();
    }

	@Test
    public void testImportExtendedMutationDataExtended() throws IOException, DaoException {
		
        MySQLbulkLoader.bulkLoadOn();
        
		// TBD: change this to use getResourceAsStream()
        File file = new File("src/test/resources/data_mutations_extended.txt");
        ImportExtendedMutationData parser = new ImportExtendedMutationData(file, geneticProfileId, null);
        parser.importData();
        MySQLbulkLoader.flushAll();
        ConsoleUtil.showMessages();
        
        int sampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-AA-3664-01").getInternalId();
        
        checkBasicFilteringRules();
        
        // accept everything else
        validateMutationAminoAcid(geneticProfileId, sampleId, 51806, "P113L");   // valid Unknown
        validateMutationAminoAcid(geneticProfileId, sampleId, 89, "S116R"); // Unknown  Somatic
    }

    /**
     * Tests import of data files with names in the SWISSPROT column.
     *
     * @throws IOException  if something goes wrong reading from the data file
     * @throws DaoException  if something goes wrong talking to the database
     */
    @Test
    public void testImportExtendedMutationDataSwissprotName() throws IOException, DaoException {
        loadStudyContext1();
        MySQLbulkLoader.bulkLoadOn();

        File file = new File("src/test/resources/data_mutations_swissprotname.maf");
        ImportExtendedMutationData parser = new ImportExtendedMutationData(file, geneticProfileId, null);
        parser.importData();
        MySQLbulkLoader.flushAll();

        checkSwissprotLoaded();
        // unknown accessions are only loaded if the column lists accessions
        int sampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-A2-A0CR-01").getInternalId();
        ExtendedMutation m = DaoMutation.getMutations(
                geneticProfileId, sampleId, 64581).get(0);
        assertNull(m.getOncotatorUniprotAccession());
    }

    /**
     * Tests import of data files with accessions in the SWISSPROT column.
     *
     * @throws IOException  if something goes wrong reading from the data file
     * @throws DaoException  if something goes wrong talking to the database
     */
    @Test
    public void testImportExtendedMutationDataSwissprotAccession() throws IOException, DaoException {
        loadStudyContext1();
        MySQLbulkLoader.bulkLoadOn();

        File file = new File("src/test/resources/data_mutations_swissprotaccession.maf");
        ImportExtendedMutationData parser = new ImportExtendedMutationData(file, geneticProfileId, null);
        parser.setSwissprotIsAccession(true);
        parser.importData();
        MySQLbulkLoader.flushAll();

        checkSwissprotLoaded();
        // unknown accessions are only loaded if the column lists accessions
        int sampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-A2-A0CR-01").getInternalId();
        ExtendedMutation m = DaoMutation.getMutations(
                geneticProfileId, sampleId, 64581).get(0);
        assertEquals("Z9ZZZ9ZZZ9", m.getOncotatorUniprotAccession());
    }

    /**
     * Performs common assertions for the tests about the SWISSPROT column.
     *
     * @throws DaoException  on errors reading from the database
     */
    private void checkSwissprotLoaded() throws DaoException {

        int sampleId;
        ExtendedMutation m;

        sampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-A2-A0CR-01").getInternalId();
        validateMutationAminoAcid(geneticProfileId, sampleId, 64581, "K145Q");

        sampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-A2-A0T5-01").getInternalId();
        validateMutationAminoAcid(geneticProfileId, sampleId, 3339, "X4137?");
        m = DaoMutation.getMutations(
                geneticProfileId, sampleId, 3339).get(0);
        assertEquals("P98160", m.getOncotatorUniprotAccession());
        validateMutationAminoAcid(geneticProfileId, sampleId, 54407, "T32P");
        m = DaoMutation.getMutations(
                geneticProfileId, sampleId, 54407).get(0);
        assertEquals("Q96QD8", m.getOncotatorUniprotAccession());

    }

	/**
	 * Check that import of oncotated data works
	 * @throws IOException
	 * @throws DaoException
	 */
	@Test
    public void testImportExtendedMutationDataOncotated() throws IOException, DaoException {
        File file = new File("src/test/resources/data_mutations_oncotated.txt");
        ImportExtendedMutationData parser = new ImportExtendedMutationData(file, geneticProfileId, null);
        parser.importData();
        MySQLbulkLoader.flushAll();
        
		ArrayList<ExtendedMutation> mutationList = DaoMutation.getAllMutations(geneticProfileId);

		// assert table size; 3 silent mutations should be rejected
		assertEquals(17, mutationList.size());

		// assert data for oncotator columns
		//assertEquals("FAM90A1", mutationList.get(0).getGeneSymbol());
		//assertEquals("Missense_Mutation", mutationList.get(1).getOncotatorVariantClassification());
		//assertEquals("p.R131H", mutationList.get(4).getOncotatorProteinChange());
		//assertEquals("rs76360727;rs33980232", mutationList.get(9).getOncotatorDbSnpRs());
//		assertEquals("p.E366_Q409del(13)|p.Q367R(1)|p.E366_K477del(1)",
//		             mutationList.get(15).getOncotatorCosmicOverlapping());
}
	       /**
	        * Tests custom filtering mutation types option (filtering for missense and nonsensemutations).
	        * @throws IOException
	        * @throws DaoException
	        */
	       @Test
	       public void testImportExtendedMutationDataExtendedCustomFiltering() throws IOException, DaoException {
	                   
	           MySQLbulkLoader.bulkLoadOn();
	           
	           File file = new File("src/test/resources/data_mutations_extended.txt");
	           Set<String> customFiltering = new HashSet<String>(Arrays.asList("Missense_Mutation", "Nonsense_Mutation"));
	           ImportExtendedMutationData parser = new ImportExtendedMutationData(file, geneticProfileId, null, customFiltering);
	           parser.importData();
	           MySQLbulkLoader.flushAll();
	           ConsoleUtil.showMessages();
	           
	           rejectMissenseAndNonsenseMutations();
	       }
	       
	       /**
                * Tests custom filtering mutation types option (no filtering at all).
                * @throws IOException
                * @throws DaoException
                */
               @Test
               public void testImportExtendedMutationDataExtendedNoFiltering() throws IOException, DaoException {
                           
                   MySQLbulkLoader.bulkLoadOn();
                   
                   File file = new File("src/test/resources/data_mutations_extended.txt");
                   Set<String> customFiltering = new HashSet<String>(Arrays.asList(""));
                   ImportExtendedMutationData parser = new ImportExtendedMutationData(file, geneticProfileId, null, customFiltering);
                   parser.importData();
                   MySQLbulkLoader.flushAll();
                   ConsoleUtil.showMessages();
                   
                   acceptAllMutationTypes();
               }

    private void checkBasicFilteringRules() throws DaoException {
        rejectSilentLOHIntronWildtype();
        acceptValidSomaticMutations();
    }

    private void validateMutationAminoAcid (int geneticProfileId, Integer sampleId, long entrezGeneId,
            String expectedAminoAcidChange) throws DaoException {
        ArrayList<ExtendedMutation> mutationList = DaoMutation.getMutations
                (geneticProfileId, sampleId, entrezGeneId);
        assertEquals(1, mutationList.size());
        assertEquals(expectedAminoAcidChange, mutationList.get(0).getProteinChange());
    }
    
    private void acceptValidSomaticMutations() throws DaoException {
        int sampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-AA-3664-01").getInternalId();

        // valid Somatic
        validateMutationAminoAcid (geneticProfileId, sampleId, 282770, "R113C");

        // valid Somatic
        validateMutationAminoAcid (geneticProfileId, sampleId, 51259, "G61G");
    }

    private void rejectSilentLOHIntronWildtype() throws DaoException {
        int sampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-AA-3664-01").getInternalId();

        assertEquals(0, DaoMutation.getMutations(geneticProfileId, sampleId, 114548).size()); // silent
        assertEquals(0, DaoMutation.getMutations(geneticProfileId, sampleId, 343035).size()); // LOH
        assertEquals(0, DaoMutation.getMutations(geneticProfileId, sampleId, 80114).size()); // Wildtype
        assertEquals(0, DaoMutation.getMutations(geneticProfileId, sampleId, 219736).size()); // Wildtype
        assertEquals(0, DaoMutation.getMutations(geneticProfileId, sampleId, 6609).size()); // Intron
    }


    private void checkGermlineMutations() throws DaoException {
        int sampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-AA-3664-01").getInternalId();

        assertEquals(1, DaoMutation.getMutations(geneticProfileId, sampleId, 64581).size());
        // missense, Germline mutation on germline whitelist

        // Germline mutation on germline whitelist
        validateMutationAminoAcid (geneticProfileId, sampleId, 2842, "L113P");
        assertEquals(1, DaoMutation.getMutations(geneticProfileId, sampleId, 50839).size());
        // Germline mutations NOT on germline whitelist
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

        boolean origBulkLoad = MySQLbulkLoader.isBulkLoad();
        try {
            MySQLbulkLoader.bulkLoadOn();
            DaoUniProtIdMapping.addUniProtIdMapping("Q08043", "ACTN3_HUMAN", 89);
            DaoUniProtIdMapping.addUniProtIdMapping("Q9H694", "BICC1_HUMAN", 80114);
            DaoUniProtIdMapping.addUniProtIdMapping("Q9NZT1", "CALL5_HUMAN", 51806);
            DaoUniProtIdMapping.addUniProtIdMapping("Q9BXN2", "CLC7A_HUMAN", 64581);
            DaoUniProtIdMapping.addUniProtIdMapping("Q15760", "GPR19_HUMAN", 2842);
            DaoUniProtIdMapping.addUniProtIdMapping("P98160", "PGBM_HUMAN", 3339);
            DaoUniProtIdMapping.addUniProtIdMapping("Q96P20", "NALP3_HUMAN", 114548);
            DaoUniProtIdMapping.addUniProtIdMapping("Q8NH19", "O10AG_HUMAN", 282770);
            DaoUniProtIdMapping.addUniProtIdMapping("Q7Z3Z2", "RD3_HUMAN", 343035);
            DaoUniProtIdMapping.addUniProtIdMapping("Q96QD8", "S38A2_HUMAN", 54407);
            DaoUniProtIdMapping.addUniProtIdMapping("P17405", "ASM_HUMAN", 6609);
            DaoUniProtIdMapping.addUniProtIdMapping("P08047", "SP1_HUMAN", 6667);
            DaoUniProtIdMapping.addUniProtIdMapping("Q6ZVD7", "STOX1_HUMAN", 219736);
            DaoUniProtIdMapping.addUniProtIdMapping("Q9NYW0", "T2R10_HUMAN", 50839);
            DaoUniProtIdMapping.addUniProtIdMapping("Q9P0N5", "TM216_HUMAN", 51259);
        } finally {
            if (!origBulkLoad) {
                MySQLbulkLoader.bulkLoadOff();
            }
        }

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

    /**
     * Loads the study context (defined samples) for specific test data files.
     *
     * @throws DaoException  if failing to write to the database
     */
    private void loadStudyContext1() throws DaoException {
        CancerStudy study = DaoCancerStudy.getCancerStudyByInternalId(studyId);
        int pId;
        pId = DaoPatient.addPatient(new Patient(study, "TCGA-A2-A04T"));
        DaoSample.addSample(new Sample("TCGA-A2-A04T-01", pId, "brca"));
        pId = DaoPatient.addPatient(new Patient(study, "TCGA-A2-A0CR"));
        DaoSample.addSample(new Sample("TCGA-A2-A0CR-01", pId, "brca"));
        pId = DaoPatient.addPatient(new Patient(study, "TCGA-A2-A0CW"));
        DaoSample.addSample(new Sample("TCGA-A2-A0CW-01", pId, "brca"));
        pId = DaoPatient.addPatient(new Patient(study, "TCGA-A2-A0D3"));
        DaoSample.addSample(new Sample("TCGA-A2-A0D3-01", pId, "brca"));
        pId = DaoPatient.addPatient(new Patient(study, "TCGA-A2-A0SY"));
        DaoSample.addSample(new Sample("TCGA-A2-A0SY-01", pId, "brca"));
        pId = DaoPatient.addPatient(new Patient(study, "TCGA-A2-A0T5"));
        DaoSample.addSample(new Sample("TCGA-A2-A0T5-01", pId, "brca"));
        pId = DaoPatient.addPatient(new Patient(study, "TCGA-A2-A25D"));
        DaoSample.addSample(new Sample("TCGA-A2-A25D-01", pId, "brca"));
        pId = DaoPatient.addPatient(new Patient(study, "TCGA-A2-A4RW"));
        DaoSample.addSample(new Sample("TCGA-A2-A4RW-01", pId, "brca"));
    }
    
    private void rejectMissenseAndNonsenseMutations() throws DaoException {
        int sampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-AA-3664-01").getInternalId();

        assertEquals(0, DaoMutation.getMutations(geneticProfileId, sampleId, 63967).size()); // Missense
        assertEquals(0, DaoMutation.getMutations(geneticProfileId, sampleId, 79699).size()); // Missense
        assertEquals(0, DaoMutation.getMutations(geneticProfileId, sampleId, 204219).size()); // Missense
        assertEquals(0, DaoMutation.getMutations(geneticProfileId, sampleId, 51259).size()); // Nonsense
        assertEquals(0, DaoMutation.getMutations(geneticProfileId, sampleId, 84902).size()); // Nonsense
    }
    
    private void acceptAllMutationTypes() throws DaoException {
        int sampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-AA-3664-01").getInternalId();

        // valid Nonsense
        validateMutationAminoAcid (geneticProfileId, sampleId, 2842, "L113P");
        
        // valid Silent
        validateMutationAminoAcid (geneticProfileId, sampleId, 114548, "G982G");
    }
}
