package org.cbioportal.application.file.export.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import org.cbioportal.application.file.export.ExportException;
import org.cbioportal.application.file.export.exporters.ExportDetails;
import org.cbioportal.application.file.export.exporters.Exporter;
import org.cbioportal.application.file.utils.FileWriterFactory;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.web.parameter.VirtualStudy;
import org.cbioportal.legacy.web.parameter.VirtualStudyData;
import org.cbioportal.legacy.web.parameter.VirtualStudySamples;

public class VirtualStudyExportDecoratorService implements Exporter {

  private final VirtualStudyService virtualStudyService;
  private final ExportService exportService;

  public VirtualStudyExportDecoratorService(
      VirtualStudyService virtualStudyService, ExportService exportService) {
    this.virtualStudyService = virtualStudyService;
    this.exportService = exportService;
  }

  // FIXME: fix export for published study when
  // feature.published_virtual_study.single-sourced.backend-mode is on
  public boolean isStudyExportable(String studyId) {
    var virtualStudy = virtualStudyService.getVirtualStudyByIdIfExists(studyId);
    return virtualStudy
        .map(
            study ->
                study.getData().getStudies().stream()
                    .map(VirtualStudySamples::getId)
                    .allMatch(exportService::isStudyExportable))
        .orElseGet(() -> exportService.isStudyExportable(studyId));
  }

  @Override
  public boolean exportData(FileWriterFactory fileWriterFactory, ExportDetails exportDetails) {
    var virtualStudyOpt =
        virtualStudyService.getVirtualStudyByIdIfExists(exportDetails.getStudyId());
    if (virtualStudyOpt.isEmpty()) {
      return exportService.exportData(fileWriterFactory, exportDetails);
    } else {
      VirtualStudy virtualStudy = virtualStudyOpt.get();
      VirtualStudyData virtualStudyData = virtualStudy.getData();
      Set<VirtualStudySamples> virtualStudySamples = virtualStudyData.getStudies();
      boolean exported =
          exportVirtualStudySamples(
              fileWriterFactory, exportDetails, virtualStudyData, virtualStudySamples);
      if (exported) {
        writeVirtualStudyDefinitionJsonFile(fileWriterFactory, virtualStudy);
      }
      return exported;
    }
  }

  private boolean exportVirtualStudySamples(
      FileWriterFactory fileWriterFactory,
      ExportDetails exportDetails,
      VirtualStudyData virtualStudyData,
      Set<VirtualStudySamples> virtualStudySamples) {

    if (virtualStudySamples.size() == 1) {
      return exportSingleStudy(
          fileWriterFactory,
          exportDetails,
          virtualStudyData,
          virtualStudySamples.iterator().next());
    } else {
      return exportMultipleStudies(fileWriterFactory, exportDetails, virtualStudySamples);
    }
  }

  private boolean exportSingleStudy(
      FileWriterFactory fileWriterFactory,
      ExportDetails exportDetails,
      VirtualStudyData virtualStudyData,
      VirtualStudySamples virtualStudySample) {

    ExportDetails newExportDetails =
        new ExportDetails(
            virtualStudySample.getId(),
            exportDetails.getStudyId(),
            virtualStudySample.getSamples(),
            virtualStudyData.getName(),
            virtualStudyData.getDescription(),
            virtualStudyData.getPmid(),
            virtualStudyData.getTypeOfCancerId());
    return exportService.exportData(fileWriterFactory, newExportDetails);
  }

  private boolean exportMultipleStudies(
      FileWriterFactory fileWriterFactory,
      ExportDetails exportDetails,
      Set<VirtualStudySamples> virtualStudySamples) {

    boolean allStudiesExported = true;
    for (VirtualStudySamples virtualStudySample : virtualStudySamples) {
      String exportAsStudyId = exportDetails.getStudyId() + "_" + virtualStudySample.getId();
      fileWriterFactory.setBasePath(exportAsStudyId);
      ExportDetails newExportDetails =
          new ExportDetails(
              virtualStudySample.getId(), exportAsStudyId, virtualStudySample.getSamples());
      allStudiesExported &= exportService.exportData(fileWriterFactory, newExportDetails);
    }
    fileWriterFactory.setBasePath(null);
    return allStudiesExported;
  }

  private static void writeVirtualStudyDefinitionJsonFile(
      FileWriterFactory fileWriterFactory, VirtualStudy virtualStudy) {
    try (Writer writer = fileWriterFactory.newWriter("virtual_study_definition.json")) {
      ObjectMapper mapper = new ObjectMapper();
      String json = mapper.writeValueAsString(virtualStudy);
      writer.write(json);
    } catch (IOException e) {
      throw new ExportException(
          "Error while writing virtual study definition JSON file for study "
              + virtualStudy.getId(),
          e);
    }
  }
}
