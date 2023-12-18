package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import org.cbioportal.model.ReferenceGenomeGene;
import org.cbioportal.service.GeneMemoizerService;
import org.cbioportal.service.ReferenceGenomeGeneService;
import org.cbioportal.service.exception.GeneNotFoundException;
import org.cbioportal.web.config.InternalApiTags;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@InternalApi
@RestController
@Validated
@Tag(name = InternalApiTags.REFERENCE_GENOME_GENES, description = " ")
public class ReferenceGenomeGeneController {
    private static final int GENE_MAX_PAGE_SIZE = 100000;
    private static final String GENE_DEFAULT_PAGE_SIZE = "100000";

    @Autowired
    private ReferenceGenomeGeneService referenceGenomeGeneService;
    
    @Autowired
    private GeneMemoizerService geneMemoizerService;
    
    /**
     * The memoization logic in this method is a temporary fix to make this work until
     * Ehcache is working correctly on cbioportal.org.
     * This endpoint creates a large response and seems to bloat the webserver's heap
     * size, leading to poor performance and crashes. The caching seems to improve
     * processor load, heap size and response time.
     */
    @RequestMapping(value = "/api/reference-genome-genes/{genomeName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get all reference genes")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReferenceGenomeGene.class))))
    public ResponseEntity<List<ReferenceGenomeGene>> getAllReferenceGenomeGenes(
        @Parameter(required = true, description = "Name of Reference Genome hg19")
        @PathVariable String genomeName)  {

        List<ReferenceGenomeGene> genes = geneMemoizerService.fetchGenes(genomeName);
        if (genes == null) {
            genes = referenceGenomeGeneService.fetchAllReferenceGenomeGenes(genomeName);
            geneMemoizerService.cacheGenes(genes, genomeName);
        }
        return new ResponseEntity<>(genes, HttpStatus.OK);
    }

    @RequestMapping(value = "/api/reference-genome-genes/{genomeName}/{geneId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get a gene of a reference genome of interest")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(schema = @Schema(implementation = ReferenceGenomeGene.class)))
    public ResponseEntity<ReferenceGenomeGene> getReferenceGenomeGene(
        @Parameter(required = true, description = "Name of Reference Genome hg19")
        @PathVariable String genomeName,
        @Parameter(required = true, description = "Entrez Gene ID 207")
        @PathVariable Integer geneId) throws GeneNotFoundException {

        return new ResponseEntity<>(referenceGenomeGeneService.getReferenceGenomeGene(geneId, genomeName), HttpStatus.OK);
    }

    @RequestMapping(value = "/api/reference-genome-genes/{genomeName}/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch genes of reference genome of interest")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReferenceGenomeGene.class))))
    public ResponseEntity<List<ReferenceGenomeGene>> fetchReferenceGenomeGenes(
        @Parameter(required = true, description = "Name of Reference Genome hg19")
        @PathVariable String genomeName,
        @Parameter(required = true, description = "List of Gene IDs")
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

