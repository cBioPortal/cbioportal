package org.cbioportal.web;

import org.cbioportal.service.impl.ExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    //TODO make it work for virtual studies as well
    //@PreAuthorize("hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @GetMapping("/export/study/{studyId}.zip")
    public ResponseEntity<byte[]> downloadStudyData(@PathVariable String studyId) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = exportService.exportStudyDataToZip(studyId);

        // Build the response
        byte[] zipBytes = byteArrayOutputStream.toByteArray();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "zip"));
        headers.setContentDispositionFormData("attachment", studyId + ".zip");

        return ResponseEntity.ok()
            .headers(headers)
            .body(zipBytes);
    }
}
