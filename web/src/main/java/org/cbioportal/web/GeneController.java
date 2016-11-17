package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.Gene;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.exception.GeneNotFoundException;
import org.cbioportal.web.exception.PageSizeTooBigException;
import org.cbioportal.web.parameter.Direction;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.sort.GeneSortBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @Autowired
    private GeneService geneService;

    @RequestMapping(value = "/genes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get all genes")
    public ResponseEntity<List<Gene>> getAllGenes(
            @ApiParam("Level of detail of the response")
            @RequestParam(defaultValue = "SUMMARY") Projection projection,
            @ApiParam("Page size of the result list")
            @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
            @ApiParam("Page number of the result list")
            @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
            @ApiParam("Name of the property that the result list is sorted by")
            @RequestParam(required = false) GeneSortBy sortBy,
            @ApiParam("Direction of the sort")
            @RequestParam(defaultValue = "ASC") Direction direction) {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, geneService.getMetaGenes().getTotalCount()
                    .toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                    geneService.getAllGenes(projection.name(), pageSize, pageNumber,
                            sortBy == null ? null : sortBy.name(), direction.name()), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/genes/{geneId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get a gene")
    public ResponseEntity<Gene> getGene(
            @ApiParam(required = true, value = "Entrez Gene ID or Hugo Gene Symbol e.g. 1 or A1BG")
            @PathVariable String geneId) throws GeneNotFoundException {

        return new ResponseEntity<>(geneService.getGene(geneId), HttpStatus.OK);
    }

    @RequestMapping(value = "/genes/{geneId}/aliases", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get aliases of a gene")
    public ResponseEntity<List<String>> getAliasesOfGene(
            @ApiParam(required = true, value = "Entrez Gene ID or Hugo Gene Symbol e.g. 1 or A1BG")
            @PathVariable String geneId) {

        return new ResponseEntity<>(geneService.getAliasesOfGene(geneId), HttpStatus.OK);
    }

    @RequestMapping(value = "/genes/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch genes by ID")
    public ResponseEntity<List<Gene>> fetchGenes(
            @ApiParam("Level of detail of the response")
            @RequestParam(defaultValue = "SUMMARY") Projection projection,
            @ApiParam(required = true, value = "List of Entrez Gene IDs and/or Hugo Gene Symbols")
            @RequestBody List<String> geneIds) throws PageSizeTooBigException {

        if (geneIds.size() > PagingConstants.MAX_PAGE_SIZE) {
            throw new PageSizeTooBigException(geneIds.size());
        }

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, geneService.fetchMetaGenes(geneIds)
                    .getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                    geneService.fetchGenes(geneIds, projection.name()), HttpStatus.OK);
        }
    }
}
