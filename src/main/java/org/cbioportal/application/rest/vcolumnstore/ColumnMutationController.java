package org.cbioportal.application.rest.vcolumnstore;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.cbioportal.domain.mutation.usecase.GetMutationDataUseCases;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.model.meta.MutationMeta;
import org.cbioportal.legacy.web.parameter.*;
import org.cbioportal.legacy.web.parameter.sort.MutationSortBy;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * REST controller for managing and retrieving Mutation data from a column-store data
 * source.
 *
 * <p>This controller provides an endpoint to fetch Mutation data with support for
 * filtering, sorting, and controlling the level of detail in the response. It is designed to work
 * with a column-store database, which is optimized for querying large datasets efficiently.
 *
 */


@RestController
@RequestMapping("/api/column-store")
@Profile("clickhouse")
public class ColumnMutationController {
    private final GetMutationDataUseCases getMutationDataUseCases;

    /**
     * Constructs a new {@link ColumnMutationController} with the specified use case.
     *
     * @param getMutationDataUseCases the use case responsible for retrieving cancer study
     *     metadata.
     */
    public ColumnMutationController(GetMutationDataUseCases getMutationDataUseCases) {
        this.getMutationDataUseCases = getMutationDataUseCases;
    }

    @Hidden
    @RequestMapping(
        value = "/mutations/fetch",
        method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Mutation>> fetchMutationsInMultipleMolecularProfiles(
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies")
        Collection<String> involvedCancerStudies,
        @Parameter(
            hidden =
                true) // prevent reference to this attribute in the swagger-ui interface. this
        // attribute is needed for the @PreAuthorize tag above.
        @Valid
        @RequestAttribute(required = false, value = "interceptedMutationMultipleStudyFilter")
        MutationMultipleStudyFilter interceptedMutationMultipleStudyFilter,
        @Parameter(
            required = true,
            description =
                "List of Molecular Profile IDs or List of Molecular Profile ID / Sample ID pairs,"
                    + " and List of Entrez Gene IDs")
        @Valid
        @RequestBody(required = false)
        MutationMultipleStudyFilter mutationMultipleStudyFilter,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY")
        Projection projection,
        @Parameter(description = "Page size of the result list")
        @Max(PagingConstants.MAX_PAGE_SIZE)
        @Min(PagingConstants.MIN_PAGE_SIZE)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE)
        Integer pageSize,
        @Parameter(description = "Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER)
        Integer pageNumber,
        @Parameter(description = "Name of the property that the result list is sorted by")
        @RequestParam(required = false)
        MutationSortBy sortBy,
        @Parameter(description = "Direction of the sort") @RequestParam(defaultValue = "ASC")
        Direction direction) {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            MutationMeta mutationMeta;

            if (interceptedMutationMultipleStudyFilter.getMolecularProfileIds() != null) {
                mutationMeta =
                    getMutationDataUseCases.fetchAllMetaMutationsInProfileUseCase().execute(
                        interceptedMutationMultipleStudyFilter.getMolecularProfileIds(),
                        null,
                        interceptedMutationMultipleStudyFilter.getEntrezGeneIds());
            } else {
                List<String> molecularProfileIds = new ArrayList<>();
                List<String> sampleIds = new ArrayList<>();
                extractMolecularProfileAndSampleIds(
                    interceptedMutationMultipleStudyFilter, molecularProfileIds, sampleIds);
                mutationMeta =
                    getMutationDataUseCases.fetchAllMetaMutationsInProfileUseCase().execute(
                        molecularProfileIds,
                        sampleIds,
                        interceptedMutationMultipleStudyFilter.getEntrezGeneIds());
            }
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, mutationMeta.getTotalCount().toString());
            responseHeaders.add(
                HeaderKeyConstants.SAMPLE_COUNT, mutationMeta.getSampleCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            List<Mutation> mutations;
            if (interceptedMutationMultipleStudyFilter.getMolecularProfileIds() != null) {
                mutations =
                    getMutationDataUseCases.fetchAllMutationsInProfileUseCase().execute(
                        interceptedMutationMultipleStudyFilter.getMolecularProfileIds(),
                        null,
                        interceptedMutationMultipleStudyFilter.getEntrezGeneIds(),
                        projection.name(),
                        pageSize,
                        pageNumber,
                        sortBy == null ? null : sortBy.getOriginalValue(),
                        direction.name());
            } else {

                List<String> molecularProfileIds = new ArrayList<>();
                List<String> sampleIds = new ArrayList<>();
                extractMolecularProfileAndSampleIds(
                    interceptedMutationMultipleStudyFilter, molecularProfileIds, sampleIds);
                mutations =
                    getMutationDataUseCases.fetchAllMutationsInProfileUseCase().execute(
                        molecularProfileIds,
                        sampleIds,
                        interceptedMutationMultipleStudyFilter.getEntrezGeneIds(),
                        projection.name(),
                        pageSize,
                        pageNumber,
                        sortBy == null ? null : sortBy.getOriginalValue(),
                        direction.name());
            }
            return ResponseEntity.ok(mutations);
        }
    }
    private void extractMolecularProfileAndSampleIds(
        MutationMultipleStudyFilter mutationMultipleStudyFilter,
        List<String> molecularProfileIds,
        List<String> sampleIds) {

        for (SampleMolecularIdentifier sampleMolecularIdentifier :
            mutationMultipleStudyFilter.getSampleMolecularIdentifiers()) {

            molecularProfileIds.add(sampleMolecularIdentifier.getMolecularProfileId());
            sampleIds.add(sampleMolecularIdentifier.getSampleId());
        }
    }
}
