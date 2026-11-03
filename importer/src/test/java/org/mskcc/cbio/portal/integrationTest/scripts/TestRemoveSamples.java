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
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.scripts.RemoveSamples;
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
 * JUnit tests for RemoveSamples class.
 *
 * @author Ruslan Forostianov
 * @author Pieter Lukasse
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/applicationContext-dao.xml"})
public class TestRemoveSamples extends IntegrationTestBase {

    @Test
    public void testRemoveSamples() throws DaoException {
        String sample1StableId = "TCGA-A1-A0SB-01";
        String sample2StableId = "TCGA-A1-A0SD-01";
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub");
        List<String> beforeSampleIds = DaoSample.getSampleStableIdsByCancerStudy(cancerStudy.getInternalId());
        assertTrue(beforeSampleIds.contains(sample1StableId));
        assertTrue(beforeSampleIds.contains(sample2StableId));
        int sample1InternalId = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudy.getInternalId(), sample1StableId).getInternalId();
        int sample2InternalId = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudy.getInternalId(), sample2StableId).getInternalId();

        new RemoveSamples(new String[]{
                "--study_ids", "study_tcga_pub",
                "--sample_ids", "TCGA-A1-A0SB-01,TCGA-A1-A0SD-01"
        }).run();

        DaoSample.reCache();

        List<String> afterSampleIds = DaoSample.getSampleStableIdsByCancerStudy(cancerStudy.getInternalId());
        assertFalse(afterSampleIds.contains(sample1StableId));
        assertFalse(afterSampleIds.contains(sample2StableId));
        assertEquals(beforeSampleIds.size() - 2, afterSampleIds.size());

        List<GeneticProfile> geneticProfiles = Stream.of("study_tcga_pub_gistic", "study_tcga_pub_mrna", "study_tcga_pub_log2CNA",
                "study_tcga_pub_rppa", "study_tcga_pub_treatment_ic50").map(DaoGeneticProfile::getGeneticProfileByStableId).toList();
        for (GeneticProfile geneticProfile : geneticProfiles) {
            HashMap<Integer, HashMap<Integer, String>> geneticAlterationMapForEntityIds = DaoGeneticAlteration.getInstance()
                    .getGeneticAlterationMapForEntityIds(geneticProfile.getGeneticProfileId(), null);
            for (Map.Entry<Integer, HashMap<Integer, String>> gaEntry : geneticAlterationMapForEntityIds.entrySet()) {
                assertFalse(gaEntry.getValue().containsKey(sample1InternalId),
                        "Genetic entity with id "
                                + gaEntry.getKey()
                                + " of " + geneticProfile.getStableId() + "genetic profile"
                                + " must have " + sample1StableId + " sample deleted");
                assertFalse(gaEntry.getValue().containsKey(sample2InternalId),
                        "Genetic entity with id "
                                + gaEntry.getKey()
                                + " of " + geneticProfile.getStableId() + "genetic profile"
                                + " must have " + sample2StableId + " sample deleted");
            }
        }
        int studyTcgaPubMethylationHm27 = DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_methylation_hm27").getGeneticProfileId();
        assertTrue("The methylation platform has to loose it's last sample", DaoGeneticProfileSamples.getOrderedSampleList(
                studyTcgaPubMethylationHm27).isEmpty());
    }

    @Test
    public void testStudyIdsOptionIsRequired() {
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () ->
                new RemoveSamples(new String[]{
                        "--sample_ids", "TCGA-A1-A0SB-01"
                }).run()
        );
        assertThat(runtimeException.getMessage(),
                containsString("'--study_ids' argument has to specify study id"));
    }

    @Test
    public void testStudyIdsOptionValueIsRequired() {
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () ->
                new RemoveSamples(new String[]{
                        "--study_ids", "",
                        "--sample_ids", "TCGA-A1-A0SB-01"
                }).run()
        );
        assertThat(runtimeException.getMessage(),
                containsString("'--study_ids' argument has to specify study id"));
    }

    @Test
    public void testSampleIdsOptionIsRequired() {
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () ->
                new RemoveSamples(new String[]{
                        "--study_ids", "study_tcga_pub",
                }).run()
        );
        assertThat(runtimeException.getMessage(),
                containsString("'--sample_ids' argument has to specify sample id"));
    }

    @Test
    public void testSampleIdsOptionValueIsRequired() {
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () ->
                new RemoveSamples(new String[]{
                        "--study_ids", "study_tcga_pub",
                        "--sample_ids", ""
                }).run()
        );
        assertThat(runtimeException.getMessage(),
                containsString("'--sample_ids' argument has to specify sample id"));
    }

    @Test
    public void testNoStudyExists() {
       RuntimeException runtimeException = assertThrows(RuntimeException.class, () ->
           new RemoveSamples(new String[]{
                   "--study_ids", "study_tcga_pub,non_existing_study",
                   "--sample_ids", "TCGA-A1-A0SB-01"
           }).run()
       );
       assertThat(runtimeException.getMessage(),
               containsString("Cancer study with stable id=non_existing_study not found."));
    }
    @Test
    public void testNoSampleExists() {
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () ->
                new RemoveSamples(new String[]{
                        "--study_ids", "study_tcga_pub",
                        "--sample_ids", "TCGA-A1-A0SB-01,NON_EXISTING_SAMPLE"
                }).run()
        );
        assertThat(runtimeException.getMessage(),
                containsString("Sample with stable id=NON_EXISTING_SAMPLE not found in study with internal id="));
    }

    @Test
    public void testRollbackSampleRemovalWithGsvaScore() throws DaoException {
        GeneticProfile gsvaScoreGeneticProfile = DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_gsva_scores");
        HashMap<Integer, HashMap<Integer, String>> beforeData = DaoGeneticAlteration.getInstance()
                .getGeneticAlterationMapForEntityIds(gsvaScoreGeneticProfile.getGeneticProfileId(), null);

        RuntimeException runtimeException = assertThrows(RuntimeException.class, () ->
                new RemoveSamples(new String[]{
                        "--study_ids", "study_tcga_pub",
                        "--sample_ids", "TCGA-TEST-SAMPLE-22"
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
    public void testRollbackSampleRemovalWithGsvaPvalue() throws DaoException {
        GeneticProfile gsvaPvalueGeneticProfile = DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_gsva_pvalues");
        HashMap<Integer, HashMap<Integer, String>> beforeData = DaoGeneticAlteration.getInstance()
                .getGeneticAlterationMapForEntityIds(gsvaPvalueGeneticProfile.getGeneticProfileId(), null);

        RuntimeException runtimeException = assertThrows(RuntimeException.class, () ->
                new RemoveSamples(new String[]{
                        "--study_ids", "study_tcga_pub",
                        "--sample_ids", "TCGA-TEST-SAMPLE-23"
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
