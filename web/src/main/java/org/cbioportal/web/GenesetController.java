package org.cbioportal.web;

import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import org.cbioportal.model.Geneset;
import org.cbioportal.service.GenesetService;
import org.cbioportal.service.exception.GenesetNotFoundException;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.Projection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
@Api(tags = "Gene Sets", description = " ")
public class GenesetController {

    @Autowired
    private GenesetService genesetService;

    @RequestMapping(value = "/genesets", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get all gene sets")
    public ResponseEntity<List<Geneset>> getAllGenesets(
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection,
        @ApiParam("Page size of the result list")
        @Max(Integer.MAX_VALUE)
        @Min(PagingConstants.MIN_PAGE_SIZE)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
        @ApiParam("Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber) {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, genesetService.getMetaGenesets().getTotalCount()
                .toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
            		genesetService.getAllGenesets(projection.name(), pageSize, pageNumber), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/genesets/{genesetId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get a gene set")
    public ResponseEntity<Geneset> getGeneset(
        @ApiParam(required = true, value = "Gene set ID e.g. GNF2_ZAP70")
        @PathVariable String genesetId) throws GenesetNotFoundException {

        return new ResponseEntity<>(genesetService.getGeneset(genesetId), HttpStatus.OK);
    }

    @RequestMapping(value = "/genesets/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
	        produces = MediaType.APPLICATION_JSON_VALUE)
	    @ApiOperation("Fetch gene sets by ID")
	    public ResponseEntity<List<Geneset>> fetchGenesets(
	        @ApiParam(required = true, value = "List of Gene set IDs")
	        @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
	        @RequestBody List<String> genesetIds) {

	        return new ResponseEntity<>(genesetService.fetchGenesets(genesetIds), HttpStatus.OK);
    }

}
