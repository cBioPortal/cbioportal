package org.cbioportal.application.file.model;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.SequencedMap;
import org.junit.Test;

public class CancerTypeTest {

  @Test
  public void testToRow() {
    CancerType cancerType = new CancerType();
    cancerType.setTypeOfCancerId("brca");
    cancerType.setName("Breast Invasive Carcinoma");
    cancerType.setShortName("Breast");
    cancerType.setDedicatedColor("HotPink");
    cancerType.setParent("tissue");

    SequencedMap<String, String> row = cancerType.toRow();

    // Assert values
    assertEquals("brca", row.get("TYPE_OF_CANCER_ID"));
    assertEquals("Breast Invasive Carcinoma", row.get("NAME"));
    assertEquals("Breast", row.get("SHORT_NAME"));
    assertEquals("HotPink", row.get("DEDICATED_COLOR"));
    assertEquals("tissue", row.get("PARENT"));
    assertEquals(5, row.size());

    // Assert column order (important for TSV export)
    assertEquals(
        List.of("TYPE_OF_CANCER_ID", "NAME", "DEDICATED_COLOR", "PARENT", "SHORT_NAME"),
        row.sequencedKeySet().stream().toList());
  }

  @Test
  public void testGetHeader() {
    assertEquals(
        List.of("TYPE_OF_CANCER_ID", "NAME", "DEDICATED_COLOR", "PARENT", "SHORT_NAME"),
        CancerType.getHeader().stream().toList());
  }
}
