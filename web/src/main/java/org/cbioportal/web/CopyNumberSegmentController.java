package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.cbioportal.model.CopyNumberSegment;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.Projection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Api(tags = "Copy Number Segments", description = " ")
public class CopyNumberSegmentController {

    @RequestMapping(value = "/studies/{studyId}/samples/{sampleId}/copy-number-segments", method = RequestMethod.GET)
    @ApiOperation("Get all copy number segments in a sample in a study")
    public ResponseEntity<List<CopyNumberSegment>> getAllCopyNumberSegmentsInSampleInStudy(@PathVariable String studyId,
                                                                                           @PathVariable String sampleId,
                                                                                           @RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                                           @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                                           @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber) {

        throw new UnsupportedOperationException();
    }
}
