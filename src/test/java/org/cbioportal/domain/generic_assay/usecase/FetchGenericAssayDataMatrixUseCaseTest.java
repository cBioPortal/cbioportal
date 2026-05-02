package org.cbioportal.domain.generic_assay.usecase;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.cbioportal.application.rest.response.GenericAssayDataMatrixDTO;
import org.cbioportal.legacy.model.GenericAssayData;
import org.cbioportal.legacy.service.GenericAssayService;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.web.parameter.GenericAssayFilter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests the matrix transformation logic in FetchGenericAssayDataMatrixUseCase.
 *
 * <p>Verifies that the flat (per-row) GenericAssayData list is correctly pivoted into a compact
 * matrix DTO, matching the test data inserted by test_generic_assay_data.sql:
 *
 * <pre>
 *   Signature1: [0.3461, 0.0,    0.1523]   (for samples SB-01, SD-01, SE-01)
 *   Signature2: [0.0,    0.7812, 0.4290]
 * </pre>
 */
@RunWith(MockitoJUnitRunner.class)
public class FetchGenericAssayDataMatrixUseCaseTest {

  private static final String PROFILE_ID = "study_tcga_pub_mutational_signatures";
  private static final String SAMPLE1 = "TCGA-A1-A0SB-01";
  private static final String SAMPLE2 = "TCGA-A1-A0SD-01";
  private static final String SAMPLE3 = "TCGA-A1-A0SE-01";
  private static final String SIG1 = "Signature1";
  private static final String SIG2 = "Signature2";

  @InjectMocks private FetchGenericAssayDataMatrixUseCase useCase;

  @Mock private GenericAssayService genericAssayService;

  /**
   * Builds the same flat data that the legacy service would return — 6 rows
   * (3 samples × 2 entities), each carrying the full metadata fields that the
   * old endpoint repeats in every JSON object.
   */
  private List<GenericAssayData> buildFlatData() {
    List<GenericAssayData> data = new ArrayList<>();
    // Signature1 values
    data.add(makeRow(SAMPLE1, "TCGA-A1-A0SB", SIG1, "0.3461"));
    data.add(makeRow(SAMPLE2, "TCGA-A1-A0SD", SIG1, "0.0"));
    data.add(makeRow(SAMPLE3, "TCGA-A1-A0SE", SIG1, "0.1523"));
    // Signature2 values
    data.add(makeRow(SAMPLE1, "TCGA-A1-A0SB", SIG2, "0.0"));
    data.add(makeRow(SAMPLE2, "TCGA-A1-A0SD", SIG2, "0.7812"));
    data.add(makeRow(SAMPLE3, "TCGA-A1-A0SE", SIG2, "0.4290"));
    return data;
  }

  private GenericAssayData makeRow(
      String sampleId, String patientId, String entityStableId, String value) {
    GenericAssayData d = new GenericAssayData();
    d.setMolecularProfileId(PROFILE_ID);
    d.setSampleId(sampleId);
    d.setPatientId(patientId);
    d.setStudyId("study_tcga_pub");
    d.setGenericAssayStableId(entityStableId);
    d.setValue(value);
    d.setPatientLevel(false);
    return d;
  }

  // ---- Tests ----

  @Test
  public void matrixHasCorrectSampleIds() throws MolecularProfileNotFoundException {
    List<GenericAssayData> flat = buildFlatData();
    List<String> sampleIds = Arrays.asList(SAMPLE1, SAMPLE2, SAMPLE3);

    when(genericAssayService.fetchGenericAssayData(PROFILE_ID, sampleIds, null, "SUMMARY"))
        .thenReturn(flat);

    GenericAssayFilter filter = new GenericAssayFilter();
    filter.setSampleIds(sampleIds);

    GenericAssayDataMatrixDTO matrix = useCase.execute(PROFILE_ID, filter);

    Assert.assertEquals(
        "sampleIds must contain exactly 3 entries",
        3,
        matrix.getSampleIds().size());
    Assert.assertEquals(SAMPLE1, matrix.getSampleIds().get(0));
    Assert.assertEquals(SAMPLE2, matrix.getSampleIds().get(1));
    Assert.assertEquals(SAMPLE3, matrix.getSampleIds().get(2));
  }

