package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.CoExpression;
import org.cbioportal.service.CoExpressionService;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.CoExpressionFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.validation.Valid;
import java.util.List;

@InternalApi
@RestController
@Validated
@Api(tags = "Co-Expressions", description = " ")
public class CoExpressionController {
    
    @Autowired
    private CoExpressionService coExpressionService;

    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfileId', 'read')")
    @RequestMapping(value = "/molecular-profiles/{molecularProfileId}/co-expressions/fetch",
        method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Calculates correlations between a genetic entity from a specific profile and another profile from the same study")
    public ResponseEntity<List<CoExpression>> fetchCoExpressions(
        @ApiParam(required = true, value = "Molecular Profile ID from the Genetic Entity referenced in the co-expression filter e.g. acc_tcga_rna_seq_v2_mrna")
        @RequestParam String molecularProfileIdA,
        @ApiParam(required = true, value = "Molecular Profile ID (can be the same as molecularProfileIdA) e.g. acc_tcga_rna_seq_v2_mrna")
        @RequestParam String molecularProfileIdB,
        @ApiParam(required = true, value = "List of Sample IDs/Sample List ID and Entrez Gene ID/Gene set ID")
        @Valid @RequestBody CoExpressionFilter coExpressionFilter,
        @ApiParam("Threshold")
        @RequestParam(defaultValue = "0.3") Double threshold) throws Exception {

        List<CoExpression> coExpressionList;
        String geneticEntityId = null;
        CoExpression.GeneticEntityType geneticEntityType = null;

        if (coExpressionFilter.getEntrezGeneId() != null) {
            geneticEntityId = coExpressionFilter.getEntrezGeneId().toString();
            geneticEntityType = CoExpression.GeneticEntityType.GENE;
        } else {
            geneticEntityId = coExpressionFilter.getGenesetId();
            geneticEntityType = CoExpression.GeneticEntityType.GENESET;
        }

        if (coExpressionFilter.getSampleListId() != null) {
            coExpressionList = coExpressionService.getCoExpressions(geneticEntityId,
                    geneticEntityType, coExpressionFilter.getSampleListId(), molecularProfileIdA, molecularProfileIdB,
                    threshold);
        } else {
            coExpressionList = coExpressionService.fetchCoExpressions(geneticEntityId,
                    geneticEntityType, coExpressionFilter.getSampleIds(), molecularProfileIdA, molecularProfileIdB,
                    threshold);
        }

        return new ResponseEntity<>(coExpressionList, HttpStatus.OK);
    }
}
