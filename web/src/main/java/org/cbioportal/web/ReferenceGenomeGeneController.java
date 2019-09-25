package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.ReferenceGenomeGene;
import org.cbioportal.service.ReferenceGenomeGeneService;
import org.cbioportal.service.exception.GeneNotFoundException;
import org.cbioportal.web.config.annotation.PublicApi;
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

import javax.validation.constraints.Size;
import java.util.List;
import java.util.stream.Collectors;

@PublicApi
@RestController
@Validated
@Api(tags = "Reference Genome Genes", description = " ")
public class ReferenceGenomeGeneController {

    private static final int GENE_MAX_PAGE_SIZE = 100000;
    private static final String GENE_DEFAULT_PAGE_SIZE = "100000";
    
    @Autowired
    private ReferenceGenomeGeneService referenceGenomeGeneService;

    @RequestMapping(value = "/reference-genome-genes/{genomeName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get all reference genes")
    public ResponseEntity<List<ReferenceGenomeGene>> getAllReferenceGenomeGenes(
        @ApiParam(required = true, value = "Name of Reference Genome hg19")
        @PathVariable String genomeName)  {
        
        return new ResponseEntity<>(
                referenceGenomeGeneService.fetchAllReferenceGenomeGenes(genomeName), HttpStatus.OK);
    }

    @RequestMapping(value = "/reference-genome-genes/{genomeName}/{geneId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get a gene of a reference genome of interest")
    public ResponseEntity<ReferenceGenomeGene> getReferenceGenomeGene(
        @ApiParam(required = true, value = "Name of Reference Genome hg19")
        @PathVariable String genomeName,
        @ApiParam(required = true, value = "Entrez Gene ID 207")
        @PathVariable Integer geneId) throws GeneNotFoundException {
    
        return new ResponseEntity<>(referenceGenomeGeneService.getReferenceGenomeGene(geneId, genomeName), HttpStatus.OK);
    }
    
    @RequestMapping(value = "/reference-genome-genes/{genomeName}/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch genes of reference genome of interest")
    public ResponseEntity<List<ReferenceGenomeGene>> fetchReferenceGenomeGenes(
        @ApiParam(required = true, value = "Name of Reference Genome hg19")
        @PathVariable String genomeName,
        @ApiParam(required = true, value = "List of Entrez Gene IDs")
        @Size(min = 1, max = GENE_MAX_PAGE_SIZE)
        @RequestBody List<String> geneIds) {
        
        if (isInteger(geneIds.get(0))) {
            List<Integer> newIds = geneIds.stream().map(s -> Integer.parseInt(s)).collect(Collectors.toList());
            return new ResponseEntity<>(referenceGenomeGeneService.fetchGenesByGenomeName(newIds, genomeName), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(referenceGenomeGeneService.fetchGenesByHugoGeneSymbolsAndGenomeName(
                    geneIds, genomeName), HttpStatus.OK);
        }
    }
    
    private boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException | NullPointerException e) {
            return false;
        }
        return true;
    }
}

