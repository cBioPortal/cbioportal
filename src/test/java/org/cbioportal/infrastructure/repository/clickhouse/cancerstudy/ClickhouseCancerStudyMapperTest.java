package org.cbioportal.infrastructure.repository.clickhouse.cancerstudy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.cbioportal.domain.cancerstudy.ResourceCount;
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

/**
 * Covers {@code getResourceCountsForAllStudies}, which had no coverage at all and was still reading
 * the legacy resource_sample/resource_patient tables after the importer stopped writing them. The
 * test schema's stale copies of those tables were what kept it passing; once the test schema
 * matched production, the query could not resolve its tables.
 */
@RunWith(SpringRunner.class)
@Import(MyBatisConfig.class)
@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class ClickhouseCancerStudyMapperTest {

  @Autowired private ClickhouseCancerStudyMapper mapper;

  @Test
  public void getResourceCountsForAllStudies_readsUnifiedResourceDataTable() {
    List<ResourceCount> counts = mapper.getResourceCountsForAllStudies();

    assertThat(counts).isNotEmpty();
    // Sample- and patient-level resources both contribute; study-level ones belong to neither
    // half and must not appear.
    assertThat(counts).noneMatch(c -> "FIGURES".equals(c.resourceId()));
  }

  @Test
  public void getResourceCountsForAllStudies_scopesJoinsByStudy() {
    // resource_data stores stable ids, which repeat across studies -- the seed deliberately gives
    // acc_tcga a sample carrying study_tcga_pub's barcode. Joins that are not scoped by
    // cancer_study_id inflate these counts.
    List<ResourceCount> heSlide =
        mapper.getResourceCountsForAllStudies().stream()
            .filter(c -> "HE_SLIDE".equals(c.resourceId()))
            .toList();

    assertThat(heSlide).isNotEmpty();
    for (ResourceCount count : heSlide) {
      assertThat(count.sampleCount())
          .as("sampleCount for HE_SLIDE in %s", count.cancerStudyIdentifier())
          .isLessThanOrEqualTo(2);
    }
  }
}
