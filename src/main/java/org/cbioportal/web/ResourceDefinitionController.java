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
import jakarta.validation.constraints.Size;
import org.cbioportal.model.ResourceDefinition;
import org.cbioportal.service.ResourceDefinitionService;
import org.cbioportal.service.exception.ResourceDefinitionNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.web.config.InternalApiTags;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.Direction;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.sort.ResourceDefinitionSortBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@InternalApi
@RestController()
@RequestMapping("/api")
@Validated
@Tag(name = InternalApiTags.RESOURCE_DEFINITIONS, description = " ")
public class ResourceDefinitionController {

    @Autowired
    private ResourceDefinitionService resourceDefinitionService;

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/studies/{studyId}/resource-definitions", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get all resource definitions in the specified study")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResourceDefinition.class))))
    public ResponseEntity<List<ResourceDefinition>> getAllResourceDefinitionsInStudy(
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
            @RequestParam(required = false) ResourceDefinitionSortBy sortBy,
            @Parameter(description = "Direction of the sort")
            @RequestParam(defaultValue = "ASC") Direction direction) throws StudyNotFoundException {

        if (projection == Projection.META) {
            throw new UnsupportedOperationException("Requested API is not implemented yet");
        } else {
            return new ResponseEntity<>(
                    resourceDefinitionService.getAllResourceDefinitionsInStudy(studyId, projection.name(), pageSize,
                            pageNumber, sortBy == null ? null : sortBy.getOriginalValue(), direction.name()),
                    HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/studies/{studyId}/resource-definitions/{resourceId}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get specified resource definition")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(schema = @Schema(implementation = ResourceDefinition.class)))
    public ResponseEntity<ResourceDefinition> getResourceDefinitionInStudy(
            @Parameter(required = true, description = "Study ID e.g. acc_tcga")
            @PathVariable String studyId,
            @Parameter(required = true, description= "Resource ID")
            @PathVariable String resourceId)
        throws StudyNotFoundException, ResourceDefinitionNotFoundException {

        return new ResponseEntity<>(resourceDefinitionService.getResourceDefinition(studyId, resourceId),
                HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#studyIds, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/resource-definitions/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get all resource definitions for specified studies")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResourceDefinition.class))))
    public ResponseEntity<List<ResourceDefinition>> fetchResourceDefinitions(
        @Parameter(required = true, description = "List of Study IDs")
        @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
        @RequestBody List<String> studyIds,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) throws StudyNotFoundException {

        return new ResponseEntity<>(resourceDefinitionService.fetchResourceDefinitions(studyIds, projection.name()),
            HttpStatus.OK);
    }
}
