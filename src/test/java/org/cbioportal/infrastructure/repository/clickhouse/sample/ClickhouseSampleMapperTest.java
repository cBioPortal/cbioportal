package org.cbioportal.infrastructure.repository.clickhouse.sample;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.cbioportal.domain.sample.Sample;
import org.cbioportal.domain.sample.SampleType;
import org.cbioportal.domain.studyview.StudyViewFilterFactory;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.cbioportal.legacy.web.parameter.ClinicalDataFilter;
import org.cbioportal.legacy.web.parameter.CustomSampleIdentifier;
import org.cbioportal.legacy.web.parameter.DataFilterValue;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@Import(MyBatisConfig.class)
@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class ClickhouseSampleMapperTest {
  private static final String STUDY_TCGA_PUB = "study_tcga_pub";
  private static final String STUDY_ACC_TCGA = "acc_tcga";
  private static final String STUDY_GENIE_PUB = "study_genie_pub";

  @Autowired private ClickhouseSampleMapper mapper;

  @Test
  public void getFilteredSamples() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(Arrays.asList(STUDY_TCGA_PUB, STUDY_ACC_TCGA));
    var filteredSamples =
        mapper.getFilteredSamples(
            StudyViewFilterFactory.make(
                studyViewFilter, null, studyViewFilter.getStudyIds(), null));
    assertEquals(19, filteredSamples.size());

    ClinicalDataFilter customDataFilter = new ClinicalDataFilter();
    customDataFilter.setAttributeId("123");
    DataFilterValue value = new DataFilterValue();
    customDataFilter.setValues(List.of(value));
    studyViewFilter.setCustomDataFilters(List.of(customDataFilter));
    var filteredSamples1 =
        mapper.getFilteredSamples(
            StudyViewFilterFactory.make(
                studyViewFilter, List.of(), studyViewFilter.getStudyIds(), null));
    assertEquals(0, filteredSamples1.size());

    CustomSampleIdentifier customSampleIdentifier = new CustomSampleIdentifier();
    customSampleIdentifier.setStudyId("acc_tcga");
    customSampleIdentifier.setSampleId("tcga-a1-a0sb-01");
    var filteredSamples2 =
        mapper.getFilteredSamples(
            StudyViewFilterFactory.make(
                studyViewFilter,
                List.of(customSampleIdentifier),
                studyViewFilter.getStudyIds(),
                null));
    assertEquals(1, filteredSamples2.size());
  }

  @Test
  public void getSamplesFilteredByClinicalData() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(Arrays.asList(STUDY_GENIE_PUB, STUDY_ACC_TCGA));

    // samples of patients with AGE <= 20.0
    studyViewFilter.setClinicalDataFilters(
        List.of(newClinicalDataFilter("age", List.of(newDataFilterValue(null, 20.0, null)))));
    var filteredSamples1 =
        mapper.getFilteredSamples(
            StudyViewFilterFactory.make(
                studyViewFilter, List.of(), studyViewFilter.getStudyIds(), null));
    assertEquals(4, filteredSamples1.size());

    // samples of patients with AGE <= 20.0 or (80.0, 82.0]
    studyViewFilter.setClinicalDataFilters(
        List.of(
            newClinicalDataFilter(
                "age",
                List.of(
                    newDataFilterValue(null, 20.0, null), newDataFilterValue(80.0, 82.0, null)))));
    var filteredSamples2 =
        mapper.getFilteredSamples(
            StudyViewFilterFactory.make(
                studyViewFilter, List.of(), studyViewFilter.getStudyIds(), null));
    assertEquals(6, filteredSamples2.size());

    // samples of patients with UNKNOWN AGE
    studyViewFilter.setClinicalDataFilters(
        List.of(newClinicalDataFilter("age", List.of(newDataFilterValue(null, null, "Unknown")))));
    var filteredSamples3 =
        mapper.getFilteredSamples(
            StudyViewFilterFactory.make(
                studyViewFilter, List.of(), studyViewFilter.getStudyIds(), null));
    assertEquals(1, filteredSamples3.size());

    // samples of patients with AGE <= 20.0 or (80.0, 82.0] or UNKNOWN
    // this is a mixed list of filters of both numerical and non-numerical values
    studyViewFilter.setClinicalDataFilters(
        List.of(
            newClinicalDataFilter(
                "age",
                List.of(
                    newDataFilterValue(null, 20.0, null),
                    newDataFilterValue(80.0, 82.0, null),
                    newDataFilterValue(null, null, "unknown")))));
    var filteredSamples4 =
        mapper.getFilteredSamples(
            StudyViewFilterFactory.make(
                studyViewFilter, List.of(), studyViewFilter.getStudyIds(), null));
    assertEquals(7, filteredSamples4.size());

    // NA filter
    studyViewFilter.setClinicalDataFilters(
        List.of(newClinicalDataFilter("age", List.of(newDataFilterValue(null, null, "NA")))));
    var filteredSamples5 =
        mapper.getFilteredSamples(
            StudyViewFilterFactory.make(
                studyViewFilter, List.of(), studyViewFilter.getStudyIds(), null));
    // 4 acc_tcga + 7 study_genie_pub samples with "NA" AGE data or no AGE data
    assertEquals(11, filteredSamples5.size());

    // NA + UNKNOWN
    studyViewFilter.setClinicalDataFilters(
        List.of(
            newClinicalDataFilter(
                "age",
                List.of(
                    newDataFilterValue(null, null, "NA"),
                    newDataFilterValue(null, null, "UNKNOWN")))));
    var filteredSamples6 =
        mapper.getFilteredSamples(
            StudyViewFilterFactory.make(
                studyViewFilter, List.of(), studyViewFilter.getStudyIds(), null));
    // 11 NA + 1 UNKNOWN
    assertEquals(12, filteredSamples6.size());
  }

  @Test
  public void getMetaSamples() {
    var allAccSamplesMeta =
        mapper.getMetaSamples(Collections.singletonList(STUDY_ACC_TCGA), null, null, null);
    assertEquals(4, allAccSamplesMeta.getTotalCount().intValue());

    var allTcgaPubSamplesMeta =
        mapper.getMetaSamples(Collections.singletonList(STUDY_TCGA_PUB), null, null, null);
    assertEquals(15, allTcgaPubSamplesMeta.getTotalCount().intValue());

    var allSamplesMetaForAccAndTcga =
        mapper.getMetaSamples(List.of(STUDY_ACC_TCGA, STUDY_TCGA_PUB), null, null, null);
    assertEquals(19, allSamplesMetaForAccAndTcga.getTotalCount().intValue());

    var tcgaPubSamplesMetaForPatientA1A0SB =
        mapper.getMetaSamples(
            Collections.singletonList(STUDY_TCGA_PUB), "tcga-a1-a0sb", null, null);
    assertEquals(2, tcgaPubSamplesMetaForPatientA1A0SB.getTotalCount().intValue());

    var specificSamplesMetaForAccAndTcga =
        mapper.getMetaSamples(
            List.of(
                "invalid_study",
                "invalid_study",
                STUDY_ACC_TCGA,
                STUDY_TCGA_PUB,
                STUDY_TCGA_PUB,
                "mismatching_study_id",
                "mismatching_study_id"),
            null,
            List.of(
                // invalid/unknown samples
                "unknown",
                "does_not_exist",
                // samples from acc_tcga or study_tcga_pub
                "tcga-a1-b0sq-01",
                "tcga-a1-a0sq-01",
                "tcga-a1-a0si-01",
                // samples from study_genie_pub (should be filtered out because we didn't include
                // this study)
                "GENIE-TEST-302-01",
                "GENIE-TEST-309-01"),
            null);
    assertEquals(3, specificSamplesMetaForAccAndTcga.getTotalCount().intValue());

    var keywordFilteredSamplesMetaForAccAndTcgaAndGenie =
        mapper.getMetaSamples(
            List.of(STUDY_ACC_TCGA, STUDY_TCGA_PUB, STUDY_GENIE_PUB), null, null, "-02");
    assertEquals(4, keywordFilteredSamplesMetaForAccAndTcgaAndGenie.getTotalCount().intValue());
  }

  @Test
  public void getMetaSamplesBySampleListIds() {
    var studyTcgaPubAll =
        mapper.getMetaSamplesBySampleListIds(Collections.singletonList("study_tcga_pub_all"));
    assertEquals(14, studyTcgaPubAll.getTotalCount().intValue());

    var studyTcgaPubAcgh =
        mapper.getMetaSamplesBySampleListIds(Collections.singletonList("study_tcga_pub_acgh"));
    assertEquals(14, studyTcgaPubAcgh.getTotalCount().intValue());

    var studyTcgaPubCnaseq =
        mapper.getMetaSamplesBySampleListIds(Collections.singletonList("study_tcga_pub_cnaseq"));
    assertEquals(7, studyTcgaPubCnaseq.getTotalCount().intValue());

    var studyTcgaPubComplete =
        mapper.getMetaSamplesBySampleListIds(Collections.singletonList("study_tcga_pub_complete"));
    assertEquals(7, studyTcgaPubComplete.getTotalCount().intValue());

    var studyTcgaPubLog2cna =
        mapper.getMetaSamplesBySampleListIds(Collections.singletonList("study_tcga_pub_log2cna"));
    assertEquals(14, studyTcgaPubLog2cna.getTotalCount().intValue());

    var studyTcgaPubMethylationHm27 =
        mapper.getMetaSamplesBySampleListIds(
            Collections.singletonList("study_tcga_pub_methylation_hm27"));
    assertEquals(1, studyTcgaPubMethylationHm27.getTotalCount().intValue());

    var studyTcgaPubMrna =
        mapper.getMetaSamplesBySampleListIds(Collections.singletonList("study_tcga_pub_mrna"));
    assertEquals(8, studyTcgaPubMrna.getTotalCount().intValue());

    var studyTcgaPubSequenced =
        mapper.getMetaSamplesBySampleListIds(Collections.singletonList("study_tcga_pub_sequenced"));
    assertEquals(7, studyTcgaPubSequenced.getTotalCount().intValue());

    var studyTcgaPubCna =
        mapper.getMetaSamplesBySampleListIds(Collections.singletonList("study_tcga_pub_cna"));
    assertEquals(7, studyTcgaPubCna.getTotalCount().intValue());

    var studyTcgaPubRnaSeqV2Mrna =
        mapper.getMetaSamplesBySampleListIds(
            Collections.singletonList("study_tcga_pub_rna_seq_v2_mrna"));
    assertEquals(7, studyTcgaPubRnaSeqV2Mrna.getTotalCount().intValue());

    var studyTcgaPubMicrorna =
        mapper.getMetaSamplesBySampleListIds(Collections.singletonList("study_tcga_pub_microrna"));
    assertEquals(0, studyTcgaPubMicrorna.getTotalCount().intValue());

    var studyTcgaPubRppa =
        mapper.getMetaSamplesBySampleListIds(Collections.singletonList("study_tcga_pub_rppa"));
    assertEquals(0, studyTcgaPubRppa.getTotalCount().intValue());

    var studyTcgaPub3wayComplete =
        mapper.getMetaSamplesBySampleListIds(
            Collections.singletonList("study_tcga_pub_3way_complete"));
    assertEquals(7, studyTcgaPub3wayComplete.getTotalCount().intValue());

    var accTcgaAll =
        mapper.getMetaSamplesBySampleListIds(Collections.singletonList("acc_tcga_all"));
    assertEquals(1, accTcgaAll.getTotalCount().intValue());
  }

  @Test
  public void getSamples() {
    var allAccSamples =
        mapper.getSamples(
            Collections.singletonList(STUDY_ACC_TCGA), null, null, null, null, null, null, null);
    assertAccTcgaSampleIds(allAccSamples);
    assertNull(allAccSamples.getFirst().sampleType());

    var allTcgaPubSamples =
        mapper.getSamples(
            Collections.singletonList(STUDY_TCGA_PUB), null, null, null, null, null, null, null);
    assertTcgaPubSampleIds(allTcgaPubSamples);
    assertNull(allTcgaPubSamples.getFirst().sampleType());

    var allSamplesForAccAndTcga =
        mapper.getSamples(
            List.of(STUDY_ACC_TCGA, STUDY_TCGA_PUB), null, null, null, null, null, null, null);
    assertMultiStudySampleIds(allSamplesForAccAndTcga);
    assertNull(allSamplesForAccAndTcga.getFirst().sampleType());

    var tcgaPubSamplesForPatientA1A0SB =
        mapper.getSamples(
            Collections.singletonList(STUDY_TCGA_PUB),
            "tcga-a1-a0sb",
            null,
            null,
            null,
            null,
            null,
            null);
    assertSampleIdsForPatientA1A0SB(tcgaPubSamplesForPatientA1A0SB);
    assertNull(tcgaPubSamplesForPatientA1A0SB.getFirst().sampleType());

    var keywordFilteredSamplesForAccAndTcgaAndGenie =
        mapper.getSamples(
            List.of(STUDY_ACC_TCGA, STUDY_TCGA_PUB, STUDY_GENIE_PUB),
            null,
            null,
            "-02",
            null,
            null,
            null,
            null);
    assertKeywordFilteredSampleIds(keywordFilteredSamplesForAccAndTcgaAndGenie);
    assertNull(keywordFilteredSamplesForAccAndTcgaAndGenie.getFirst().sampleType());
  }

  @Test
  public void getSamplesByStudyAndSampleIds() {
    var userDefinedSamplesForAccAndTcga =
        mapper.getSamples(
            List.of(
                "invalid_study",
                "invalid_study",
                STUDY_ACC_TCGA,
                STUDY_TCGA_PUB,
                STUDY_TCGA_PUB,
                "mismatching_study_id",
                "mismatching_study_id"),
            null,
            List.of(
                // invalid/unknown samples
                "unknown",
                "does_not_exist",
                // samples from acc_tcga or study_tcga_pub
                "tcga-a1-b0sq-01",
                "tcga-a1-a0sq-01",
                "tcga-a1-a0si-01",
                // samples from study_genie_pub (should be filtered out because we didn't include
                // this study)
                "GENIE-TEST-302-01",
                "GENIE-TEST-309-01"),
            null,
            null,
            null,
            null,
            null);
    assertUserDefinedSampleIds(userDefinedSamplesForAccAndTcga);
    assertNull(userDefinedSamplesForAccAndTcga.getFirst().sampleType());

    var userDefinedSamplesForGenie =
        mapper.getSamples(
            Collections.singletonList(STUDY_GENIE_PUB),
            null,
            List.of(
                // samples from study_genie_pub
                "GENIE-TEST-302-01",
                "GENIE-TEST-309-01",
                // samples from acc_tcga or study_tcga_pub (should be filtered out because we didn't
                // include this study)
                "tcga-a1-b0sq-01",
                "tcga-a1-a0sq-01",
                "tcga-a1-a0si-01"),
            null,
            null,
            null,
            null,
            null);
    assertUserDefinedGenieSampleIds(userDefinedSamplesForGenie);
    assertNull(userDefinedSamplesForGenie.getFirst().sampleType());
  }

  @Test
  public void getSummarySamples() {
    var allAccSamples =
        mapper.getSummarySamples(
            Collections.singletonList(STUDY_ACC_TCGA), null, null, null, null, null, null, null);
    assertAccTcgaSamplesSummary(allAccSamples);
    assertNull(allAccSamples.getFirst().sequenced());
    assertNull(allAccSamples.getFirst().copyNumberSegmentPresent());

    var allTcgaPubSamples =
        mapper.getSummarySamples(
            Collections.singletonList(STUDY_TCGA_PUB), null, null, null, null, null, null, null);
    assertTcgaPubSamplesSummary(allTcgaPubSamples);
    assertNull(allTcgaPubSamples.getFirst().sequenced());
    assertNull(allTcgaPubSamples.getFirst().copyNumberSegmentPresent());

    var allSamplesForAccAndTcga =
        mapper.getSummarySamples(
            List.of(STUDY_ACC_TCGA, STUDY_TCGA_PUB), null, null, null, null, null, null, null);
    assertMultiStudySamplesSummary(allSamplesForAccAndTcga);
    assertNull(allSamplesForAccAndTcga.getFirst().sequenced());
    assertNull(allSamplesForAccAndTcga.getFirst().copyNumberSegmentPresent());

    var tcgaPubSamplesForPatientA1A0SB =
        mapper.getSummarySamples(
            Collections.singletonList(STUDY_TCGA_PUB),
            "tcga-a1-a0sb",
            null,
            null,
            null,
            null,
            null,
            null);
    assertSamplesSummaryForPatientA1A0SB(tcgaPubSamplesForPatientA1A0SB);
    assertNull(tcgaPubSamplesForPatientA1A0SB.getFirst().sequenced());
    assertNull(tcgaPubSamplesForPatientA1A0SB.getFirst().copyNumberSegmentPresent());

    var keywordFilteredSamplesForAccAndTcgaAndGenie =
        mapper.getSummarySamples(
            List.of(STUDY_ACC_TCGA, STUDY_TCGA_PUB, STUDY_GENIE_PUB),
            null,
            null,
            "-02",
            null,
            null,
            null,
            null);
    assertKeywordFilteredSamplesSummary(keywordFilteredSamplesForAccAndTcgaAndGenie);
    assertNull(keywordFilteredSamplesForAccAndTcgaAndGenie.getFirst().sequenced());
    assertNull(keywordFilteredSamplesForAccAndTcgaAndGenie.getFirst().copyNumberSegmentPresent());
  }

  @Test
  public void getSummarySamplesByStudyAndSampleIds() {
    var userDefinedSamplesForAccAndTcga =
        mapper.getSummarySamples(
            List.of(
                "invalid_study",
                "invalid_study",
                STUDY_ACC_TCGA,
                STUDY_TCGA_PUB,
                STUDY_TCGA_PUB,
                "mismatching_study_id",
                "mismatching_study_id"),
            null,
            List.of(
                // invalid/unknown samples
                "unknown",
                "does_not_exist",
                // samples from acc_tcga or study_tcga_pub
                "tcga-a1-b0sq-01",
                "tcga-a1-a0sq-01",
                "tcga-a1-a0si-01",
                // samples from study_genie_pub (should be filtered out because we didn't include
                // this study)
                "GENIE-TEST-302-01",
                "GENIE-TEST-309-01"),
            null,
            null,
            null,
            null,
            null);
    assertUserDefinedSamplesSummary(userDefinedSamplesForAccAndTcga);
    assertNull(userDefinedSamplesForAccAndTcga.getFirst().sequenced());
    assertNull(userDefinedSamplesForAccAndTcga.getFirst().copyNumberSegmentPresent());

    var userDefinedSamplesForGenie =
        mapper.getSummarySamples(
            Collections.singletonList(STUDY_GENIE_PUB),
            null,
            List.of(
                // samples from study_genie_pub
                "GENIE-TEST-302-01",
                "GENIE-TEST-309-01",
                // samples from acc_tcga or study_tcga_pub (should be filtered out because we didn't
                // include this study)
                "tcga-a1-b0sq-01",
                "tcga-a1-a0sq-01",
                "tcga-a1-a0si-01"),
            null,
            null,
            null,
            null,
            null);
    assertUserDefinedGenieSamplesSummary(userDefinedSamplesForGenie);
    assertNull(userDefinedSamplesForGenie.getFirst().sequenced());
    assertNull(userDefinedSamplesForGenie.getFirst().copyNumberSegmentPresent());
  }

  @Test
  public void getDetailedSamples() {
    var allAccSamples =
        mapper.getDetailedSamples(
            Collections.singletonList(STUDY_ACC_TCGA), null, null, null, null, null, null, null);
    assertAccTcgaSamplesDetailed(allAccSamples);

    var allTcgaPubSamples =
        mapper.getDetailedSamples(
            Collections.singletonList(STUDY_TCGA_PUB), null, null, null, null, null, null, null);
    assertTcgaPubSamplesDetailed(allTcgaPubSamples);

    var allSamplesForAccAndTcga =
        mapper.getDetailedSamples(
            List.of(STUDY_ACC_TCGA, STUDY_TCGA_PUB), null, null, null, null, null, null, null);
    assertMultiStudySamplesDetailed(allSamplesForAccAndTcga);

    var tcgaPubSamplesForPatientA1A0SB =
        mapper.getDetailedSamples(
            Collections.singletonList(STUDY_TCGA_PUB),
            "tcga-a1-a0sb",
            null,
            null,
            null,
            null,
            null,
            null);
    assertSamplesDetailedForPatientA1A0SB(tcgaPubSamplesForPatientA1A0SB);

    var keywordFilteredSamplesForAccAndTcgaAndGenie =
        mapper.getDetailedSamples(
            List.of(STUDY_ACC_TCGA, STUDY_TCGA_PUB, STUDY_GENIE_PUB),
            null,
            null,
            "-02",
            null,
            null,
            null,
            null);
    assertKeywordFilteredSamplesDetailed(keywordFilteredSamplesForAccAndTcgaAndGenie);
  }

  @Test
  public void getDetailedSamplesByStudyAndSampleIds() {
    var userDefinedSamplesForAccAndTcga =
        mapper.getDetailedSamples(
            List.of(
                "invalid_study",
                "invalid_study",
                STUDY_ACC_TCGA,
                STUDY_TCGA_PUB,
                STUDY_TCGA_PUB,
                "mismatching_study_id",
                "mismatching_study_id"),
            null,
            List.of(
                // invalid/unknown samples
                "unknown",
                "does_not_exist",
                // samples from acc_tcga or study_tcga_pub
                "tcga-a1-b0sq-01",
                "tcga-a1-a0sq-01",
                "tcga-a1-a0si-01",
                // samples from study_genie_pub (should be filtered out because we didn't include
                // this study)
                "GENIE-TEST-302-01",
                "GENIE-TEST-309-01"),
            null,
            null,
            null,
            null,
            null);
    assertUserDefinedSamplesDetailed(userDefinedSamplesForAccAndTcga);

    var userDefinedSamplesForGenie =
        mapper.getDetailedSamples(
            Collections.singletonList(STUDY_GENIE_PUB),
            null,
            List.of(
                // samples from study_genie_pub
                "GENIE-TEST-302-01",
                "GENIE-TEST-309-01",
                // samples from acc_tcga or study_tcga_pub (should be filtered out because we didn't
                // include this study)
                "tcga-a1-b0sq-01",
                "tcga-a1-a0sq-01",
                "tcga-a1-a0si-01"),
            null,
            null,
            null,
            null,
            null);
    assertUserDefinedGenieSamplesDetailed(userDefinedSamplesForGenie);
  }

  private void assertAccTcgaSampleIds(List<Sample> allAccSamples) {
    assertEquals(4, allAccSamples.size());
    var allAccSampleIds = allAccSamples.stream().map(Sample::stableId).toList();
    assertTrue(allAccSampleIds.contains("tcga-a1-a0sb-01"));
    assertTrue(allAccSampleIds.contains("tcga-a1-b0so-01"));
    assertTrue(allAccSampleIds.contains("tcga-a1-b0sp-01"));
    assertTrue(allAccSampleIds.contains("tcga-a1-b0sq-01"));
  }

  private void assertAccTcgaSamplesSummary(List<Sample> allAccSamples) {
    assertAccTcgaSampleIds(allAccSamples);
    var a0sb01 =
        allAccSamples.stream()
            .filter(s -> s.stableId().equals("tcga-a1-a0sb-01"))
            .findFirst()
            .get();
    assertEquals(18, a0sb01.patientId().intValue());
    assertEquals(SampleType.PRIMARY_SOLID_TUMOR, a0sb01.sampleType());
  }

  private void assertAccTcgaSamplesDetailed(List<Sample> allAccSamples) {
    assertAccTcgaSamplesSummary(allAccSamples);
    var a0sb01 =
        allAccSamples.stream()
            .filter(s -> s.stableId().equals("tcga-a1-a0sb-01"))
            .findFirst()
            .get();
    assertFalse(a0sb01.sequenced());
    assertFalse(a0sb01.copyNumberSegmentPresent());
    assertEquals("tcga-a1-a0sb", a0sb01.patientStableId());
    assertEquals("tcga-a1-a0sb", a0sb01.patient().stableId());
    assertEquals(STUDY_ACC_TCGA, a0sb01.patient().cancerStudy().cancerStudyIdentifier());
  }

  private void assertTcgaPubSampleIds(List<Sample> allTcgaPubSamples) {
    assertEquals(15, allTcgaPubSamples.size());
    var allTcgaPubSampleIds = allTcgaPubSamples.stream().map(Sample::stableId).toList();
    assertTrue(allTcgaPubSampleIds.contains("tcga-a1-a0sb-01"));
    assertTrue(allTcgaPubSampleIds.contains("tcga-a1-a0sb-02"));
    assertTrue(allTcgaPubSampleIds.contains("tcga-a1-a0sm-01"));
    assertTrue(allTcgaPubSampleIds.contains("tcga-a1-a0sn-01"));
    assertTrue(allTcgaPubSampleIds.contains("tcga-a1-a0so-01"));
    assertTrue(allTcgaPubSampleIds.contains("tcga-a1-a0sp-01"));
    assertTrue(allTcgaPubSampleIds.contains("tcga-a1-a0sq-01"));
  }

  private void assertTcgaPubSamplesSummary(List<Sample> allTcgaPubSamples) {
    assertTcgaPubSampleIds(allTcgaPubSamples);
    var a0sb02 =
        allTcgaPubSamples.stream()
            .filter(s -> s.stableId().equals("tcga-a1-a0sb-02"))
            .findFirst()
            .get();
    assertEquals(1, a0sb02.patientId().intValue());
    assertEquals(SampleType.PRIMARY_SOLID_TUMOR, a0sb02.sampleType());
  }

  private void assertTcgaPubSamplesDetailed(List<Sample> allTcgaPubSamples) {
    assertTcgaPubSamplesSummary(allTcgaPubSamples);
    var a0sb02 =
        allTcgaPubSamples.stream()
            .filter(s -> s.stableId().equals("tcga-a1-a0sb-02"))
            .findFirst()
            .get();
    assertFalse(a0sb02.sequenced());
    assertFalse(a0sb02.copyNumberSegmentPresent());
    assertEquals("tcga-a1-a0sb", a0sb02.patientStableId());
    assertEquals("tcga-a1-a0sb", a0sb02.patient().stableId());
    assertEquals(STUDY_TCGA_PUB, a0sb02.patient().cancerStudy().cancerStudyIdentifier());
  }

  private void assertMultiStudySampleIds(List<Sample> allSamplesForAccAndTcga) {
    assertEquals(19, allSamplesForAccAndTcga.size());
    var allSamplesIdsForAccAndTcga =
        allSamplesForAccAndTcga.stream().map(Sample::stableId).toList();
    // we have tcga-a1-a0sb-01 in both studies, so the count should be 2
    assertEquals(
        2,
        allSamplesIdsForAccAndTcga.stream()
            .filter(s -> s.equals("tcga-a1-a0sb-01"))
            .toList()
            .size());
    assertTrue(allSamplesIdsForAccAndTcga.contains("tcga-a1-a0sb-02"));
    assertTrue(allSamplesIdsForAccAndTcga.contains("tcga-a1-a0sm-01"));
    assertTrue(allSamplesIdsForAccAndTcga.contains("tcga-a1-a0sn-01"));
    assertTrue(allSamplesIdsForAccAndTcga.contains("tcga-a1-a0so-01"));
    assertTrue(allSamplesIdsForAccAndTcga.contains("tcga-a1-a0sp-01"));
    assertTrue(allSamplesIdsForAccAndTcga.contains("tcga-a1-a0sq-01"));
    assertTrue(allSamplesIdsForAccAndTcga.contains("tcga-a1-b0so-01"));
    assertTrue(allSamplesIdsForAccAndTcga.contains("tcga-a1-b0sp-01"));
    assertTrue(allSamplesIdsForAccAndTcga.contains("tcga-a1-b0sq-01"));
  }

  private void assertMultiStudySamplesSummary(List<Sample> allSamplesForAccAndTcga) {
    assertMultiStudySampleIds(allSamplesForAccAndTcga);
    assertEquals(SampleType.PRIMARY_SOLID_TUMOR, allSamplesForAccAndTcga.getFirst().sampleType());
  }

  private void assertMultiStudySamplesDetailed(List<Sample> allSamplesForAccAndTcga) {
    assertMultiStudySamplesSummary(allSamplesForAccAndTcga);
    assertNotNull(
        allSamplesForAccAndTcga.getFirst().patient().cancerStudy().cancerStudyIdentifier());
  }

  private void assertSampleIdsForPatientA1A0SB(List<Sample> tcgaPubSamplesForPatientA1A0SB) {
    assertEquals(2, tcgaPubSamplesForPatientA1A0SB.size());
    var sampleIdsForPatientA1A0SB =
        tcgaPubSamplesForPatientA1A0SB.stream().map(Sample::stableId).toList();
    assertTrue(sampleIdsForPatientA1A0SB.contains("tcga-a1-a0sb-01"));
    assertTrue(sampleIdsForPatientA1A0SB.contains("tcga-a1-a0sb-02"));
  }

  private void assertSamplesSummaryForPatientA1A0SB(List<Sample> tcgaPubSamplesForPatientA1A0SB) {
    assertSampleIdsForPatientA1A0SB(tcgaPubSamplesForPatientA1A0SB);
    assertEquals(1, tcgaPubSamplesForPatientA1A0SB.getFirst().patientId().intValue());
    assertEquals(
        SampleType.PRIMARY_SOLID_TUMOR, tcgaPubSamplesForPatientA1A0SB.getFirst().sampleType());
    assertEquals(1, tcgaPubSamplesForPatientA1A0SB.getLast().patientId().intValue());
    assertEquals(
        SampleType.PRIMARY_SOLID_TUMOR, tcgaPubSamplesForPatientA1A0SB.getLast().sampleType());
  }

  private void assertSamplesDetailedForPatientA1A0SB(List<Sample> tcgaPubSamplesForPatientA1A0SB) {
    assertSamplesSummaryForPatientA1A0SB(tcgaPubSamplesForPatientA1A0SB);
    assertEquals("tcga-a1-a0sb", tcgaPubSamplesForPatientA1A0SB.getFirst().patient().stableId());
    assertEquals(
        STUDY_TCGA_PUB,
        tcgaPubSamplesForPatientA1A0SB.getFirst().patient().cancerStudy().cancerStudyIdentifier());
    assertEquals("tcga-a1-a0sb", tcgaPubSamplesForPatientA1A0SB.getLast().patient().stableId());
    assertEquals(
        STUDY_TCGA_PUB,
        tcgaPubSamplesForPatientA1A0SB.getLast().patient().cancerStudy().cancerStudyIdentifier());
  }

  private void assertUserDefinedSampleIds(List<Sample> userDefinedSamplesForAccAndTcga) {
    assertEquals(3, userDefinedSamplesForAccAndTcga.size());
    var specificSampleIdsForAccAndTcga =
        userDefinedSamplesForAccAndTcga.stream().map(Sample::stableId).toList();
    assertTrue(specificSampleIdsForAccAndTcga.contains("tcga-a1-b0sq-01"));
    assertTrue(specificSampleIdsForAccAndTcga.contains("tcga-a1-a0sq-01"));
    assertTrue(specificSampleIdsForAccAndTcga.contains("tcga-a1-a0si-01"));
  }

  private void assertUserDefinedSamplesSummary(List<Sample> userDefinedSamplesForAccAndTcga) {
    assertUserDefinedSampleIds(userDefinedSamplesForAccAndTcga);
    assertEquals(
        SampleType.PRIMARY_SOLID_TUMOR, userDefinedSamplesForAccAndTcga.get(0).sampleType());
    assertEquals(
        SampleType.PRIMARY_SOLID_TUMOR, userDefinedSamplesForAccAndTcga.get(1).sampleType());
    assertEquals(
        SampleType.PRIMARY_SOLID_TUMOR, userDefinedSamplesForAccAndTcga.get(2).sampleType());
  }

  private void assertUserDefinedSamplesDetailed(List<Sample> userDefinedSamplesForAccAndTcga) {
    assertUserDefinedSamplesSummary(userDefinedSamplesForAccAndTcga);
    assertNotNull(
        userDefinedSamplesForAccAndTcga.get(0).patient().cancerStudy().cancerStudyIdentifier());
    assertNotNull(
        userDefinedSamplesForAccAndTcga.get(1).patient().cancerStudy().cancerStudyIdentifier());
    assertNotNull(
        userDefinedSamplesForAccAndTcga.get(2).patient().cancerStudy().cancerStudyIdentifier());
  }

  private void assertUserDefinedGenieSampleIds(List<Sample> userDefinedSamplesForGenie) {
    assertEquals(2, userDefinedSamplesForGenie.size());
    var specificSampleIdsForGenie =
        userDefinedSamplesForGenie.stream().map(Sample::stableId).toList();
    assertTrue(specificSampleIdsForGenie.contains("GENIE-TEST-302-01"));
    assertTrue(specificSampleIdsForGenie.contains("GENIE-TEST-309-01"));
  }

  private void assertUserDefinedGenieSamplesSummary(List<Sample> userDefinedSamplesForGenie) {
    assertUserDefinedGenieSampleIds(userDefinedSamplesForGenie);
    assertEquals(SampleType.PRIMARY_SOLID_TUMOR, userDefinedSamplesForGenie.get(0).sampleType());
    assertEquals(SampleType.PRIMARY_SOLID_TUMOR, userDefinedSamplesForGenie.get(1).sampleType());
  }

  private void assertUserDefinedGenieSamplesDetailed(List<Sample> userDefinedSamplesForGenie) {
    assertUserDefinedGenieSamplesSummary(userDefinedSamplesForGenie);
    assertNotNull(
        userDefinedSamplesForGenie.get(0).patient().cancerStudy().cancerStudyIdentifier());
    assertNotNull(
        userDefinedSamplesForGenie.get(1).patient().cancerStudy().cancerStudyIdentifier());
  }

  private void assertKeywordFilteredSampleIds(
      List<Sample> keywordFilteredSamplesForAccAndTcgaAndGenie) {
    assertEquals(4, keywordFilteredSamplesForAccAndTcgaAndGenie.size());
    var keywordFilteredSamplesIds =
        keywordFilteredSamplesForAccAndTcgaAndGenie.stream().map(Sample::stableId).toList();
    assertTrue(keywordFilteredSamplesIds.contains("tcga-a1-a0sb-02"));
    assertTrue(keywordFilteredSamplesIds.contains("GENIE-TEST-321-02"));
    assertTrue(keywordFilteredSamplesIds.contains("GENIE-TEST-322-02"));
    assertTrue(keywordFilteredSamplesIds.contains("GENIE-TEST-323-02"));
  }

  private void assertKeywordFilteredSamplesSummary(
      List<Sample> keywordFilteredSamplesForAccAndTcgaAndGenie) {
    assertKeywordFilteredSampleIds(keywordFilteredSamplesForAccAndTcgaAndGenie);
    assertEquals(
        SampleType.PRIMARY_SOLID_TUMOR,
        keywordFilteredSamplesForAccAndTcgaAndGenie.get(0).sampleType());
    assertEquals(
        SampleType.PRIMARY_SOLID_TUMOR,
        keywordFilteredSamplesForAccAndTcgaAndGenie.get(1).sampleType());
    assertEquals(
        SampleType.PRIMARY_SOLID_TUMOR,
        keywordFilteredSamplesForAccAndTcgaAndGenie.get(2).sampleType());
    assertEquals(
        SampleType.PRIMARY_SOLID_TUMOR,
        keywordFilteredSamplesForAccAndTcgaAndGenie.get(3).sampleType());
  }

  private void assertKeywordFilteredSamplesDetailed(
      List<Sample> keywordFilteredSamplesForAccAndTcgaAndGenie) {
    assertKeywordFilteredSamplesSummary(keywordFilteredSamplesForAccAndTcgaAndGenie);
    assertNotNull(
        keywordFilteredSamplesForAccAndTcgaAndGenie
            .get(0)
            .patient()
            .cancerStudy()
            .cancerStudyIdentifier());
    assertNotNull(
        keywordFilteredSamplesForAccAndTcgaAndGenie
            .get(1)
            .patient()
            .cancerStudy()
            .cancerStudyIdentifier());
    assertNotNull(
        keywordFilteredSamplesForAccAndTcgaAndGenie
            .get(2)
            .patient()
            .cancerStudy()
            .cancerStudyIdentifier());
    assertNotNull(
        keywordFilteredSamplesForAccAndTcgaAndGenie
            .get(3)
            .patient()
            .cancerStudy()
            .cancerStudyIdentifier());
  }

  @Test
  public void getSamplesBySampleListIds() {
    var studyTcgaPubAll =
        mapper.getSamplesBySampleListIds(Collections.singletonList("study_tcga_pub_all"));
    assertEquals(14, studyTcgaPubAll.size());

    var studyTcgaPubAcgh =
        mapper.getSamplesBySampleListIds(Collections.singletonList("study_tcga_pub_acgh"));
    assertEquals(14, studyTcgaPubAcgh.size());

    var studyTcgaPubCnaseq =
        mapper.getSamplesBySampleListIds(Collections.singletonList("study_tcga_pub_cnaseq"));
    assertEquals(7, studyTcgaPubCnaseq.size());

    var studyTcgaPubComplete =
        mapper.getSamplesBySampleListIds(Collections.singletonList("study_tcga_pub_complete"));
    assertEquals(7, studyTcgaPubComplete.size());

    var studyTcgaPubLog2cna =
        mapper.getSamplesBySampleListIds(Collections.singletonList("study_tcga_pub_log2cna"));
    assertEquals(14, studyTcgaPubLog2cna.size());

    var studyTcgaPubMethylationHm27 =
        mapper.getSamplesBySampleListIds(
            Collections.singletonList("study_tcga_pub_methylation_hm27"));
    assertEquals(1, studyTcgaPubMethylationHm27.size());

    var studyTcgaPubMrna =
        mapper.getSamplesBySampleListIds(Collections.singletonList("study_tcga_pub_mrna"));
    assertEquals(8, studyTcgaPubMrna.size());

    var studyTcgaPubSequenced =
        mapper.getSamplesBySampleListIds(Collections.singletonList("study_tcga_pub_sequenced"));
    assertEquals(7, studyTcgaPubSequenced.size());

    var studyTcgaPubCna =
        mapper.getSamplesBySampleListIds(Collections.singletonList("study_tcga_pub_cna"));
    assertEquals(7, studyTcgaPubCna.size());

    var studyTcgaPubRnaSeqV2Mrna =
        mapper.getSamplesBySampleListIds(
            Collections.singletonList("study_tcga_pub_rna_seq_v2_mrna"));
    assertEquals(7, studyTcgaPubRnaSeqV2Mrna.size());

    var studyTcgaPubMicrorna =
        mapper.getSamplesBySampleListIds(Collections.singletonList("study_tcga_pub_microrna"));
    assertEquals(0, studyTcgaPubMicrorna.size());

    var studyTcgaPubRppa =
        mapper.getSamplesBySampleListIds(Collections.singletonList("study_tcga_pub_rppa"));
    assertEquals(0, studyTcgaPubRppa.size());

    var studyTcgaPub3wayComplete =
        mapper.getSamplesBySampleListIds(Collections.singletonList("study_tcga_pub_3way_complete"));
    assertEquals(7, studyTcgaPub3wayComplete.size());

    var accTcgaAll = mapper.getSamplesBySampleListIds(Collections.singletonList("acc_tcga_all"));
    assertEquals(1, accTcgaAll.size());
  }

  @Test
  public void getSummarySamplesBySampleListIds() {
    var studyTcgaPubAll =
        mapper.getSummarySamplesBySampleListIds(Collections.singletonList("study_tcga_pub_all"));
    assertEquals(14, studyTcgaPubAll.size());
    assertEquals(SampleType.PRIMARY_SOLID_TUMOR, studyTcgaPubAll.getFirst().sampleType());

    var studyTcgaPubAcgh =
        mapper.getSummarySamplesBySampleListIds(Collections.singletonList("study_tcga_pub_acgh"));
    assertEquals(14, studyTcgaPubAcgh.size());
    assertEquals(SampleType.PRIMARY_SOLID_TUMOR, studyTcgaPubAcgh.getFirst().sampleType());

    var studyTcgaPubCnaseq =
        mapper.getSummarySamplesBySampleListIds(Collections.singletonList("study_tcga_pub_cnaseq"));
    assertEquals(7, studyTcgaPubCnaseq.size());
    assertEquals(SampleType.PRIMARY_SOLID_TUMOR, studyTcgaPubCnaseq.getFirst().sampleType());

    var studyTcgaPubComplete =
        mapper.getSummarySamplesBySampleListIds(
            Collections.singletonList("study_tcga_pub_complete"));
    assertEquals(7, studyTcgaPubComplete.size());
    assertEquals(SampleType.PRIMARY_SOLID_TUMOR, studyTcgaPubComplete.getFirst().sampleType());

    var studyTcgaPubLog2cna =
        mapper.getSummarySamplesBySampleListIds(
            Collections.singletonList("study_tcga_pub_log2cna"));
    assertEquals(14, studyTcgaPubLog2cna.size());
    assertEquals(SampleType.PRIMARY_SOLID_TUMOR, studyTcgaPubLog2cna.getFirst().sampleType());

    var studyTcgaPubMethylationHm27 =
        mapper.getSummarySamplesBySampleListIds(
            Collections.singletonList("study_tcga_pub_methylation_hm27"));
    assertEquals(1, studyTcgaPubMethylationHm27.size());
    assertEquals(
        SampleType.PRIMARY_SOLID_TUMOR, studyTcgaPubMethylationHm27.getFirst().sampleType());

    var studyTcgaPubMrna =
        mapper.getSummarySamplesBySampleListIds(Collections.singletonList("study_tcga_pub_mrna"));
    assertEquals(8, studyTcgaPubMrna.size());
    assertEquals(SampleType.PRIMARY_SOLID_TUMOR, studyTcgaPubMrna.getFirst().sampleType());

    var studyTcgaPubSequenced =
        mapper.getSummarySamplesBySampleListIds(
            Collections.singletonList("study_tcga_pub_sequenced"));
    assertEquals(7, studyTcgaPubSequenced.size());
    assertEquals(SampleType.PRIMARY_SOLID_TUMOR, studyTcgaPubSequenced.getFirst().sampleType());

    var studyTcgaPubCna =
        mapper.getSummarySamplesBySampleListIds(Collections.singletonList("study_tcga_pub_cna"));
    assertEquals(7, studyTcgaPubCna.size());
    assertEquals(SampleType.PRIMARY_SOLID_TUMOR, studyTcgaPubCna.getFirst().sampleType());

    var studyTcgaPubRnaSeqV2Mrna =
        mapper.getSummarySamplesBySampleListIds(
            Collections.singletonList("study_tcga_pub_rna_seq_v2_mrna"));
    assertEquals(7, studyTcgaPubRnaSeqV2Mrna.size());
    assertEquals(SampleType.PRIMARY_SOLID_TUMOR, studyTcgaPubRnaSeqV2Mrna.getFirst().sampleType());

    var studyTcgaPub3wayComplete =
        mapper.getSummarySamplesBySampleListIds(
            Collections.singletonList("study_tcga_pub_3way_complete"));
    assertEquals(7, studyTcgaPub3wayComplete.size());
    assertEquals(SampleType.PRIMARY_SOLID_TUMOR, studyTcgaPub3wayComplete.getFirst().sampleType());

    var accTcgaAll =
        mapper.getSummarySamplesBySampleListIds(Collections.singletonList("acc_tcga_all"));
    assertEquals(1, accTcgaAll.size());
    assertEquals(SampleType.PRIMARY_SOLID_TUMOR, accTcgaAll.getFirst().sampleType());
  }

  @Test
  public void getDetailedSamplesBySampleListIds() {
    var studyTcgaPubAll =
        mapper.getDetailedSamplesBySampleListIds(Collections.singletonList("study_tcga_pub_all"));
    assertEquals(14, studyTcgaPubAll.size());

    var studyTcgaPubAcgh =
        mapper.getDetailedSamplesBySampleListIds(Collections.singletonList("study_tcga_pub_acgh"));
    assertEquals(14, studyTcgaPubAcgh.size());

    var studyTcgaPubCnaseq =
        mapper.getDetailedSamplesBySampleListIds(
            Collections.singletonList("study_tcga_pub_cnaseq"));
    assertEquals(7, studyTcgaPubCnaseq.size());

    var studyTcgaPubComplete =
        mapper.getDetailedSamplesBySampleListIds(
            Collections.singletonList("study_tcga_pub_complete"));
    assertEquals(7, studyTcgaPubComplete.size());

    var studyTcgaPubLog2cna =
        mapper.getDetailedSamplesBySampleListIds(
            Collections.singletonList("study_tcga_pub_log2cna"));
    assertEquals(14, studyTcgaPubLog2cna.size());

    var studyTcgaPubMethylationHm27 =
        mapper.getDetailedSamplesBySampleListIds(
            Collections.singletonList("study_tcga_pub_methylation_hm27"));
    assertEquals(1, studyTcgaPubMethylationHm27.size());

    var studyTcgaPubMrna =
        mapper.getDetailedSamplesBySampleListIds(Collections.singletonList("study_tcga_pub_mrna"));
    assertEquals(8, studyTcgaPubMrna.size());

    var studyTcgaPubSequenced =
        mapper.getDetailedSamplesBySampleListIds(
            Collections.singletonList("study_tcga_pub_sequenced"));
    assertEquals(7, studyTcgaPubSequenced.size());
    // all samples should be sequenced
    assertEquals(7, studyTcgaPubSequenced.stream().filter(Sample::sequenced).count());

    var studyTcgaPubCna =
        mapper.getDetailedSamplesBySampleListIds(Collections.singletonList("study_tcga_pub_cna"));
    assertEquals(7, studyTcgaPubCna.size());
    // only 2 samples should have copy number segments
    assertEquals(2, studyTcgaPubCna.stream().filter(Sample::copyNumberSegmentPresent).count());
    assertTrue(studyTcgaPubCna.get(0).copyNumberSegmentPresent());
    assertEquals("tcga-a1-a0sd", studyTcgaPubCna.get(0).patient().stableId());
    assertTrue(studyTcgaPubCna.get(1).copyNumberSegmentPresent());
    assertEquals("tcga-a1-a0se", studyTcgaPubCna.get(1).patient().stableId());

    var studyTcgaPubRnaSeqV2Mrna =
        mapper.getDetailedSamplesBySampleListIds(
            Collections.singletonList("study_tcga_pub_rna_seq_v2_mrna"));
    assertEquals(7, studyTcgaPubRnaSeqV2Mrna.size());

    var studyTcgaPubMicrorna =
        mapper.getDetailedSamplesBySampleListIds(
            Collections.singletonList("study_tcga_pub_microrna"));
    assertEquals(0, studyTcgaPubMicrorna.size());

    var studyTcgaPubRppa =
        mapper.getDetailedSamplesBySampleListIds(Collections.singletonList("study_tcga_pub_rppa"));
    assertEquals(0, studyTcgaPubRppa.size());

    var studyTcgaPub3wayComplete =
        mapper.getDetailedSamplesBySampleListIds(
            Collections.singletonList("study_tcga_pub_3way_complete"));
    assertEquals(7, studyTcgaPub3wayComplete.size());

    var accTcgaAll =
        mapper.getDetailedSamplesBySampleListIds(Collections.singletonList("acc_tcga_all"));
    assertEquals(1, accTcgaAll.size());
  }

  @Test
  public void getSample() {
    var sample1 = mapper.getSample(STUDY_TCGA_PUB, "tcga-a1-a0sb-01");
    assertEquals("tcga-a1-a0sb-01", sample1.stableId());
    assertFalse(sample1.sequenced());
    assertTrue(sample1.copyNumberSegmentPresent());
    assertEquals(1, sample1.patientId().intValue());
    assertEquals(1, sample1.patient().internalId().intValue());
    assertEquals(1, sample1.patient().cancerStudyId().intValue());
    assertEquals(1, sample1.patient().cancerStudy().cancerStudyId().intValue());
    assertEquals(STUDY_TCGA_PUB, sample1.patient().cancerStudyIdentifier());
    assertEquals(STUDY_TCGA_PUB, sample1.patient().cancerStudy().cancerStudyIdentifier());

    var sample2 = mapper.getSample(STUDY_ACC_TCGA, "tcga-a1-a0sb-01");
    assertEquals("tcga-a1-a0sb-01", sample2.stableId());
    assertFalse(sample2.sequenced());
    assertFalse(sample2.copyNumberSegmentPresent());
    assertEquals(18, sample2.patientId().intValue());
    assertEquals(18, sample2.patient().internalId().intValue());
    assertEquals(2, sample2.patient().cancerStudyId().intValue());
    assertEquals(2, sample2.patient().cancerStudy().cancerStudyId().intValue());
    assertEquals(STUDY_ACC_TCGA, sample2.patient().cancerStudyIdentifier());
    assertEquals(STUDY_ACC_TCGA, sample2.patient().cancerStudy().cancerStudyIdentifier());

    var sample3 = mapper.getSample(STUDY_TCGA_PUB, "tcga-a1-a0sd-01");
    assertTrue(sample3.sequenced());
  }

  private DataFilterValue newDataFilterValue(Double start, Double end, String value) {
    DataFilterValue dataFilterValue = new DataFilterValue();

    dataFilterValue.setStart(start == null ? null : new BigDecimal(start));
    dataFilterValue.setEnd(end == null ? null : new BigDecimal(end));
    dataFilterValue.setValue(value);

    return dataFilterValue;
  }

  private ClinicalDataFilter newClinicalDataFilter(
      String attributeId, List<DataFilterValue> values) {
    ClinicalDataFilter clinicalDataFilter = new ClinicalDataFilter();

    clinicalDataFilter.setAttributeId(attributeId);
    clinicalDataFilter.setValues(values);

    return clinicalDataFilter;
  }
}
