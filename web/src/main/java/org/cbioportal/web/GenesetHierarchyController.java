package org.cbioportal.web;

import java.util.List;

import org.cbioportal.model.GenesetHierarchyInfo;
import org.cbioportal.service.GenesetHierarchyService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.cbioportal.web.config.annotation.InternalApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@InternalApi
@RestController
@Validated
@Api(tags = "Gene set hierarchy", description = " ")
public class GenesetHierarchyController {

    @Autowired
    private GenesetHierarchyService genesetHierarchyService;

    @RequestMapping(value = "/genesets/hierarchy", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get gene set hierarchical organization information. I.e. how different gene sets relate to other gene sets, in a hierarchy")
    public ResponseEntity<List<GenesetHierarchyInfo>> fetchGenesetHierarchyInfo(
		@ApiParam(required = true, value = "Genetic Profile ID e.g. brca_tcga_gsva_oncogenic_sets_scores. If set, the final hierarchy "
				+ " will only include gene sets scored in the specified profile.")
    	@RequestParam String geneticProfileId)
        throws GeneticProfileNotFoundException {

        return new ResponseEntity<>(
        		genesetHierarchyService.getGenesetHierarchyInfo(geneticProfileId), HttpStatus.OK);
    }
}
