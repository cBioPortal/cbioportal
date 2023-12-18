package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import org.cbioportal.model.MrnaPercentile;
import org.cbioportal.service.MrnaPercentileService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.PagingConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
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
@Tag(name = "mRNA Percentile", description = " ")
public class MrnaPercentileController {

    @Autowired
    private MrnaPercentileService mrnaPercentileService;

    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfileId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/molecular-profiles/{molecularProfileId}/mrna-percentile/fetch",
        method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get mRNA expression percentiles for list of genes for a sample")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = MrnaPercentile.class))))
    public ResponseEntity<List<MrnaPercentile>> fetchMrnaPercentile(
        @Parameter(required = true, description = "Molecular Profile ID e.g. acc_tcga_rna_seq_v2_mrna")
        @PathVariable String molecularProfileId,
        @Parameter(required = true, description = "Sample ID e.g. TCGA-OR-A5J2-01")
        @RequestParam String sampleId,
        @Parameter(required = true, description = "List of Entrez Gene IDs")
        @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
        @RequestBody List<Integer> entrezGeneIds)
        throws MolecularProfileNotFoundException {

        return new ResponseEntity<>(
            mrnaPercentileService.fetchMrnaPercentile(molecularProfileId, sampleId, entrezGeneIds), HttpStatus.OK);
    }
}
