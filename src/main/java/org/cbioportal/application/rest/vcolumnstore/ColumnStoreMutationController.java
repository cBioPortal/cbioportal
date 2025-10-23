package org.cbioportal.application.rest.vcolumnstore;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.cbioportal.application.rest.mapper.MutationMapper;
import org.cbioportal.application.rest.response.MutationDTO;
import org.cbioportal.domain.mutation.usecase.MutationUseCases;
import org.cbioportal.legacy.model.meta.MutationMeta;
import org.cbioportal.legacy.web.parameter.*;
import org.cbioportal.legacy.web.parameter.sort.MutationSortBy;
import org.cbioportal.shared.MutationQueryOptions;
import org.cbioportal.shared.enums.ProjectionType;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
 * </p>
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Configurable projection levels (ID, SUMMARY, DETAILED) for response optimization
 *   <li>Multi-study filtering capabilities
 *   <li>Metadata queries for count operations
 * </ul>
 * 
 * <p>This controller is only active when the "clickhouse" profile is enabled and requires
 * appropriate read permissions for the requested cancer studies.
 * 
 * @see MutationDTO
 */


@RestController
@RequestMapping("/api/column-store")
@Profile("clickhouse")
public class ColumnStoreMutationController {
    private final MutationUseCases mutationUseCases;

    /**
     * Constructs a new {@link ColumnStoreMutationController} with the specified use case.
     *
     * @param mutationUseCases the use case responsible for retrieving Mutation metadata or Mutation
     *   
     */
    public ColumnStoreMutationController(MutationUseCases mutationUseCases) {
        this.mutationUseCases = mutationUseCases;
    }


    /**
     * Fetch Mutation by exactly one sampleUniqueIdentifier or molecularProfileId must or entrezGeneIds
     * 
     *
     * @param interceptedMutationMultipleStudyFilter security-intercepted filter for permission
     *     validation
     * @param mutationMultipleStudyFilter filter containing patient/sample identifiers and attribute
     *     IDs 
     * @param projection level of detail for the response data
     * @return ResponseEntity containing list of Mutation data DTOs, or empty body with count header
     *  for META projection
     */
    @Hidden
    @PreAuthorize(
        "hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
    @PostMapping(
        value = "/mutations/fetch",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MutationDTO>> fetchMutationsInMultipleMolecularProfiles(
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies")
        Collection<String> involvedCancerStudies, 
        @Parameter(
            hidden =
                true) // prevent reference to this attribute in the swagger-ui interface. this
        // attribute is needed for now but was needed previously for @PreAuthorize .
        @Valid
        @RequestBody(required = false)
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
        ProjectionType projection,
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

        if (projection == ProjectionType.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            MutationMeta mutationMeta = mutationUseCases.fetchMetaMutationsUseCase().execute(interceptedMutationMultipleStudyFilter);
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, mutationMeta.getTotalCount().toString());
            responseHeaders.add(
                HeaderKeyConstants.SAMPLE_COUNT, mutationMeta.getSampleCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        }
        MutationQueryOptions mutationQueryOptions = new MutationQueryOptions(projection,pageSize,
            pageNumber,
            sortBy == null ? null : sortBy.getOriginalValue(),
            direction);
        List<MutationDTO> mutations= MutationMapper.INSTANCE.toDTOs(mutationUseCases.fetchAllMutationsInProfileUseCase()
            .execute(
            interceptedMutationMultipleStudyFilter,
                mutationQueryOptions));
        return ResponseEntity.ok(mutations);
    }
}
