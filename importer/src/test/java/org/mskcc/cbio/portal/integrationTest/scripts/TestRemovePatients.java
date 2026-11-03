/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.scripts;

import java.util.*;
import java.util.stream.Stream;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneticAlteration;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoGeneticProfileSamples;
import org.mskcc.cbio.portal.dao.DaoPatient;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.Patient;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.scripts.RemovePatients;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * JUnit tests for RemovePatients class.
 *
 * @author Ruslan Forostianov
 * @author Pieter Lukasse
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/applicationContext-dao.xml"})
public class TestRemovePatients extends IntegrationTestBase {

    @Test
    public void testRemovePatients() throws DaoException {
        String patient1StableId = "TCGA-A1-A0SB";
        String patient2StableId = "TCGA-A1-A0SD";
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub");
        int patient1InternalId = DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudy.getInternalId(), patient1StableId).getInternalId();
        List<Integer> patient1InternalsSampleIds = DaoSample.getSamplesByPatientId(patient1InternalId).stream().map(Sample::getInternalPatientId).toList();
        int patient2InternalId = DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudy.getInternalId(), patient2StableId).getInternalId();
        List<Integer> patient2InternalsSampleIds = DaoSample.getSamplesByPatientId(patient2InternalId).stream().map(Sample::getInternalPatientId).toList();
        List<Integer> beforePatientIds = DaoPatient.getPatientsByCancerStudyId(cancerStudy.getInternalId()).stream().map(Patient::getInternalId).toList();
        assertTrue(beforePatientIds.contains(patient1InternalId));
        assertTrue(beforePatientIds.contains(patient2InternalId));

        new RemovePatients(new String[]{
                "--study_ids", "study_tcga_pub",
                "--patient_ids", "TCGA-A1-A0SB,TCGA-A1-A0SD"
        }).run();

        DaoPatient.reCache();

        List<Integer> afterPatientIds = DaoPatient.getPatientsByCancerStudyId(cancerStudy.getInternalId()).stream().map(Patient::getInternalId).toList();
        assertFalse(afterPatientIds.contains(patient1InternalId));
        assertFalse(afterPatientIds.contains(patient2InternalId));
        assertEquals(beforePatientIds.size() - 2, afterPatientIds.size());

        List<GeneticProfile> geneticProfiles = Stream.of("study_tcga_pub_gistic", "study_tcga_pub_mrna", "study_tcga_pub_log2CNA",
                "study_tcga_pub_rppa", "study_tcga_pub_treatment_ic50").map(DaoGeneticProfile::getGeneticProfileByStableId).toList();
        for (GeneticProfile geneticProfile : geneticProfiles) {
            HashMap<Integer, HashMap<Integer, String>> geneticAlterationMapForEntityIds = DaoGeneticAlteration.getInstance()
                    .getGeneticAlterationMapForEntityIds(geneticProfile.getGeneticProfileId(), null);
            for (Map.Entry<Integer, HashMap<Integer, String>> gaEntry : geneticAlterationMapForEntityIds.entrySet()) {
                for (Integer patient1InternalsSampleId : patient1InternalsSampleIds) {
                    assertFalse(gaEntry.getValue().containsKey(patient1InternalsSampleId),
                            "Genetic entity with id "
                                    + gaEntry.getKey()
                                    + " of " + geneticProfile.getStableId() + "genetic profile"
                                    + " must have all samples of " + patient1StableId + " patient deleted");
                }
                for (Integer patient2InternalsSampleId : patient2InternalsSampleIds) {
                    assertFalse(gaEntry.getValue().containsKey(patient2InternalsSampleId),
                            "Genetic entity with id "
                                    + gaEntry.getKey()
                                    + " of " + geneticProfile.getStableId() + "genetic profile"
                                    + " must have all samples of " + patient2StableId + " patient deleted");
                }
            }
        }
        int studyTcgaPubMethylationHm27 = DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_methylation_hm27").getGeneticProfileId();
        assertTrue("The methylation platform has to loose it's last sample", DaoGeneticProfileSamples.getOrderedSampleList(
                studyTcgaPubMethylationHm27).isEmpty());
    }

    @Test
    public void testStudyIdsOptionIsRequired() {
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () ->
                new RemovePatients(new String[]{
                        "--patient_ids", "TCGA-A1-A0SB-01"
                }).run()
        );
        assertThat(runtimeException.getMessage(),
                containsString("'--study_ids' argument has to specify study id"));
    }

    @Test
    public void testStudyIdsOptionValueIsRequired() {
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () ->
                new RemovePatients(new String[]{
                        "--study_ids", "",
                        "--patient_ids", "TCGA-A1-A0SB"
                }).run()
        );
        assertThat(runtimeException.getMessage(),
                containsString("'--study_ids' argument has to specify study id"));
    }

    @Test
    public void testPatientIdsOptionIsRequired() {
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () ->
                new RemovePatients(new String[]{
                        "--study_ids", "study_tcga_pub",
                }).run()
        );
        assertThat(runtimeException.getMessage(),
                containsString("'--patient_ids' argument has to specify patient id"));
    }

    @Test
    public void testPatientIdsOptionValueIsRequired() {
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () ->
                new RemovePatients(new String[]{
                        "--study_ids", "study_tcga_pub",
                        "--patient_ids", ""
                }).run()
        );
        assertThat(runtimeException.getMessage(),
                containsString("'--patient_ids' argument has to specify patient id"));
    }

    @Test
    public void testNoStudyExists() {
       RuntimeException runtimeException = assertThrows(RuntimeException.class, () ->
           new RemovePatients(new String[]{
                   "--study_ids", "study_tcga_pub,non_existing_study",
                   "--patient_ids", "TCGA-A1-A0SB"
           }).run()
       );
       assertThat(runtimeException.getMessage(),
               containsString("Cancer study with stable id=non_existing_study not found."));
    }
    @Test
    public void testNoPatientExists() {
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () ->
                new RemovePatients(new String[]{
                        "--study_ids", "study_tcga_pub",
                        "--patient_ids", "TCGA-A1-A0SB,NON_EXISTING_PATIENT"
                }).run()
        );
        assertThat(runtimeException.getMessage(),
                containsString("Patient with stable id=NON_EXISTING_PATIENT not found in study with internal id="));
    }

    @Test
    public void testRollbackPatientRemovalWithGsvaScore() throws DaoException {
        GeneticProfile gsvaScoreGeneticProfile = DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_gsva_scores");
        HashMap<Integer, HashMap<Integer, String>> beforeData = DaoGeneticAlteration.getInstance()
                .getGeneticAlterationMapForEntityIds(gsvaScoreGeneticProfile.getGeneticProfileId(), null);

        RuntimeException runtimeException = assertThrows(RuntimeException.class, () ->
                new RemovePatients(new String[]{
                        "--study_ids", "study_tcga_pub",
                        "--patient_ids", "TCGA-TEST-PATIENT-21"
                }).run()
        );
        assertThat(runtimeException.getMessage(),
                containsString("Sample(s) with stable id TCGA-TEST-SAMPLE-22 can't be removed as it contains GSVA data." +
                        " Consider dropping and re-uploading the whole study."));

        HashMap<Integer, HashMap<Integer, String>> afterData = DaoGeneticAlteration.getInstance()
                .getGeneticAlterationMapForEntityIds(gsvaScoreGeneticProfile.getGeneticProfileId(), null);

        assertEquals(beforeData, afterData);
    }

    @Test
    public void testRollbackPatientRemovalWithGsvaPvalue() throws DaoException {
        GeneticProfile gsvaPvalueGeneticProfile = DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_gsva_pvalues");
        HashMap<Integer, HashMap<Integer, String>> beforeData = DaoGeneticAlteration.getInstance()
                .getGeneticAlterationMapForEntityIds(gsvaPvalueGeneticProfile.getGeneticProfileId(), null);

        RuntimeException runtimeException = assertThrows(RuntimeException.class, () ->
                new RemovePatients(new String[]{
                        "--study_ids", "study_tcga_pub",
                        "--patient_ids", "TCGA-TEST-PATIENT-22"
                }).run()
        );
        assertThat(runtimeException.getMessage(),
                containsString("Sample(s) with stable id TCGA-TEST-SAMPLE-23 can't be removed as it contains GSVA data." +
                        " Consider dropping and re-uploading the whole study."));

        HashMap<Integer, HashMap<Integer, String>> afterData = DaoGeneticAlteration.getInstance()
                .getGeneticAlterationMapForEntityIds(gsvaPvalueGeneticProfile.getGeneticProfileId(), null);

        assertEquals(beforeData, afterData);
    }
}
