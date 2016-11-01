package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.cbioportal.model.summary.ClinicalDataSummary;
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
@Api(tags = "Clinical Data", description = " ")
public class ClinicalDataController {

    @RequestMapping(value = "/studies/{studyId}/samples/{sampleId}/clinical-data", method = RequestMethod.GET)
    @ApiOperation("Get all clinical data in a sample in a study")
    public ResponseEntity<List<? extends ClinicalDataSummary>> getAllClinicalDataInSampleInStudy(@PathVariable String studyId,
                                                                                                 @PathVariable String sampleId,
                                                                                                 @RequestParam(required = false) String attributeId,
                                                                                                 @RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                                                 @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                                                 @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber) {

        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/studies/{studyId}/patients/{patientId}/clinical-data", method = RequestMethod.GET)
    @ApiOperation("Get all clinical data in a patient in a study")
    public ResponseEntity<List<? extends ClinicalDataSummary>> getAllClinicalDataInPatientInStudy(@PathVariable String studyId,
                                                                                                  @PathVariable String patientId,
                                                                                                  @RequestParam(required = false) String attributeId,
                                                                                                  @RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                                                  @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                                                  @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber) {

        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/studies/{studyId}/clinical-data", method = RequestMethod.GET)
    @ApiOperation("Get all clinical data in a study")
    public ResponseEntity<List<? extends ClinicalDataSummary>> getAllClinicalDataInStudy(@PathVariable String studyId,
                                                                                         @RequestParam(required = false) String attributeId,
                                                                                         @RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                                         @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                                         @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber) {

        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/clinical-data/query", method = RequestMethod.POST)
    @ApiOperation("Query clinical data by example")
    public ResponseEntity<List<? extends ClinicalDataSummary>> queryClinicalDataByExample(@RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                                          @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                                          @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
                                                                                          @RequestBody ClinicalDataSummary exampleClinicalData) {

        throw new UnsupportedOperationException();
    }
}
