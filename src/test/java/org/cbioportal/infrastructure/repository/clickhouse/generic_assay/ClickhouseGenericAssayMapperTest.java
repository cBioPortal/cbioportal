package org.cbioportal.infrastructure.repository.clickhouse.generic_assay;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.cbioportal.domain.studyview.StudyViewFilterFactory;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.model.GenericAssayDataCount;
import org.cbioportal.legacy.model.GenericAssayDataCountItem;
import org.cbioportal.legacy.model.meta.GenericAssayMeta;
import org.cbioportal.legacy.web.parameter.GenericAssayDataBinFilter;
import org.cbioportal.legacy.web.parameter.GenericAssayDataFilter;
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
public class ClickhouseGenericAssayMapperTest {

  private static final String ACC_TCGA = "acc_tcga";
  private static final String STUDY_GENIE_PUB = "study_genie_pub";
  private static final String ACC_TCGA_ARMLEVEL_CNA_PROFILE = "acc_tcga_armlevel_cna";
  private static final String SAMPLE_LEVEL_ASSAY_PROFILE_TYPE = "sample_level_assay";

  @Autowired private ClickhouseGenericAssayMapper mapper;

  @Test
  public void getSampleCategoricalGenericAssayDataBinCounts() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(ACC_TCGA));

    GenericAssayDataBinFilter genericAssayDataBinFilter = new GenericAssayDataBinFilter();
    genericAssayDataBinFilter.setStableId("1p_status");
    genericAssayDataBinFilter.setProfileType("armlevel_cna");

    List<ClinicalDataCount> actualCounts =
        mapper.getGenericAssayDataBinCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            List.of(genericAssayDataBinFilter));

    ClinicalDataCount expectedLoss = new ClinicalDataCount();
    expectedLoss.setAttributeId("1p_statusarmlevel_cna");
    expectedLoss.setValue("Loss");
    expectedLoss.setCount(1);
    ClinicalDataCount expectedGain = new ClinicalDataCount();
    expectedGain.setAttributeId("1p_statusarmlevel_cna");
    expectedGain.setValue("Gain");
    expectedGain.setCount(1);
    ClinicalDataCount expectedUnchanged = new ClinicalDataCount();
    expectedUnchanged.setAttributeId("1p_statusarmlevel_cna");
    expectedUnchanged.setValue("Unchanged");
    expectedUnchanged.setCount(1);
    ClinicalDataCount expectedNa = new ClinicalDataCount();
    expectedNa.setAttributeId("1p_statusarmlevel_cna");
    expectedNa.setValue("NA");
    expectedNa.setCount(1);

    assertThat(actualCounts)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .isEqualTo(List.of(expectedLoss, expectedGain, expectedUnchanged, expectedNa));
  }

  @Test
  public void getPatientCategoricalGenericAssayDataBinCounts() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_GENIE_PUB));

    GenericAssayDataBinFilter genericAssayDataBinFilter = new GenericAssayDataBinFilter();
    genericAssayDataBinFilter.setStableId("DMETS_DX_ADRENAL");
    genericAssayDataBinFilter.setProfileType("distant_mets");

    List<ClinicalDataCount> actualCounts =
        mapper.getGenericAssayDataBinCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            List.of(genericAssayDataBinFilter));

    ClinicalDataCount expectedNo = new ClinicalDataCount();
    expectedNo.setAttributeId("DMETS_DX_ADRENALdistant_mets");
    expectedNo.setValue("No");
    expectedNo.setCount(9);
    ClinicalDataCount expectedYes = new ClinicalDataCount();
    expectedYes.setAttributeId("DMETS_DX_ADRENALdistant_mets");
    expectedYes.setValue("Yes");
    expectedYes.setCount(1);
    ClinicalDataCount expectedNa = new ClinicalDataCount();
    expectedNa.setAttributeId("DMETS_DX_ADRENALdistant_mets");
    expectedNa.setValue("NA");
    expectedNa.setCount(14);

    assertThat(actualCounts)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .isEqualTo(List.of(expectedNo, expectedYes, expectedNa));
  }

  @Test
  public void getSampleCategoricalGenericAssayDataCounts() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(ACC_TCGA));

    GenericAssayDataFilter genericAssayDataFilter =
        new GenericAssayDataFilter("1p_status", "armlevel_cna");
    List<GenericAssayDataCountItem> actualCounts =
        mapper.getGenericAssayDataCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            List.of(genericAssayDataFilter));

    List<GenericAssayDataCountItem> expectedCounts =
        List.of(
            new GenericAssayDataCountItem(
                "1p_status",
                List.of(
                    new GenericAssayDataCount("Loss", 1),
                    new GenericAssayDataCount("Gain", 1),
                    new GenericAssayDataCount("Unchanged", 1),
                    new GenericAssayDataCount("NA", 1))));

    assertThat(actualCounts)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .isEqualTo(expectedCounts);
  }

  @Test
  public void getPatientCategoricalGenericAssayDataCounts() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_GENIE_PUB));

    GenericAssayDataFilter genericAssayDataFilter =
        new GenericAssayDataFilter("DMETS_DX_ADRENAL", "distant_mets");
    List<GenericAssayDataCountItem> actualCounts =
        mapper.getGenericAssayDataCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            List.of(genericAssayDataFilter));

    List<GenericAssayDataCountItem> expectedCounts =
        List.of(
            new GenericAssayDataCountItem(
                "DMETS_DX_ADRENAL",
                List.of(
                    new GenericAssayDataCount("No", 9),
                    new GenericAssayDataCount("Yes", 1),
                    new GenericAssayDataCount("NA", 14))));

    assertThat(actualCounts)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .isEqualTo(expectedCounts);
  }

  @Test
  public void getSampleCategoricalGenericAssayDataCounts_countsDistinctSamples() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_GENIE_PUB));

    GenericAssayDataFilter genericAssayDataFilter =
        new GenericAssayDataFilter("sample_dup_status", SAMPLE_LEVEL_ASSAY_PROFILE_TYPE);
    List<GenericAssayDataCountItem> actualCounts =
        mapper.getGenericAssayDataCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            List.of(genericAssayDataFilter));

    List<GenericAssayDataCountItem> expectedCounts =
        List.of(
            new GenericAssayDataCountItem(
                "sample_dup_status",
                List.of(
                    new GenericAssayDataCount("Loss", 2),
                    new GenericAssayDataCount("Gain", 2),
                    new GenericAssayDataCount("NA", 23))));

    assertThat(actualCounts)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .isEqualTo(expectedCounts);
  }

  @Test
  public void getSampleCategoricalGenericAssayDataCountsByProfileType() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(ACC_TCGA));

    List<GenericAssayDataCountItem> actualCounts =
        mapper.getGenericAssayDataCountsByProfileType(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            "armlevel_cna");

    List<GenericAssayDataCountItem> expectedCounts =
        List.of(
            new GenericAssayDataCountItem(
                "1p_status",
                List.of(
                    new GenericAssayDataCount("Loss", 1),
                    new GenericAssayDataCount("Gain", 1),
                    new GenericAssayDataCount("Unchanged", 1),
                    new GenericAssayDataCount("NA", 1))),
            new GenericAssayDataCountItem(
                "2p_status",
                List.of(
                    new GenericAssayDataCount("Loss", 1),
                    new GenericAssayDataCount("Unchanged", 2),
                    new GenericAssayDataCount("NA", 1))),
            new GenericAssayDataCountItem(
                "9p_status",
                List.of(
                    new GenericAssayDataCount("Loss", 1),
                    new GenericAssayDataCount("Gain", 1),
                    new GenericAssayDataCount("Unchanged", 2))),
            new GenericAssayDataCountItem(
                "10p_status",
                List.of(
                    new GenericAssayDataCount("Gain", 1),
                    new GenericAssayDataCount("Unchanged", 2),
                    new GenericAssayDataCount("NA", 1))));

    assertThat(actualCounts)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .isEqualTo(expectedCounts);
  }

  @Test
  public void
      getSampleCategoricalGenericAssayDataCountsByProfileType_ignoresStudiesWithoutProfile() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(ACC_TCGA, STUDY_GENIE_PUB));

    List<GenericAssayDataCountItem> actualCounts =
        mapper.getGenericAssayDataCountsByProfileType(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            "armlevel_cna");

    List<GenericAssayDataCountItem> expectedCounts =
        List.of(
            new GenericAssayDataCountItem(
                "1p_status",
                List.of(
                    new GenericAssayDataCount("Loss", 1),
                    new GenericAssayDataCount("Gain", 1),
                    new GenericAssayDataCount("Unchanged", 1),
                    new GenericAssayDataCount("NA", 1))),
            new GenericAssayDataCountItem(
                "2p_status",
                List.of(
                    new GenericAssayDataCount("Loss", 1),
                    new GenericAssayDataCount("Unchanged", 2),
                    new GenericAssayDataCount("NA", 1))),
            new GenericAssayDataCountItem(
                "9p_status",
                List.of(
                    new GenericAssayDataCount("Loss", 1),
                    new GenericAssayDataCount("Gain", 1),
                    new GenericAssayDataCount("Unchanged", 2))),
            new GenericAssayDataCountItem(
                "10p_status",
                List.of(
                    new GenericAssayDataCount("Gain", 1),
                    new GenericAssayDataCount("Unchanged", 2),
                    new GenericAssayDataCount("NA", 1))));

    assertThat(actualCounts)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .isEqualTo(expectedCounts);
  }

  @Test
  public void getSampleCategoricalGenericAssayDataCountsByProfileType_countsDistinctSamples() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_GENIE_PUB));

    List<GenericAssayDataCountItem> actualCounts =
        mapper.getGenericAssayDataCountsByProfileType(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            SAMPLE_LEVEL_ASSAY_PROFILE_TYPE);

    List<GenericAssayDataCountItem> expectedCounts =
        List.of(
            new GenericAssayDataCountItem(
                "sample_dup_status",
                List.of(
                    new GenericAssayDataCount("Loss", 2), new GenericAssayDataCount("Gain", 2))));

    assertThat(actualCounts)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .isEqualTo(expectedCounts);
  }

  @Test
  public void getPatientCategoricalGenericAssayDataCountsByProfileType() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_GENIE_PUB));

    List<GenericAssayDataCountItem> actualCounts =
        mapper.getGenericAssayDataCountsByProfileType(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            "distant_mets");

    List<GenericAssayDataCountItem> expectedCounts =
        List.of(
            new GenericAssayDataCountItem(
                "DMETS_DX_ADRENAL",
                List.of(
                    new GenericAssayDataCount("No", 9),
                    new GenericAssayDataCount("Yes", 1),
                    new GenericAssayDataCount("NA", 14))));

    assertThat(actualCounts)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .isEqualTo(expectedCounts);
  }

  @Test
  public void getGenericAssayStableIdsByProfileIds_returnsEntityStableIds() {
    List<String> stableIds =
        mapper.getGenericAssayStableIdsByProfileIds(List.of(ACC_TCGA_ARMLEVEL_CNA_PROFILE));

    assertThat(stableIds).isNotEmpty().contains("1p_status");
  }

  @Test
  public void getGenericAssayStableIdsByProfileIds_sortsNumerically() {
    // Lexicographic order would be: 10p_status, 1p_status, 2p_status, 9p_status
    // Numeric-aware order should be: 1p_status, 2p_status, 9p_status, 10p_status
    List<String> stableIds =
        mapper.getGenericAssayStableIdsByProfileIds(List.of(ACC_TCGA_ARMLEVEL_CNA_PROFILE));

    assertThat(stableIds).containsExactly("1p_status", "2p_status", "9p_status", "10p_status");
  }

  @Test
  public void getGenericAssayMetaByStableIds_returnsMetaWithProperties() {
    List<GenericAssayMeta> result = mapper.getGenericAssayMetaByStableIds(List.of("1p_status"));

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getStableId()).isEqualTo("1p_status");
    assertThat(result.get(0).getEntityType()).isEqualTo("GENERIC_ASSAY");
    assertThat(result.get(0).getGenericEntityMetaProperties()).isNotNull();
  }

  @Test
  public void getGenericAssayMetaByStableIds_sortsNumerically() {
    // Input in reverse/lexicographic order; result should be numerically ordered
    // Lexicographic order would be: 10p_status, 1p_status, 2p_status, 9p_status
    // Numeric-aware order should be: 1p_status, 2p_status, 9p_status, 10p_status
    List<GenericAssayMeta> result =
        mapper.getGenericAssayMetaByStableIds(
            List.of("10p_status", "9p_status", "2p_status", "1p_status"));

    assertThat(result)
        .extracting(GenericAssayMeta::getStableId)
        .containsExactly("1p_status", "2p_status", "9p_status", "10p_status");
  }

  @Test
  public void getGenericAssayMetaByProfileIds_noStableIdsFilter_returnsAllEntities() {
    List<GenericAssayMeta> result =
        mapper.getGenericAssayMetaByProfileIds(List.of(ACC_TCGA_ARMLEVEL_CNA_PROFILE), null);

    assertThat(result)
        .isNotEmpty()
        .allMatch(m -> m.getEntityType() != null)
        .allMatch(m -> m.getGenericEntityMetaProperties() != null)
        .extracting(GenericAssayMeta::getStableId)
        .contains("1p_status");
  }

  @Test
  public void getGenericAssayMetaByProfileIds_sortsNumerically() {
    // Lexicographic order would be: 10p_status, 1p_status, 2p_status, 9p_status
    // Numeric-aware order should be: 1p_status, 2p_status, 9p_status, 10p_status
    List<GenericAssayMeta> result =
        mapper.getGenericAssayMetaByProfileIds(List.of(ACC_TCGA_ARMLEVEL_CNA_PROFILE), null);

    assertThat(result)
        .extracting(GenericAssayMeta::getStableId)
        .containsExactly("1p_status", "2p_status", "9p_status", "10p_status");
  }

  @Test
  public void getGenericAssayMetaByProfileIds_withStableIdsFilter_returnsOnlyMatchingEntities() {
    List<GenericAssayMeta> result =
        mapper.getGenericAssayMetaByProfileIds(
            List.of(ACC_TCGA_ARMLEVEL_CNA_PROFILE), List.of("1p_status"));

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getStableId()).isEqualTo("1p_status");
  }

  @Test
  public void getGenericAssayMetaByProfileIds_withSearchTermAndPaging_returnsMatchingPage() {
    List<GenericAssayMeta> result =
        mapper.getGenericAssayMetaByProfileIds(
            List.of(ACC_TCGA_ARMLEVEL_CNA_PROFILE), null, "status", 2, 1);

    assertThat(result)
        .extracting(GenericAssayMeta::getStableId)
        .containsExactly("2p_status", "9p_status");
  }

  @Test
  public void countGenericAssayMetaByProfileIds_withSearchTerm_returnsTotalMatches() {
    Integer result =
        mapper.countGenericAssayMetaByProfileIds(
            List.of(ACC_TCGA_ARMLEVEL_CNA_PROFILE), null, "status");

    assertThat(result).isEqualTo(4);
  }
}
