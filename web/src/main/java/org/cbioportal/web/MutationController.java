package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiParam;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCount;
import org.cbioportal.model.meta.MutationMeta;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.Direction;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.MutationFilter;
import org.cbioportal.web.parameter.MutationMultipleStudyFilter;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.SampleGeneticIdentifier;
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

import javax.validation.Valid;
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
    @ApiOperation("Get mutations in a genetic profile by Sample List ID")
    public ResponseEntity<List<Mutation>> getMutationsInGeneticProfileBySampleListId(
        @ApiParam(required = true, value = "Genetic Profile ID e.g. acc_tcga_mutations")
        @PathVariable String geneticProfileId,
        @ApiParam(required = true, value = "Sample List ID e.g. acc_tcga_all")
        @RequestParam String sampleListId,
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
        @RequestParam(defaultValue = "ASC") Direction direction) throws GeneticProfileNotFoundException {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT,
                mutationService.getMetaMutationsInGeneticProfileBySampleListId(geneticProfileId,
                    sampleListId, null).getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                mutationService.getMutationsInGeneticProfileBySampleListId(geneticProfileId, sampleListId, null,
                    projection.name(), pageSize, pageNumber, sortBy == null ? null : sortBy.getOriginalValue(),
                    direction.name()), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/genetic-profiles/{geneticProfileId}/mutations/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch mutations in a genetic profile by sample IDs")
    public ResponseEntity<List<Mutation>> fetchMutationsInGeneticProfile(
        @ApiParam(required = true, value = "Genetic Profile ID e.g. acc_tcga_mutations")
        @PathVariable String geneticProfileId,
        @ApiParam(required = true, value = "List of Sample IDs/Sample List ID and Entrez Gene IDs")
        @Valid @RequestBody MutationFilter mutationFilter,
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
        @RequestParam(defaultValue = "ASC") Direction direction) throws GeneticProfileNotFoundException {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            MutationMeta mutationMeta;

            if (mutationFilter.getSampleListId() != null) {
                mutationMeta = mutationService.getMetaMutationsInGeneticProfileBySampleListId(geneticProfileId,
                    mutationFilter.getSampleListId(), mutationFilter.getEntrezGeneIds());
            } else {
                mutationMeta = mutationService.fetchMetaMutationsInGeneticProfile(geneticProfileId,
                    mutationFilter.getSampleIds(), mutationFilter.getEntrezGeneIds());
            }
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, mutationMeta.getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            List<Mutation> mutations;
            if (mutationFilter.getSampleListId() != null) {
                mutations = mutationService.getMutationsInGeneticProfileBySampleListId(geneticProfileId,
                    mutationFilter.getSampleListId(), mutationFilter.getEntrezGeneIds(), projection.name(), pageSize,
                    pageNumber, sortBy == null ? null : sortBy.getOriginalValue(), direction.name());
            } else {
                mutations = mutationService.fetchMutationsInGeneticProfile(geneticProfileId,
                    mutationFilter.getSampleIds(), mutationFilter.getEntrezGeneIds(), projection.name(), pageSize,
                    pageNumber, sortBy == null ? null : sortBy.getOriginalValue(), direction.name());
            }

            return new ResponseEntity<>(mutations, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/mutations/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch mutations in multiple genetic profiles by sample IDs")
    public ResponseEntity<List<Mutation>> fetchMutationsInMultipleGeneticProfiles(
        @ApiParam(required = true, value = "List of Genetic Profile ID and Sample ID pairs and Entrez Gene IDs")
        @Valid @RequestBody MutationMultipleStudyFilter mutationMultipleStudyFilter,
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
            MutationMeta mutationMeta;

            if (mutationMultipleStudyFilter.getGeneticProfileIds() != null) {
                mutationMeta = mutationService.getMetaMutationsInMultipleGeneticProfiles(
                    mutationMultipleStudyFilter.getGeneticProfileIds(), null, 
                    mutationMultipleStudyFilter.getEntrezGeneIds());
            } else {

                List<String> geneticProfileIds = new ArrayList<>();
                List<String> sampleIds = new ArrayList<>();
                extractGeneticProfileAndSampleIds(mutationMultipleStudyFilter, geneticProfileIds, sampleIds);
                mutationMeta = mutationService.getMetaMutationsInMultipleGeneticProfiles(geneticProfileIds,
                    sampleIds, mutationMultipleStudyFilter.getEntrezGeneIds());
            }
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, mutationMeta.getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            List<Mutation> mutations;
            if (mutationMultipleStudyFilter.getGeneticProfileIds() != null) {
                mutations = mutationService.getMutationsInMultipleGeneticProfiles(
                    mutationMultipleStudyFilter.getGeneticProfileIds(), null, 
                    mutationMultipleStudyFilter.getEntrezGeneIds(), projection.name(), pageSize, pageNumber, 
                    sortBy == null ? null : sortBy.getOriginalValue(), direction.name());
            } else {

                List<String> geneticProfileIds = new ArrayList<>();
                List<String> sampleIds = new ArrayList<>();
                extractGeneticProfileAndSampleIds(mutationMultipleStudyFilter, geneticProfileIds, sampleIds);
                mutations = mutationService.getMutationsInMultipleGeneticProfiles(geneticProfileIds,
                    sampleIds, mutationMultipleStudyFilter.getEntrezGeneIds(), projection.name(), pageSize,
                    pageNumber, sortBy == null ? null : sortBy.getOriginalValue(), direction.name());
            }

            return new ResponseEntity<>(mutations, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/genetic-profiles/{geneticProfileId}/mutation-counts", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get mutation counts in a genetic profile by Sample List ID")
    public ResponseEntity<List<MutationCount>> getMutationCountsInGeneticProfileBySampleListId(
        @ApiParam(required = true, value = "Genetic Profile ID e.g. acc_tcga_mutations")
        @PathVariable String geneticProfileId,
        @ApiParam(required = true, value = "Sample List ID e.g. acc_tcga_all")
        @RequestParam String sampleListId) throws GeneticProfileNotFoundException {

        return new ResponseEntity<>(mutationService.getMutationCountsInGeneticProfileBySampleListId(
            geneticProfileId, sampleListId), HttpStatus.OK);
    }

    @RequestMapping(value = "/genetic-profiles/{geneticProfileId}/mutation-counts/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch mutation counts in a genetic profile by sample IDs")
    public ResponseEntity<List<MutationCount>> fetchMutationCountsInGeneticProfile(
        @ApiParam(required = true, value = "Genetic Profile ID e.g. acc_tcga_mutations")
        @PathVariable String geneticProfileId,
        @ApiParam(required = true, value = "List of Sample IDs")
        @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
        @RequestBody List<String> sampleIds) throws GeneticProfileNotFoundException {

        return new ResponseEntity<>(mutationService.fetchMutationCountsInGeneticProfile(geneticProfileId,
            sampleIds), HttpStatus.OK);
    }

    private void extractGeneticProfileAndSampleIds(MutationMultipleStudyFilter mutationMultipleStudyFilter, 
                                                   List<String> geneticProfileIds, List<String> sampleIds) {
        
        for (SampleGeneticIdentifier sampleGeneticIdentifier :
            mutationMultipleStudyFilter.getSampleGeneticIdentifiers()) {

            geneticProfileIds.add(sampleGeneticIdentifier.getGeneticProfileId());
            sampleIds.add(sampleGeneticIdentifier.getSampleId());
        }
    }
}