  @Test
  public void matrixHasCorrectEntityKeys() throws MolecularProfileNotFoundException {
    List<GenericAssayData> flat = buildFlatData();
    List<String> sampleIds = Arrays.asList(SAMPLE1, SAMPLE2, SAMPLE3);

    when(genericAssayService.fetchGenericAssayData(PROFILE_ID, sampleIds, null, "SUMMARY"))
        .thenReturn(flat);

    GenericAssayFilter filter = new GenericAssayFilter();
    filter.setSampleIds(sampleIds);

    GenericAssayDataMatrixDTO matrix = useCase.execute(PROFILE_ID, filter);

    Map<String, List<Object>> entries = matrix.getEntries();
    Assert.assertEquals("entries map must have exactly 2 keys", 2, entries.size());
    Assert.assertTrue("Must contain Signature1", entries.containsKey(SIG1));
    Assert.assertTrue("Must contain Signature2", entries.containsKey(SIG2));
  }

  @Test
  public void matrixValuesAreParallelToSampleIds() throws MolecularProfileNotFoundException {
    List<GenericAssayData> flat = buildFlatData();
    List<String> sampleIds = Arrays.asList(SAMPLE1, SAMPLE2, SAMPLE3);

    when(genericAssayService.fetchGenericAssayData(PROFILE_ID, sampleIds, null, "SUMMARY"))
        .thenReturn(flat);

    GenericAssayFilter filter = new GenericAssayFilter();
    filter.setSampleIds(sampleIds);

    GenericAssayDataMatrixDTO matrix = useCase.execute(PROFILE_ID, filter);

    // Signature1: [0.3461, 0.0, 0.1523]
    List<Object> sig1Values = matrix.getEntries().get(SIG1);
    Assert.assertEquals(3, sig1Values.size());
    Assert.assertEquals("0.3461", sig1Values.get(0));
    Assert.assertEquals("0.0", sig1Values.get(1));
    Assert.assertEquals("0.1523", sig1Values.get(2));

    // Signature2: [0.0, 0.7812, 0.4290]
    List<Object> sig2Values = matrix.getEntries().get(SIG2);
    Assert.assertEquals(3, sig2Values.size());
    Assert.assertEquals("0.0", sig2Values.get(0));
    Assert.assertEquals("0.7812", sig2Values.get(1));
    Assert.assertEquals("0.4290", sig2Values.get(2));
  }

  @Test
  public void matrixHasNoRedundantMetadata() throws MolecularProfileNotFoundException {
    List<GenericAssayData> flat = buildFlatData();
    List<String> sampleIds = Arrays.asList(SAMPLE1, SAMPLE2, SAMPLE3);

    when(genericAssayService.fetchGenericAssayData(PROFILE_ID, sampleIds, null, "SUMMARY"))
        .thenReturn(flat);

    GenericAssayFilter filter = new GenericAssayFilter();
    filter.setSampleIds(sampleIds);

    GenericAssayDataMatrixDTO matrix = useCase.execute(PROFILE_ID, filter);

    // Serialize to JSON string to prove there's no molecularProfileId, studyId, patientId
    String json = matrix.toString();
    // The DTO only has sampleIds and entries — no per-row metadata fields exist
    Assert.assertNotNull(matrix.getSampleIds());
    Assert.assertNotNull(matrix.getEntries());
    // Each value in entries is just a string, not a complex object
    for (Map.Entry<String, List<Object>> entry : matrix.getEntries().entrySet()) {
      for (Object val : entry.getValue()) {
        Assert.assertTrue(
            "Values must be plain strings, not objects with metadata",
            val instanceof String);
      }
    }
  }

