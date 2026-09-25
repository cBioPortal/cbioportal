package org.cbioportal.infrastructure.repository.clickhouse.cancerstudy;

import static org.junit.Assert.assertEquals;

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

@RunWith(SpringRunner.class)
@Import({MyBatisConfig.class, ClickhouseCancerStudyRepository.class})
@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class ClickhouseCancerStudyRepositoryTest {

  @Autowired private ClickhouseCancerStudyRepository repository;

  @Test
  public void countsResourcesFromResourceData() {
    List<ResourceCount> counts = repository.getResourceCountsForAllStudies();

    ResourceCount sampleSlides = find(counts, "wsi_test_study", "WSI_SAMPLE");
    assertEquals("SAMPLE", sampleSlides.resourceType());
    assertEquals(Integer.valueOf(1), sampleSlides.sampleCount());
    assertEquals(Integer.valueOf(1), sampleSlides.patientCount());

    ResourceCount patientSlides = find(counts, "wsi_test_study", "WSI_PATIENT");
    assertEquals("PATIENT", patientSlides.resourceType());
    assertEquals(Integer.valueOf(1), patientSlides.patientCount());
  }

  private static ResourceCount find(List<ResourceCount> counts, String studyId, String resourceId) {
    return counts.stream()
        .filter(count -> studyId.equals(count.cancerStudyIdentifier()))
        .filter(count -> resourceId.equals(count.resourceId()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("no count for " + studyId + "/" + resourceId));
  }
}
