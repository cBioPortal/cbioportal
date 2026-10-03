package org.cbioportal.infrastructure.repository.clickhouse.wsi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
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

  private static final String SAMPLE_SLIDE_KEY = "2351e12d49557627b24fe71e17ec5c64";
  private static final String PATIENT_SLIDE_KEY = "b3286836fc27c777260ada0dcb8a6857";

  @Test
  public void readsNormalizedActivePatientHierarchy() {
    WsiHierarchy hierarchy = repository.getPatientHierarchy("wsi_test_study", "WSI-PATIENT");

    assertEquals(2, hierarchy.sampleGroups().size());
    assertEquals("WSI-SAMPLE", hierarchy.referenceSampleId());
    // Unmatched slides sort first, as they did in the native wsi_* hierarchy.
    assertNull(hierarchy.sampleGroups().get(0).sampleId());
    // The legacy row without a slide_key (900104) is omitted.
    assertEquals(List.of(PATIENT_SLIDE_KEY, SAMPLE_SLIDE_KEY), slideKeys(hierarchy));

    WsiSlide timedSlide =
        slides(hierarchy).stream()
            .filter(slide -> slide.slideKey().equals(SAMPLE_SLIDE_KEY))
            .findFirst()
            .orElseThrow();
    assertEquals("WSI-SAMPLE", timedSlide.sampleId());
    assertEquals(Integer.valueOf(-17), timedSlide.procedureDateDays());
    assertEquals(
        "Recorded procedure date relative to first tumor sequencing", timedSlide.timepointSource());
    assertEquals("Specimen 2", hierarchy.sampleGroups().get(1).parts().get(0).partDescription());
    assertEquals(
        "Block 1", hierarchy.sampleGroups().get(1).parts().get(0).blocks().get(0).blockLabel());
  }

  @Test
  public void neverServesImageIdsBarcodesOrResourceIdentifiers() throws Exception {
    String json =
        new ObjectMapper()
            .writeValueAsString(repository.getPatientHierarchy("wsi_test_study", "WSI-PATIENT"));

    for (String forbidden :
        List.of(
            "imageId",
            "image_id",
            "barcode",
            "resourceId",
            "resourceDataId",
            "syn-img-",
            "syn-legacy-",
            "s3://",
            "900101",
            "900104",
            "partDesignator\":\"",
            "pathDxTitle\":\"")) {
      assertFalse("hierarchy exposes " + forbidden, json.contains(forbidden));
    }
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
        "e8ac3c1341f0fb1fa1a7c8e69ba27a51",
        hierarchy.sampleGroups().get(0).parts().get(0).blocks().get(0).slides().get(0).slideKey());
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
  public void returnsEmptyHierarchyWhenPatientHasNoWsiRows() {
    WsiHierarchy hierarchy =
        repository.getPatientHierarchy("wsi_missing_data_study", "MISSING-DATA");

    assertTrue(hierarchy.sampleGroups().isEmpty());
    assertNull(hierarchy.referenceSampleId());
  }

  @Test
  public void returnsNullWhenWsiDataIsMissing() {
    assertNull(repository.getPatientHierarchy("wsi_missing_data_study", "NOT-A-PATIENT"));
    assertNull(repository.getPatientHierarchy("no_such_wsi_study", "MISSING-DATA"));
    assertNull(repository.getPatientHierarchy("wsi_test_study", "MISSING-DATA"));
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
  public void usesUnknownForUnclassifiedLegacyRow() {
    Map<String, Object> row = new HashMap<>();
    row.put("is_hne", false);
    row.put("is_ihc", false);
    row.put("slide_type", null);

    assertEquals("Unknown", ClickhouseWsiHierarchyRepository.resolveSlideType(row));
  }

  private static List<WsiSlide> slides(WsiHierarchy hierarchy) {
    return hierarchy.sampleGroups().stream()
        .flatMap(group -> group.parts().stream())
        .flatMap(part -> part.blocks().stream())
        .flatMap(block -> block.slides().stream())
        .toList();
  }

  private static List<String> slideKeys(WsiHierarchy hierarchy) {
    return slides(hierarchy).stream().map(WsiSlide::slideKey).toList();
  }
}
