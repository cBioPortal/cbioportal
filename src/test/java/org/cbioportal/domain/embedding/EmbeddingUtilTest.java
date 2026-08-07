package org.cbioportal.domain.embedding;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.cbioportal.domain.embedding.util.EmbeddingUtil;
import org.junit.Before;
import org.junit.Test;

public class EmbeddingUtilTest {

  private List<EmbeddingRow> embeddingRows;

  @Before
  public void setUp() {
    // Sample data for testing
    // internalId, shortName, description, entityType, reductionTechnique, name,
    // embeddingIdentifier, sampleId, patientId, x, y, customAttribute, studyIdentifier, embeddingId
    EmbeddingRow row1 =
        new EmbeddingRow(
            1,
            "short1",
            "desc1",
            "type1",
            "tech1",
            "name1",
            "embId1",
            "sample1",
            "patient1",
            0.1,
            0.2,
            "attr1",
            "study1",
            100);
    EmbeddingRow row2 =
        new EmbeddingRow(
            1,
            "short1",
            "desc1",
            "type1",
            "tech1",
            "name1",
            "embId1",
            "sample2",
            "patient1",
            0.3,
            0.4,
            "attr2",
            "study1",
            100);
    EmbeddingRow row3 =
        new EmbeddingRow(
            2,
            "short2",
            "desc2",
            "type2",
            "tech2",
            "name2",
            "embId2",
            "sample3",
            "patient2",
            0.5,
            0.6,
            "attr3",
            "study2",
            101);
    embeddingRows = Arrays.asList(row1, row2, row3);
  }

  @Test
  public void testCountPatient() {
    assertEquals(2, EmbeddingUtil.countPatient(embeddingRows));
    assertEquals(0, EmbeddingUtil.countPatient(Collections.emptyList()));
  }

  @Test
  public void testCountSample() {
    assertEquals(3, EmbeddingUtil.countSample(embeddingRows));
    assertEquals(0, EmbeddingUtil.countSample(Collections.emptyList()));
  }

  @Test
  public void testGetEmbeddingData() {
    List<EmbeddingData> data = EmbeddingUtil.getEmbeddingData(embeddingRows);
    assertEquals(3, data.size());
    assertEquals("patient1", data.get(0).patientId());
    assertEquals("sample1", data.get(0).sampleId());
    assertEquals(0.1, data.get(0).x(), 0.001);
    assertEquals(0.2, data.get(0).y(), 0.001);
    assertEquals("attr1", data.get(0).customAttribute());
  }

  @Test
  public void testGetStudies() {
    List<String> studies = EmbeddingUtil.getStudies(embeddingRows);
    assertEquals(2, studies.size());
    assertTrue(studies.contains("study1"));
    assertTrue(studies.contains("study2"));
  }

  @Test
  public void testGetUniqueEmbeddingDefinitions() {
    List<EmbeddingDefinition> definitions =
        EmbeddingUtil.getUniqueEmbeddingDefinitions(embeddingRows);
    assertEquals(2, definitions.size());

    // Ensure definitions are correct
    boolean found1 = false;
    boolean found2 = false;
    for (EmbeddingDefinition def : definitions) {
      if (def.internalId() == 1) {
        found1 = true;
        assertEquals("desc1", def.description());
      } else if (def.internalId() == 2) {
        found2 = true;
        assertEquals("desc2", def.description());
      }
    }
    assertTrue(found1);
    assertTrue(found2);
  }
}
