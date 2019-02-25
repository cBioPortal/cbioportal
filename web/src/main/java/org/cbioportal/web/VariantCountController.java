package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.VariantCount;
import org.cbioportal.service.VariantCountService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.VariantCountIdentifier;
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
import org.springframework.security.access.prepost.PreAuthorize;

import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@InternalApi
@RestController
@Validated
@Api(tags = "Variant Counts", description = " ")
public class VariantCountController {

    private static final int VARIANT_COUNT_MAX_PAGE_SIZE = 50000;

    @Autowired
    private VariantCountService variantCountService;

    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfileId', 'read')")
    @RequestMapping(value = "/molecular-profiles/{molecularProfileId}/variant-counts/fetch", 
        method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, 
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get counts of specific variants within a mutation molecular profile")
    public ResponseEntity<List<VariantCount>> fetchVariantCounts(
        @ApiParam(required = true, value = "Molecular Profile ID e.g. acc_tcga_mutations")
        @PathVariable String molecularProfileId,
        @ApiParam(required = true, value = "List of variant count identifiers")
        @Size(min = 1, max = VARIANT_COUNT_MAX_PAGE_SIZE)
        @RequestBody List<VariantCountIdentifier> variantCountIdentifiers) throws MolecularProfileNotFoundException {

        List<Integer> entrezGeneIds = new ArrayList<>();
        List<String> keywords = new ArrayList<>(); 
        
        for (VariantCountIdentifier variantCountIdentifier : variantCountIdentifiers) {
         
            entrezGeneIds.add(variantCountIdentifier.getEntrezGeneId());
            keywords.add(variantCountIdentifier.getKeyword());
        }

        return new ResponseEntity<>(variantCountService.fetchVariantCounts(molecularProfileId, entrezGeneIds, keywords), 
            HttpStatus.OK);
    }
}
