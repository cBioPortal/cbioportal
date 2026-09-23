package org.cbioportal.application.file.export;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;
import org.cbioportal.application.file.export.exporters.ExportDetails;
import org.cbioportal.application.file.export.exporters.GenePanelMatrixDatatypeExporter;
import org.cbioportal.application.file.export.services.GenePanelMatrixService;
import org.cbioportal.application.file.model.GenePanelMatrixItem;
import org.cbioportal.application.file.utils.CloseableIterator;
import org.junit.Test;

public class GenePanelMatrixDatatypeExporterTests {

  @Test
  public void testStudyIdIsRemovedOnlyFromStartOfProfileId() {
    GenePanelMatrixItem item = new GenePanelMatrixItem();
    item.setRowKey(1);
    item.setSampleStableId("SAMPLE_1");
    item.setGeneticProfileStableId("study_study_mutations");
    item.setGenePanelStableId("panel_1");

    GenePanelMatrixService service =
        new GenePanelMatrixService(null) {
          @Override
          public boolean hasGenePanelMatrix(String studyId, Set<String> sampleIds) {
            return true;
          }

          @Override
          public CloseableIterator<GenePanelMatrixItem> getGenePanelMatrix(
              String studyId, Set<String> sampleIds) {
            return new SimpleCloseableIterator<>(List.of(item));
          }

          @Override
          public List<String> getDistinctGeneProfileIdsWithGenePanelMatrix(
              String studyId, Set<String> sampleIds) {
            return List.of("study_study_mutations");
          }
        };

    var factory = new InMemoryFileWriterFactory();
    var exporter = new GenePanelMatrixDatatypeExporter(service);
    exporter.exportData(factory, new ExportDetails("study"));

    assertEquals(
        "SAMPLE_ID\tstudy_mutations\nSAMPLE_1\tpanel_1\n",
        factory.getFileContents().get("data_gene_panel_matrix_gene_panel_matrix.txt").toString());
  }
}
