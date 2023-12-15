package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.cbioportal.model.Gene;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.exception.GeneNotFoundException;
import org.cbioportal.service.exception.GeneWithMultipleEntrezIdsException;
import org.cbioportal.web.config.PublicApiTags;
import org.cbioportal.web.config.annotation.PublicApi;
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

import java.util.List;

@PublicApi
@RestController
@Validated
@Tag(name = PublicApiTags.GENES, description = " ")
public class GeneController {

    @Autowired
    private GeneService geneService;

    @RequestMapping(value = "/api/genes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get all genes")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = Gene.class))))
    public ResponseEntity<List<Gene>> getAllGenes(
        @Parameter(description = "Search keyword that applies to hugo gene symbol of the genes")
        @RequestParam(required = false) String keyword,
        @Parameter(description = "Alias of the gene")
        @RequestParam(required = false) String alias,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection,
        @Parameter(description = "Page size of the result list")
        @Max(PagingConstants.MAX_PAGE_SIZE)
        @Min(PagingConstants.MIN_PAGE_SIZE)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
        @Parameter(description = "Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
        @Parameter(description = "Name of the property that the result list is sorted by")
        @RequestParam(required = false) GeneSortBy sortBy,
        @Parameter(description = "Direction of the sort")
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

    @RequestMapping(value = "/api/genes/{geneId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get a gene")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(schema = @Schema(implementation = Gene.class)))
    public ResponseEntity<Gene> getGene(
        @Parameter(required = true, description = "Entrez Gene ID or Hugo Gene Symbol e.g. 1 or A1BG")
        @PathVariable String geneId) throws GeneNotFoundException, GeneWithMultipleEntrezIdsException {

        return new ResponseEntity<>(geneService.getGene(geneId), HttpStatus.OK);
    }

    @RequestMapping(value = "/api/genes/{geneId}/aliases", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get aliases of a gene")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class))))
    public ResponseEntity<List<String>> getAliasesOfGene(
        @Parameter(required = true, description = "Entrez Gene ID or Hugo Gene Symbol e.g. 1 or A1BG")
        @PathVariable String geneId) throws GeneNotFoundException, GeneWithMultipleEntrezIdsException {

        return new ResponseEntity<>(geneService.getAliasesOfGene(geneId), HttpStatus.OK);
    }

    @RequestMapping(value = "/api/genes/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch genes by ID")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = Gene.class))))
    public ResponseEntity<List<Gene>> fetchGenes(
        @RequestParam(defaultValue = "ENTREZ_GENE_ID") GeneIdType geneIdType,
        @Parameter(required = true, description = "List of Gene IDs")
        @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
        @RequestBody List<String> geneIds,
        @Parameter(description = "Level of detail of the response")
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