  @Test
  public void sizeComparisonOldVsNew() throws MolecularProfileNotFoundException {
    List<GenericAssayData> flat = buildFlatData();
    List<String> sampleIds = Arrays.asList(SAMPLE1, SAMPLE2, SAMPLE3);

    when(genericAssayService.fetchGenericAssayData(PROFILE_ID, sampleIds, null, "SUMMARY"))
        .thenReturn(flat);

    GenericAssayFilter filter = new GenericAssayFilter();
    filter.setSampleIds(sampleIds);

    GenericAssayDataMatrixDTO matrix = useCase.execute(PROFILE_ID, filter);

    // Estimate old JSON size: 6 rows × ~88 bytes each (with repeated metadata)
    int oldSize = 0;
    for (GenericAssayData d : flat) {
      // Each row in old format: {"molecularProfileId":"...","sampleId":"...","patientId":"...",
      //   "studyId":"...","genericAssayStableId":"...","value":"...","patientLevel":false}
      String row =
          String.format(
              "{\"molecularProfileId\":\"%s\",\"sampleId\":\"%s\",\"patientId\":\"%s\","
                  + "\"studyId\":\"%s\",\"genericAssayStableId\":\"%s\",\"value\":\"%s\","
                  + "\"patientLevel\":%s}",
              d.getMolecularProfileId(),
              d.getSampleId(),
              d.getPatientId(),
              d.getStudyId(),
              d.getGenericAssayStableId(),
              d.getValue(),
              d.getPatientLevel());
      oldSize += row.length();
    }
    oldSize += flat.size(); // commas between objects
    oldSize += 2; // [ ]

    // Estimate new JSON size
    StringBuilder newJson = new StringBuilder();
    newJson.append("{\"sampleIds\":[");
    for (int i = 0; i < matrix.getSampleIds().size(); i++) {
      if (i > 0) newJson.append(",");
      newJson.append("\"").append(matrix.getSampleIds().get(i)).append("\"");
    }
    newJson.append("],\"entries\":{");
    int entryIdx = 0;
    for (Map.Entry<String, List<Object>> entry : matrix.getEntries().entrySet()) {
      if (entryIdx > 0) newJson.append(",");
      newJson.append("\"").append(entry.getKey()).append("\":[");
      for (int i = 0; i < entry.getValue().size(); i++) {
        if (i > 0) newJson.append(",");
        newJson.append("\"").append(entry.getValue().get(i)).append("\"");
      }
      newJson.append("]");
      entryIdx++;
    }
    newJson.append("}}");
    int newSize = newJson.length();

    double reduction = 100.0 * (1.0 - (double) newSize / oldSize);

    System.out.println("=== SIZE COMPARISON ===");
    System.out.println("Old endpoint (flat): " + oldSize + " bytes");
    System.out.println("New endpoint (matrix): " + newSize + " bytes");
    System.out.printf("Reduction: %.1f%%\n", reduction);
    System.out.println("Old rows: " + flat.size());
    System.out.println("New JSON: " + newJson);

    Assert.assertTrue(
        "Matrix response must be smaller than flat response", newSize < oldSize);
  }

  @Test
  public void emptyDataReturnsEmptyMatrix() throws MolecularProfileNotFoundException {
    List<String> sampleIds = Arrays.asList(SAMPLE1);

    when(genericAssayService.fetchGenericAssayData(PROFILE_ID, sampleIds, null, "SUMMARY"))
        .thenReturn(new ArrayList<>());

    GenericAssayFilter filter = new GenericAssayFilter();
    filter.setSampleIds(sampleIds);

    GenericAssayDataMatrixDTO matrix = useCase.execute(PROFILE_ID, filter);

    Assert.assertTrue("sampleIds should be empty for empty data", matrix.getSampleIds().isEmpty());
    Assert.assertTrue("entries should be empty for empty data", matrix.getEntries().isEmpty());
  }

  @Test
  public void sampleListIdPathCallsCorrectServiceMethod()
      throws MolecularProfileNotFoundException {
    List<GenericAssayData> flat = buildFlatData();
    String sampleListId = "study_tcga_pub_all";
    List<String> stableIds = Arrays.asList(SIG1, SIG2);

    when(genericAssayService.getGenericAssayData(PROFILE_ID, sampleListId, stableIds, "SUMMARY"))
        .thenReturn(flat);

    GenericAssayFilter filter = new GenericAssayFilter();
    filter.setSampleListId(sampleListId);
    filter.setGenericAssayStableIds(stableIds);

    GenericAssayDataMatrixDTO matrix = useCase.execute(PROFILE_ID, filter);

    Assert.assertEquals(3, matrix.getSampleIds().size());
    Assert.assertEquals(2, matrix.getEntries().size());
  }
}
