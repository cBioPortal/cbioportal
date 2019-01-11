package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.MutationSpectrum;
import org.cbioportal.service.MutationSpectrumService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.MutationSpectrumFilter;
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

import javax.validation.Valid;
import java.util.List;

@InternalApi
@RestController
@Validated
@Api(tags = "Mutation Spectrums", description = " ")
public class MutationSpectrumController {

    @Autowired
    private MutationSpectrumService mutationSpectrumService;

    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfile', 'read')")
    @RequestMapping(value = "/molecular-profiles/{molecularProfileId}/mutation-spectrums/fetch",
        method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch mutation spectrums in a molecular profile")
    public ResponseEntity<List<MutationSpectrum>> fetchMutationSpectrums(
        @ApiParam(required = true, value = "Molecular Profile ID e.g. acc_tcga_mutations")
        @PathVariable String molecularProfileId,
        @ApiParam(required = true, value = "List of Sample IDs/Sample List ID")
        @Valid @RequestBody MutationSpectrumFilter mutationSpectrumFilter) throws MolecularProfileNotFoundException {

        List<MutationSpectrum> mutationSpectrums;
        if (mutationSpectrumFilter.getSampleListId() != null) {
            mutationSpectrums = mutationSpectrumService.getMutationSpectrums(molecularProfileId,
                mutationSpectrumFilter.getSampleListId());
        } else {
            mutationSpectrums = mutationSpectrumService.fetchMutationSpectrums(molecularProfileId,
                mutationSpectrumFilter.getSampleIds());
        }

        return new ResponseEntity<>(mutationSpectrums, HttpStatus.OK);
    }
}
