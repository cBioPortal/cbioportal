package org.cbioportal.infrastructure.repository.clickhouse.wsi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.cbioportal.domain.wsi.WsiHierarchy;
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
public class ClickhouseWsiHierarchyMapperTest {

  @Autowired private ClickhouseWsiHierarchyRepository repository;

  @Test
  public void readsNormalizedActivePatientHierarchy() {
    WsiHierarchy hierarchy = repository.getPatientHierarchy("wsi_test_study", "WSI-PATIENT");

    assertEquals(2, hierarchy.sampleGroups().size());
    assertTrue(
        hierarchy.sampleGroups().stream()
            .anyMatch(group -> group.sampleId() == null));
    assertTrue(
        hierarchy.sampleGroups().stream()
            .flatMap(group -> group.parts().stream())
            .flatMap(part -> part.blocks().stream())
            .flatMap(block -> block.slides().stream())
            .anyMatch(slide -> slide.imageId().equals("3020726") && slide.sampleId().equals("WSI-SAMPLE")));
  }

  @Test
  public void returnsNullForUnknownPatient() {
    assertNull(repository.getPatientHierarchy("wsi_test_study", "missing"));
  }

  @Test
  public void readsOnlyActiveReleaseVersion() {
    WsiHierarchy hierarchy = repository.getPatientHierarchy("wsi_versioned_study", "VERSIONED-PATIENT");

    assertEquals(1, hierarchy.sampleGroups().size());
    assertEquals(
        "active-slide",
        hierarchy.sampleGroups().get(0).parts().get(0).blocks().get(0).slides().get(0).imageId());
  }

  @Test
  public void readsEmptyHierarchyPayload() {
    WsiHierarchy hierarchy = repository.getPatientHierarchy("wsi_empty_hierarchy_study", "EMPTY-PATIENT");

    assertTrue(hierarchy.sampleGroups().isEmpty());
    assertNull(hierarchy.referenceSampleId());
  }

  @Test
  public void returnsNullWhenReleaseIsMissing() {
    assertNull(repository.getPatientHierarchy("wsi_missing_release_study", "MISSING-RELEASE"));
  }
}
