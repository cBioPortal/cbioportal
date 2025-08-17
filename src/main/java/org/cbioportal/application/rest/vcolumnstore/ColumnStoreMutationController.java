package org.cbioportal.application.rest.vcolumnstore;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.cbioportal.domain.mutation.usecase.GetMutationUseCases;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.model.meta.MutationMeta;
import org.cbioportal.legacy.web.parameter.*;
import org.cbioportal.legacy.web.parameter.sort.MutationSortBy;
import org.cbioportal.shared.MutationSearchCriteria;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
public class ColumnStoreMutationController {
    private final GetMutationUseCases getMutationUseCases;

    /**
     * Constructs a new {@link ColumnStoreMutationController} with the specified use case.
     *
     * @param getMutationUseCases the use case responsible for retrieving Mutation metadata or Mutation
     *   
     */
    public ColumnStoreMutationController(GetMutationUseCases getMutationUseCases) {
        this.getMutationUseCases = getMutationUseCases;
    }
    @Hidden
    @PostMapping(
        value = "/mutations/fetch",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Mutation>> fetchMutationsInMultipleMolecularProfiles(
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies")
        Collection<String> involvedCancerStudies, //variable is not being used- should be removed 
        @Parameter(
            hidden =
                true) // prevent reference to this attribute in the swagger-ui interface. this
        // attribute is needed for now but was needed previously for @PreAuthorize .
        @Valid
        @RequestAttribute(required = false, value = "interceptedMutationMultipleStudyFilter")
        MutationMultipleStudyFilter interceptedMutationMultipleStudyFilter, // This is being intercepted will leave this 
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
            MutationMeta mutationMeta=getMutationUseCases.fetchMetaMutationsUseCase().execute(mutationMultipleStudyFilter);
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, mutationMeta.getTotalCount().toString());
            responseHeaders.add(
                HeaderKeyConstants.SAMPLE_COUNT, mutationMeta.getSampleCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        }
        MutationSearchCriteria mutationSearchCriteria = new MutationSearchCriteria(projection,pageSize,
            pageNumber,
            sortBy == null ? null : sortBy.getOriginalValue(),
            direction);
        List<Mutation> mutations=
            getMutationUseCases.fetchAllMutationsInProfileUseCase().execute(
                mutationMultipleStudyFilter,
                mutationSearchCriteria);
        return ResponseEntity.ok(mutations);
    }
}
