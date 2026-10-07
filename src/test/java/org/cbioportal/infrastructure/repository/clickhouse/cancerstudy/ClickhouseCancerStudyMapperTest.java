package org.cbioportal.infrastructure.repository.clickhouse.cancerstudy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.cbioportal.domain.cancerstudy.CancerStudyMetadata;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.cbioportal.shared.SortAndSearchCriteria;
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
public class ClickhouseCancerStudyMapperTest {
  private static final String STUDY_TCGA_PUB = "study_tcga_pub";
  private static final String STUDY_ACC_TCGA = "acc_tcga";
  private static final String LICENSE = "CC-BY-NC-ND-4.0";

  private static final SortAndSearchCriteria NO_SORT_OR_SEARCH =
      new SortAndSearchCriteria(null, null, null, null, null);

  @Autowired private ClickhouseCancerStudyMapper mapper;

  @Test
  public void getCancerStudiesMetadataIncludesLicense() {
    var studies =
        byStudyId(
            mapper.getCancerStudiesMetadata(
                NO_SORT_OR_SEARCH, List.of(STUDY_TCGA_PUB, STUDY_ACC_TCGA)));

    assertEquals(LICENSE, studies.get(STUDY_TCGA_PUB).license());
    assertNull(studies.get(STUDY_ACC_TCGA).license());
  }

  @Test
  public void getCancerStudiesMetadataSummaryIncludesLicense() {
    var studies =
        byStudyId(
            mapper.getCancerStudiesMetadataSummary(
                NO_SORT_OR_SEARCH, List.of(STUDY_TCGA_PUB, STUDY_ACC_TCGA)));

    assertEquals(LICENSE, studies.get(STUDY_TCGA_PUB).license());
    assertNull(studies.get(STUDY_ACC_TCGA).license());
  }

  private static Map<String, CancerStudyMetadata> byStudyId(List<CancerStudyMetadata> studies) {
    return studies.stream()
        .collect(Collectors.toMap(CancerStudyMetadata::cancerStudyIdentifier, Function.identity()));
  }
}
