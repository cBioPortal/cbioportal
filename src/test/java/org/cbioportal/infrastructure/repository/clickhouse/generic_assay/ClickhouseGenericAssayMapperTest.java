package org.cbioportal.infrastructure.repository.clickhouse.generic_assay;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.cbioportal.domain.studyview.StudyViewFilterFactory;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.cbioportal.legacy.model.GenericAssayDataCount;
import org.cbioportal.legacy.model.GenericAssayDataCountItem;
import org.cbioportal.legacy.model.meta.GenericAssayMeta;
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

  @Autowired private ClickhouseGenericAssayMapper mapper;

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
}
