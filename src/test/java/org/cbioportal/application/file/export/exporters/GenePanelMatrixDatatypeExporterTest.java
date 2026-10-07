package org.cbioportal.application.file.export.exporters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.cbioportal.application.file.export.repositories.GenePanelMatrixRepository;
import org.cbioportal.application.file.export.services.GenePanelMatrixService;
import org.cbioportal.application.file.model.GenePanelMatrixItem;
import org.cbioportal.application.file.model.Table;
import org.cbioportal.application.file.utils.CloseableIterator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(
    MockitoJUnitRunner
        .class) // switch to @ExtendWith(MockitoExtension.class) if the repo uses JUnit 5
public class GenePanelMatrixDatatypeExporterTest {

  private static final String STUDY_ID = "study";
  private static final Set<String> SAMPLE_IDS = Set.of("sample1");

  @Mock private GenePanelMatrixRepository repository;

  private static <T> CloseableIterator<T> iteratorOf(List<T> items) {
    Iterator<T> it = items.iterator();
    return new CloseableIterator<>() {
      @Override
      public void close() throws IOException {}

      @Override
      public boolean hasNext() {
        return it.hasNext();
      }

      @Override
      public T next() {
        return it.next();
      }
    };
  }

  private static GenePanelMatrixItem item(String profileId) {
    var item = new GenePanelMatrixItem();
    item.setRowKey(1);
    item.setSampleStableId("sample1");
    item.setGeneticProfileStableId(profileId);
    item.setGenePanelStableId("PANEL1");
    return item;
  }

  private Table export(String profileId) {
    when(repository.getGenePanelMatrix(STUDY_ID, SAMPLE_IDS))
        .thenReturn(iteratorOf(List.of(item(profileId))));
    when(repository.getDistinctGeneProfileIdsWithGenePanelMatrix(STUDY_ID, SAMPLE_IDS))
        .thenReturn(List.of(profileId));
    var exporter = new GenePanelMatrixDatatypeExporter(new GenePanelMatrixService(repository));
    return exporter.getData(STUDY_ID, SAMPLE_IDS);
  }

  @Test
  public void keepsRepeatedStudyIdInProfileName() throws IOException {
    try (Table table = export("study_study_mutations")) {
      assertEquals(List.of("SAMPLE_ID", "study_mutations"), List.copyOf(table.getHeader()));

      var row = table.next();
      assertEquals(List.of("SAMPLE_ID", "study_mutations"), List.copyOf(row.keySet()));
      assertEquals("sample1", row.get("SAMPLE_ID"));
      assertEquals("PANEL1", row.get("study_mutations"));
      assertFalse(table.hasNext());
    }
  }

  @Test
  public void stripsSingleLeadingStudyPrefix() throws IOException {
    try (Table table = export("study_mutations")) {
      assertEquals(List.of("SAMPLE_ID", "mutations"), List.copyOf(table.getHeader()));
      assertEquals("PANEL1", table.next().get("mutations"));
    }
  }

  @Test
  public void leavesIdsWithoutStudyPrefixUnchanged() throws IOException {
    try (Table table = export("other_mutations")) {
      assertEquals(List.of("SAMPLE_ID", "other_mutations"), List.copyOf(table.getHeader()));
      assertEquals("PANEL1", table.next().get("other_mutations"));
    }
  }
}
