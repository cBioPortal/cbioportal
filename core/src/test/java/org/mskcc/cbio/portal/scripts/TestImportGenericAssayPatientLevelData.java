/*
 * Copyright (c) 2021 Memorial Sloan-Kettering Cancer Center.
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

import java.io.BufferedReader;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.cbioportal.model.GeneticEntity;
import org.mskcc.cbio.portal.util.*;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JUnit tests for ImportGenericAssayPatientLevelData class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Ignore

@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestImportGenericAssayPatientLevelData {

    private int studyId;
    private int geneticProfileId;
    private int sample1;
    private int sample2;
    private int sample3;
    private CancerStudy study;
    private GeneticEntity geneticEntity1;
    private GeneticEntity geneticEntity2;

    @Before
    public void setUp() throws DaoException {
        DaoCancerStudy.reCacheAll();
        DaoGeneOptimized.getInstance().reCache();
        ProgressMonitor.resetWarnings();

        study = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub");
        studyId =study.getInternalId();

        GeneticProfile newGeneticProfile = new GeneticProfile();
        newGeneticProfile.setCancerStudyId(studyId);
        newGeneticProfile.setGeneticAlterationType(GeneticAlterationType.GENERIC_ASSAY);
        newGeneticProfile.setStableId("study_tcga_pub_generic_assay_patient_test");
        newGeneticProfile.setProfileName("Generic Assay Patient Level data");
        newGeneticProfile.setDatatype("LIMIT-VALUE");
        newGeneticProfile.setPatientLevel(true);
        DaoGeneticProfile.addGeneticProfile(newGeneticProfile);

        geneticProfileId =  DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_generic_assay_patient_test").getGeneticProfileId();

        sample1 = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-A1-A0SB-01").getInternalId();
        sample2 = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-A1-A0SB-02").getInternalId();
        sample3 = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-A1-A0SD-01").getInternalId();

        geneticEntity1 = new GeneticEntity("GENERIC_ASSAY", "test_patient_generic_assay_1");
        geneticEntity1 = DaoGeneticEntity.addNewGeneticEntity(geneticEntity1);
        DaoGenericAssay.setGenericEntityProperty(geneticEntity1.getId(), "name", "test_patient_generic_assay_1");
        DaoGenericAssay.setGenericEntityProperty(geneticEntity1.getId(), "description", "test_patient_generic_assay_1");
        geneticEntity2 = new GeneticEntity("GENERIC_ASSAY", "test_patient_generic_assay_2");
        geneticEntity2 = DaoGeneticEntity.addNewGeneticEntity(geneticEntity2);
        DaoGenericAssay.setGenericEntityProperty(geneticEntity2.getId(), "name", "test_patient_generic_assay_2");
        DaoGenericAssay.setGenericEntityProperty(geneticEntity2.getId(), "description", "test_patient_generic_assay_2");
    }

    /**
     * Test importing of data_patient_generic_assay.txt file.
     * @throws Exception All Errors.
     */
	@Test
    public void testImportGenericAssayPatientLevelDataBulkLoadOff() throws Exception {

        MySQLbulkLoader.bulkLoadOff();
        runImportGenericAssayPatientLevelData();
    }
    
    /**
     * Test importing of data_patient_generic_assay.txt file.
     * @throws Exception All Errors.
     */
	@Test
    public void testImportGenericAssayPatientLevelDataBulkLoadOn() throws Exception {
		MySQLbulkLoader.bulkLoadOn();
        runImportGenericAssayPatientLevelData();
    }
    
    private void runImportGenericAssayPatientLevelData() throws DaoException, IOException{

        DaoGeneticAlteration daoGeneticAlteration = DaoGeneticAlteration.getInstance();

        ProgressMonitor.setConsoleMode(false);

        File file = new File("src/test/resources/tabDelimitedData/data_patient_generic_assay.txt");
        ImportGenericAssayPatientLevelData parser = new ImportGenericAssayPatientLevelData(file, null, geneticProfileId, null, "name,description");

        int numLines = FileUtil.getNumLines(file);
        parser.importData(numLines);

        HashMap<Integer,HashMap<Integer, String>> geneticAlterationMap = daoGeneticAlteration.getGeneticAlterationMapForEntityIds(geneticProfileId, Arrays.asList(geneticEntity1.getId(), geneticEntity2.getId()));

        HashMap<Integer, String> geneticAlterationMapForEntity1 = geneticAlterationMap.get(geneticEntity1.getId());
        String value = geneticAlterationMapForEntity1.get(sample1);
        assertEquals ("0.370266873", value);
        value = geneticAlterationMapForEntity1.get(sample2);
        assertEquals ("0.370266873", value);
        value = geneticAlterationMapForEntity1.get(sample3);
        assertEquals ("0.010373016", value);
        HashMap<Integer, String> geneticAlterationMapForEntity2 = geneticAlterationMap.get(geneticEntity2.getId());        
        value = geneticAlterationMapForEntity2.get(sample1);
        assertEquals ("0.002709404", value);
        value = geneticAlterationMapForEntity2.get(sample2);
        assertEquals ("0.002709404", value);
        value = geneticAlterationMapForEntity2.get(sample3);
        assertEquals ("0.009212318", value);

        Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(studyId, "TCGA-A1-A0SB");
        Sample sample = DaoSample.getSampleByPatientAndSampleId(patient.getInternalId(), "TCGA-A1-A0SB-01");
        assertTrue(DaoSampleProfile.sampleExistsInGeneticProfile(sample.getInternalId(), geneticProfileId));
        sample = DaoSample.getSampleByPatientAndSampleId(patient.getInternalId(), "TCGA-A1-A0SB-02");
        assertTrue(DaoSampleProfile.sampleExistsInGeneticProfile(sample.getInternalId(), geneticProfileId));

        patient = DaoPatient.getPatientByCancerStudyAndPatientId(studyId, "TCGA-A1-A0SD");
        sample = DaoSample.getSampleByPatientAndSampleId(patient.getInternalId(), "TCGA-A1-A0SD-01");
        assertTrue(DaoSampleProfile.sampleExistsInGeneticProfile(sample.getInternalId(), geneticProfileId));

        ArrayList caseIds = DaoSampleProfile.getAllSampleIdsInProfile(geneticProfileId);
        assertEquals(3, caseIds.size());
    }
    
}
