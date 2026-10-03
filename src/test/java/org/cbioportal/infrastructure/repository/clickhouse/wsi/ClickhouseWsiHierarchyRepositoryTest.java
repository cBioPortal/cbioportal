package org.cbioportal.infrastructure.repository.clickhouse.wsi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cbioportal.domain.wsi.WsiDeidentification;
import org.cbioportal.domain.wsi.WsiHierarchy;
import org.cbioportal.domain.wsi.WsiSlide;
import org.junit.Before;
import org.junit.Test;

/** Repository-level de-identification rules, independent of a ClickHouse container. */
public class ClickhouseWsiHierarchyRepositoryTest {

  private static final String KEY_1 = "2351e12d49557627b24fe71e17ec5c64";
  private static final String KEY_2 = "e766df6e31f08c1b363951525204ed55";

  private final ClickhouseWsiHierarchyMapper mapper = mock(ClickhouseWsiHierarchyMapper.class);
  private final ClickhouseWsiContextMapper contextMapper = mock(ClickhouseWsiContextMapper.class);
  private final ClickhouseWsiHierarchyRepository repository =
      new ClickhouseWsiHierarchyRepository(mapper, contextMapper);

  @Before
  public void setUp() {
    when(contextMapper.getPatientContext("study", "patient"))
        .thenReturn(Map.of("cancer_study_id", 1L, "patient_stable_id", "patient"));
  }

  @Test
  public void exposesOnlyTheOpaqueSlideKey() throws Exception {
    rows(row(KEY_1));

    WsiHierarchy hierarchy = repository.getPatientHierarchy("study", "patient");

    assertEquals(List.of(KEY_1), slideKeys(hierarchy));
    String json = new ObjectMapper().writeValueAsString(hierarchy);
    List<String> keys = new ArrayList<>();
    collectKeys(new ObjectMapper().readTree(json), keys);
    assertTrue(keys.contains("slideKey"));
    for (String forbidden : List.of("imageId", "image_id", "barcode", "sourceUrl")) {
      assertFalse("hierarchy exposes " + forbidden, keys.contains(forbidden));
    }
    assertFalse(WsiDeidentification.containsAccession(json));
  }

  @Test
  public void dropsRowsContainingAnAccessionNumberInAnyTextField() {
    for (String field :
        List.of("part_description", "block_label", "stain_name", "specimen_key", "sample_id")) {
      for (String accession : List.of("S12-34567", "s99-123", "Part from MSK:S1234")) {
        Map<String, Object> leaking = row(KEY_2);
        leaking.put(field, accession);
        rows(row(KEY_1), leaking);

        WsiHierarchy hierarchy = repository.getPatientHierarchy("study", "patient");

        assertEquals(field + "=" + accession, List.of(KEY_1), slideKeys(hierarchy));
      }
    }
  }

  @Test
  public void dropsAnAccessionShapedReferenceSample() {
    Map<String, Object> row = row(KEY_1);
    row.put("reference_sample_id", "S12-34567");
    rows(row);
    // The row itself is dropped; no slide and no reference sample are served.

    WsiHierarchy hierarchy = repository.getPatientHierarchy("study", "patient");

    assertNull(hierarchy.referenceSampleId());
    assertTrue(hierarchy.sampleGroups().isEmpty());
  }

  @Test
  public void doesNotTreatNonAccessionTextAsAnAccession() {
    Map<String, Object> row = row(KEY_1);
    row.put("part_description", "Specimen 12");
    row.put("block_label", "S1-2");
    row.put("stain_name", "SOX10-123");
    rows(row);

    assertEquals(List.of(KEY_1), slideKeys(repository.getPatientHierarchy("study", "patient")));
  }

  @Test
  public void dropsSlidesWithoutAValidSlideKey() {
    Map<String, Object> missing = row(KEY_2);
    missing.put("slide_key", "");
    Map<String, Object> rawImageId = row(KEY_2);
    rawImageId.put("slide_key", "3020726");
    rows(row(KEY_1), missing, rawImageId);

    assertEquals(List.of(KEY_1), slideKeys(repository.getPatientHierarchy("study", "patient")));
  }

  @Test
  public void acceptsOpaqueKeysWhoseHexLooksLikeACompactDate() {
    // "a20240131b" contains an eight-digit YYYYMMDD-looking run.
    String dateLikeKey = "a20240131b" + "0".repeat(22);
    Map<String, Object> row = row(dateLikeKey);
    row.put("part_key", "part:" + dateLikeKey);
    row.put("block_key", "block:" + dateLikeKey);
    row.put("specimen_key", "block::part:" + dateLikeKey + "::block:" + dateLikeKey);
    rows(row);

    assertEquals(
        List.of(dateLikeKey), slideKeys(repository.getPatientHierarchy("study", "patient")));
  }

