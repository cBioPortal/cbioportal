package org.cbioportal.application.file.export;

import org.cbioportal.application.file.export.services.CancerStudyMetadataService;
import org.cbioportal.application.file.export.services.ExportService;
import org.cbioportal.application.file.utils.ZipOutputStreamWriterFactory;
import org.cbioportal.legacy.utils.config.annotation.ConditionalOnProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedOutputStream;

@RestController
//How to have only one conditional on property in the config only
// https://stackoverflow.com/questions/62355615/define-a-spring-restcontroller-via-java-configuration
@ConditionalOnProperty(name = "dynamic_study_export_mode", havingValue = "true")
public class ExportController {

    private static final Logger LOG = LoggerFactory.getLogger(ExportController.class);
    private final ExportService exportService;
    private final CancerStudyMetadataService cancerStudyMetadataService;

    public ExportController(CancerStudyMetadataService cancerStudyMetadataService, ExportService exportService) {
        this.exportService = exportService;
        this.cancerStudyMetadataService = cancerStudyMetadataService;
    }

    @GetMapping("/export/study/{studyId}.zip")
    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    public ResponseEntity<StreamingResponseBody> downloadStudyData(@PathVariable String studyId) {
        if (cancerStudyMetadataService.getCancerStudyMetadata(studyId) == null) {
            return ResponseEntity.notFound().build();
        }

        StreamingResponseBody stream = outputStream -> {
            try (BufferedOutputStream bos = new BufferedOutputStream(outputStream);
                 ZipOutputStreamWriterFactory zipFactory = new ZipOutputStreamWriterFactory(bos)) {
                exportService.exportData(zipFactory, studyId);
            }
        };

        return ResponseEntity.ok()
            .contentType(new MediaType("application", "zip"))
            .header("Content-Disposition", "attachment; filename=\"" + studyId + ".zip\"")
            .body(stream);
    }
}
