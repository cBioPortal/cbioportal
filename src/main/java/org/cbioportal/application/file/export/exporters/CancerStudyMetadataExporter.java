package org.cbioportal.application.file.export.exporters;

import java.util.Optional;
import java.util.SequencedMap;
import org.cbioportal.application.file.export.services.CancerStudyMetadataService;
import org.cbioportal.application.file.model.CancerStudyMetadata;

/** Exports metadata for a cancer study */
public class CancerStudyMetadataExporter extends MetadataExporter<CancerStudyMetadata> {

  private final CancerStudyMetadataService cancerStudyMetadataService;

  public CancerStudyMetadataExporter(CancerStudyMetadataService cancerStudyMetadataService) {
    this.cancerStudyMetadataService = cancerStudyMetadataService;
  }

  @Override
  public String getMetaFilename(CancerStudyMetadata metadata) {
    return "meta_study.txt";
  }

  @Override
  protected Optional<CancerStudyMetadata> getMetadata(String studyId) {
    return Optional.ofNullable(cancerStudyMetadataService.getCancerStudyMetadata(studyId));
  }

  @Override
  protected void updateMetadata(
      ExportDetails exportDetails, SequencedMap<String, String> metadataSeqMap) {
    super.updateMetadata(exportDetails, metadataSeqMap);
    // used primarily while downloading a Virtual Study
    CancerStudyMetadata alternativeCancerStudyMetadata = new CancerStudyMetadata();
    alternativeCancerStudyMetadata.setCancerStudyIdentifier(exportDetails.getExportWithStudyId());
    alternativeCancerStudyMetadata.setName(exportDetails.getExportWithStudyName());
    alternativeCancerStudyMetadata.setDescription(exportDetails.getExportAsStudyDescription());
    alternativeCancerStudyMetadata.setPmid(exportDetails.getExportWithStudyPmid());
    alternativeCancerStudyMetadata.setTypeOfCancer(
        exportDetails.getExportWithStudyTypeOfCancerId());
    alternativeCancerStudyMetadata
        .toMetadataKeyValues()
        .forEach(
            (key, value) -> {
              if (value != null && metadataSeqMap.containsKey(key)) {
                metadataSeqMap.put(key, value);
              }
            });
  }
}
