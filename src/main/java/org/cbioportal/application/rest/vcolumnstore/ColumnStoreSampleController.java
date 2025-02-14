package org.cbioportal.application.rest.vcolumnstore;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.cbioportal.application.rest.mapper.SampleMapper;
import org.cbioportal.application.rest.response.SampleDTO;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.domain.sample.Sample;
import org.cbioportal.domain.sample.service.SampleService;
import org.cbioportal.legacy.service.StudyService;
import org.cbioportal.legacy.service.exception.PatientNotFoundException;
import org.cbioportal.legacy.service.exception.SampleNotFoundException;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.cbioportal.legacy.utils.security.AccessLevel;
import org.cbioportal.legacy.utils.security.PortalSecurityConfig;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.PagingConstants;
import org.cbioportal.legacy.web.parameter.SampleFilter;
import org.cbioportal.legacy.web.parameter.sort.SampleSortBy;
import org.cbioportal.shared.enums.ProjectionType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/column-store")
@Validated
@Profile("clickhouse")
public class ColumnStoreSampleController {
    public static final int SAMPLE_MAX_PAGE_SIZE = 10000000;
    private static final String SAMPLE_DEFAULT_PAGE_SIZE = "10000000";
    
    private final SampleService sampleService;
    
    private final StudyService studyService;

    @Value("${authenticate}")
    private String authenticate;
    
    public ColumnStoreSampleController(
        SampleService sampleService,
        StudyService studyService
    ) {
        this.sampleService = sampleService;
        this.studyService = studyService;
    }
    
