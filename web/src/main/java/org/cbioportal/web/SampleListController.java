package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.SampleList;
import org.cbioportal.service.SampleListService;
import org.cbioportal.service.exception.SampleListNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.web.config.PublicApiTags;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.Direction;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.sort.SampleListSortBy;
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

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.List;

@PublicApi
@RestController
@Validated
@Api(tags = PublicApiTags.SAMPLE_LISTS, description = " ")
public class SampleListController {

    @Autowired
    private SampleListService sampleListService;

    @RequestMapping(value = "/sample-lists", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get all sample lists")
    public ResponseEntity<List<SampleList>> getAllSampleLists(
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
        @RequestParam(required = false) SampleListSortBy sortBy,
        @ApiParam("Direction of the sort")
        @RequestParam(defaultValue = "ASC") Direction direction) {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, sampleListService.getMetaSampleLists()
                .getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                sampleListService.getAllSampleLists(projection.name(), pageSize, pageNumber,
                    sortBy == null ? null : sortBy.getOriginalValue(), direction.name()), HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#sampleListId, 'SampleListId', 'read')")
    @RequestMapping(value = "/sample-lists/{sampleListId}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get sample list")
    public ResponseEntity<SampleList> getSampleList(
        @ApiParam(required = true, value = "Sample List ID e.g. acc_tcga_all")
        @PathVariable String sampleListId) throws SampleListNotFoundException {

        return new ResponseEntity<>(sampleListService.getSampleList(sampleListId), HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', 'read')")
    @RequestMapping(value = "/studies/{studyId}/sample-lists", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get all sample lists in a study")
    public ResponseEntity<List<SampleList>> getAllSampleListsInStudy(
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
        @RequestParam(required = false) SampleListSortBy sortBy,
        @ApiParam("Direction of the sort")
        @RequestParam(defaultValue = "ASC") Direction direction) throws StudyNotFoundException {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, sampleListService
                .getMetaSampleListsInStudy(studyId).getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                sampleListService.getAllSampleListsInStudy(studyId, projection.name(), pageSize, pageNumber,
                    sortBy == null ? null : sortBy.getOriginalValue(), direction.name()), HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#sampleListId, 'SampleListId', 'read')")
    @RequestMapping(value = "/sample-lists/{sampleListId}/sample-ids", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get all sample IDs in a sample list")
    public ResponseEntity<List<String>> getAllSampleIdsInSampleList(
        @ApiParam(required = true, value = "Sample List ID e.g. acc_tcga_all")
        @PathVariable String sampleListId) throws SampleListNotFoundException {

        return new ResponseEntity<>(sampleListService.getAllSampleIdsInSampleList(sampleListId), HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#sampleListIds, 'Collection<SampleListId>', 'read')")
    @RequestMapping(value = "/sample-lists/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch sample lists by ID")
    public ResponseEntity<List<SampleList>> fetchSampleLists(
        @ApiParam(required = true, value = "List of sample list IDs")
        @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
        @RequestBody List<String> sampleListIds,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) {

        return new ResponseEntity<>(sampleListService.fetchSampleLists(sampleListIds, projection.name()), HttpStatus.OK);
    }
}
