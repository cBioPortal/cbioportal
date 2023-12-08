package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.cbioportal.model.ClinicalAttributeCount;
import org.cbioportal.service.ClinicalAttributeService;
import org.cbioportal.web.config.InternalApiTags;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.ClinicalAttributeCountFilter;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@InternalApi
@RestController()
@RequestMapping("/api")
@Validated
@Tag(name = InternalApiTags.CLINICAL_ATTRIBUTES_COUNT, description = " ")
public class ClinicalAttributeCountController {

    @Autowired
    private ClinicalAttributeService clinicalAttributeService;
 
    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/clinical-attributes/counts/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get counts for clinical attributes according to their data availability for selected samples/patients")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClinicalAttributeCount.class))))
    public ResponseEntity<List<ClinicalAttributeCount>> getClinicalAttributeCounts(
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedClinicalAttributeCountFilter") ClinicalAttributeCountFilter interceptedClinicalAttributeCountFilter,
            @Parameter(required = true, description = "List of SampleIdentifiers or Sample List ID")
            @Valid @RequestBody(required = false) ClinicalAttributeCountFilter clinicalAttributeCountFilter) {

        List<ClinicalAttributeCount> clinicalAttributeCountList;
        if (interceptedClinicalAttributeCountFilter.getSampleListId() != null) {
            clinicalAttributeCountList = clinicalAttributeService.getClinicalAttributeCountsBySampleListId(
                interceptedClinicalAttributeCountFilter.getSampleListId());
        } else {
            List<SampleIdentifier> sampleIdentifiers = interceptedClinicalAttributeCountFilter.getSampleIdentifiers();
            List<String> studyIds = new ArrayList<>();
            List<String> sampleIds = new ArrayList<>();
            for (SampleIdentifier sampleIdentifier : sampleIdentifiers) {
                studyIds.add(sampleIdentifier.getStudyId());
                sampleIds.add(sampleIdentifier.getSampleId());
            }
            clinicalAttributeCountList = clinicalAttributeService.getClinicalAttributeCountsBySampleIds(studyIds, sampleIds);
        }

        return new ResponseEntity<>(clinicalAttributeCountList, HttpStatus.OK);
    }
}
