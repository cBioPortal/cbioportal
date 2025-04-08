package org.cbioportal.application.rest.vcolumnstore;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.cbioportal.domain.clinical_data.usecase.GetClinicalDataUseCase;
import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.web.parameter.ClinicalDataMultiStudyFilter;
import org.cbioportal.shared.enums.ClinicalDataType;
import org.cbioportal.shared.enums.ProjectionType;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing and retrieving cancer study metadata from a column-store data source.
 * <p>
 * This controller provides an endpoint to fetch cancer study metadata with support for filtering,
 * sorting, and controlling the level of detail in the response. It is designed to work with a
 * column-store database, which is optimized for querying large datasets efficiently.
 * </p>
 *
 * @see org.cbioportal.domain.clinical_data.usecase.GetSampleClinicalDataUseCase
 */
@RestController
@RequestMapping("/api/column-store")
@Profile("clickhouse")
public class ColumnStoreClinicalDataController {
    
    private final GetClinicalDataUseCase getClinicalDataUseCase;
    
    public ColumnStoreClinicalDataController(GetClinicalDataUseCase getClinicalDataUseCase) {
        this.getClinicalDataUseCase = getClinicalDataUseCase;
    }

    @Hidden
    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
    @PostMapping(value = "/clinical-data/fetch", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch clinical data by patient IDs or sample IDs (all studies)")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClinicalData.class))))
    public ResponseEntity<List<ClinicalData>> fetchClinicalData(
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestBody(required = false) ClinicalDataMultiStudyFilter interceptedClinicalDataMultiStudyFilter,
        @Parameter(description = "Type of the clinical data")
        @RequestParam(defaultValue = "SAMPLE") ClinicalDataType clinicalDataType,
        @Parameter(required = true, description = "List of patient or sample identifiers and attribute IDs")
        @Valid @RequestBody(required = false) ClinicalDataMultiStudyFilter clinicalDataMultiStudyFilter,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") ProjectionType projection) {
        
        return ResponseEntity.ok(getClinicalDataUseCase.execute(interceptedClinicalDataMultiStudyFilter, interceptedClinicalDataMultiStudyFilter.getAttributeIds(), clinicalDataType, projection));
    }
}
