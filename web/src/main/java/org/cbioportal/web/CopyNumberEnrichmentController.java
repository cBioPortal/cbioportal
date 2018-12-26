package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.service.CopyNumberEnrichmentService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.CopyNumberEnrichmentEventType;
import org.cbioportal.web.parameter.EnrichmentFilter;
import org.cbioportal.web.parameter.EnrichmentType;
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
@Api(tags = "Copy Number Enrichments", description = " ")
public class CopyNumberEnrichmentController {

    @Autowired
    private CopyNumberEnrichmentService copyNumberEnrichmentService;

    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfile', 'read')")
    @RequestMapping(value = "/molecular-profiles/{molecularProfileId}/copy-number-enrichments/fetch",
        method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch copy number enrichments in a molecular profile")
    public ResponseEntity<List<AlterationEnrichment>> fetchCopyNumberEnrichments(
        @ApiParam(required = true, value = "Molecular Profile ID e.g. acc_tcga_mutations")
        @PathVariable String molecularProfileId,
        @ApiParam("Type of the copy number event")
        @RequestParam(defaultValue = "HOMDEL") CopyNumberEnrichmentEventType copyNumberEventType,
        @ApiParam("Type of the enrichment e.g. SAMPLE or PATIENT")
        @RequestParam(defaultValue = "SAMPLE") EnrichmentType enrichmentType,
        @ApiParam(required = true, value = "List of altered and unaltered Sample/Patient IDs")
        @Valid @RequestBody EnrichmentFilter enrichmentFilter) throws MolecularProfileNotFoundException {

        return new ResponseEntity<>(copyNumberEnrichmentService.getCopyNumberEnrichments(molecularProfileId,
            enrichmentFilter.getAlteredIds(), enrichmentFilter.getUnalteredIds(),
            copyNumberEventType.getAlterationTypes(), enrichmentType.name()), HttpStatus.OK);
    }
}
