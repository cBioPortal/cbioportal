package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.cbioportal.model.MutationSpectrum;
import org.cbioportal.service.MutationSpectrumService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.MutationSpectrumFilter;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@InternalApi
@RestController()
@RequestMapping("/api")
@Validated
@Tag(name = "Mutation Spectrums", description = " ")
public class MutationSpectrumController {

    @Autowired
    private MutationSpectrumService mutationSpectrumService;

    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfileId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/molecular-profiles/{molecularProfileId}/mutation-spectrums/fetch",
        method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch mutation spectrums in a molecular profile")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = MutationSpectrum.class))))
    public ResponseEntity<List<MutationSpectrum>> fetchMutationSpectrums(
        @Parameter(required = true, description = "Molecular Profile ID e.g. acc_tcga_mutations")
        @PathVariable String molecularProfileId,
        @Parameter(required = true, description = "List of Sample IDs/Sample List ID")
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
