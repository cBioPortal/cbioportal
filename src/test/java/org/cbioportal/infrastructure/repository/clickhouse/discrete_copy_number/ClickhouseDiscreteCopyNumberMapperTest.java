package org.cbioportal.infrastructure.repository.clickhouse.discrete_copy_number;

import static org.junit.Assert.*;

import java.util.List;
import java.util.stream.Collectors;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.cbioportal.legacy.model.DiscreteCopyNumberData;
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
public class ClickhouseDiscreteCopyNumberMapperTest {

  // Events of profile study_tcga_pub_gistic: tcga-a1-a0sb-01 has AKT1 (207) -2 and AKT2 (208) +2,
  // tcga-a1-a0sd-01 has AKT1 (207) +2.

  private static final String PROFILE = "study_tcga_pub_gistic";
  private static final String SAMPLE_LIST = "study_tcga_pub_all";
  private static final List<Integer> HOMDEL_AND_AMP = List.of(-2, 2);

  @Autowired private ClickhouseDiscreteCopyNumberMapper mapper;

  @Test
  public void fetchesEventsOfASampleListOrderedByGeneAlterationAndSample() {
    List<DiscreteCopyNumberData> result =
        mapper.getDiscreteCopyNumbers(PROFILE, SAMPLE_LIST, null, null, HOMDEL_AND_AMP, false);

    assertEquals(
        List.of("207/-2/tcga-a1-a0sb-01", "207/2/tcga-a1-a0sd-01", "208/2/tcga-a1-a0sb-01"),
        result.stream()
            .map(d -> d.getEntrezGeneId() + "/" + d.getAlteration() + "/" + d.getSampleId())
            .collect(Collectors.toList()));
    DiscreteCopyNumberData first = result.get(0);
    assertEquals(PROFILE, first.getMolecularProfileId());
    assertEquals("study_tcga_pub", first.getStudyId());
    assertEquals("tcga-a1-a0sb", first.getPatientId());
    assertNull("gene is only populated for DETAILED", first.getGene());
  }

  @Test
  public void alterationTypeSelectsHomdelOrAmp() {
    assertEquals(
        1,
        mapper.getDiscreteCopyNumbers(PROFILE, SAMPLE_LIST, null, null, List.of(-2), false).size());
    assertEquals(
        2,
        mapper.getDiscreteCopyNumbers(PROFILE, SAMPLE_LIST, null, null, List.of(2), false).size());
  }

  @Test
  public void fetchesBySampleIdsAndGenes() {
    List<DiscreteCopyNumberData> bySample =
        mapper.getDiscreteCopyNumbers(
            PROFILE, null, List.of("tcga-a1-a0sd-01"), null, HOMDEL_AND_AMP, false);
    assertEquals(1, bySample.size());
    assertEquals(Integer.valueOf(207), bySample.get(0).getEntrezGeneId());

    List<DiscreteCopyNumberData> byGene =
        mapper.getDiscreteCopyNumbers(
            PROFILE, SAMPLE_LIST, null, List.of(208), HOMDEL_AND_AMP, false);
    assertEquals(1, byGene.size());
    assertEquals("tcga-a1-a0sb-01", byGene.get(0).getSampleId());
  }

  @Test
  public void detailedProjectionPopulatesGene() {
    List<DiscreteCopyNumberData> result =
        mapper.getDiscreteCopyNumbers(
            PROFILE, SAMPLE_LIST, null, List.of(207), HOMDEL_AND_AMP, true);

    assertEquals(2, result.size());
    for (DiscreteCopyNumberData data : result) {
      assertNotNull(data.getGene());
      assertEquals("AKT1", data.getGene().getHugoGeneSymbol());
      assertEquals(Integer.valueOf(207), data.getGene().getEntrezGeneId());
    }
  }

  @Test
  public void countMatchesTheFetchedEvents() {
    assertEquals(
        3, mapper.countDiscreteCopyNumbers(PROFILE, SAMPLE_LIST, null, null, HOMDEL_AND_AMP));
    assertEquals(
        1,
        mapper.countDiscreteCopyNumbers(
            PROFILE, null, List.of("tcga-a1-a0sd-01"), null, HOMDEL_AND_AMP));
  }

  @Test
  public void unknownSampleListProfileOrOtherProfileReturnNothing() {
    assertTrue(
        mapper
            .getDiscreteCopyNumbers(PROFILE, "no_such_list", null, null, HOMDEL_AND_AMP, false)
            .isEmpty());
    assertTrue(
        mapper
            .getDiscreteCopyNumbers(
                "no_such_profile", SAMPLE_LIST, null, null, HOMDEL_AND_AMP, false)
            .isEmpty());
    assertTrue(
        mapper
            .getDiscreteCopyNumbers(
                "study_tcga_pub_cna", SAMPLE_LIST, null, null, HOMDEL_AND_AMP, false)
            .isEmpty());
    assertEquals(
        0, mapper.countDiscreteCopyNumbers(PROFILE, "no_such_list", null, null, HOMDEL_AND_AMP));
  }
}
