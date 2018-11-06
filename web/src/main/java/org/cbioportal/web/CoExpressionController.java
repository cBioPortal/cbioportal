package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.CoExpression;
import org.cbioportal.service.CoExpressionService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.CoExpressionFilter;
import org.springframework.beans.factory.annotation.Autowired;
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
    @ApiOperation("Fetch co-expressions in a molecular profile")
    public ResponseEntity<List<CoExpression>> fetchCoExpressions(
        @ApiParam(required = true, value = "Molecular Profile ID e.g. acc_tcga_rna_seq_v2_mrna")
        @PathVariable String molecularProfileId,
        @ApiParam(required = true, value = "List of Sample IDs/Sample List ID")
        @Valid @RequestBody CoExpressionFilter coExpressionFilter,
        @ApiParam(required = true, value = "Entrez Gene ID")
        @RequestParam Integer entrezGeneId,
        @ApiParam("Threshold")
        @RequestParam(defaultValue = "0.3") Double threshold) throws MolecularProfileNotFoundException {

        List<CoExpression> coExpressionList;
        if (coExpressionFilter.getSampleListId() != null) {
            coExpressionList = coExpressionService.getCoExpressions(molecularProfileId,
                coExpressionFilter.getSampleListId(), entrezGeneId, threshold);
        } else {
            coExpressionList = coExpressionService.fetchCoExpressions(molecularProfileId,
                coExpressionFilter.getSampleIds(), entrezGeneId, threshold);
        }

        return new ResponseEntity<>(coExpressionList, HttpStatus.OK);
    }
}
