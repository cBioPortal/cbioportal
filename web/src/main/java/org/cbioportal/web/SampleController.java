package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.cbioportal.model.Sample;
import org.cbioportal.model.summary.SampleSummary;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Api(tags = "Samples", description = " ")
public class SampleController {

    @RequestMapping(value = "/studies/{studyId}/samples", method = RequestMethod.GET)
    @ApiOperation("Get all samples of a patient in a study")
    public ResponseEntity<List<? extends SampleSummary>> getAllSamplesOfPatientInStudy(@RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                                       @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                                       @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
                                                                                       @PathVariable String studyId) {

        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/studies/{studyId}/samples/{sampleId}", method = RequestMethod.GET)
    @ApiOperation("Get a sample in a study")
    public ResponseEntity<SampleSummary> getSampleInStudy(@PathVariable String studyId,
                                                          @PathVariable String sampleId) {

        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/samples/fetch", method = RequestMethod.POST)
    @ApiOperation("Fetch samples by ID")
    public ResponseEntity<List<? extends SampleSummary>> fetchSamples(@RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                      @RequestBody List<SampleIdentifier> sampleIdentifiers) {

        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/samples/query", method = RequestMethod.POST)
    @ApiOperation("Query samples by example")
    public ResponseEntity<List<? extends SampleSummary>> querySamplesByExample(@RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                               @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                               @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
                                                                               @RequestBody Sample exampleSample) {

        throw new UnsupportedOperationException();
    }
}
