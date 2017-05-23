package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.cbioportal.model.GeneGeneticData;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.Projection;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@PublicApi
@RestController
@Validated
@Api(tags = "Genetic Data", description = " ")
public class GeneticDataController {

    @RequestMapping(value = "/studies/{studyId}/samples/{sampleId}/genetic-data", method = RequestMethod.GET)
    @ApiOperation("Get all genetic data of a sample in a study")
    public ResponseEntity<List<GeneGeneticData>> getAllGeneticDataInSampleInStudy(@PathVariable String studyId,
                                                                                               @PathVariable String sampleId,
                                                                                               @RequestParam String geneticProfileId,
                                                                                               @RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                                               @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                                               @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber) {

        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/studies/{studyId}/patients/{patientId}/genetic-data", method = RequestMethod.GET)
    @ApiOperation("Get all genetic data of a patient in a study")
    public ResponseEntity<List<GeneGeneticData>> getAllGeneticDataInPatientInStudy(@PathVariable String studyId,
                                                                                                @PathVariable String patientId,
                                                                                                @RequestParam String geneticProfileId,
                                                                                                @RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                                                @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                                                @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber) {

        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/genetic-profiles/{geneticProfileId}/genetic-data", method = RequestMethod.GET)
    @ApiOperation("Get all genetic data in a genetic profile")
    public ResponseEntity<List<GeneGeneticData>> getAllGeneticDataInGeneticProfile(@PathVariable String geneticProfileId,
                                                                                                @RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                                                @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                                                @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber) {

        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/genetic-data/query", method = RequestMethod.POST)
    @ApiOperation("Query genetic data by example")
    public ResponseEntity<List<GeneGeneticData>> queryGeneticDataByExample(@RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                                        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                                        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
                                                                                        @RequestBody GeneGeneticData exampleGenericData) {

        throw new UnsupportedOperationException();
    }
}
