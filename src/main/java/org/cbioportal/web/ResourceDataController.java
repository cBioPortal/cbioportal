package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.cbioportal.model.ResourceData;
import org.cbioportal.service.ResourceDataService;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.service.exception.SampleNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.web.config.InternalApiTags;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.Direction;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.sort.ResourceDataSortBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@InternalApi
@RestController()
@RequestMapping("/api")
@Validated
@Tag(name = InternalApiTags.RESOURCE_DATA, description = " ")
public class ResourceDataController {

    public static final int RESOURCE_DATA_MAX_PAGE_SIZE = 10000000;
    private static final String RESOURCE_DATA_DEFAULT_PAGE_SIZE = "10000000";

    @Autowired
    private ResourceDataService resourceDataService;

    @Autowired
    private ApplicationContext applicationContext;
    ResourceDataController instance;
    
    private ResourceDataController getInstance() {
        if (Objects.isNull(instance)) {
            instance = applicationContext.getBean(ResourceDataController.class);
        }
        return instance;
    }

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/studies/{studyId}/samples/{sampleId}/resource-data", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get all resource data of a sample in a study")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResourceData.class))))
    public ResponseEntity<List<ResourceData>> getAllResourceDataOfSampleInStudy(
        @Parameter(required = true, description = "Study ID e.g. acc_tcga")
        @PathVariable String studyId,
        @Parameter(required = true, description = "Sample ID e.g. TCGA-OR-A5J2-01")
        @PathVariable String sampleId,
        @Parameter(description = "Resource ID")
        @RequestParam(required = false) String resourceId,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection,
        @Parameter(description = "Page size of the result list")
        @Max(RESOURCE_DATA_MAX_PAGE_SIZE)
        @Min(PagingConstants.MIN_PAGE_SIZE)
        @RequestParam(defaultValue = RESOURCE_DATA_DEFAULT_PAGE_SIZE) Integer pageSize,
        @Parameter(description = "Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
        @Parameter(description = "Name of the property that the result list is sorted by")
        @RequestParam(required = false) ResourceDataSortBy sortBy,
        @Parameter(description = "Direction of the sort")
        @RequestParam(defaultValue = "ASC") Direction direction) throws SampleNotFoundException,
        StudyNotFoundException {

        if (projection == Projection.META) {
            throw new UnsupportedOperationException("Requested API is not implemented yet");
        } else {
            return new ResponseEntity<>(
                    resourceDataService.getAllResourceDataOfSampleInStudy(
                    studyId, sampleId, resourceId, projection.name(), pageSize, pageNumber,
                    sortBy == null ? null : sortBy.getOriginalValue(), direction.name()), HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/studies/{studyId}/patients/{patientId}/resource-data", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get all resource data of a patient in a study")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResourceData.class))))
    public ResponseEntity<List<ResourceData>> getAllResourceDataOfPatientInStudy(
        @Parameter(required = true, description = "Study ID e.g. acc_tcga")
        @PathVariable String studyId,
        @Parameter(required = true, description = "Patient ID e.g. TCGA-OR-A5J2")
        @PathVariable String patientId,
        @Parameter(description = "Resource ID")
        @RequestParam(required = false) String resourceId,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection,
        @Parameter(description = "Page size of the result list")
        @Max(RESOURCE_DATA_MAX_PAGE_SIZE)
        @Min(PagingConstants.MIN_PAGE_SIZE)
        @RequestParam(defaultValue = RESOURCE_DATA_DEFAULT_PAGE_SIZE) Integer pageSize,
        @Parameter(description = "Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
        @Parameter(description = "Name of the property that the result list is sorted by")
        @RequestParam(required = false) ResourceDataSortBy sortBy,
        @Parameter(description = "Direction of the sort")
        @RequestParam(defaultValue = "ASC") Direction direction) throws PatientNotFoundException,
        StudyNotFoundException {

        if (projection == Projection.META) {
            throw new UnsupportedOperationException("Requested API is not implemented yet");
        } else {
            return new ResponseEntity<>(
                    resourceDataService.getAllResourceDataOfPatientInStudy(
                    studyId, patientId, resourceId, projection.name(), pageSize, pageNumber,
                    sortBy == null ? null : sortBy.getOriginalValue(), direction.name()), HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/studies/{studyId}/resource-data", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get all resource data for a study")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResourceData.class))))
    public ResponseEntity<List<ResourceData>> getAllStudyResourceDataInStudy(
        @Parameter(required = true, description = "Study ID e.g. acc_tcga")
        @PathVariable String studyId,
        @Parameter(description = "Resource ID")
        @RequestParam(required = false) String resourceId,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection,
        @Parameter(description = "Page size of the result list")
        @Max(RESOURCE_DATA_MAX_PAGE_SIZE)
        @Min(PagingConstants.MIN_PAGE_SIZE)
        @RequestParam(defaultValue = RESOURCE_DATA_DEFAULT_PAGE_SIZE) Integer pageSize,
        @Parameter(description = "Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
        @Parameter(description = "Name of the property that the result list is sorted by")
        @RequestParam(required = false) ResourceDataSortBy sortBy,
        @Parameter(description = "Direction of the sort")
        @RequestParam(defaultValue = "ASC") Direction direction) throws StudyNotFoundException {

        if (projection == Projection.META) {
            throw new UnsupportedOperationException("Requested API is not implemented yet");
        } else {
            return new ResponseEntity<>(
                    resourceDataService.getAllResourceDataForStudy(studyId, resourceId, projection.name(), pageSize, pageNumber,
                    sortBy == null ? null : sortBy.getOriginalValue(), direction.name()), HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/studies/{studyId}/resource-data-all", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get all resource data for for all patients and all samples within a study")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResourceData.class))))
    public ResponseEntity<List<ResourceData>> getAllStudyResourceDataInStudyPatientSample(
        @Parameter(required = true, description = "Study ID e.g. acc_tcga")
        @PathVariable String studyId,
        @Parameter(description = "Resource ID")
        @RequestParam(required = false) String resourceId,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection,
        @Parameter(description = "Page size of the result list")
        @Max(RESOURCE_DATA_MAX_PAGE_SIZE)
        @Min(PagingConstants.MIN_PAGE_SIZE)
        @RequestParam(defaultValue = RESOURCE_DATA_DEFAULT_PAGE_SIZE) Integer pageSize,
        @Parameter(description = "Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
        @Parameter(description = "Name of the property that the result list is sorted by")
        @RequestParam(required = false) ResourceDataSortBy sortBy,
        @Parameter(description = "Direction of the sort")
        @RequestParam(defaultValue = "ASC") Direction direction) throws StudyNotFoundException {

        if (projection == Projection.META) {
            throw new UnsupportedOperationException("Requested API is not implemented yet");
        } else {
            return new ResponseEntity<>(this.getInstance().cacheableFetchAllResourceDataForStudyPatientSample(
                studyId, resourceId, projection.name(), pageSize, pageNumber, sortBy == null ? null : sortBy.getOriginalValue(), 
                direction.name()) , HttpStatus.OK);
        }
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabled()"
    )
    public List<ResourceData> cacheableFetchAllResourceDataForStudyPatientSample(String studyId, String resourceId, String projectionName, 
        Integer pageSize, Integer pageNumber, String sortBy, String directionName) throws StudyNotFoundException {

        return resourceDataService.getAllResourceDataForStudyPatientSample(studyId, resourceId, projectionName, pageSize, pageNumber, 
            sortBy, directionName);
    }

}
