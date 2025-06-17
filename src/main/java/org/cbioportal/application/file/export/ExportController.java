package org.cbioportal.application.file.export;

import java.io.BufferedOutputStream;
import org.cbioportal.application.file.export.exporters.ExportDetails;
import org.cbioportal.application.file.export.services.VirtualStudyAwareExportService;
import org.cbioportal.application.file.export.services.ZipOutputStreamWriterService;
import org.cbioportal.legacy.utils.config.annotation.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
// How to have only one conditional on property in the config only
// https://stackoverflow.com/questions/62355615/define-a-spring-restcontroller-via-java-configuration
@ConditionalOnProperty(name = "dynamic_study_export_mode", havingValue = "true")
public class ExportController {

  private final VirtualStudyAwareExportService exportService;

  public ExportController(VirtualStudyAwareExportService exportService) {
    this.exportService = exportService;
  }

  @GetMapping("/export/study/{studyId}.zip")
  public ResponseEntity<StreamingResponseBody> downloadStudyData(@PathVariable String studyId) {
    if (!exportService.isStudyExportable(studyId)) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok()
        .contentType(new MediaType("application", "zip"))
        .header("Content-Disposition", "attachment; filename=\"" + studyId + ".zip\"")
        .body(
            outputStream -> {
              try (BufferedOutputStream bos = new BufferedOutputStream(outputStream);
                  ZipOutputStreamWriterService zipFactory = new ZipOutputStreamWriterService(bos)) {
                exportService.exportData(zipFactory, new ExportDetails(studyId));
              }
            });
  }
}
