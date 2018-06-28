package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.Patient;
import org.cbioportal.service.PatientService;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.*;
import org.cbioportal.web.parameter.sort.PatientSortBy;
import org.cbioportal.web.util.UniqueKeyExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;

@PublicApi
@RestController
@Validated
@Api(tags = "Patients", description = " ")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private UniqueKeyExtractor uniqueKeyExtractor;

    @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    @RequestMapping(value = "/studies/{studyId}/patients", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get all patients in a study")
    public ResponseEntity<List<Patient>> getAllPatientsInStudy(
        @ApiParam(required = true, value = "Study ID e.g. acc_tcga")
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
        @RequestParam(required = false) PatientSortBy sortBy,
        @ApiParam("Direction of the sort")
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

    @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    @RequestMapping(value = "/studies/{studyId}/patients/{patientId}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get a patient in a study")
    public ResponseEntity<Patient> getPatientInStudy(
        @ApiParam(required = true, value = "Study ID e.g. acc_tcga")
        @PathVariable String studyId,
        @ApiParam(required = true, value = "Patient ID e.g. TCGA-OR-A5J2")
        @PathVariable String patientId) throws PatientNotFoundException, StudyNotFoundException {

        return new ResponseEntity<>(patientService.getPatientInStudy(studyId, patientId), HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#patientFilter, 'PatientFilter', 'read')")
    @RequestMapping(value = "/patients/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch patients by ID")
    public ResponseEntity<List<Patient>> fetchPatients(
        @ApiParam(required = true, value = "List of patient identifiers")
        @Valid @RequestBody PatientFilter patientFilter,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) {

        List<String> studyIds = new ArrayList<>();
        List<String> patientIds = new ArrayList<>();

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            if (patientFilter.getPatientIdentifiers() != null) {
                extractStudyAndPatientIds(patientFilter, studyIds, patientIds);
            } else {
                uniqueKeyExtractor.extractUniqueKeys(patientFilter.getUniquePatientKeys(), studyIds, patientIds);
            }
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, patientService.fetchMetaPatients(studyIds, patientIds)
                .getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            if (patientFilter.getPatientIdentifiers() != null) {
                extractStudyAndPatientIds(patientFilter, studyIds, patientIds);
            } else {
                uniqueKeyExtractor.extractUniqueKeys(patientFilter.getUniquePatientKeys(), studyIds, patientIds);
            }
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
