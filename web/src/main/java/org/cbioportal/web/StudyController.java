package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.cbioportal.model.summary.CancerStudySummary;
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
@Api(tags = "Studies", description = " ")
public class StudyController {

    @RequestMapping(value = "/studies", method = RequestMethod.GET)
    @ApiOperation("Get all studies")
    public ResponseEntity<List<? extends CancerStudySummary>> getAllStudies(@RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                            @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                            @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber) {

        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/studies/{studyId}", method = RequestMethod.GET)
    @ApiOperation("Get a study")
    public ResponseEntity<CancerStudySummary> getStudy(@PathVariable String studyId) {

        throw new UnsupportedOperationException();
    }
}
