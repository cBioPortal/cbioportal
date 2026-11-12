package org.cbioportal.application.file.export;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.cbioportal.application.file.export.exporters.GenePanelMatrixDatatypeExporter;
import org.cbioportal.application.file.export.services.GenePanelMatrixService;
import org.cbioportal.application.file.model.GenePanelMatrixItem;
import org.cbioportal.application.file.model.Table;
import org.cbioportal.application.file.utils.CloseableIterator;
import org.junit.Test;

public class GenePanelMatrixDatatypeExporterTests {
  @Test
  public void preservesRepeatedStudyIdentifierInMatrixColumns() throws Exception {
    var item = new GenePanelMatrixItem();
    item.setRowKey(1);
    item.setSampleStableId("sample1");
    item.setGeneticProfileStableId("demo_demo_mutations");
    item.setGenePanelStableId("panel1");
    var service =
        new GenePanelMatrixService(null) {
          @Override
          public CloseableIterator<GenePanelMatrixItem> getGenePanelMatrix(
              String studyId, Set<String> sampleIds) {
            return new SimpleCloseableIterator<>(List.of(item));
          }

          @Override
          public List<String> getDistinctGeneProfileIdsWithGenePanelMatrix(
              String studyId, Set<String> sampleIds) {
            return List.of("demo_demo_mutations");
          }
        };
    var exporter =
        new GenePanelMatrixDatatypeExporter(service) {
          @Override
          public Table getData(String studyId, Set<String> sampleIds) {
            return super.getData(studyId, sampleIds);
          }
        };

    try (var table = exporter.getData("demo", Set.of("sample1"))) {
      assertEquals(List.of("SAMPLE_ID", "demo_mutations"), List.copyOf(table.getHeader()));
      assertEquals(Map.of("SAMPLE_ID", "sample1", "demo_mutations", "panel1"), table.next());
    }
  }
}
