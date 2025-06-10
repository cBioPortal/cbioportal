package org.cbioportal.application.file.export.services;

import java.util.List;
import org.cbioportal.application.file.export.exporters.ExportDetails;
import org.cbioportal.application.file.export.exporters.Exporter;
import org.cbioportal.application.file.model.CancerStudyMetadata;
import org.cbioportal.application.file.utils.FileWriterFactory;
import org.cbioportal.application.security.CancerStudyPermissionEvaluator;
import org.cbioportal.legacy.utils.security.AccessLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

public class ExportService implements Exporter {

  private static final Logger LOG = LoggerFactory.getLogger(ExportService.class);

  private final CancerStudyMetadataService cancerStudyMetadataService;
  private final List<Exporter> exporters;
  private final CancerStudyPermissionEvaluator cancerStudyPermissionEvaluator;

  public ExportService(
      CancerStudyMetadataService cancerStudyMetadataService,
      CancerStudyPermissionEvaluator cancerStudyPermissionEvaluator,
      List<Exporter> exporters) {
    this.cancerStudyMetadataService = cancerStudyMetadataService;
    this.cancerStudyPermissionEvaluator = cancerStudyPermissionEvaluator;
    this.exporters = exporters;
  }

  public boolean isStudyExportable(String studyId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    CancerStudyMetadata cancerStudyMetadata =
        this.cancerStudyMetadataService.getCancerStudyMetadata(studyId);
    return cancerStudyMetadata != null
        && (authentication == null
            || cancerStudyPermissionEvaluator == null
            || cancerStudyPermissionEvaluator.hasPermission(
                authentication, studyId, "CancerStudyId", AccessLevel.READ));
  }

  @Transactional
  @PreAuthorize(
      "hasPermission(#exportDetails.studyId, 'CancerStudyId', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @Override
  public boolean exportData(FileWriterFactory fileWriterFactory, ExportDetails exportDetails) {
    boolean atLeastOneDataFileExportedSuccesfully = false;
    for (Exporter exporter : exporters) {
      try {
        LOG.debug(
            "Exporting data for studyId: {} using exporter: {}",
            exportDetails.getStudyId(),
            exporter.getClass().getSimpleName());
        boolean exportedDataType = exporter.exportData(fileWriterFactory, exportDetails);
        LOG.debug(
            "{} data for studyId: {} using exporter: {}",
            exportedDataType ? "Exported" : "No data exported",
            exportDetails.getStudyId(),
            exporter.getClass().getSimpleName());
        atLeastOneDataFileExportedSuccesfully |= exportedDataType;
      } catch (Exception e) {
        LOG.error(
            "Error exporting data for study {}: {}. The file will be intentionally corrupted.",
            exportDetails.getStudyId(),
            e.getMessage(),
            e);
        fileWriterFactory.fail(e);
      }
    }
    return atLeastOneDataFileExportedSuccesfully;
  }
}