  @Test
  public void stillRejectsDatesInFreeText() {
    Map<String, Object> row = row(KEY_1);
    row.put("part_description", "Resected 2024-01-31");
    rows(row);

    assertNull(repository.getPatientHierarchy("study", "patient"));
  }

  @Test
  public void neverServesAccessionsInTheReferenceSampleEvenFromKeptRows() {
    Map<String, Object> dropped = row(KEY_2);
    dropped.put("reference_sample_id", "S12-34567");
    rows(dropped, row(KEY_1));

    WsiHierarchy hierarchy = repository.getPatientHierarchy("study", "patient");

    assertEquals("WSI-FIXTURE-SAMPLE", hierarchy.referenceSampleId());
    assertEquals(List.of(KEY_1), slideKeys(hierarchy));
  }

  @Test
  public void neverExposesPartDesignatorOrPathologyDiagnosisTitle() throws Exception {
    Map<String, Object> row = row(KEY_1);
    row.put("part_designator", "A");
    row.put("path_dx_title", "right ovary");
    rows(row);

    WsiHierarchy hierarchy = repository.getPatientHierarchy("study", "patient");

    assertNull(hierarchy.sampleGroups().get(0).parts().get(0).partDesignator());
    assertNull(hierarchy.sampleGroups().get(0).parts().get(0).pathDxTitle());
  }

  // ---- reference sample selection ----

  private static Map<String, Object> ref(String referenceSampleId) {
    Map<String, Object> row = new HashMap<>();
    // ClickHouse's JSONExtractString yields '' when a row carries no reference sample.
    row.put("reference_sample_id", referenceSampleId == null ? "" : referenceSampleId);
    return row;
  }

  @Test
  public void takesTheReferenceSampleFromTheFirstRowThatCarriesOne() {
    // Unmatched slides sort first and may omit the reference sample.
    assertEquals(
        "P-1-T01",
        ClickhouseWsiHierarchyRepository.referenceSampleId(
            List.of(ref(null), ref("P-1-T01"), ref("P-1-T01")), "P-1"));
  }

  @Test
  public void keepsTheFirstReferenceSampleWhenRowsDisagree() {
    assertEquals(
        "P-1-T01",
        ClickhouseWsiHierarchyRepository.referenceSampleId(
            List.of(ref("P-1-T01"), ref("P-1-T02")), "P-1"));
  }

  @Test
  public void hasNoReferenceSampleWhenNoRowCarriesOne() {
    assertNull(
        ClickhouseWsiHierarchyRepository.referenceSampleId(List.of(ref(null), ref(null)), "P-1"));
  }

  @Test
  public void skipsAccessionShapedReferenceSamples() {
    assertEquals(
        "P-1-T01",
        ClickhouseWsiHierarchyRepository.referenceSampleId(
            List.of(ref("MSK:S1234"), ref("P-1-T01")), "P-1"));
  }

  private void rows(Map<String, Object>... rows) {
    when(mapper.getPatientHierarchy(1L, "patient")).thenReturn(List.of(rows));
  }

  private static Map<String, Object> row(String slideKey) {
    Map<String, Object> row = new HashMap<>();
    row.put("reference_sample_id", "WSI-FIXTURE-SAMPLE");
    row.put("sample_id", "WSI-FIXTURE-SAMPLE");
    row.put("part_key", "part:" + KEY_1);
    row.put("part_number", "1");
    row.put("part_type", "FALLOPIAN TUBE AND OVARY");
    row.put("part_description", "Specimen 1");
    row.put("block_key", "block:" + slideKey);
    row.put("block_number", "1");
    row.put("block_label", "Block 1");
    row.put("slide_key", slideKey);
    row.put("stain_name", "H&E, Initial");
    row.put("stain_group", "H&E (Initial)");
    row.put("is_hne", true);
    row.put("is_ihc", false);
    row.put("magnification", "20x");
    row.put("file_size_bytes", 20123456L);
    row.put("can_serve_tiles", true);
    row.put("slide_type", "H&E");
    row.put("match_level", "BLOCK");
    row.put("specimen_key", "block::part:" + KEY_1 + "::block:" + slideKey);
    row.put("procedure_date_days", -17);
    row.put("timepoint_source", "Recorded procedure date relative to first tumor sequencing");
    row.put("date_kind", "RECORDED");
    row.put("date_source", "recorded_procedure_date");
    row.put("date_status", "AVAILABLE");
    row.put("coordinate_system", "patient_first_tumor_sequencing_day_zero");
    return row;
  }

  private static List<String> slideKeys(WsiHierarchy hierarchy) {
    return hierarchy.sampleGroups().stream()
        .flatMap(group -> group.parts().stream())
        .flatMap(part -> part.blocks().stream())
        .flatMap(block -> block.slides().stream())
        .map(WsiSlide::slideKey)
        .toList();
  }

  private static void collectKeys(JsonNode node, List<String> keys) {
    node.fieldNames().forEachRemaining(keys::add);
    node.elements().forEachRemaining(child -> collectKeys(child, keys));
  }
}
