package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.DiscreteCopyNumberEventType;
import org.cbioportal.web.parameter.DiscreteCopyNumberFilter;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.Projection;
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

import javax.validation.Valid;
import java.util.List;

@PublicApi
@RestController
@Validated
@Api(tags = "Discrete Copy Number Alterations", description = " ")
public class DiscreteCopyNumberController {

    @Autowired
    private DiscreteCopyNumberService discreteCopyNumberService;

    @RequestMapping(value = "/genetic-profiles/{geneticProfileId}/discrete-copy-number", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get discrete copy number alterations in a genetic profile")
    public ResponseEntity<List<DiscreteCopyNumberData>> getDiscreteCopyNumbersInGeneticProfile(
        @ApiParam(required = true, value = "Genetic Profile ID e.g. acc_tcga_gistic")
        @PathVariable String geneticProfileId,
        @ApiParam(required = true, value = "Sample ID e.g. TCGA-OR-A5J2-01")
        @RequestParam String sampleId,
        @ApiParam("Type of the copy number event")
        @RequestParam(defaultValue = "HOMDEL_AND_AMP") DiscreteCopyNumberEventType discreteCopyNumberEventType,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) throws GeneticProfileNotFoundException {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, discreteCopyNumberService
                .getMetaDiscreteCopyNumbersInGeneticProfile(geneticProfileId, sampleId,
                    discreteCopyNumberEventType.getAlterations()).getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                discreteCopyNumberService.getDiscreteCopyNumbersInGeneticProfile(geneticProfileId, sampleId,
                    discreteCopyNumberEventType.getAlterations(), projection.name()), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/genetic-profiles/{geneticProfileId}/discrete-copy-number/fetch",
        method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch discrete copy number alterations in a genetic profile by sample ID")
    public ResponseEntity<List<DiscreteCopyNumberData>> fetchDiscreteCopyNumbersInGeneticProfile(
        @ApiParam(required = true, value = "Genetic Profile ID e.g. acc_tcga_gistic")
        @PathVariable String geneticProfileId,
        @ApiParam("Type of the copy number event")
        @RequestParam(defaultValue = "HOMDEL_AND_AMP") DiscreteCopyNumberEventType discreteCopyNumberEventType,
        @ApiParam(required = true, value = "List of Sample IDs and Entrez Gene IDs")
        @Valid @RequestBody DiscreteCopyNumberFilter discreteCopyNumberFilter,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection)
        throws GeneticProfileNotFoundException {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, discreteCopyNumberService
                .fetchMetaDiscreteCopyNumbersInGeneticProfile(geneticProfileId, discreteCopyNumberFilter.getSampleIds(),
                    discreteCopyNumberFilter.getEntrezGeneIds(), discreteCopyNumberEventType.getAlterations())
                .getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                discreteCopyNumberService.fetchDiscreteCopyNumbersInGeneticProfile(geneticProfileId,
                    discreteCopyNumberFilter.getSampleIds(), discreteCopyNumberFilter.getEntrezGeneIds(),
                    discreteCopyNumberEventType.getAlterations(), projection.name()), HttpStatus.OK);
        }
    }
}
