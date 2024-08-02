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
import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.Sample;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.SampleListService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.service.exception.SampleListNotFoundException;
import org.cbioportal.service.exception.SampleNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.utils.security.AccessLevel;
import org.cbioportal.utils.security.PortalSecurityConfig;
import org.cbioportal.web.config.PublicApiTags;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.Direction;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.SampleFilter;
import org.cbioportal.web.parameter.SampleIdentifier;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@PublicApi
@RestController()
@RequestMapping("/api")
@Validated
@Tag(name = PublicApiTags.SAMPLES, description = " ")
public class SampleController {

    public static final int SAMPLE_MAX_PAGE_SIZE = 10000000;
    private static final String SAMPLE_DEFAULT_PAGE_SIZE = "10000000";

    @Value("${authenticate}")
    private String authenticate;
    
    @Autowired
    private SampleService sampleService;

    @Autowired
    private SampleListService sampleListService;
    
    @Autowired
    private StudyService studyService;

    private boolean usingAuth() {
        return !authenticate.isEmpty()
            && !authenticate.equals("false")
            && !authenticate.contains("optional_oauth2");
    }
    
    @RequestMapping(value = "/samples", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get all samples matching keyword")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = Sample.class))))
    public ResponseEntity<List<Sample>> getSamplesByKeyword(
        @Parameter(description = "Search keyword that applies to the study ID")
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
        @RequestParam(required = false) SampleSortBy sortBy,
        
        @Parameter(description = "Direction of the sort")
        @RequestParam(defaultValue = "ASC") Direction direction
    ) {
        String sort = sortBy == null ? null : sortBy.getOriginalValue();
        List<String> studyIds = null;
        if (PortalSecurityConfig.userAuthorizationEnabled(authenticate)) {
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
                    direction.name(),
                    null,
                    AccessLevel.READ
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

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/studies/{studyId}/samples", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get all samples in a study")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = Sample.class))))
    public ResponseEntity<List<Sample>> getAllSamplesInStudy(
        @Parameter(required = true, description = "Study ID e.g. acc_tcga")
        @PathVariable String studyId,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection,
        @Parameter(description = "Page size of the result list")
        @Max(SAMPLE_MAX_PAGE_SIZE)
        @Min(PagingConstants.MIN_PAGE_SIZE)
        @RequestParam(defaultValue = SAMPLE_DEFAULT_PAGE_SIZE) Integer pageSize,
        @Parameter(description = "Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
        @Parameter(description = "Name of the property that the result list is sorted by")
        @RequestParam(required = false) SampleSortBy sortBy,
        @Parameter(description = "Direction of the sort")
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

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/studies/{studyId}/samples/{sampleId}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get a sample in a study")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(schema = @Schema(implementation = Sample.class)))
    public ResponseEntity<Sample> getSampleInStudy(
        @Parameter(required = true, description = "Study ID e.g. acc_tcga")
        @PathVariable String studyId,
        @Parameter(required = true, description = "Sample ID e.g. TCGA-OR-A5J2-01")
        @PathVariable String sampleId) throws SampleNotFoundException, StudyNotFoundException {

        return new ResponseEntity<>(sampleService.getSampleInStudy(studyId, sampleId), HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/studies/{studyId}/patients/{patientId}/samples", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get all samples of a patient in a study")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = Sample.class))))
    public ResponseEntity<List<Sample>> getAllSamplesOfPatientInStudy(
        @Parameter(required = true, description = "Study ID e.g. acc_tcga")
        @PathVariable String studyId,
        @Parameter(required = true, description = "Patient ID e.g. TCGA-OR-A5J2")
        @PathVariable String patientId,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection,
        @Parameter(description = "Page size of the result list")
        @Max(SAMPLE_MAX_PAGE_SIZE)
        @Min(PagingConstants.MIN_PAGE_SIZE)
        @RequestParam(defaultValue = SAMPLE_DEFAULT_PAGE_SIZE) Integer pageSize,
        @Parameter(description = "Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
        @Parameter(description = "Name of the property that the result list is sorted by")
        @RequestParam(required = false) SampleSortBy sortBy,
        @Parameter(description = "Direction of the sort")
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

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/samples/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch samples by ID")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = Sample.class))))
    public ResponseEntity<List<Sample>> fetchSamples(
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedSampleFilter") SampleFilter interceptedSampleFilter,
        @Parameter(required = true, description = "List of sample identifiers")
        @Valid @RequestBody(required = false) SampleFilter sampleFilter,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) throws SampleListNotFoundException {

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
                    UniqueKeyExtractor.extractUniqueKeys(interceptedSampleFilter.getUniqueSampleKeys(), studyIds, sampleIds);
                }
                baseMeta = sampleService.fetchMetaSamples(studyIds, sampleIds);
            }
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, baseMeta.getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            List<Sample> samples;
            if (interceptedSampleFilter.getSampleListIds() != null) {
                List<String> sampleListIds = interceptedSampleFilter.getSampleListIds();
                
                samples = new ArrayList<Sample>();
                
                for (String sampleListId : sampleListIds) {
                    // check that all sample lists exist (this method throws an exception if one does not)
                    sampleListService.getSampleList(sampleListId);
                }
                
                for (String sampleListId : sampleListIds) {
                    // fetch by sampleId so that we get cache at level of id instead list of ids
                    samples.addAll(
                        sampleService.fetchSamples(Arrays.asList(sampleListId), projection.name())
                    );
                }
                
                //samples = sampleService.fetchSamples(sampleListIds, projection.name());
            
            } else {
                if (interceptedSampleFilter.getSampleIdentifiers() != null) {
                    extractStudyAndSampleIds(interceptedSampleFilter, studyIds, sampleIds);
                } else {
                    UniqueKeyExtractor.extractUniqueKeys(interceptedSampleFilter.getUniqueSampleKeys(), studyIds, sampleIds);
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
