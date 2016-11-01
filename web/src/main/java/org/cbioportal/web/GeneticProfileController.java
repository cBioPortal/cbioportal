package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.cbioportal.model.summary.GeneticProfileSummary;
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
@Api(tags = "Genetic Profiles", description = " ")
public class GeneticProfileController {

    @RequestMapping(value = "/genetic-profiles", method = RequestMethod.GET)
    @ApiOperation("Get all genetic profiles")
    public ResponseEntity<List<? extends GeneticProfileSummary>> getAllGeneticProfiles(@RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                                       @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                                       @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber) {

        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/genetic-profiles/{geneticProfileId}", method = RequestMethod.GET)
    @ApiOperation("Get genetic profile")
    public ResponseEntity<GeneticProfileSummary> getGeneticProfile(@PathVariable String geneticProfileId) {

        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/studies/{studyId}/genetic-profiles", method = RequestMethod.GET)
    @ApiOperation("Get all genetic profiles in a study")
    public ResponseEntity<List<? extends GeneticProfileSummary>> getAllGeneticProfilesInStudy(@PathVariable String studyId,
                                                                                              @RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                                              @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                                              @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber) {

        throw new UnsupportedOperationException();
    }
}
