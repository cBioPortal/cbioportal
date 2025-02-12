package org.cbioportal.legacy.web.columnar;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.model.Sample;
import org.cbioportal.legacy.service.SampleColumnarService;
import org.cbioportal.legacy.service.StudyService;
import org.cbioportal.legacy.service.exception.PatientNotFoundException;
import org.cbioportal.legacy.service.exception.SampleNotFoundException;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.cbioportal.legacy.utils.config.annotation.ConditionalOnProperty;
import org.cbioportal.legacy.utils.security.AccessLevel;
import org.cbioportal.legacy.utils.security.PortalSecurityConfig;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.PagingConstants;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.SampleFilter;
import org.cbioportal.legacy.web.parameter.sort.SampleSortBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

@RestController()
@RequestMapping("/api")
@Validated
@ConditionalOnProperty(name = "clickhouse_mode", havingValue = "true")
public class SampleColumnStoreController {
    public static final int SAMPLE_MAX_PAGE_SIZE = 10000000;
    private static final String SAMPLE_DEFAULT_PAGE_SIZE = "10000000";
    
    @Autowired
    private SampleColumnarService sampleService;
    
    @Autowired
    private StudyService studyService;

    @Value("${authenticate}")
    private String authenticate;
    
    @GetMapping(value = "/column-store/samples", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get all samples matching keyword")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = Sample.class))))
    public ResponseEntity<List<Sample>> getSamplesByKeyword(
        @Parameter(description = "Search keyword that applies to the study ID")
        @RequestParam(required = false) 
        String keyword,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") 
        Projection projection,
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
                .toList();
        }

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = sampleService.getMetaSamplesHeaders(keyword, studyIds);
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(
                sampleService.getAllSamples(
                    keyword,
                    studyIds,
                    projection.name(),
                    pageSize,
                    pageNumber,
                    sort,
                    direction.name()
                ),
                HttpStatus.OK
            );
        }
    }
    
    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
    @PostMapping(
        value = "/column-store/samples/fetch",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(description = "Fetch samples by ID")
    @ApiResponse(
        responseCode = "200",
        description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = Sample.class)))
    )
    public ResponseEntity<List<Sample>> fetchSamples(
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
        Projection projection
    ) {
        if (projection == Projection.META) {
            HttpHeaders responseHeaders = sampleService.fetchMetaSamplesHeaders(interceptedSampleFilter);
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        }
        else {
            List<Sample> samples = sampleService.fetchSamples(interceptedSampleFilter, projection.name());
            return new ResponseEntity<>(samples, HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
    @GetMapping(value = "/column-store/studies/{studyId}/samples", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get all samples in a study")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = Sample.class))))
    public ResponseEntity<List<Sample>> getAllSamplesInStudy(
        @Parameter(required = true, description = "Study ID e.g. acc_tcga")
        @PathVariable
        String studyId,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY")
        Projection projection,
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
        if (projection == Projection.META) {
            HttpHeaders responseHeaders = sampleService.getMetaSamplesInStudyHeaders(studyId);
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                sampleService.getAllSamplesInStudy(
                    studyId,
                    projection.name(),
                    pageSize,
                    pageNumber,
                    sortBy == null ? null : sortBy.getOriginalValue(),
                    direction.name()
                ),
                HttpStatus.OK
            );
        }
    }

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
    @GetMapping(value = "/column-store/studies/{studyId}/samples/{sampleId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get a sample in a study")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(schema = @Schema(implementation = Sample.class)))
    public ResponseEntity<Sample> getSampleInStudy(
        @Parameter(required = true, description = "Study ID e.g. acc_tcga")
        @PathVariable
        String studyId,
        @Parameter(required = true, description = "Sample ID e.g. TCGA-OR-A5J2-01")
        @PathVariable
        String sampleId
    ) throws SampleNotFoundException, StudyNotFoundException {
        return new ResponseEntity<>(
            sampleService.getSampleInStudy(studyId, sampleId),
            HttpStatus.OK
        );
    }

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
    @GetMapping(value = "/column-store/studies/{studyId}/patients/{patientId}/samples", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get all samples of a patient in a study")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = Sample.class))))
    public ResponseEntity<List<Sample>> getAllSamplesOfPatientInStudy(
        @Parameter(required = true, description = "Study ID e.g. acc_tcga")
        @PathVariable
        String studyId,
        @Parameter(required = true, description = "Patient ID e.g. TCGA-OR-A5J2")
        @PathVariable
        String patientId,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY")
        Projection projection,
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
        if (projection == Projection.META) {
            HttpHeaders responseHeaders = sampleService.getMetaSamplesOfPatientInStudyHeaders(studyId, patientId);
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                sampleService.getAllSamplesOfPatientInStudy(
                    studyId,
                    patientId,
                    projection.name(),
                    pageSize,
                    pageNumber,
                    sortBy == null ? null : sortBy.getOriginalValue(),
                    direction.name()
                ),
                HttpStatus.OK
            );
        }
    }
}
