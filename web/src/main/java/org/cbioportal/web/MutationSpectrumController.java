package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.MutationSpectrum;
import org.cbioportal.service.MutationSpectrumService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
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

import javax.validation.Valid;
import java.util.List;

@InternalApi
@RestController
@Validated
@Api(tags = "Mutation Spectrums", description = " ")
public class MutationSpectrumController {

    @Autowired
    private MutationSpectrumService mutationSpectrumService;

    @RequestMapping(value = "/genetic-profiles/{geneticProfileId}/mutation-spectrums/fetch",
        method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch mutation spectrums in a genetic profile")
    public ResponseEntity<List<MutationSpectrum>> fetchMutationSpectrums(
        @ApiParam(required = true, value = "Genetic Profile ID e.g. acc_tcga_mutations")
        @PathVariable String geneticProfileId,
        @ApiParam(required = true, value = "List of Sample IDs/Sample List ID")
        @Valid @RequestBody MutationSpectrumFilter mutationSpectrumFilter) throws GeneticProfileNotFoundException {

        List<MutationSpectrum> fractionGenomeAlteredList;
        if (mutationSpectrumFilter.getSampleListId() != null) {
            fractionGenomeAlteredList = mutationSpectrumService.getMutationSpectrums(geneticProfileId,
                mutationSpectrumFilter.getSampleListId());
        } else {
            fractionGenomeAlteredList = mutationSpectrumService.fetchMutationSpectrums(geneticProfileId,
                mutationSpectrumFilter.getSampleIds());
        }

        return new ResponseEntity<>(fractionGenomeAlteredList, HttpStatus.OK);
    }
}
