package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.cbioportal.model.ClinicalEvent;
import org.cbioportal.service.ClinicalEventService;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.web.config.InternalApiTags;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.*;
import org.cbioportal.web.parameter.sort.ClinicalEventSortBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@InternalApi
@RestController()
@RequestMapping("/api")
@Validated
@Tag(name = InternalApiTags.CLINICAL_EVENTS, description = " ")
public class ClinicalEventController {

    @Autowired
    private ClinicalEventService clinicalEventService;

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/studies/{studyId}/patients/{patientId}/clinical-events", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get all clinical events of a patient in a study")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClinicalEvent.class))))
    public ResponseEntity<List<ClinicalEvent>> getAllClinicalEventsOfPatientInStudy(
        @Parameter(required = true, description = "Study ID e.g. lgg_ucsf_2014")
        @PathVariable String studyId,
        @Parameter(required = true, description = "Patient ID e.g. P01")
        @PathVariable String patientId,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection,
        @Parameter(description = "Page size of the result list")
        @Max(PagingConstants.MAX_PAGE_SIZE)
        @Min(PagingConstants.MIN_PAGE_SIZE)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
        @Parameter(description = "Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
        @Parameter(description = "Name of the property that the result list is sorted by")
        @RequestParam(required = false) ClinicalEventSortBy sortBy,
        @Parameter(description = "Direction of the sort")
        @RequestParam(defaultValue = "ASC") Direction direction) throws PatientNotFoundException,
        StudyNotFoundException {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, clinicalEventService.getMetaPatientClinicalEvents(
                studyId, patientId).getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                clinicalEventService.getAllClinicalEventsOfPatientInStudy(
                    studyId, patientId, projection.name(), pageSize, pageNumber,
                    sortBy == null ? null : sortBy.getOriginalValue(), direction.name()), HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/studies/{studyId}/clinical-events", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get all clinical events in a study")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClinicalEvent.class))))
    public ResponseEntity<List<ClinicalEvent>> getAllClinicalEventsInStudy(
        @Parameter(required = true, description = "Study ID e.g. lgg_ucsf_2014")
        @PathVariable String studyId,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection,
        @Parameter(description = "Page size of the result list")
        @Max(PagingConstants.MAX_PAGE_SIZE)
        @Min(PagingConstants.MIN_PAGE_SIZE)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
        @Parameter(description = "Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
        @Parameter(description = "Name of the property that the result list is sorted by")
        @RequestParam(required = false) ClinicalEventSortBy sortBy,
        @Parameter(description = "Direction of the sort")
        @RequestParam(defaultValue = "ASC") Direction direction) throws PatientNotFoundException,
        StudyNotFoundException {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, clinicalEventService.getMetaClinicalEvents(
                studyId).getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                clinicalEventService.getAllClinicalEventsInStudy(
                    studyId, projection.name(), pageSize, pageNumber,
                    sortBy == null ? null : sortBy.getOriginalValue(), direction.name()), HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/clinical-events-meta/fetch",
        method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch clinical events meta")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClinicalEvent.class))))
    public ResponseEntity<List<ClinicalEvent>> fetchClinicalEventsMeta(
        @Parameter(required = true, description = "clinical events Request")
        @Valid @RequestBody(required = false) ClinicalEventAttributeRequest clinicalEventAttributeRequest,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface. This attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedClinicalEventAttributeRequest") ClinicalEventAttributeRequest interceptedClinicalEventAttributeRequest) {

        return new ResponseEntity<>(cachedClinicalEventsMeta(interceptedClinicalEventAttributeRequest), HttpStatus.OK);

    }

    @Cacheable(
        cacheResolver = "generalRepositoryCacheResolver",
        condition = "@cacheEnabledConfig.getEnabled()"
    )
    public List<ClinicalEvent> cachedClinicalEventsMeta(ClinicalEventAttributeRequest interceptedClinicalEventAttributeRequest) {
        List<String> studyIds = new ArrayList<>();
        List<String> patientIds = new ArrayList<>();
        for (PatientIdentifier patientIdentifier : interceptedClinicalEventAttributeRequest.getPatientIdentifiers()) {
            studyIds.add(patientIdentifier.getStudyId());
            patientIds.add(patientIdentifier.getPatientId());
        }

        List<ClinicalEvent> clinicalEventsRequest = interceptedClinicalEventAttributeRequest.getClinicalEventRequests()
            .stream()
            .map(x -> {
                ClinicalEvent clinicalEvent = new ClinicalEvent();
                clinicalEvent.setEventType(x.getEventType());
                clinicalEvent.setAttributes(x.getAttributes());
                return clinicalEvent;
            })
            .toList();
        return clinicalEventService.getClinicalEventsMeta(
            studyIds, patientIds, clinicalEventsRequest);
    }
}
