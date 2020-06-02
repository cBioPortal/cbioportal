package org.cbioportal.web;

import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.cbioportal.model.ResourceDefinition;
import org.cbioportal.service.ResourceDefinitionService;
import org.cbioportal.service.exception.ResourceDefinitionNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.web.config.PublicApiTags;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.*;
import org.cbioportal.web.parameter.sort.ResourceDefinitionSortBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@PublicApi
@RestController
@Validated
@Api(tags = PublicApiTags.RESOURCE_DEFINITIONS, description = " ")
public class ResourceDefinitionController {

    @Autowired
    private ResourceDefinitionService resourceDefinitionService;

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', 'read')")
    @RequestMapping(value = "/studies/{studyId}/resource-definitions", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get all resoruce definitions in the specified study")
    public ResponseEntity<List<ResourceDefinition>> getAllResourceDefinitionsInStudy(
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
            @RequestParam(required = false) ResourceDefinitionSortBy sortBy,
            @ApiParam("Direction of the sort")
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

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', 'read')")
    @RequestMapping(value = "/studies/{studyId}/resource-definitions/{resourceId}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get specified resource definition")
    public ResponseEntity<ResourceDefinition> getResourceDefinitionInStudy(
            @ApiParam(required = true, value = "Study ID e.g. acc_tcga")
            @PathVariable String studyId,
            @ApiParam(required = true, value= "Resource ID")
            @PathVariable String resourceId)
        throws StudyNotFoundException, ResourceDefinitionNotFoundException {

        return new ResponseEntity<>(resourceDefinitionService.getResourceDefinition(studyId, resourceId),
                HttpStatus.OK);
    }
}
