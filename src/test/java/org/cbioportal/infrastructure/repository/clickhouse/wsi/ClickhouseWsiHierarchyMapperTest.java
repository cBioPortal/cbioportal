package org.cbioportal.infrastructure.repository.clickhouse.wsi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.cbioportal.domain.wsi.WsiHierarchy;
import org.cbioportal.domain.wsi.WsiSlide;
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
@Import({MyBatisConfig.class, ClickhouseWsiHierarchyRepository.class})
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
    assertTrue(hierarchy.sampleGroups().stream().anyMatch(group -> group.sampleId() == null));
    assertTrue(
        hierarchy.sampleGroups().stream()
            .flatMap(group -> group.parts().stream())
            .flatMap(part -> part.blocks().stream())
            .flatMap(block -> block.slides().stream())
            .anyMatch(
                slide ->
                    slide.imageId().equals("3020726") && slide.sampleId().equals("WSI-SAMPLE")));

    WsiSlide timedSlide =
        hierarchy.sampleGroups().stream()
            .flatMap(group -> group.parts().stream())
            .flatMap(part -> part.blocks().stream())
            .flatMap(block -> block.slides().stream())
            .filter(slide -> slide.imageId().equals("3020726"))
            .findFirst()
            .orElseThrow();
    assertEquals(Integer.valueOf(-17), timedSlide.procedureDateDays());
    assertEquals("Procedure date relative to first ICD-O diagnosis", timedSlide.timepointSource());
  }

  @Test
  public void returnsNullForUnknownPatient() {
    assertNull(repository.getPatientHierarchy("wsi_test_study", "missing"));
  }

  @Test
  public void readsTheStudySnapshot() {
    WsiHierarchy hierarchy =
        repository.getPatientHierarchy("wsi_snapshot_study", "SNAPSHOT-PATIENT");

    assertEquals(1, hierarchy.sampleGroups().size());
    assertEquals(
        "active-slide",
        hierarchy.sampleGroups().get(0).parts().get(0).blocks().get(0).slides().get(0).imageId());
    assertNull(
        hierarchy
            .sampleGroups()
            .get(0)
            .parts()
            .get(0)
            .blocks()
            .get(0)
            .slides()
            .get(0)
            .procedureDateDays());
  }

  @Test
  public void readsEmptyHierarchyPayload() {
    WsiHierarchy hierarchy =
        repository.getPatientHierarchy("wsi_empty_hierarchy_study", "EMPTY-PATIENT");

    assertTrue(hierarchy.sampleGroups().isEmpty());
    assertNull(hierarchy.referenceSampleId());
  }

  @Test
  public void returnsNullWhenWsiDataIsMissing() {
    assertNull(repository.getPatientHierarchy("wsi_missing_data_study", "MISSING-DATA"));
  }

  @Test
  public void derivesIhcTypeWhenLegacyRowHasNoSlideType() {
    Map<String, Object> row = new HashMap<>();
    row.put("is_hne", false);
    row.put("is_ihc", true);
    row.put("slide_type", null);

    assertEquals("IHC", ClickhouseWsiHierarchyRepository.resolveSlideType(row));
  }

  @Test
  public void usesOtherForUnclassifiedLegacyRow() {
    Map<String, Object> row = new HashMap<>();
    row.put("is_hne", false);
    row.put("is_ihc", false);
    row.put("slide_type", null);

    assertEquals("Other", ClickhouseWsiHierarchyRepository.resolveSlideType(row));
  }
}
