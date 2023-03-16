package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.CopyNumberCount;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.web.config.InternalApiTags;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.webparam.CopyNumberCountIdentifier;
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
import org.springframework.security.access.prepost.PreAuthorize;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@InternalApi
@RestController
@Validated
@Api(tags = InternalApiTags.DISCRETE_COPY_NUMBER_ALTERATION_COUNTS, description = " ")
public class DiscreteCopyNumberCountController {
    
    private static final int COPY_NUMBER_COUNT_MAX_PAGE_SIZE = 50000;
    
    @Autowired
    private DiscreteCopyNumberService discreteCopyNumberService;

    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfileId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/molecular-profiles/{molecularProfileId}/discrete-copy-number-counts/fetch",
        method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get counts of specific genes and alterations within a CNA molecular profile")
    public ResponseEntity<List<CopyNumberCount>> fetchCopyNumberCounts(
        @ApiParam(required = true, value = "Molecular Profile ID e.g. acc_tcga_gistic")
        @PathVariable String molecularProfileId,
        @ApiParam(required = true, value = "List of copy number count identifiers")
        @Size(min = 1, max = COPY_NUMBER_COUNT_MAX_PAGE_SIZE)
        @RequestBody List<CopyNumberCountIdentifier> copyNumberCountIdentifiers)
        throws MolecularProfileNotFoundException {

        List<Integer> entrezGeneIds = new ArrayList<>();
        List<Integer> alterations = new ArrayList<>();

        for (CopyNumberCountIdentifier copyNumberCountIdentifier : copyNumberCountIdentifiers) {

            entrezGeneIds.add(copyNumberCountIdentifier.getEntrezGeneId());
            alterations.add(copyNumberCountIdentifier.getAlteration());
        }

        return new ResponseEntity<>(discreteCopyNumberService.fetchCopyNumberCounts(molecularProfileId, entrezGeneIds,
            alterations), HttpStatus.OK);
    }
}
