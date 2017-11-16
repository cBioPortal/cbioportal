package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.Sample;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.service.exception.SampleNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.*;
import org.cbioportal.web.parameter.sort.SampleSortBy;
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

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@PublicApi
@RestController
@Validated
@Api(tags = "Samples", description = " ")
public class SampleController {

    public static final int SAMPLE_MAX_PAGE_SIZE = 100000;
    private static final String SAMPLE_DEFAULT_PAGE_SIZE = "100000";

    @Autowired
    private SampleService sampleService;

    @RequestMapping(value = "/studies/{studyId}/samples", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get all samples in a study")
    public ResponseEntity<List<Sample>> getAllSamplesInStudy(
        @ApiParam(required = true, value = "Study ID e.g. acc_tcga")
        @PathVariable String studyId,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection,
        @ApiParam("Page size of the result list")
        @Max(SAMPLE_MAX_PAGE_SIZE)
        @Min(PagingConstants.MIN_PAGE_SIZE)
        @RequestParam(defaultValue = SAMPLE_DEFAULT_PAGE_SIZE) Integer pageSize,
        @ApiParam("Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
        @ApiParam("Name of the property that the result list is sorted by")
        @RequestParam(required = false) SampleSortBy sortBy,
        @ApiParam("Direction of the sort")
        @RequestParam(defaultValue = "ASC") Direction direction) throws StudyNotFoundException {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, sampleService.getMetaSamplesInStudy(studyId)
                .getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                sampleService.getAllSamplesInStudy(studyId, projection.name(), pageSize, pageNumber,
                    sortBy == null ? null : sortBy.getOriginalValue(), direction.name()), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/studies/{studyId}/samples/{sampleId}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get a sample in a study")
    public ResponseEntity<Sample> getSampleInStudy(
        @ApiParam(required = true, value = "Study ID e.g. acc_tcga")
        @PathVariable String studyId,
        @ApiParam(required = true, value = "Sample ID e.g. TCGA-OR-A5J2-01")
        @PathVariable String sampleId) throws SampleNotFoundException, StudyNotFoundException {

        return new ResponseEntity<>(sampleService.getSampleInStudy(studyId, sampleId), HttpStatus.OK);
    }

    @RequestMapping(value = "/studies/{studyId}/patients/{patientId}/samples", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get all samples of a patient in a study")
    public ResponseEntity<List<Sample>> getAllSamplesOfPatientInStudy(
        @ApiParam(required = true, value = "Study ID e.g. acc_tcga")
        @PathVariable String studyId,
        @ApiParam(required = true, value = "Patient ID e.g. TCGA-OR-A5J2")
        @PathVariable String patientId,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection,
        @ApiParam("Page size of the result list")
        @Max(SAMPLE_MAX_PAGE_SIZE)
        @Min(PagingConstants.MIN_PAGE_SIZE)
        @RequestParam(defaultValue = SAMPLE_DEFAULT_PAGE_SIZE) Integer pageSize,
        @ApiParam("Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
        @ApiParam("Name of the property that the result list is sorted by")
        @RequestParam(required = false) SampleSortBy sortBy,
        @ApiParam("Direction of the sort")
        @RequestParam(defaultValue = "ASC") Direction direction) throws PatientNotFoundException, 
        StudyNotFoundException {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT,
                sampleService.getMetaSamplesOfPatientInStudy(studyId, patientId).getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                sampleService.getAllSamplesOfPatientInStudy(studyId, patientId, projection.name(), pageSize,
                    pageNumber, sortBy == null ? null : sortBy.getOriginalValue(), direction.name()), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/samples/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch samples by ID")
    public ResponseEntity<List<Sample>> fetchSamples(
        @ApiParam(required = true, value = "List of sample identifiers")
        @Size(min = 1, max = SAMPLE_MAX_PAGE_SIZE)
        @RequestBody List<SampleIdentifier> sampleIdentifiers,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) {

        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();

        for (SampleIdentifier sampleIdentifier : sampleIdentifiers) {
            studyIds.add(sampleIdentifier.getStudyId());
            sampleIds.add(sampleIdentifier.getSampleId());
        }

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, sampleService.fetchMetaSamples(studyIds, sampleIds)
                .getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                sampleService.fetchSamples(studyIds, sampleIds, projection.name()), HttpStatus.OK);
        }
    }
}
