package org.cbioportal.web;

import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.cbioportal.model.GenesetHierarchyInfo;
import org.cbioportal.service.GenesetHierarchyService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.cbioportal.web.config.annotation.InternalApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
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

    @RequestMapping(value = "/genesets/hierarchy/fetch", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get gene set hierarchical organization information. I.e. how different gene sets relate to other gene sets, in a hierarchy")
    public ResponseEntity<List<GenesetHierarchyInfo>> fetchGenesetHierarchyInfo(
		@ApiParam(required = true, value = "Genetic Profile ID e.g. brca_tcga_gsva_scores. The final hierarchy "
				+ " will only include gene sets scored in the specified profile.")
    	@RequestParam String geneticProfileId,
        @ApiParam("Percentile (for score calculation). Which percentile to use when determining the *representative score*")
        @Max(100)
        @Min(0)
        @RequestParam(defaultValue = "75") Integer percentile,
        @ApiParam("Gene set score threshold (for absolute score value). Filters out gene sets where the GSVA(like) *representative score* is under this threshold.")
        @Max(1)
        @Min(0)
        @RequestParam(defaultValue = "0.4") Double scoreThreshold,
    	@ApiParam("p-value threshold. Filters out gene sets for which the score p-value is higher than this threshold.")
        @Max(1)
        @Min(0)
        @RequestParam(defaultValue = "0.05") Double pvalueThreshold,
        @ApiParam(required = false, value = "Identifier of pre-defined sample list with samples to query, e.g. brca_tcga_all")
    	@RequestParam(required = false) String sampleListId,
        @ApiParam(required = false, value = "Fill this one if you want to specify a subset of samples:"
        		+ " sampleIds: custom list of samples or patients to query, e.g. [\"TCGA-A1-A0SD-01\", \"TCGA-A1-A0SE-01\"]")
        @RequestBody(required = false) List<String> sampleIds)
        throws GeneticProfileNotFoundException {

	        if (sampleListId != null && sampleListId.trim().length() > 0) {
	    		return new ResponseEntity<>(
	    				genesetHierarchyService.fetchGenesetHierarchyInfo(geneticProfileId, percentile, scoreThreshold, pvalueThreshold, sampleListId)
		        		, HttpStatus.OK);
	    	} else if (sampleIds != null && sampleIds.size() > 0){
		        return new ResponseEntity<>(
		        		genesetHierarchyService.fetchGenesetHierarchyInfo(geneticProfileId, percentile, scoreThreshold, pvalueThreshold, sampleIds)
		        		, HttpStatus.OK);
	    	} else {
		        return new ResponseEntity<>(
		        		genesetHierarchyService.fetchGenesetHierarchyInfo(geneticProfileId, percentile, scoreThreshold, pvalueThreshold)
		        		, HttpStatus.OK);
	    	}
        
    }
}
