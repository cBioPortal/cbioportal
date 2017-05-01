package org.cbioportal.web;

import java.util.List;

import org.cbioportal.model.GenesetGeneticData;
import org.cbioportal.service.GenesetDataService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.cbioportal.service.exception.SampleListNotFoundException;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.GenesetDataFilterCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@PublicApi
@RestController
@Validated
@Api(tags = "Gene Set Genetic Data", description = " ")
public class GenesetDataController {


	@Autowired
    private GenesetDataService genesetDataService;
    
    @RequestMapping(value = "/genetic-profiles/{geneticProfileId}/geneset-genetic-data/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch gene set \"genetic data\" items (gene set scores) by profile Id, gene set ids and sample ids")
    public ResponseEntity<List<GenesetGeneticData>> fetchGeneticDataItems(
            @ApiParam(required = true, value = "Genetic profile ID, e.g. gbm_tcga_gsva_scores")
            @PathVariable String geneticProfileId,
            @ApiParam(required = true, value = "Search criteria to return the values for a given set of samples and gene set items. "
            		+ "genesetIds: The list of identifiers for the gene sets of interest, e.g. HINATA_NFKB_MATRIX. "
            		+ "Use one of these if you want to specify a subset of samples:"
            		+ "(1) sampleListId: Identifier of pre-defined sample list with samples to query, e.g. brca_tcga_all " 
            		+ "or (2) sampleIds: custom list of samples or patients to query, e.g. TCGA-BH-A1EO-01, TCGA-AR-A1AR-01")
            @RequestBody GenesetDataFilterCriteria genesetDataFilterCriteria) throws GeneticProfileNotFoundException, SampleListNotFoundException {

    	if (genesetDataFilterCriteria.getSampleListId() != null && genesetDataFilterCriteria.getSampleListId().trim().length() > 0) {
    		return new ResponseEntity<>(
    				genesetDataService.fetchGenesetData(geneticProfileId, genesetDataFilterCriteria.getSampleListId(), 
    						genesetDataFilterCriteria.getGenesetIds()), HttpStatus.OK);
    	} else {
    		return new ResponseEntity<>(
    				genesetDataService.fetchGenesetData(geneticProfileId, genesetDataFilterCriteria.getSampleIds(), 
    						genesetDataFilterCriteria.getGenesetIds()), HttpStatus.OK);
    	}
    }

}
