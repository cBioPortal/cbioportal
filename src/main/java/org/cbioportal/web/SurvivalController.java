package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.service.ClinicalEventService;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.PatientIdentifier;
import org.cbioportal.web.parameter.SurvivalRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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
@Tag(name = "Survival", description = " ")
public class SurvivalController {
    private final ClinicalEventService clinicalEventService;

    @Autowired
    public SurvivalController(ClinicalEventService clinicalEventService) {
        this.clinicalEventService = clinicalEventService;
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/survival-data/fetch",
        method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch survival data")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClinicalData.class))))
    public ResponseEntity<List<ClinicalData>> fetchSurvivalData(
        @Parameter(required = true, description = "Survival Data Request")
        @Valid @RequestBody(required = false) SurvivalRequest survivalRequest,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @Parameter(hidden = true)
        // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedSurvivalRequest") SurvivalRequest interceptedSurvivalRequest) {

        return new ResponseEntity<>(cachedSurvivalData(interceptedSurvivalRequest),
                                    HttpStatus.OK);
    }

    @Cacheable(
        cacheResolver = "generalRepositoryCacheResolver",
        condition = "@cacheEnabledConfig.getEnabled()"
    )
    public List<ClinicalData> cachedSurvivalData(SurvivalRequest interceptedSurvivalRequest) {
        List<String> studyIds = new ArrayList<>();
        List<String> patientIds = new ArrayList<>();
        for (PatientIdentifier patientIdentifier : interceptedSurvivalRequest.getPatientIdentifiers()) {
            studyIds.add(patientIdentifier.getStudyId());
            patientIds.add(patientIdentifier.getPatientId());
        }

        return clinicalEventService.getSurvivalData(studyIds,
                                                                         patientIds,
                                                                         interceptedSurvivalRequest.getAttributeIdPrefix(),
                                                                         interceptedSurvivalRequest);
    }
}
