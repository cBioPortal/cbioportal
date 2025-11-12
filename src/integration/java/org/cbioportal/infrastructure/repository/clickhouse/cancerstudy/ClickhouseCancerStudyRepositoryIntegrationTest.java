package org.cbioportal.infrastructure.repository.clickhouse.cancerstudy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.cbioportal.AbstractClickhouseIntegrationTest;
import org.cbioportal.shared.SortAndSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class ClickhouseCancerStudyRepositoryIntegrationTest extends AbstractClickhouseIntegrationTest {

  private static final int TOTAL_STUDIES = 492;

  private ClickhouseCancerStudyRepository repository;

  @Autowired private ClickhouseCancerStudyMapper mapper;

  @BeforeEach
  void setup() {
    repository = new ClickhouseCancerStudyRepository(mapper);
  }

  @Test
  void testGetCancerStudiesMetadata() {
    var studies =
        repository.getCancerStudiesMetadata(new SortAndSearchCriteria("", "", "", null, null));
    assertEquals(TOTAL_STUDIES, studies.size());
  }
}
