package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.ClinicalEvent;
import org.cbioportal.service.ClinicalEventService;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.web.config.InternalApiTags;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.*;
import org.cbioportal.web.parameter.sort.ClinicalEventSortBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@InternalApi
@RestController
@Validated
@Api(tags = InternalApiTags.CLINICAL_EVENTS, description = " ")
public class ClinicalEventController {

    @Autowired
    private ClinicalEventService clinicalEventService;

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/studies/{studyId}/patients/{patientId}/clinical-events", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get all clinical events of a patient in a study")
    public ResponseEntity<List<ClinicalEvent>> getAllClinicalEventsOfPatientInStudy(
        @ApiParam(required = true, value = "Study ID e.g. lgg_ucsf_2014")
        @PathVariable String studyId,
        @ApiParam(required = true, value = "Patient ID e.g. P01")
        @PathVariable String patientId,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection,
        @ApiParam("Page size of the result list")
        @Max(PagingConstants.MAX_PAGE_SIZE)
        @Min(PagingConstants.MIN_PAGE_SIZE)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
        @ApiParam("Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
        @ApiParam("Name of the property that the result list is sorted by")
        @RequestParam(required = false) ClinicalEventSortBy sortBy,
        @ApiParam("Direction of the sort")
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
    @ApiOperation("Get all clinical events in a study")
    public ResponseEntity<List<ClinicalEvent>> getAllClinicalEventsInStudy(
        @ApiParam(required = true, value = "Study ID e.g. lgg_ucsf_2014")
        @PathVariable String studyId,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection,
        @ApiParam("Page size of the result list")
        @Max(PagingConstants.MAX_PAGE_SIZE)
        @Min(PagingConstants.MIN_PAGE_SIZE)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
        @ApiParam("Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
        @ApiParam("Name of the property that the result list is sorted by")
        @RequestParam(required = false) ClinicalEventSortBy sortBy,
        @ApiParam("Direction of the sort")
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
    @ApiOperation("Fetch clinical events meta")
    public ResponseEntity<List<ClinicalEvent>> fetchClinicalEventsMeta(
        @ApiParam(required = true, value = "clinical events Request")
        @Valid @RequestBody(required = false) ClinicalEventAttributeRequest clinicalEventAttributeRequest,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore
        // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedClinicalEventAttributeRequest") ClinicalEventAttributeRequest interceptedClinicalEventAttributeRequest) {

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
            .collect(Collectors.toList());

        return new ResponseEntity<>(clinicalEventService.getClinicalEventsMeta(
            studyIds, patientIds, clinicalEventsRequest), HttpStatus.OK);

    }
}
