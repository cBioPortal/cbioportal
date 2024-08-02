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
import org.cbioportal.model.Patient;
import org.cbioportal.service.PatientService;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.web.config.PublicApiTags;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.Direction;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.PatientFilter;
import org.cbioportal.web.parameter.PatientIdentifier;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.sort.PatientSortBy;
import org.cbioportal.web.util.UniqueKeyExtractor;
import org.springframework.beans.factory.annotation.Autowired;
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

@PublicApi
@RestController()
@RequestMapping("/api")
@Validated
@Tag(name = PublicApiTags.PATIENTS, description = " ")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @RequestMapping(value = "/patients", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get all patients")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = Patient.class))))
    public ResponseEntity<List<Patient>> getAllPatients(
        @Parameter(description = "Search keyword that applies to ID of the patients")
        @RequestParam(required = false) String keyword,
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
        @RequestParam(required = false) PatientSortBy sortBy,
        @Parameter(description = "Direction of the sort")
        @RequestParam(defaultValue = "ASC") Direction direction) throws StudyNotFoundException {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, patientService.getMetaPatients(keyword)
                .getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                patientService.getAllPatients(keyword, projection.name(), pageSize, pageNumber,
                    sortBy == null ? null : sortBy.getOriginalValue(), direction.name()), HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/studies/{studyId}/patients", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get all patients in a study")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = Patient.class))))
    public ResponseEntity<List<Patient>> getAllPatientsInStudy(
        @Parameter(required = true, description = "Study ID e.g. acc_tcga")
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
        @RequestParam(required = false) PatientSortBy sortBy,
        @Parameter(description = "Direction of the sort")
        @RequestParam(defaultValue = "ASC") Direction direction) throws StudyNotFoundException {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, patientService.getMetaPatientsInStudy(studyId)
                .getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                patientService.getAllPatientsInStudy(studyId, projection.name(), pageSize, pageNumber,
                    sortBy == null ? null : sortBy.getOriginalValue(), direction.name()), HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/studies/{studyId}/patients/{patientId}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get a patient in a study")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(schema = @Schema(implementation = Patient.class)))
    public ResponseEntity<Patient> getPatientInStudy(
        @Parameter(required = true, description = "Study ID e.g. acc_tcga")
        @PathVariable String studyId,
        @Parameter(required = true, description = "Patient ID e.g. TCGA-OR-A5J2")
        @PathVariable String patientId) throws PatientNotFoundException, StudyNotFoundException {

        return new ResponseEntity<>(patientService.getPatientInStudy(studyId, patientId), HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/patients/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = Patient.class)))) 
    public ResponseEntity<List<Patient>> fetchPatients(
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedPatientFilter") PatientFilter interceptedPatientFilter,
        @Parameter(required = true, description = "List of patient identifiers")
        @Valid @RequestBody(required = false) PatientFilter patientFilter,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) {

        List<String> studyIds = new ArrayList<>();
        List<String> patientIds = new ArrayList<>();

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            if (interceptedPatientFilter.getPatientIdentifiers() != null) {
                extractStudyAndPatientIds(interceptedPatientFilter, studyIds, patientIds);
            } else {
                UniqueKeyExtractor.extractUniqueKeys(interceptedPatientFilter.getUniquePatientKeys(), studyIds, patientIds);
            }
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, patientService.fetchMetaPatients(studyIds, patientIds)
                .getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            if (interceptedPatientFilter.getPatientIdentifiers() != null) {
                extractStudyAndPatientIds(interceptedPatientFilter, studyIds, patientIds);
            } else {
                UniqueKeyExtractor.extractUniqueKeys(interceptedPatientFilter.getUniquePatientKeys(), studyIds, patientIds);
            }
            //TODO: since we are already extracting the studyIds in the interceptor, we might not need to use the service here
            return new ResponseEntity<>(
                patientService.fetchPatients(studyIds, patientIds, projection.name()), HttpStatus.OK);
        }
    }

    private void extractStudyAndPatientIds(PatientFilter patientFilter, List<String> studyIds, List<String> patientIds) {

        for (PatientIdentifier patientIdentifier : patientFilter.getPatientIdentifiers()) {
            studyIds.add(patientIdentifier.getStudyId());
            patientIds.add(patientIdentifier.getPatientId());
        }
    }
}
