package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.cbioportal.model.CoExpression;
import org.cbioportal.model.EntityType;
import org.cbioportal.service.CoExpressionService;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.CoExpressionFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@InternalApi
@RestController()
@RequestMapping("/api")
@Validated
@Tag(name = "Co-Expressions", description = " ")
public class CoExpressionController {

    @Autowired
    private CoExpressionService coExpressionService;

    // requires permission to access both molecularProfileIdA and molecularProfileIdB because service layer does not enforce requirement that both profiles are in the same study
    @PreAuthorize("hasPermission(#molecularProfileIdA, 'MolecularProfileId', T(org.cbioportal.utils.security.AccessLevel).READ) and hasPermission(#molecularProfileIdB, 'MolecularProfileId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/molecular-profiles/co-expressions/fetch",
        method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Calculates correlations between a genetic entity from a specific profile and another profile from the same study")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = CoExpression.class))))
    public ResponseEntity<List<CoExpression>> fetchCoExpressions(
        @Parameter(required = true, description = "Molecular Profile ID from the Genetic Entity referenced in the co-expression filter e.g. acc_tcga_rna_seq_v2_mrna")
        @RequestParam String molecularProfileIdA,
        @Parameter(required = true, description = "Molecular Profile ID (can be the same as molecularProfileIdA) e.g. acc_tcga_rna_seq_v2_mrna")
        @RequestParam String molecularProfileIdB,
        @Parameter(required = true, description = "List of Sample IDs/Sample List ID and Entrez Gene ID/Gene set ID")
        @Valid @RequestBody CoExpressionFilter coExpressionFilter,
        @Parameter(description = "Threshold")
        @RequestParam(defaultValue = "0.3") Double threshold) throws Exception {

        List<CoExpression> coExpressionList;
        String geneticEntityId = null;
        EntityType geneticEntityType = null;

        if (coExpressionFilter.getEntrezGeneId() != null) {
            geneticEntityId = coExpressionFilter.getEntrezGeneId().toString();
            geneticEntityType = EntityType.GENE;
        } else {
            geneticEntityId = coExpressionFilter.getGenesetId();
            geneticEntityType = EntityType.GENESET;
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
