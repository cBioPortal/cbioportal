package org.cbioportal.infrastructure.repository.clickhouse.generic_assay;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.cbioportal.legacy.model.GenericAssayData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@Import(MyBatisConfig.class)
@JdbcTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class ClickhouseGenericAssayDataMapperTest {

  // acc_tcga_armlevel_cna: samples tcga-a1-b0so-01, -b0sp-01, -b0sq-01 and tcga-a1-a0sb-01 (in that
  // order) with
  //   1p_status:  Loss, Gain, Unchanged, NA       2p_status:  Loss, Unchanged, Unchanged, NA
  //   9p_status:  Loss, Gain, Unchanged, Unchanged 10p_status: Gain, Unchanged, Unchanged, NA
  // NA values are not returned.

  private static final String STUDY = "acc_tcga";
  private static final String PROFILE = "acc_tcga_armlevel_cna";
  private static final String TYPE = "armlevel_cna";

  @Autowired private ClickhouseGenericAssayDataMapper mapper;

  @Test
  public void returnsAllNonNaValuesOfTheSelectedSamples() {
    List<GenericAssayData> result =
        mapper.getGenericAssayData(STUDY, PROFILE, TYPE, uniqueIds(allSamples()), null);

    assertEquals(13, result.size());
    assertTrue(result.stream().noneMatch(d -> d.getValue().equals("NA") || d.getValue().isEmpty()));
    GenericAssayData first =
        result.stream()
            .filter(
                d ->
                    d.getSampleId().equals("tcga-a1-b0so-01")
                        && d.getGenericAssayStableId().equals("1p_status"))
            .findFirst()
            .orElseThrow();
    assertEquals("Loss", first.getValue());
    assertEquals(PROFILE, first.getMolecularProfileId());
    assertEquals(STUDY, first.getStudyId());
    assertEquals("tcga-a1-b0so", first.getPatientId());
    assertFalse(first.getPatientLevel());
  }

  @Test
  public void entitiesRestrictTheResult() {
    List<GenericAssayData> result =
        mapper.getGenericAssayData(
            STUDY, PROFILE, TYPE, uniqueIds(allSamples()), List.of("1p_status"));

    Map<String, String> bySample =
        result.stream()
            .collect(Collectors.toMap(GenericAssayData::getSampleId, GenericAssayData::getValue));
    assertEquals(
        Map.of(
            "tcga-a1-b0so-01", "Loss", "tcga-a1-b0sp-01", "Gain", "tcga-a1-b0sq-01", "Unchanged"),
        bySample);
  }

  @Test
  public void samplesAreRestrictedToTheSelectedOnes() {
    List<GenericAssayData> result =
        mapper.getGenericAssayData(
            STUDY, PROFILE, TYPE, new String[] {STUDY + "_tcga-a1-b0so-01"}, null);

    assertEquals(4, result.size());
    assertTrue(result.stream().allMatch(d -> d.getSampleId().equals("tcga-a1-b0so-01")));
  }

  @Test
  public void samplesOfAnotherStudyUnknownProfileOrEntityReturnNothing() {
    assertTrue(
        mapper
            .getGenericAssayData(
                STUDY, PROFILE, TYPE, new String[] {"study_tcga_pub_tcga-a1-a0sb-01"}, null)
            .isEmpty());
    assertTrue(
        mapper
            .getGenericAssayData(STUDY, "acc_tcga_nope", "nope", uniqueIds(allSamples()), null)
            .isEmpty());
    assertTrue(
        mapper
            .getGenericAssayData(
                STUDY, PROFILE, TYPE, new String[] {"acc_tcga_no_such_sample"}, null)
            .isEmpty());
    assertTrue(
        mapper
            .getGenericAssayData(
                STUDY, PROFILE, TYPE, uniqueIds(allSamples()), List.of("no_such_entity"))
            .isEmpty());
  }

  @Test
  public void numericValuesAreRoundedLikeGeneLevelData() {
    // study_tcga_pub_treatment_ic50: tcga-a1-a0sb-01 has -0.0670 for the first entity
    List<GenericAssayData> result =
        mapper.getGenericAssayData(
            "study_tcga_pub",
            "study_tcga_pub_treatment_ic50",
            "treatment_ic50",
            new String[] {"study_tcga_pub_tcga-a1-a0sb-01"},
            null);

    assertFalse(result.isEmpty());
    assertTrue(result.stream().anyMatch(d -> d.getValue().equals("-0.067")));
    assertTrue(result.stream().noneMatch(d -> d.getValue().equals("-0.0670")));
  }

  private List<String> allSamples() {
    return List.of("tcga-a1-b0so-01", "tcga-a1-b0sp-01", "tcga-a1-b0sq-01", "tcga-a1-a0sb-01");
  }

  private String[] uniqueIds(List<String> sampleIds) {
    return sampleIds.stream().map(id -> STUDY + "_" + id).toArray(String[]::new);
  }
}
