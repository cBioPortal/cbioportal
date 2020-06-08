package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.model.Sample;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.service.exception.SampleNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.service.SampleService;
import org.cbioportal.web.config.PublicApiTags;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.*;
import org.cbioportal.web.parameter.sort.SampleSortBy;
import org.cbioportal.web.util.UniqueKeyExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@PublicApi
@RestController
@Validated
@Api(tags = PublicApiTags.SAMPLES, description = " ")
public class SampleController {

    public static final int SAMPLE_MAX_PAGE_SIZE = 10000000;
    private static final String SAMPLE_DEFAULT_PAGE_SIZE = "10000000";

    @Autowired
    private SampleService sampleService;
    
    @Autowired
    private StudyService studyService;

    @Autowired
    private UniqueKeyExtractor uniqueKeyExtractor;

    @Value("${authenticate:false}")
    private String authenticate;
    
    private boolean usingAuth() {
        return !authenticate.isEmpty()
                && !authenticate.equals("false")
                && !authenticate.contains("social_auth");
    }
    
    @RequestMapping(value = "/samples", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get all samples matching keyword")
    public ResponseEntity<List<Sample>> getSamplesByKeyword(
        @ApiParam("Search keyword that applies to the study ID")
        @RequestParam(required = false) String keyword,
        
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
        @RequestParam(required = false) SampleSortBy sortBy,
        
        @ApiParam("Direction of the sort")
        @RequestParam(defaultValue = "ASC") Direction direction
    ) {
        String sort = sortBy == null ? null : sortBy.getOriginalValue();
        List<String> studyIds = null;
        if (usingAuth()) {
            /*
             If using auth, filter the list of samples returned using the list of study ids the
             user has access to. If the user has access to no studies, the endpoint should not 403,
             but instead return an empty list.
            */
            studyIds = studyService
                .getAllStudies(
                    null,
                    Projection.SUMMARY.name(), // force to summary so that post filter doesn't NPE
                    PagingConstants.MAX_PAGE_SIZE,
                    0,
                    null,
                    direction.name()
                )
                .stream()
                .map(CancerStudy::getCancerStudyIdentifier)
                .collect(Collectors.toList());
        }

        if (projection == Projection.META) {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(
                    HeaderKeyConstants.TOTAL_COUNT,
                    sampleService.getMetaSamples(keyword, studyIds).getTotalCount().toString());
            return new ResponseEntity<>(httpHeaders, HttpStatus.OK);
        }
        return new ResponseEntity<>(
            sampleService.getAllSamples(keyword, studyIds, projection.name(), pageSize, pageNumber, sort, direction.name()),
            HttpStatus.OK
        );
    }

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', 'read')")
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

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', 'read')")
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

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', 'read')")
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

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', 'read')")
    @RequestMapping(value = "/samples/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch samples by ID")
    public ResponseEntity<List<Sample>> fetchSamples(
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedSampleFilter") SampleFilter interceptedSampleFilter,
        @ApiParam(required = true, value = "List of sample identifiers")
        @Valid @RequestBody(required = false) SampleFilter sampleFilter,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) {

        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            BaseMeta baseMeta;

            if (interceptedSampleFilter.getSampleListIds() != null) {
                baseMeta = sampleService.fetchMetaSamples(interceptedSampleFilter.getSampleListIds());
            } else {
                if (interceptedSampleFilter.getSampleIdentifiers() != null) {
                    extractStudyAndSampleIds(interceptedSampleFilter, studyIds, sampleIds);
                } else {
                    uniqueKeyExtractor.extractUniqueKeys(interceptedSampleFilter.getUniqueSampleKeys(), studyIds, sampleIds);
                }
                baseMeta = sampleService.fetchMetaSamples(studyIds, sampleIds);
            }
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, baseMeta.getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            List<Sample> samples;

            if (interceptedSampleFilter.getSampleListIds() != null) {
                samples = sampleService.fetchSamples(interceptedSampleFilter.getSampleListIds(), projection.name());
            } else {
                if (interceptedSampleFilter.getSampleIdentifiers() != null) {
                    extractStudyAndSampleIds(interceptedSampleFilter, studyIds, sampleIds);
                } else {
                    uniqueKeyExtractor.extractUniqueKeys(interceptedSampleFilter.getUniqueSampleKeys(), studyIds, sampleIds);
                }
                samples = sampleService.fetchSamples(studyIds, sampleIds, projection.name());
            }

            return new ResponseEntity<>(samples, HttpStatus.OK);
        }
    }

    private void extractStudyAndSampleIds(SampleFilter sampleFilter, List<String> studyIds, List<String> sampleIds) {

        for (SampleIdentifier sampleIdentifier : sampleFilter.getSampleIdentifiers()) {
            studyIds.add(sampleIdentifier.getStudyId());
            sampleIds.add(sampleIdentifier.getSampleId());
        }
    }
}
