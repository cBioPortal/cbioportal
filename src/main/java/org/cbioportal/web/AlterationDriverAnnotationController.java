package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.cbioportal.model.CustomDriverAnnotationReport;
import org.cbioportal.service.AlterationDriverAnnotationService;
import org.cbioportal.web.config.annotation.InternalApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@InternalApi
@RestController()
@RequestMapping("/api")
@Validated
@Tag(name = "Custom driver annotations", description = " ")
public class AlterationDriverAnnotationController {

    @Autowired
    private AlterationDriverAnnotationService alterationDriverAnnotationService;

    @PreAuthorize("hasPermission(#molecularProfileIds, 'Collection<MolecularProfileId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/custom-driver-annotation-report/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Return availability of custom driver annotations for molecular profiles")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(schema = @Schema(implementation = CustomDriverAnnotationReport.class)))
    public ResponseEntity<CustomDriverAnnotationReport> fetchAlterationDriverAnnotationReport(
        @Parameter(required = true, description = "List of Molecular Profile Ids")
        @RequestBody List<String> molecularProfileIds) {

        CustomDriverAnnotationReport customDriverAnnotationReport = alterationDriverAnnotationService.getCustomDriverAnnotationProps(molecularProfileIds);

        return new ResponseEntity<>(customDriverAnnotationReport, HttpStatus.OK);
    }
}

