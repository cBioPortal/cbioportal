package org.cbioportal.application.file.export;

import jakarta.servlet.http.HttpServletResponse;
import org.cbioportal.application.file.export.services.ExportService;
import org.cbioportal.application.file.utils.ZipOutputStreamWriterFactory;
import org.cbioportal.legacy.utils.config.annotation.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@RestController
//How to have only one conditional on property in the config only
// https://stackoverflow.com/questions/62355615/define-a-spring-restcontroller-via-java-configuration
@ConditionalOnProperty(name = "dynamic_study_export_mode", havingValue = "true")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @GetMapping("/export/study/{studyId}.zip")
    public void downloadStudyData(HttpServletResponse response, @PathVariable String studyId) throws IOException {

        response.setContentType(("application/zip"));
        response.setHeader("Content-Disposition", "attachment; filename=\"" + studyId + ".zip\"");

        try (OutputStream out = response.getOutputStream(); BufferedOutputStream bof = new BufferedOutputStream(out); ZipOutputStreamWriterFactory zipOutputStreamWriterFactory = new ZipOutputStreamWriterFactory(bof)) {
            if (!exportService.exportData(zipOutputStreamWriterFactory, studyId)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND); 
            }
        }
    }
}
