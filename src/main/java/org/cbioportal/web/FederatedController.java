package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.service.FederatedService;
import org.cbioportal.service.exception.FederationException;
import org.cbioportal.web.config.PublicApiTags;
import org.cbioportal.web.config.annotation.FederatedApi;
import org.cbioportal.web.parameter.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FederatedApi
@RestController()
@RequestMapping("/api-fed")
@Validated
@Tag(name = PublicApiTags.CLINICAL_ATTRIBUTES, description = " ")
public class FederatedController {
    
    @Autowired
    private FederatedService federatedService;

    @RequestMapping(value = "/clinical-attributes/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch clinical attributes")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClinicalAttribute.class))))
    public ResponseEntity<List<ClinicalAttribute>> fetchClinicalAttributes() throws FederationException {

        return new ResponseEntity<>(federatedService.fetchClinicalAttributes(), HttpStatus.OK);
    }

    @RequestMapping(value = "/clinical-data-counts/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch clinical data counts by study view filter")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClinicalDataCountItem.class))))
    public ResponseEntity<List<ClinicalDataCountItem>> fetchClinicalDataCounts(
        @Parameter(required = true, description = "Clinical data count filter")
        @Valid @RequestBody(required = false)  ClinicalDataCountFilter clinicalDataCountFilter
    ) throws FederationException {
        
        var result = federatedService.fetchClinicalDataCounts(
            clinicalDataCountFilter
        );
        return new ResponseEntity<>(result, HttpStatus.OK);

    }

    @RequestMapping(value = "/clinical-data-bin-counts/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch clinical data bin counts by study view filter")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClinicalDataBin.class))))
    public ResponseEntity<List<ClinicalDataBin>> fetchClinicalDataBinCounts(
        @Parameter(required = true, description = "Clinical data bin count filter")
        @Valid @RequestBody(required = false) ClinicalDataBinCountFilter clinicalDataBinCountFilter
    ) throws FederationException {
        var clinicalDataBins = federatedService.fetchClinicalDataBinCounts(
            clinicalDataBinCountFilter
        );

        return new ResponseEntity<>(clinicalDataBins, HttpStatus.OK);
    }
}
