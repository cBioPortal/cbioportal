package org.cbioportal.application.file.export.exporters;

import java.util.List;
import java.util.Optional;
import java.util.SequencedMap;
import org.cbioportal.application.file.export.services.CaseListMetadataService;
import org.cbioportal.application.file.model.CaseListMetadata;
import org.cbioportal.application.file.utils.FileWriterFactory;

/** Exports all case lists for a study */
public class CaseListsExporter implements Exporter {

  private final CaseListMetadataService caseListMetadataService;

  public CaseListsExporter(CaseListMetadataService caseListMetadataService) {
    this.caseListMetadataService = caseListMetadataService;
  }

  @Override
  public boolean exportData(FileWriterFactory fileWriterFactory, ExportDetails exportDetails) {
    List<CaseListMetadata> caseLists =
        caseListMetadataService.getCaseListsMetadata(
            exportDetails.getStudyId(), exportDetails.getSampleIds());
    boolean exported = false;
    for (CaseListMetadata metadata : caseLists) {
      exported |= new CaseListExporter(metadata).exportData(fileWriterFactory, exportDetails);
    }
    return exported;
  }

  /** Exports a case list metadata to a file */
  public static class CaseListExporter extends MetadataExporter<CaseListMetadata> {

    private final CaseListMetadata caseListMetadata;

    public CaseListExporter(CaseListMetadata caseListMetadata) {
      this.caseListMetadata = caseListMetadata;
    }

    @Override
    public String getMetaFilename(CaseListMetadata metadata) {
      return "case_lists/cases_" + metadata.getCaseListTypeStableId() + ".txt";
    }

    @Override
    protected Optional<CaseListMetadata> getMetadata(String studyId) {
      return Optional.of(caseListMetadata);
    }

    @Override
    protected void updateMetadata(
        ExportDetails exportDetails, SequencedMap<String, String> metadataSeqMap) {
      super.updateMetadata(exportDetails, metadataSeqMap);
      if (exportDetails.getExportWithStudyId() != null) {
        metadataSeqMap.put(
            "stable_id",
            exportDetails.getExportWithStudyId()
                + "_"
                + caseListMetadata.getCaseListTypeStableId());
      }
    }
  }
}
