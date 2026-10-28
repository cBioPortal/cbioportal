package org.cbioportal.infrastructure.repository.clickhouse.ascn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.cbioportal.domain.ascn.AscnSegment;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
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
public class ClickhouseAscnSegmentMapperTest {
  private static final String STUDY = "study_tcga_pub";

  @Autowired private ClickhouseAscnSegmentMapper mapper;

  @Test
  public void getAscnSegmentsInStudy() {
    List<AscnSegment> segments = mapper.getAscnSegments(List.of(STUDY), null, null, null);
    assertEquals(3, segments.size());
    AscnSegment first = segments.get(0);
    assertEquals(Long.valueOf(1), first.segId());
    assertEquals(Long.valueOf(1), first.cancerStudyId());
    assertEquals(Long.valueOf(1), first.sampleId());
    assertEquals(Integer.valueOf(3), first.tcn());
    assertEquals(Integer.valueOf(1), first.lcn());
    assertEquals(Double.valueOf(2.1), first.ploidy());
    assertEquals(Double.valueOf(0.1), first.dipLogR());
  }

  @Test
  public void getAscnSegmentsPaginated() {
    List<AscnSegment> segments = mapper.getAscnSegments(List.of(STUDY), null, 2, 2);
    assertEquals(1, segments.size());
    assertEquals(Long.valueOf(3), segments.get(0).segId());
  }

  @Test
  public void getAscnSegmentsForMultipleSamples() {
    List<AscnSegment> segments =
        mapper.getAscnSegments(
            List.of(STUDY), List.of("tcga-a1-a0sb-01", "tcga-a1-a0sd-01"), null, null);
    assertEquals(3, segments.size());
  }

  @Test
  public void getAscnSegmentsForSamplesAcrossStudies() {
    List<AscnSegment> segments =
        mapper.getAscnSegments(
            List.of(STUDY, "acc_tcga"), List.of("tcga-a1-a0sb-01", "tcga-a1-a0sb-01"), null, null);
    assertEquals(2, segments.size());
  }

  @Test
  public void getAscnSegmentsMatchesStudiesAndSamplesIndependently() {
    List<AscnSegment> segments =
        mapper.getAscnSegments(
            List.of(STUDY, "acc_tcga"), List.of("tcga-a1-a0sb-01", "tcga-a1-a0sd-01"), null, null);
    assertEquals(3, segments.size());
  }

  @Test
  public void getAscnSegmentsFilteredBySample() {
    List<AscnSegment> segments =
        mapper.getAscnSegments(List.of(STUDY), List.of("tcga-a1-a0sd-01"), null, null);
    assertEquals(1, segments.size());
    assertEquals(Long.valueOf(3), segments.get(0).segId());
  }

  @Test
  public void getAscnSegmentsDoesNotLeakOtherStudies() {
    assertTrue(mapper.getAscnSegments(List.of("acc_tcga"), null, null, null).isEmpty());
    assertTrue(
        mapper
            .getAscnSegments(List.of("acc_tcga"), List.of("tcga-a1-a0sb-01"), null, null)
            .isEmpty());
  }

  @Test
  public void getMetaAscnSegments() {
    assertEquals(
        Integer.valueOf(3), mapper.getMetaAscnSegments(List.of(STUDY), null).getTotalCount());
    assertEquals(
        Integer.valueOf(2),
        mapper.getMetaAscnSegments(List.of(STUDY), List.of("tcga-a1-a0sb-01")).getTotalCount());
  }
}
