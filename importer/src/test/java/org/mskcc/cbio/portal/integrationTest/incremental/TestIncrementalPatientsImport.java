/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.incremental;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoClinicalData;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoPatient;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.ClinicalData;
import org.mskcc.cbio.portal.model.Patient;
import org.mskcc.cbio.portal.scripts.ImportClinicalData;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests Incremental Import of Sample Clinical Data.
 *
 * @author Ruslan Forostianov
 * @author Pieter Lukasse
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
public class TestIncrementalPatientsImport extends IntegrationTestBase {

    public static final String STUDY_ID = "study_tcga_pub";
    private CancerStudy cancerStudy;

    @Before
    public void setUp() throws DaoException {
        cancerStudy = DaoCancerStudy.getCancerStudyByStableId(STUDY_ID);
    }

    @Test
    public void testInsertNewPatient() throws DaoException {
        String newPatientId = "TEST-INC-TCGA-P2";
        File singleTcgaSampleFolder = new File("src/test/resources/incremental/insert_single_tcga_patient/");
        File metaFile = new File(singleTcgaSampleFolder, "meta_clinical_patient.txt");
        File dataFile = new File(singleTcgaSampleFolder, "clinical_data_single_PATIENT.txt");

        ImportClinicalData importClinicalData = new ImportClinicalData(new String[] {
                "--meta", metaFile.getAbsolutePath(),
                "--data", dataFile.getAbsolutePath(),
                "--overwrite-existing",
        });
        importClinicalData.run();

        Patient newPatient = DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudy.getInternalId(), newPatientId);
        assertNotNull("Patient with id " + newPatientId + " has to be injected to the DB.", newPatient);

        List<ClinicalData> clinicalData = DaoClinicalData.getData(cancerStudy.getInternalId(), List.of(newPatientId));
        Map<String, String> patientAttrs = clinicalData.stream().collect(Collectors.toMap(ClinicalData::getAttrId, ClinicalData::getAttrVal));
        assertEquals(Map.of(
                "SUBTYPE", "basal-like",
                "OS_STATUS", "0:LIVING",
                "OS_MONTHS", "45.6",
                "DFS_STATUS", "1:Recurred/Progressed"), patientAttrs);
    }

    @Test
    public void testUpdatePatientAttributes() throws DaoException {
        String updatedPatientId = "TCGA-A1-A0SB";

        Patient tcgaPatient = DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudy.getInternalId(),
                updatedPatientId);
        DaoClinicalData.addPatientDatum(tcgaPatient.getInternalId(), "SUBTYPE", "Luminal A");
        DaoClinicalData.addPatientDatum(tcgaPatient.getInternalId(), "OS_STATUS", "0:LIVING");
        DaoClinicalData.addPatientDatum(tcgaPatient.getInternalId(), "OS_MONTHS", "34.56");

        File singleTcgaSampleFolder = new File("src/test/resources/incremental/update_single_tcga_patient/");
        File metaFile = new File(singleTcgaSampleFolder, "meta_clinical_patient.txt");
        File dataFile = new File(singleTcgaSampleFolder, "clinical_data_single_PATIENT.txt");

        ImportClinicalData importClinicalData = new ImportClinicalData(new String[] {
                "--meta", metaFile.getAbsolutePath(),
                "--data", dataFile.getAbsolutePath(),
                "--overwrite-existing",
        });
        importClinicalData.run();

        Patient newPatient = DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudy.getInternalId(), updatedPatientId);
        assertNotNull("Patient with id " + updatedPatientId + " has to be injected to the DB.", newPatient);

        List<ClinicalData> clinicalData = DaoClinicalData.getData(cancerStudy.getInternalId(), List.of(updatedPatientId));
        Map<String, String> patientAttrs = clinicalData.stream().collect(Collectors.toMap(ClinicalData::getAttrId, ClinicalData::getAttrVal));
        assertEquals(Map.of(
                "SUBTYPE", "basal-like",
                "OS_MONTHS", "56.7",
                "DFS_STATUS", "1:Recurred/Progressed",
                "DFS_MONTHS", "100"), patientAttrs);
    }
}
