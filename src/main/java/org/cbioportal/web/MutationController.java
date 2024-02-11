package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.meta.MutationMeta;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.web.config.PublicApiTags;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.Direction;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.MutationFilter;
import org.cbioportal.web.parameter.MutationMultipleStudyFilter;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.SampleMolecularIdentifier;
import org.cbioportal.web.parameter.sort.MutationSortBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@PublicApi
@RestController()
@RequestMapping("/api")
@Validated
@Tag(name = PublicApiTags.MUTATIONS, description = " ")
public class MutationController {

    @Autowired
    private MutationService mutationService;

    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfileId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/molecular-profiles/{molecularProfileId}/mutations", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get mutations in a molecular profile by Sample List ID")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = Mutation.class))))
    public ResponseEntity<List<Mutation>> getMutationsInMolecularProfileBySampleListId(
        @Parameter(required = true, description = "Molecular Profile ID e.g. acc_tcga_mutations")
        @PathVariable String molecularProfileId,
        @Parameter(required = true, description = "Sample List ID e.g. acc_tcga_all")
        @RequestParam String sampleListId,
        @Parameter(description = "Entrez Gene ID")
        @RequestParam(required = false) Integer entrezGeneId,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection,
        @Parameter(description = "Page size of the result list")
        @Max(PagingConstants.MAX_PAGE_SIZE)
        @Min(PagingConstants.MIN_PAGE_SIZE)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
        @Parameter(description = "Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
        @Parameter(description = "Name of the property that the result list is sorted by")
        @RequestParam(required = false) MutationSortBy sortBy,
        @Parameter(description = "Direction of the sort")
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
                    entrezGeneId == null ? null : Arrays.asList(entrezGeneId), false, projection.name(), pageSize,
                    pageNumber, sortBy == null ? null : sortBy.getOriginalValue(), direction.name()), HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfileId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/molecular-profiles/{molecularProfileId}/mutations/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch mutations in a molecular profile")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = Mutation.class))))
    public ResponseEntity<List<Mutation>> fetchMutationsInMolecularProfile(
        @Parameter(required = true, description = "Molecular Profile ID e.g. acc_tcga_mutations")
        @PathVariable String molecularProfileId,
        @Parameter(required = true, description = "List of Sample IDs/Sample List ID and Entrez Gene IDs")
        @Valid @RequestBody MutationFilter mutationFilter,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection,
        @Parameter(description = "Page size of the result list")
        @Max(PagingConstants.MAX_PAGE_SIZE)
        @Min(PagingConstants.MIN_PAGE_SIZE)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
        @Parameter(description = "Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
        @Parameter(description = "Name of the property that the result list is sorted by")
        @RequestParam(required = false) MutationSortBy sortBy,
        @Parameter(description = "Direction of the sort")
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
                    mutationFilter.getSampleListId(), mutationFilter.getEntrezGeneIds(), false, projection.name(),
                    pageSize, pageNumber, sortBy == null ? null : sortBy.getOriginalValue(), direction.name());
            } else {
                mutations = mutationService.fetchMutationsInMolecularProfile(molecularProfileId,
                    mutationFilter.getSampleIds(), mutationFilter.getEntrezGeneIds(), false, projection.name(), pageSize,
                    pageNumber, sortBy == null ? null : sortBy.getOriginalValue(), direction.name());
            }

            return new ResponseEntity<>(mutations, HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/mutations/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch mutations in multiple molecular profiles by sample IDs")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = Mutation.class))))
    public ResponseEntity<List<Mutation>> fetchMutationsInMultipleMolecularProfiles(
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedMutationMultipleStudyFilter") MutationMultipleStudyFilter interceptedMutationMultipleStudyFilter,
        @Parameter(required = true, description = "List of Molecular Profile IDs or List of Molecular Profile ID / Sample ID pairs," +
            " and List of Entrez Gene IDs")
        @Valid @RequestBody(required = false) MutationMultipleStudyFilter mutationMultipleStudyFilter,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection,
        @Parameter(description = "Page size of the result list")
        @Max(PagingConstants.MAX_PAGE_SIZE)
        @Min(PagingConstants.MIN_PAGE_SIZE)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
        @Parameter(description = "Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
        @Parameter(description = "Name of the property that the result list is sorted by")
        @RequestParam(required = false) MutationSortBy sortBy,
        @Parameter(description = "Direction of the sort")
        @RequestParam(defaultValue = "ASC") Direction direction) {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            MutationMeta mutationMeta;

            if (interceptedMutationMultipleStudyFilter.getMolecularProfileIds() != null) {
                mutationMeta = mutationService.getMetaMutationsInMultipleMolecularProfiles(
                    interceptedMutationMultipleStudyFilter.getMolecularProfileIds(), null,
                    interceptedMutationMultipleStudyFilter.getEntrezGeneIds());
            } else {

                List<String> molecularProfileIds = new ArrayList<>();
                List<String> sampleIds = new ArrayList<>();
                extractMolecularProfileAndSampleIds(interceptedMutationMultipleStudyFilter, molecularProfileIds, sampleIds);
                mutationMeta = mutationService.getMetaMutationsInMultipleMolecularProfiles(molecularProfileIds,
                    sampleIds, interceptedMutationMultipleStudyFilter.getEntrezGeneIds());
            }
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, mutationMeta.getTotalCount().toString());
            responseHeaders.add(HeaderKeyConstants.SAMPLE_COUNT, mutationMeta.getSampleCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            List<Mutation> mutations;
            if (interceptedMutationMultipleStudyFilter.getMolecularProfileIds() != null) {
                mutations = mutationService.getMutationsInMultipleMolecularProfiles(
                    interceptedMutationMultipleStudyFilter.getMolecularProfileIds(), null,
                    interceptedMutationMultipleStudyFilter.getEntrezGeneIds(), projection.name(), pageSize, pageNumber,
                    sortBy == null ? null : sortBy.getOriginalValue(), direction.name());
            } else {

                List<String> molecularProfileIds = new ArrayList<>();
                List<String> sampleIds = new ArrayList<>();
                extractMolecularProfileAndSampleIds(interceptedMutationMultipleStudyFilter, molecularProfileIds, sampleIds);
                mutations = mutationService.getMutationsInMultipleMolecularProfiles(molecularProfileIds,
                    sampleIds, interceptedMutationMultipleStudyFilter.getEntrezGeneIds(), projection.name(), pageSize,
                    pageNumber, sortBy == null ? null : sortBy.getOriginalValue(), direction.name());
            }

            return new ResponseEntity<>(mutations, HttpStatus.OK);
        }
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
