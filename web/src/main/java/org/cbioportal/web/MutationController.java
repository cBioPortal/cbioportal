package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.swagger.annotations.ApiParam;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCountByPosition;
import org.cbioportal.model.meta.MutationMeta;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.Direction;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.MutationFilter;
import org.cbioportal.web.parameter.MutationMultipleStudyFilter;
import org.cbioportal.web.parameter.MutationPositionIdentifier;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.SampleMolecularIdentifier;
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
import org.springframework.security.access.prepost.PreAuthorize;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@PublicApi
@RestController
@Validated
@Api(tags = "Mutations", description = " ")
public class MutationController {

    public static final int MUTATION_MAX_PAGE_SIZE = 10000000;
    private static final String MUTATION_DEFAULT_PAGE_SIZE = "10000000";

    @Autowired
    private MutationService mutationService;
    @Autowired
    private MolecularProfileService molecularProfileService;

    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfile', 'read')")
    @RequestMapping(value = "/molecular-profiles/{molecularProfileId}/mutations", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get mutations in a molecular profile by Sample List ID")
    public ResponseEntity<List<Mutation>> getMutationsInMolecularProfileBySampleListId(
        @ApiParam(required = true, value = "Molecular Profile ID e.g. acc_tcga_mutations")
        @PathVariable String molecularProfileId,
        @ApiParam(required = true, value = "Sample List ID e.g. acc_tcga_all")
        @RequestParam String sampleListId,
        @ApiParam("Entrez Gene ID")
        @RequestParam(required = false) Integer entrezGeneId,
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
        @RequestParam(defaultValue = "ASC") Direction direction) throws MolecularProfileNotFoundException {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            MutationMeta mutationMeta = mutationService.getMetaMutationsInMolecularProfileBySampleListId(
                molecularProfileId, sampleListId, entrezGeneId == null ? null : Arrays.asList(entrezGeneId));
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, mutationMeta.getTotalCount().toString());
            responseHeaders.add(HeaderKeyConstants.SAMPLE_COUNT, mutationMeta.getSampleCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                mutationService.getMutationsInMolecularProfileBySampleListId(molecularProfileId, sampleListId, 
                    entrezGeneId == null ? null : Arrays.asList(entrezGeneId), null, projection.name(), pageSize, 
                    pageNumber, sortBy == null ? null : sortBy.getOriginalValue(), direction.name()), HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfile', 'read')")
    @RequestMapping(value = "/molecular-profiles/{molecularProfileId}/mutations/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch mutations in a molecular profile")
    public ResponseEntity<List<Mutation>> fetchMutationsInMolecularProfile(
        @ApiParam(required = true, value = "Molecular Profile ID e.g. acc_tcga_mutations")
        @PathVariable String molecularProfileId,
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
        @RequestParam(defaultValue = "ASC") Direction direction) throws MolecularProfileNotFoundException {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            MutationMeta mutationMeta;

            if (mutationFilter.getSampleListId() != null) {
                mutationMeta = mutationService.getMetaMutationsInMolecularProfileBySampleListId(molecularProfileId,
                    mutationFilter.getSampleListId(), mutationFilter.getEntrezGeneIds());
            } else {
                mutationMeta = mutationService.fetchMetaMutationsInMolecularProfile(molecularProfileId,
                    mutationFilter.getSampleIds(), mutationFilter.getEntrezGeneIds());
            }
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, mutationMeta.getTotalCount().toString());
            responseHeaders.add(HeaderKeyConstants.SAMPLE_COUNT, mutationMeta.getSampleCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            List<Mutation> mutations;
            if (mutationFilter.getSampleListId() != null) {
                mutations = mutationService.getMutationsInMolecularProfileBySampleListId(molecularProfileId,
                    mutationFilter.getSampleListId(), mutationFilter.getEntrezGeneIds(), null, projection.name(), 
                    pageSize, pageNumber, sortBy == null ? null : sortBy.getOriginalValue(), direction.name());
            } else {
                mutations = mutationService.fetchMutationsInMolecularProfile(molecularProfileId,
                    mutationFilter.getSampleIds(), mutationFilter.getEntrezGeneIds(), null, projection.name(), pageSize,
                    pageNumber, sortBy == null ? null : sortBy.getOriginalValue(), direction.name());
            }

            return new ResponseEntity<>(mutations, HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#mutationMultipleStudyFilter, 'MutationMultipleStudyFilter', 'read')")
    @RequestMapping(value = "/mutations/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch mutations in multiple molecular profiles by sample IDs")
    public ResponseEntity<List<Mutation>> fetchMutationsInMultipleMolecularProfiles(
        @ApiParam(required = true, value = "List of Molecular Profile ID and Sample ID pairs and Entrez Gene IDs")
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

            if (mutationMultipleStudyFilter.getMolecularProfileIds() != null) {
                mutationMeta = mutationService.getMetaMutationsInMultipleMolecularProfiles(
                    mutationMultipleStudyFilter.getMolecularProfileIds(), null, 
                    mutationMultipleStudyFilter.getEntrezGeneIds());
            } else {

                List<String> molecularProfileIds = new ArrayList<>();
                List<String> sampleIds = new ArrayList<>();
                extractMolecularProfileAndSampleIds(mutationMultipleStudyFilter, molecularProfileIds, sampleIds);
                mutationMeta = mutationService.getMetaMutationsInMultipleMolecularProfiles(molecularProfileIds,
                    sampleIds, mutationMultipleStudyFilter.getEntrezGeneIds());
            }
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, mutationMeta.getTotalCount().toString());
            responseHeaders.add(HeaderKeyConstants.SAMPLE_COUNT, mutationMeta.getSampleCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            List<Mutation> mutations;
            if (mutationMultipleStudyFilter.getMolecularProfileIds() != null) {
                mutations = mutationService.getMutationsInMultipleMolecularProfiles(
                    mutationMultipleStudyFilter.getMolecularProfileIds(), null, 
                    mutationMultipleStudyFilter.getEntrezGeneIds(), projection.name(), pageSize, pageNumber, 
                    sortBy == null ? null : sortBy.getOriginalValue(), direction.name());
            } else {

                List<String> molecularProfileIds = new ArrayList<>();
                List<String> sampleIds = new ArrayList<>();
                extractMolecularProfileAndSampleIds(mutationMultipleStudyFilter, molecularProfileIds, sampleIds);
                mutations = mutationService.getMutationsInMultipleMolecularProfiles(molecularProfileIds,
                    sampleIds, mutationMultipleStudyFilter.getEntrezGeneIds(), projection.name(), pageSize,
                    pageNumber, sortBy == null ? null : sortBy.getOriginalValue(), direction.name());
            }

            return new ResponseEntity<>(mutations, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/mutation-counts-by-position/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch mutation counts in all studies by gene and position")
    public ResponseEntity<List<MutationCountByPosition>> fetchMutationCountsByPosition(
        @ApiParam(required = true, value = "List of gene and positions")
        @Size(min = 1, max = MUTATION_MAX_PAGE_SIZE)
        @RequestBody List<MutationPositionIdentifier> mutationPositionIdentifiers) {

        List<Integer> entrezGeneIds = new ArrayList<>();
        List<Integer> proteinPosStarts = new ArrayList<>();
        List<Integer> proteinPosEnds = new ArrayList<>();
        for (MutationPositionIdentifier mutationPositionIdentifier : mutationPositionIdentifiers) {
            
            entrezGeneIds.add(mutationPositionIdentifier.getEntrezGeneId());
            proteinPosStarts.add(mutationPositionIdentifier.getProteinPosStart());
            proteinPosEnds.add(mutationPositionIdentifier.getProteinPosEnd());
        }
        
        return new ResponseEntity<>(mutationService.fetchMutationCountsByPosition(entrezGeneIds, proteinPosStarts, 
            proteinPosEnds), HttpStatus.OK);
    }

    private void extractMolecularProfileAndSampleIds(MutationMultipleStudyFilter mutationMultipleStudyFilter,
                                                     List<String> molecularProfileIds, List<String> sampleIds) {
        
        for (SampleMolecularIdentifier sampleMolecularIdentifier :
            mutationMultipleStudyFilter.getSampleMolecularIdentifiers()) {

            molecularProfileIds.add(sampleMolecularIdentifier.getMolecularProfileId());
            sampleIds.add(sampleMolecularIdentifier.getSampleId());
        }
    }
}
