/*
 * Copyright (c) 2021 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.scripts;

import java.io.*;
import java.util.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mskcc.cbio.portal.dao.ClickHouseBulkLoader;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoGenericAssay;
import org.mskcc.cbio.portal.dao.DaoGeneticAlteration;
import org.mskcc.cbio.portal.dao.DaoGeneticEntity;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoPatient;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.dao.DaoSampleProfile;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.shared.GeneticAlterationType;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.Patient;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.model.shared.GeneticEntity;
import org.mskcc.cbio.portal.scripts.ImportGenericAssayPatientLevelData;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * JUnit tests for ImportGenericAssayPatientLevelData class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
public class TestImportGenericAssayPatientLevelData extends IntegrationTestBase {

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

        ClickHouseBulkLoader.bulkLoadOff();
        runImportGenericAssayPatientLevelData();
    }
    
    /**
     * Test importing of data_patient_generic_assay.txt file.
     * @throws Exception All Errors.
     */
	@Test
    public void testImportGenericAssayPatientLevelDataBulkLoadOn() throws Exception {
		ClickHouseBulkLoader.bulkLoadOn();
        runImportGenericAssayPatientLevelData();
    }
    
    private void runImportGenericAssayPatientLevelData() throws Exception {

        DaoGeneticAlteration daoGeneticAlteration = DaoGeneticAlteration.getInstance();

        ProgressMonitor.setConsoleMode(false);

        File file = new File("src/test/resources/tabDelimitedData/data_patient_generic_assay.txt");
        ImportGenericAssayPatientLevelData parser = new ImportGenericAssayPatientLevelData(file, null, geneticProfileId, null, "name,description");

        parser.importData();

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
