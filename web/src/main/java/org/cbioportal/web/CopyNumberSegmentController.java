package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.CopyNumberSeg;
import org.cbioportal.service.CopyNumberSegmentService;
import org.cbioportal.service.exception.SampleNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.Direction;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.sort.CopyNumberSegmentSortBy;
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
import java.util.ArrayList;
import java.util.List;

@PublicApi
@RestController
@Validated
@Api(tags = "Copy Number Segments", description = " ")
public class CopyNumberSegmentController {

    private static final int COPY_NUMBER_SEGMENT_MAX_PAGE_SIZE = 20000;
    private static final String COPY_NUMBER_SEGMENT_DEFAULT_PAGE_SIZE = "20000";

    @Autowired
    private CopyNumberSegmentService copyNumberSegmentService;

    @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    @RequestMapping(value = "/studies/{studyId}/samples/{sampleId}/copy-number-segments", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get copy number segments in a sample in a study")
    public ResponseEntity<List<CopyNumberSeg>> getCopyNumberSegmentsInSampleInStudy(
        @ApiParam(required = true, value = "Study ID e.g. acc_tcga")
        @PathVariable String studyId,
        @ApiParam(required = true, value = "Sample ID e.g. TCGA-OR-A5J2-01")
        @PathVariable String sampleId,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection,
        @ApiParam("Page size of the result list")
        @Max(COPY_NUMBER_SEGMENT_MAX_PAGE_SIZE)
        @Min(PagingConstants.MIN_PAGE_SIZE)
        @RequestParam(defaultValue = COPY_NUMBER_SEGMENT_DEFAULT_PAGE_SIZE) Integer pageSize,
        @ApiParam("Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
        @ApiParam("Name of the property that the result list is sorted by")
        @RequestParam(required = false) CopyNumberSegmentSortBy sortBy,
        @ApiParam("Direction of the sort")
        @RequestParam(defaultValue = "ASC") Direction direction) throws SampleNotFoundException, 
        StudyNotFoundException {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, copyNumberSegmentService
                .getMetaCopyNumberSegmentsInSampleInStudy(studyId, sampleId).getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                copyNumberSegmentService.getCopyNumberSegmentsInSampleInStudy(studyId, sampleId,
                    projection.name(), pageSize, pageNumber, sortBy == null ? null : sortBy.getOriginalValue(),
                    direction.name()), HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#sampleIdentifiers, 'List<SampleIdentifier>', 'read')")
    @RequestMapping(value = "/copy-number-segments/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch copy number segments by sample ID")
    public ResponseEntity<List<CopyNumberSeg>> fetchCopyNumberSegments(
        @ApiParam(required = true, value = "List of sample identifiers")
        @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
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
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, copyNumberSegmentService
                .fetchMetaCopyNumberSegments(studyIds, sampleIds).getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                copyNumberSegmentService.fetchCopyNumberSegments(studyIds, sampleIds, projection.name()), 
                HttpStatus.OK);
        }
    }
}
