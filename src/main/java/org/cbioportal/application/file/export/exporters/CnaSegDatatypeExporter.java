package org.cbioportal.application.file.export.exporters;

import java.util.Optional;
import java.util.Set;
import org.cbioportal.application.file.export.services.CancerStudyMetadataService;
import org.cbioportal.application.file.export.services.CnaSegmentService;
import org.cbioportal.application.file.model.CancerStudyMetadata;
import org.cbioportal.application.file.model.CnaSegMetadata;
import org.cbioportal.application.file.model.CnaSegment;
import org.cbioportal.application.file.model.Table;

public class CnaSegDatatypeExporter extends DataTypeExporter<CnaSegMetadata, Table> {

  private final CancerStudyMetadataService cancerStudyMetadataService;
  private final CnaSegmentService cnaSegmentService;

  public CnaSegDatatypeExporter(
      CancerStudyMetadataService cancerStudyMetadataService, CnaSegmentService cnaSegmentService) {
    this.cancerStudyMetadataService = cancerStudyMetadataService;
    this.cnaSegmentService = cnaSegmentService;
  }

  @Override
  protected Optional<CnaSegMetadata> getMetadata(String studyId, Set<String> sampleIds) {
    if (!this.cnaSegmentService.hasCnaSegments(studyId, sampleIds)) {
      return Optional.empty();
    }
    CnaSegMetadata metadata = new CnaSegMetadata();
    metadata.setCancerStudyIdentifier(studyId);
    metadata.setGeneticAlterationType("COPY_NUMBER_ALTERATION");
    metadata.setDatatype("SEG");
    // Compulsory fields
    metadata.setDescription("Copy number alteration segments");
    CancerStudyMetadata cancerStudyMetadata =
        cancerStudyMetadataService.getCancerStudyMetadata(studyId);
    metadata.setReferenceGenomeId(cancerStudyMetadata.getReferenceGenome());

    return Optional.of(metadata);
  }

  @Override
  protected Table getData(String studyId, Set<String> sampleIds) {
    return new Table(cnaSegmentService.getCnaSegments(studyId, sampleIds), CnaSegment.getHeader());
  }
}
