/*
 * Copyright (C) 2026  Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoPatient;
import org.mskcc.cbio.portal.dao.DaoResourceData;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.dao.ClickHouseBulkLoader;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.Patient;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/applicationContext-dao.xml"})
public class TestDaoSampleDelete extends AbstractDaoDeleteTest {

    // Seed sample TCGA-A1-A0SB-01 belongs to patient TCGA-A1-A0SB
    private static final String SAMPLE_STABLE_ID  = "TCGA-A1-A0SB-01";
    private static final String PATIENT_STABLE_ID = "TCGA-A1-A0SB";

    private CancerStudy study;
    private int studyId;
    private int sampleInternalId;
    private int patientInternalId;

    @Before
    public void setUp() throws DaoException {
        DaoCancerStudy.reCacheAll();
        study = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub");
        assertNotNull(study);
        studyId = study.getInternalId();

        sampleInternalId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, SAMPLE_STABLE_ID).getInternalId();

        Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(studyId, PATIENT_STABLE_ID);
        assertNotNull(patient);
        patientInternalId = patient.getInternalId();
    }

    @Test
    public void testDeleteSamples_resourceRowsAreGone() throws DaoException {
        // resource keys rows by stable id where the dropped resource_sample table keyed them
        // by internal id, so deleting a sample has to resolve its resources through the stable id,
        // scoped by study. A patient-level row of the same patient must survive: it belongs to the
        // patient, not to this sample.
        DaoResourceData.addResourceDatum(studyId, "PATIENT_NOTES", "SAMPLE", PATIENT_STABLE_ID,
                SAMPLE_STABLE_ID, "https://example.org/slide.svs", null, null);
        DaoResourceData.addResourceDatum(studyId, "PATIENT_NOTES", "PATIENT", PATIENT_STABLE_ID,
                null, "https://example.org/notes.pdf", null, null);
        ClickHouseBulkLoader.flushAll();
        assertEquals(2L, countRowsWhereEq("resource", "cancer_study_id", studyId));

        DaoSample.deleteSamples(studyId, Set.of(SAMPLE_STABLE_ID));

        assertEquals("the sample's resource row should go, the patient's should stay",
                1L, countRowsWhereEq("resource", "cancer_study_id", studyId));
    }

    @Test
    public void testDeleteSamples_sampleIsGone() throws DaoException {
        DaoSample.deleteSamples(studyId, Set.of(SAMPLE_STABLE_ID));

        assertNull("sample should be gone after deletion",
            DaoSample.getSampleByCancerStudyAndSampleId(studyId, SAMPLE_STABLE_ID));
    }

    @Test
    public void testDeleteSamples_sampleRowsAreGone() throws DaoException {
        DaoSample.deleteSamples(studyId, Set.of(SAMPLE_STABLE_ID));

        assertEquals(0L, countRowsWhereEq("sample",          "internal_id", sampleInternalId));
        assertEquals(0L, countRowsWhereEq("clinical_sample", "internal_id", sampleInternalId));
    }

    @Test
    public void testDeleteSamples_relatedProfileRowsAreGone() throws DaoException {
        DaoSample.deleteSamples(studyId, Set.of(SAMPLE_STABLE_ID));

        assertEquals(0L, countRowsWhereEq("sample_profile",   "sample_id", sampleInternalId));
        assertEquals(0L, countRowsWhereEq("sample_cna_event", "sample_id", sampleInternalId));
        assertEquals(0L, countRowsWhereEq("mutation",         "sample_id", sampleInternalId));
    }

    @Test
    public void testDeleteSamples_patientIsPreserved() throws DaoException {
        DaoSample.deleteSamples(studyId, Set.of(SAMPLE_STABLE_ID));

        // Deleting a sample must not delete the parent patient
        assertNotNull("patient should still exist after sample-only deletion",
            DaoPatient.getPatientByCancerStudyAndPatientId(studyId, PATIENT_STABLE_ID));
        assertEquals(1L, countRowsWhereEq("patient", "internal_id", patientInternalId));
    }

    @Test
    public void testDeleteSamples_noStagingTablesRemain() throws DaoException {
        DaoSample.deleteSamples(studyId, Set.of(SAMPLE_STABLE_ID));
        assertNoStagingTablesRemain();
    }

    @Test
    public void testDeleteSamples_emptyInput_isNoOp() throws DaoException {
        DaoSample.deleteSamples(studyId, Set.of());

        assertNotNull("sample should still exist when nothing was deleted",
            DaoSample.getSampleByCancerStudyAndSampleId(studyId, SAMPLE_STABLE_ID));
    }
}
