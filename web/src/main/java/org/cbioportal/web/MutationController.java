package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.List;

import io.swagger.annotations.ApiParam;
import org.cbioportal.model.Mutation;
import org.cbioportal.service.MutationService;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.Direction;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.sort.MutationSortBy;
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

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@PublicApi
@RestController
@Validated
@Api(tags = "Mutations", description = " ")
public class MutationController {

    private static final int MUTATION_MAX_PAGE_SIZE = 50000;
    private static final String MUTATION_DEFAULT_PAGE_SIZE = "50000";

    @Autowired
    private MutationService mutationService;

    @RequestMapping(value = "/genetic-profiles/{geneticProfileId}/mutations", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get mutations in a genetic profile")
    public ResponseEntity<List<Mutation>> getMutationsInGeneticProfile(
        @ApiParam(required = true, value = "Genetic Profile ID e.g. acc_tcga_mutations")
        @PathVariable String geneticProfileId,
        @ApiParam(required = true, value = "Sample ID e.g. TCGA-OR-A5J2-01")
        @RequestParam String sampleId,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection,
        @ApiParam("Page size of the result list")
        @Max(MUTATION_MAX_PAGE_SIZE)
        @Min(PagingConstants.MIN_PAGE_SIZE)
        @RequestParam(defaultValue = MUTATION_DEFAULT_PAGE_SIZE) Integer pageSize,
        @ApiParam("Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
        @ApiParam("Name of the property that the result list is sorted by")
        @RequestParam(required = false) MutationSortBy sortBy,
        @ApiParam("Direction of the sort")
        @RequestParam(defaultValue = "ASC") Direction direction) {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, mutationService.getMetaMutationsInGeneticProfile(
                geneticProfileId, sampleId).getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                mutationService.getMutationsInGeneticProfile(geneticProfileId, sampleId, projection.name(), pageSize,
                    pageNumber, sortBy == null ? null : sortBy.getOriginalValue(), direction.name()), HttpStatus.OK);
        }
    }
    
    @RequestMapping(value = "/genetic-profiles/{geneticProfileId}/mutations/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch mutations in a genetic profile by sample ID")
    public ResponseEntity<List<Mutation>> fetchMutationsInGeneticProfile(
        @ApiParam(required = true, value = "Genetic Profile ID e.g. acc_tcga_mutations")
        @PathVariable String geneticProfileId,
        @ApiParam(required = true, value = "List of Sample IDs")
        @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
        @RequestBody List<String> sampleIds,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection,
        @ApiParam("Page size of the result list")
        @Max(MUTATION_MAX_PAGE_SIZE)
        @Min(PagingConstants.MIN_PAGE_SIZE)
        @RequestParam(defaultValue = MUTATION_DEFAULT_PAGE_SIZE) Integer pageSize,
        @ApiParam("Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
        @ApiParam("Name of the property that the result list is sorted by")
        @RequestParam(required = false) MutationSortBy sortBy,
        @ApiParam("Direction of the sort")
        @RequestParam(defaultValue = "ASC") Direction direction) {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, mutationService.fetchMetaMutationsInGeneticProfile(
                geneticProfileId, sampleIds).getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                mutationService.fetchMutationsInGeneticProfile(geneticProfileId, sampleIds, projection.name(), pageSize,
                    pageNumber, sortBy == null ? null : sortBy.getOriginalValue(), direction.name()), HttpStatus.OK);
        }
    }
}
