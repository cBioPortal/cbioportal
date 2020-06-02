package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.Gene;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.exception.GeneNotFoundException;
import org.cbioportal.service.exception.GeneWithMultipleEntrezIdsException;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.config.PublicApiTags;
import org.cbioportal.web.parameter.Direction;
import org.cbioportal.web.parameter.GeneIdType;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.sort.GeneSortBy;
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

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.List;

@PublicApi
@RestController
@Validated
@Api(tags = PublicApiTags.GENES, description = " ")
public class GeneController {

    private static final int GENE_MAX_PAGE_SIZE = 100000;
    private static final String GENE_DEFAULT_PAGE_SIZE = "100000";

    @Autowired
    private GeneService geneService;

    @RequestMapping(value = "/genes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get all genes")
    public ResponseEntity<List<Gene>> getAllGenes(
        @ApiParam("Search keyword that applies to hugo gene symbol of the genes")
        @RequestParam(required = false) String keyword,
        @ApiParam("Alias of the gene")
        @RequestParam(required = false) String alias,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection,
        @ApiParam("Page size of the result list")
        @Max(GENE_MAX_PAGE_SIZE)
        @Min(PagingConstants.MIN_PAGE_SIZE)
        @RequestParam(defaultValue = GENE_DEFAULT_PAGE_SIZE) Integer pageSize,
        @ApiParam("Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
        @ApiParam("Name of the property that the result list is sorted by")
        @RequestParam(required = false) GeneSortBy sortBy,
        @ApiParam("Direction of the sort")
        @RequestParam(defaultValue = "ASC") Direction direction) {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, geneService.getMetaGenes(keyword, alias).getTotalCount()
                .toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                geneService.getAllGenes(keyword, alias, projection.name(), pageSize, pageNumber,
                    sortBy == null ? null : sortBy.getOriginalValue(), direction.name()), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/genes/{geneId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get a gene")
    public ResponseEntity<Gene> getGene(
        @ApiParam(required = true, value = "Entrez Gene ID or Hugo Gene Symbol e.g. 1 or A1BG")
        @PathVariable String geneId) throws GeneNotFoundException, GeneWithMultipleEntrezIdsException {

        return new ResponseEntity<>(geneService.getGene(geneId), HttpStatus.OK);
    }

    @RequestMapping(value = "/genes/{geneId}/aliases", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get aliases of a gene")
    public ResponseEntity<List<String>> getAliasesOfGene(
        @ApiParam(required = true, value = "Entrez Gene ID or Hugo Gene Symbol e.g. 1 or A1BG")
        @PathVariable String geneId) throws GeneNotFoundException, GeneWithMultipleEntrezIdsException {

        return new ResponseEntity<>(geneService.getAliasesOfGene(geneId), HttpStatus.OK);
    }

    @RequestMapping(value = "/genes/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch genes by ID")
    public ResponseEntity<List<Gene>> fetchGenes(
        @ApiParam("Type of gene ID")
        @RequestParam(defaultValue = "ENTREZ_GENE_ID") GeneIdType geneIdType,
        @ApiParam(required = true, value = "List of Entrez Gene IDs or Hugo Gene Symbols")
        @Size(min = 1, max = GENE_MAX_PAGE_SIZE)
        @RequestBody List<String> geneIds,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, geneService.fetchMetaGenes(geneIds, geneIdType.name())
                .getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(geneService.fetchGenes(geneIds, geneIdType.name(), projection.name()),
                HttpStatus.OK);
        }
    }
}
