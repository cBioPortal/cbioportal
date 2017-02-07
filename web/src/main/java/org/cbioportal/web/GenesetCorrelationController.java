package org.cbioportal.web;

import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.cbioportal.model.GenesetCorrelation;
import org.cbioportal.service.GenesetCorrelationService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.cbioportal.web.config.annotation.InternalApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
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
@Api(tags = "Gene set correlation", description = " ")
public class GenesetCorrelationController {

    @Autowired
    private GenesetCorrelationService genesetCorrelationService;

    @RequestMapping(value = "/genesets/{genesetId}/expression-correlation/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get the genes in a gene set that have expression correlated to the gene set scores")
    public ResponseEntity<List<GenesetCorrelation>> fetchCorrelatedGenes(
    	@ApiParam(required = true, value = "Gene set ID, e.g. MORF_ATRX.")
        @PathVariable String genesetId,
        @ApiParam(required = true, value = "Genetic Profile ID e.g. brca_tcga_gsva_oncogenic_sets_scores")
    	@RequestParam String geneticProfileId,
    	@ApiParam("Correlation threshold (for absolute correlation value, Spearman correlation)")
        @Max(1)
        @Min(0)
        @RequestParam(defaultValue = "0.3") Double correlationThreshold,
        @ApiParam(required = false, value = "Identifier of pre-defined sample list with samples to query, e.g. brca_tcga_all")
    	@RequestParam(required = false) String sampleListId,
        @ApiParam(required = false, value = "Fill this one if you want to specify a subset of samples:"
        		+ " sampleIds: custom list of samples or patients to query, e.g. [\"TCGA-A1-A0SD-01\", \"TCGA-A1-A0SE-01\"]")
        @RequestBody(required = false) List<String> sampleIds)
        throws GeneticProfileNotFoundException {

    	if (sampleListId != null && sampleListId.trim().length() > 0) {
    		return new ResponseEntity<>(
	        		genesetCorrelationService.fetchCorrelatedGenes(genesetId, geneticProfileId, sampleListId, correlationThreshold.doubleValue())
	        		, HttpStatus.OK);
    	} else if (sampleIds != null && sampleIds.size() > 0){
	        return new ResponseEntity<>(
	        		genesetCorrelationService.fetchCorrelatedGenes(genesetId, geneticProfileId, sampleIds, correlationThreshold.doubleValue())
	        		, HttpStatus.OK);
    	} else {
	        return new ResponseEntity<>(
	        		genesetCorrelationService.fetchCorrelatedGenes(genesetId, geneticProfileId, correlationThreshold.doubleValue())
	        		, HttpStatus.OK);
    	}
    }
}
