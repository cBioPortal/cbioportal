package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.ExpressionEnrichment;
import org.cbioportal.service.ExpressionEnrichmentService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.EnrichmentFilter;
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

import javax.validation.Valid;
import java.util.List;

@InternalApi
@RestController
@Validated
@Api(tags = "Expression Enrichments", description = " ")
public class ExpressionEnrichmentController {

    @Autowired
    private ExpressionEnrichmentService expressionEnrichmentService;

    @RequestMapping(value = "/genetic-profiles/{geneticProfileId}/expression-enrichments/fetch",
        method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch expression enrichments in a genetic profile")
    public ResponseEntity<List<ExpressionEnrichment>> fetchExpressionEnrichments(
        @ApiParam(required = true, value = "Genetic Profile ID e.g. acc_tcga_rna_seq_v2_mrna")
        @PathVariable String geneticProfileId,
        @ApiParam(required = true, value = "List of altered and unaltered Sample IDs and Entrez Gene IDs")
        @Valid @RequestBody EnrichmentFilter enrichmentFilter) throws GeneticProfileNotFoundException {

        return new ResponseEntity<>(expressionEnrichmentService.getExpressionEnrichments(geneticProfileId,
            enrichmentFilter.getAlteredSampleIds(), enrichmentFilter.getUnalteredSampleIds()), HttpStatus.OK);
    }
}
