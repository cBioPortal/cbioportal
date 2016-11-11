package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.cbioportal.model.Gene;
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
@Api(tags = "Genes", description = " ")
public class GeneController {

    @RequestMapping(value = "/genes", method = RequestMethod.GET)
    @ApiOperation("Get all genes")
    public ResponseEntity<List<Gene>> getAllGenes(@RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                   @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                   @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber) {

        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/genes/{entrezGeneId}", method = RequestMethod.GET)
    @ApiOperation("Get a gene")
    public ResponseEntity<Gene> getGene(@PathVariable String entrezGeneId) {

        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/genes/fetch", method = RequestMethod.POST)
    @ApiOperation("Fetch genes by ID")
    public ResponseEntity<List<Gene>> fetchGenes(@RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                  @RequestBody List<String> entrezGeneIds) {

        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/genes/query", method = RequestMethod.POST)
    @ApiOperation("Query genes by example")
    public ResponseEntity<List<Gene>> queryGenesByExample(@RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                           @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                           @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
                                                                           @RequestBody Gene exampleGene) {

        throw new UnsupportedOperationException();
    }
}