    @GetMapping(value = "/samples", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get all samples matching keyword")
    @ApiResponse(
        responseCode = "200",
        description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = Sample.class)))
    )
    public ResponseEntity<List<SampleDTO>> getSamplesByKeyword(
        @Parameter(description = "Search keyword that applies to the study ID")
        @RequestParam(required = false) 
        String keyword,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") 
        ProjectionType projection,
        @Parameter(description = "Page size of the result list")
        @Max(PagingConstants.MAX_PAGE_SIZE)
        @Min(PagingConstants.MIN_PAGE_SIZE)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE)
        Integer pageSize,
        @Parameter(description = "Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER)
        Integer pageNumber,
        @Parameter(description = "Name of the property that the result list is sorted by")
        @RequestParam(required = false)
        SampleSortBy sortBy,
        @Parameter(description = "Direction of the sort")
        @RequestParam(defaultValue = "ASC")
        Direction direction
    ) {
        String sort = sortBy == null ? null : sortBy.getOriginalValue();
        List<String> studyIds = null;
        
        // TODO is there a better way to do this? something like @PreAuthorize?
        //  (this code segment is duplicate of the legacy SampleController)
        if (PortalSecurityConfig.userAuthorizationEnabled(authenticate)) {
            /*
             If using auth, filter the list of samples returned using the list of study ids the
             user has access to. If the user has access to no studies, the endpoint should not 403,
             but instead return an empty list.
            */
            studyIds = studyService
                .getAllStudies(
                    null,
                    ProjectionType.SUMMARY.name(), // force to summary so that post filter doesn't NPE
                    PagingConstants.MAX_PAGE_SIZE,
                    0,
                    null,
                    direction.name(),
                    null,
                    AccessLevel.READ
                )
                .stream()
                .map(CancerStudy::getCancerStudyIdentifier)
                .toList();
        }

        if (projection == ProjectionType.META) {
            HttpHeaders responseHeaders = sampleService.getMetaSamplesHeaders(keyword, studyIds);
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        }
        else {
            List<Sample> samples = sampleService.getAllSamples(
                keyword,
                studyIds,
                projection,
                pageSize,
                pageNumber,
                sort,
                direction.name()
            );
            
            return new ResponseEntity<>(SampleMapper.INSTANCE.toDtos(samples), HttpStatus.OK);
        }
    }
    
    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
    @PostMapping(
        value = "/samples/fetch",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(description = "Fetch samples by ID")
    @ApiResponse(
        responseCode = "200",
        description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = Sample.class)))
    )
    public ResponseEntity<List<SampleDTO>> fetchSamples(
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies")
        Collection<String> involvedCancerStudies,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid
        @RequestAttribute(required = false, value = "interceptedSampleFilter")
        SampleFilter interceptedSampleFilter,
        @Parameter(required = true, description = "List of sample identifiers")
        @Valid
        @RequestBody(required = false)
        SampleFilter sampleFilter,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY")
        ProjectionType projection
    ) {
        if (projection == ProjectionType.META) {
            HttpHeaders responseHeaders = sampleService.fetchMetaSamplesHeaders(interceptedSampleFilter);
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        }
        else {
            List<Sample> samples = sampleService.fetchSamples(interceptedSampleFilter, projection);
            return new ResponseEntity<>(SampleMapper.INSTANCE.toDtos(samples), HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
    @GetMapping(value = "/studies/{studyId}/samples", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get all samples in a study")
    @ApiResponse(
        responseCode = "200",
        description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = Sample.class)))
    )
    public ResponseEntity<List<SampleDTO>> getAllSamplesInStudy(
        @Parameter(required = true, description = "Study ID e.g. acc_tcga")
        @PathVariable
        String studyId,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY")
        ProjectionType projection,
        @Parameter(description = "Page size of the result list")
        @Max(SAMPLE_MAX_PAGE_SIZE)
        @Min(PagingConstants.MIN_PAGE_SIZE)
        @RequestParam(defaultValue = SAMPLE_DEFAULT_PAGE_SIZE)
        Integer pageSize,
        @Parameter(description = "Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER)
        Integer pageNumber,
        @Parameter(description = "Name of the property that the result list is sorted by")
        @RequestParam(required = false)
        SampleSortBy sortBy,
        @Parameter(description = "Direction of the sort")
        @RequestParam(defaultValue = "ASC")
        Direction direction
    ) throws StudyNotFoundException {
        if (projection == ProjectionType.META) {
            HttpHeaders responseHeaders = sampleService.getMetaSamplesInStudyHeaders(studyId);
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            List<Sample> samples = sampleService.getAllSamplesInStudy(
                studyId,
                projection,
                pageSize,
                pageNumber,
                sortBy == null ? null : sortBy.getOriginalValue(),
                direction.name()
            );
            
            return new ResponseEntity<>(SampleMapper.INSTANCE.toDtos(samples), HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
    @GetMapping(value = "/studies/{studyId}/samples/{sampleId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get a sample in a study")
    @ApiResponse(
        responseCode = "200",
        description = "OK",
        content = @Content(schema = @Schema(implementation = Sample.class))
    )
    public ResponseEntity<SampleDTO> getSampleInStudy(
        @Parameter(required = true, description = "Study ID e.g. acc_tcga")
        @PathVariable
        String studyId,
        @Parameter(required = true, description = "Sample ID e.g. TCGA-OR-A5J2-01")
        @PathVariable
        String sampleId
    ) throws SampleNotFoundException, StudyNotFoundException {
        return new ResponseEntity<>(
            SampleMapper.INSTANCE.toSampleDTO(
                sampleService.getSampleInStudy(studyId, sampleId)
            ),
            HttpStatus.OK
        );
    }

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
    @GetMapping(value = "/studies/{studyId}/patients/{patientId}/samples", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get all samples of a patient in a study")
    @ApiResponse(
        responseCode = "200",
        description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = Sample.class)))
    )
    public ResponseEntity<List<SampleDTO>> getAllSamplesOfPatientInStudy(
        @Parameter(required = true, description = "Study ID e.g. acc_tcga")
        @PathVariable
        String studyId,
        @Parameter(required = true, description = "Patient ID e.g. TCGA-OR-A5J2")
        @PathVariable
        String patientId,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY")
        ProjectionType projection,
        @Parameter(description = "Page size of the result list")
        @Max(SAMPLE_MAX_PAGE_SIZE)
        @Min(PagingConstants.MIN_PAGE_SIZE)
        @RequestParam(defaultValue = SAMPLE_DEFAULT_PAGE_SIZE)
        Integer pageSize,
        @Parameter(description = "Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER)
        Integer pageNumber,
        @Parameter(description = "Name of the property that the result list is sorted by")
        @RequestParam(required = false)
        SampleSortBy sortBy,
        @Parameter(description = "Direction of the sort")
        @RequestParam(defaultValue = "ASC") Direction direction
    ) throws PatientNotFoundException, StudyNotFoundException {
        if (projection == ProjectionType.META) {
            HttpHeaders responseHeaders = sampleService.getMetaSamplesOfPatientInStudyHeaders(studyId, patientId);
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            List<Sample> samples = sampleService.getAllSamplesOfPatientInStudy(
                studyId,
                patientId,
                projection,
                pageSize,
                pageNumber,
                sortBy == null ? null : sortBy.getOriginalValue(),
                direction.name()
            );
            
            return new ResponseEntity<>(SampleMapper.INSTANCE.toDtos(samples), HttpStatus.OK);
        }
    }
}
