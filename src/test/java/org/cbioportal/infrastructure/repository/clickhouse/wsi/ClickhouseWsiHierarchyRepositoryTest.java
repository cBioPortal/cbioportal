package org.cbioportal.infrastructure.repository.clickhouse.wsi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class ClickhouseWsiHierarchyRepositoryTest {

  private static Map<String, Object> row(String referenceSampleId) {
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
            List.of(row(null), row("P-1-T01"), row("P-1-T01")), "P-1"));
  }

  @Test
  public void keepsTheFirstReferenceSampleWhenRowsDisagree() {
    assertEquals(
        "P-1-T01",
        ClickhouseWsiHierarchyRepository.referenceSampleId(
            List.of(row("P-1-T01"), row("P-1-T02")), "P-1"));
  }

  @Test
  public void hasNoReferenceSampleWhenNoRowCarriesOne() {
    assertNull(
        ClickhouseWsiHierarchyRepository.referenceSampleId(List.of(row(null), row(null)), "P-1"));
  }
}
