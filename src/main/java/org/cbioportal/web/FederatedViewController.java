package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.service.FederatedViewService;
import org.cbioportal.service.exception.FederationException;
import org.cbioportal.web.config.PublicApiTags;
import org.cbioportal.web.config.annotation.FederatedApi;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@FederatedApi
@RestController()
@RequestMapping("/api-fed")
@Validated
@Tag(name = PublicApiTags.CLINICAL_ATTRIBUTES, description = " ")
public class FederatedViewController {
    
    @Autowired
    private FederatedViewService federatedViewService;
    
    @PreAuthorize("hasPermission(#studyIds, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/clinical-attributes/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch clinical attributes")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClinicalAttribute.class))))
    public ResponseEntity<List<ClinicalAttribute>> fetchClinicalAttributes(
        @Parameter(required = true, description = "List of Study IDs")
        @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
        @RequestBody List<String> studyIds,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) throws FederationException {

        return new ResponseEntity<>(federatedViewService.fetchClinicalAttributes(studyIds, projection.name()), HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/clinical-data-counts/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch clinical data counts by study view filter")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClinicalDataCountItem.class))))
    public ResponseEntity<List<ClinicalDataCountItem>> fetchClinicalDataCounts(
        @Parameter(required = true, description = "Clinical data count filter")
        @Valid @RequestBody(required = false)  ClinicalDataCountFilter clinicalDataCountFilter,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedClinicalDataCountFilter") ClinicalDataCountFilter interceptedClinicalDataCountFilter
    ) throws FederationException {
        
        var result = federatedViewService.fetchClinicalDataCounts(
            interceptedClinicalDataCountFilter
        );
        return new ResponseEntity<>(result, HttpStatus.OK);

    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/clinical-data-bin-counts/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch clinical data bin counts by study view filter")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClinicalDataBin.class))))
    public ResponseEntity<List<ClinicalDataBin>> fetchClinicalDataBinCounts(
        @Parameter(description = "Method for data binning")
        @RequestParam(defaultValue = "DYNAMIC") DataBinMethod dataBinMethod,
        @Parameter(required = true, description = "Clinical data bin count filter")
        @Valid @RequestBody(required = false) ClinicalDataBinCountFilter clinicalDataBinCountFilter,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedClinicalDataBinCountFilter") ClinicalDataBinCountFilter interceptedClinicalDataBinCountFilter
    ) throws FederationException {
        var clinicalDataBins = federatedViewService.fetchClinicalDataBinCounts(
            interceptedClinicalDataBinCountFilter,
            dataBinMethod
        );

        return new ResponseEntity<>(clinicalDataBins, HttpStatus.OK);
    }
}
