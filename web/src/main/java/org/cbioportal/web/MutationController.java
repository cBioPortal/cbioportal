package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.summary.MutationSummary;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.Projection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Api(tags = "Mutations", description = " ")
public class MutationController {

    @RequestMapping(value = "/studies/{studyId}/samples/{sampleId}/mutations", method = RequestMethod.GET)
    @ApiOperation("Get all mutations in a sample in a study")
    public ResponseEntity<List<? extends MutationSummary>> getAllMutationsInSampleInStudy(@PathVariable String studyId,
                                                                                          @PathVariable String sampleId,
                                                                                          @RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                                          @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                                          @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber) {

        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/studies/{studyId}/patients/{patientId}/mutations", method = RequestMethod.GET)
    @ApiOperation("Get all mutations in a patient in a study")
    public ResponseEntity<List<? extends MutationSummary>> getAllMutationsInPatientInStudy(@PathVariable String studyId,
                                                                                           @PathVariable String patientId,
                                                                                           @RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                                           @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                                           @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber) {

        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/studies/{studyId}/mutations", method = RequestMethod.GET)
    @ApiOperation("Get all mutations in a study")
    public ResponseEntity<List<? extends MutationSummary>> getAllMutationsInStudy(@PathVariable String studyId,
                                                                                  @RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                                  @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                                  @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber) {

        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/mutations/query", method = RequestMethod.POST)
    @ApiOperation("Query mutations by example")
    public ResponseEntity<List<? extends MutationSummary>> queryMutationsByExample(@RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                                   @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                                   @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
                                                                                   @RequestBody Mutation exampleMutation) {

        throw new UnsupportedOperationException();
    }
}