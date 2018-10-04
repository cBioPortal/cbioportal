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
import static org.junit.Assert.assertNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoClinicalData;
import org.mskcc.cbio.portal.dao.DaoCnaEvent;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoMutation;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.dao.MySQLbulkLoader;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.CnaEvent;
import org.mskcc.cbio.portal.model.ExtendedMutation;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.ImportDataUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;
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

    @Before
    public void setUp() throws DaoException {
        ProgressMonitor.setConsoleMode(false);
        loadGenes();
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testImportMutationsFile() throws Exception {
        String[] args = {
                "--data","src/test/resources/data_mutations_extended.txt",
                "--meta","src/test/resources/meta_mutations_extended.txt",
                "--loadMode", "bulkLoad"
        };
        ImportProfileData runner = new ImportProfileData(args);
        runner.run();
        //This test is to check if the ImportProfileData class indeed adds the study stable Id in front of the
        //dataset study id (e.g. studyStableId + "_breast_mutations"):
        DaoClinicalData.deleteRecords("MUTATION_COUNT");
        MySQLbulkLoader.flushAll();
        String studyStableId = "study_tcga_pub";
        studyId = DaoCancerStudy.getCancerStudyByStableId(studyStableId).getInternalId();
        int sampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-AA-3664-01").getInternalId();
        int secondSampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-AA-3665-01").getInternalId();
        geneticProfileId = DaoGeneticProfile.getGeneticProfileByStableId(studyStableId + "_breast_mutations").getGeneticProfileId();
        validateMutationAminoAcid (geneticProfileId, sampleId, 54407, "T433A");
        // data for this sample should not exist before loading the next data file
        assertEquals(DaoMutation.getMutations(geneticProfileId, secondSampleId).size(), 0);
        // load a second mutation data file
        String[] secondArgs = {
                "--data","src/test/resources/data_mutations_extended_continued.txt",
                "--meta","src/test/resources/meta_mutations_extended.txt",
                "--loadMode", "bulkLoad"
        };
        ImportProfileData secondRunner = new ImportProfileData(secondArgs);
        secondRunner.run();
        validateMutationAminoAcid (geneticProfileId, secondSampleId, 2842, "L113P");
    }

    @Test
    public void testImportCNAFile() throws Exception {
        //genes in this test:
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        daoGene.addGene(new CanonicalGene(999999672, "TESTBRCA1"));
        daoGene.addGene(new CanonicalGene(999999675, "TESTBRCA2"));
        MySQLbulkLoader.flushAll();
        String[] args = {
                "--data","src/test/resources/data_CNA_sample.txt",
                "--meta","src/test/resources/meta_CNA.txt" ,
                "--noprogress",
                "--loadMode", "bulkLoad"
        };
        String[] sampleIds = {"TCGA-02-0001-01","TCGA-02-0003-01","TCGA-02-0004-01","TCGA-02-0006-01"};
        //This test is to check if the ImportProfileData class indeed adds the study stable Id in front of the
        //dataset study id (e.g. studyStableId + "_breast_mutations"):
        String studyStableId = "study_tcga_pub";
        CancerStudy study = DaoCancerStudy.getCancerStudyByStableId(studyStableId);
        studyId = study.getInternalId();
        //will be needed when relational constraints are active:
        ImportDataUtil.addPatients(sampleIds, study);
        ImportDataUtil.addSamples(sampleIds, study);
        try {
            ImportProfileData runner = new ImportProfileData(args);
            runner.run();
        } catch (Throwable e) {
            //useful info for when this fails:
            ConsoleUtil.showMessages();
            throw e;
        }
        geneticProfileId = DaoGeneticProfile.getGeneticProfileByStableId(studyStableId + "_cna").getGeneticProfileId();
        List<Integer> sampleInternalIds = new ArrayList<Integer>();
        DaoSample.reCache();
        for (String sample : sampleIds) {
            sampleInternalIds.add(DaoSample.getSampleByCancerStudyAndSampleId(studyId, sample).getInternalId());
        }
        Collection<Short> cnaLevels = Arrays.asList((short)-2, (short)2);
        List<CnaEvent> cnaEvents = DaoCnaEvent.getCnaEvents(sampleInternalIds, null, geneticProfileId, cnaLevels);
        assertEquals(2, cnaEvents.size());
        //validate specific records. Data looks like:
        //999999672    TESTBRCA1    -2    0    1    0
        //999999675    TESTBRCA2    0    2    0    -1
        //Check if the first two samples are loaded correctly:
        int sampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-02-0001-01").getInternalId();
        sampleInternalIds = Arrays.asList((int)sampleId);
        CnaEvent cnaEvent = DaoCnaEvent.getCnaEvents(sampleInternalIds, null, geneticProfileId, cnaLevels).get(0);
        assertEquals(-2, cnaEvent.getAlteration().getCode());
        assertEquals("TESTBRCA1", cnaEvent.getGeneSymbol());
        sampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-02-0003-01").getInternalId();
        sampleInternalIds = Arrays.asList((int)sampleId);
        cnaEvent = DaoCnaEvent.getCnaEvents(sampleInternalIds, null, geneticProfileId, cnaLevels).get(0);
        assertEquals(2, cnaEvent.getAlteration().getCode());
        assertEquals("TESTBRCA2", cnaEvent.getGeneSymbol());
    }

    private void validateMutationAminoAcid (int geneticProfileId, Integer sampleId, long entrezGeneId, String expectedAminoAcidChange) throws DaoException {
        ArrayList<ExtendedMutation> mutationList = DaoMutation.getMutations(geneticProfileId, sampleId, entrezGeneId);
        assertEquals(1, mutationList.size());
        assertEquals(expectedAminoAcidChange, mutationList.get(0).getProteinChange());
    }

    private static void loadGenes() throws DaoException {
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
