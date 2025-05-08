package org.cbioportal.infrastructure.repository.clickhouse.sample;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.cbioportal.domain.sample.Sample;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.cbioportal.shared.enums.ProjectionType;
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
public class ClickhouseSampleRepositoryTest {

  private static final String STUDY_TCGA_PUB = "study_tcga_pub";

  private ClickhouseSampleRepository repository;

  @Autowired
  public void configure(ClickhouseSampleMapper mapper) {
    this.repository = new ClickhouseSampleRepository(mapper);
  }

  @Test
  public void getAllSamplesInStudy() {
    var sortedByStableIdDesc = getAllTcgaPubSamplesPaginatedAndSorted("stableId", "DESC");
    assertEquals(6, sortedByStableIdDesc.get(0).size());
    assertEquals("tcga-a1-a0sq-01", sortedByStableIdDesc.get(0).getFirst().stableId());
    assertEquals(6, sortedByStableIdDesc.get(1).size());
    assertEquals("tcga-a1-a0sj-01", sortedByStableIdDesc.get(1).getFirst().stableId());
    assertEquals(3, sortedByStableIdDesc.get(2).size());
    assertEquals("tcga-a1-a0sd-01", sortedByStableIdDesc.get(2).getFirst().stableId());

    var sortedByStableIdAsc = getAllTcgaPubSamplesPaginatedAndSorted("stableId", "ASC");
    assertEquals(6, sortedByStableIdAsc.get(0).size());
    assertEquals("tcga-a1-a0sb-01", sortedByStableIdAsc.get(0).getFirst().stableId());
    assertEquals(6, sortedByStableIdAsc.get(1).size());
    assertEquals("tcga-a1-a0sh-01", sortedByStableIdAsc.get(1).getFirst().stableId());
    assertEquals(3, sortedByStableIdAsc.get(2).size());
    assertEquals("tcga-a1-a0so-01", sortedByStableIdAsc.get(2).getFirst().stableId());
  }

  private List<List<Sample>> getAllTcgaPubSamplesPaginatedAndSorted(
      String sortBy, String direction) {
    var pages = new ArrayList<List<Sample>>();

    pages.add(
        repository.getAllSamplesInStudy(
            STUDY_TCGA_PUB, ProjectionType.DETAILED, 6, 0, sortBy, direction));

    pages.add(
        repository.getAllSamplesInStudy(
            STUDY_TCGA_PUB, ProjectionType.DETAILED, 6, 1, sortBy, direction));

    pages.add(
        repository.getAllSamplesInStudy(
            STUDY_TCGA_PUB, ProjectionType.DETAILED, 6, 2, sortBy, direction));

    return pages;
  }
}
