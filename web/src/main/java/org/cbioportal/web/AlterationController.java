package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.cbioportal.model.GeneticAlteration;
import org.cbioportal.model.summary.GeneticAlterationSummary;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.Projection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(tags = "Alterations", description = "non-mutation alterations, e.g. copy-number, mrna")
public class AlterationController {

    @RequestMapping(value = "/studies/{studyId}/samples/{sampleId}/alterations", method = RequestMethod.GET)
    @ApiOperation("Get all alterations in a sample in a study")
    public ResponseEntity<List<? extends GeneticAlterationSummary>> getAllAlterationsInSampleInStudy(@PathVariable String studyId,
                                                                                                     @PathVariable String sampleId,
                                                                                                     @RequestParam String geneticProfileId,
                                                                                                     @RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                                                     @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                                                     @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber) {

        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/genetic-profiles/{geneticProfileId}/alterations", method = RequestMethod.GET)
    @ApiOperation("Get all alterations in a genetic profile")
    public ResponseEntity<List<? extends GeneticAlterationSummary>> getAllAlterationsInGeneticProfile(@PathVariable String geneticProfileId,
                                                                                                      @RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                                                      @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                                                      @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber) {

        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/alterations/query", method = RequestMethod.POST)
    @ApiOperation("Query alterations by example")
    public ResponseEntity<List<? extends GeneticAlterationSummary>> queryAlterationsByExample(@RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                                              @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                                              @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
                                                                                              @RequestBody GeneticAlteration exampleAlteration) {

        throw new UnsupportedOperationException();
    }
}
