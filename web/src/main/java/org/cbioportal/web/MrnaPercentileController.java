package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.MrnaPercentile;
import org.cbioportal.service.MrnaPercentileService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.PagingConstants;
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

import javax.validation.constraints.Size;
import java.util.List;

@InternalApi
@RestController
@Validated
@Api(tags = "mRNA Percentile", description = " ")
public class MrnaPercentileController {

    @Autowired
    private MrnaPercentileService mrnaPercentileService;

    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfileId', 'read')")
    @RequestMapping(value = "/molecular-profiles/{molecularProfileId}/mrna-percentile/fetch", 
        method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, 
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get mRNA expression percentiles for list of genes for a sample")
    public ResponseEntity<List<MrnaPercentile>> fetchMrnaPercentile(
        @ApiParam(required = true, value = "Molecular Profile ID e.g. acc_tcga_rna_seq_v2_mrna")
        @PathVariable String molecularProfileId,
        @ApiParam(required = true, value = "Sample ID e.g. TCGA-OR-A5J2-01")
        @RequestParam String sampleId,
        @ApiParam(required = true, value = "List of Entrez Gene IDs")
        @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
        @RequestBody List<Integer> entrezGeneIds)
        throws MolecularProfileNotFoundException {

        return new ResponseEntity<>(
            mrnaPercentileService.fetchMrnaPercentile(molecularProfileId, sampleId, entrezGeneIds), HttpStatus.OK);
    }
}
