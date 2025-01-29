package org.cbioportal.application.rest.vcolumnstore;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import org.cbioportal.application.rest.mapper.CancerStudyMetadataMapper;
import org.cbioportal.application.rest.response.CancerStudyMetadataDTO;
import org.cbioportal.cancerstudy.usecase.GetCancerStudyMetadataUseCase;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.sort.StudySortBy;
import org.cbioportal.shared.enums.ProjectionType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing and retrieving cancer study metadata from a column-store data source.
 * <p>
 * This controller provides an endpoint to fetch cancer study metadata with support for filtering,
 * sorting, and controlling the level of detail in the response. It is designed to work with a
 * column-store database, which is optimized for querying large datasets efficiently.
 * </p>
 *
 * @see GetCancerStudyMetadataUseCase
 * @see CancerStudyMetadataDTO
 * @see ProjectionType
 * @see StudySortBy
 * @see Direction
 */
@RestController
@RequestMapping("/api/column-store")
public class ColumnStoreStudyController {

    private final GetCancerStudyMetadataUseCase getCancerStudyMetadataUseCase;

    /**
     * Constructs a new {@link ColumnStoreStudyController} with the specified use case.
     *
     * @param getCancerStudyMetadataUseCase the use case responsible for retrieving cancer study metadata.
     */
    public ColumnStoreStudyController(GetCancerStudyMetadataUseCase getCancerStudyMetadataUseCase) {
        this.getCancerStudyMetadataUseCase = getCancerStudyMetadataUseCase;
    }


    /**
     * Retrieves a list of cancer study metadata based on the specified criteria.
     * <p>
     * This endpoint supports filtering by keyword, controlling the level of detail in the response
     * through the projection parameter, and sorting the results by a specified property and direction.
     * </p>
     * <p>
     * <b>Note:</b> This endpoint is marked as {@link Hidden} and will not be exposed in the API documentation.
     * </p>
     *
     * @param keyword    the search keyword that applies to the name and cancer type of the studies.
     *                   This parameter is optional.
     * @param projection the level of detail of the response. Defaults to {@link ProjectionType#SUMMARY}.
     * @param sortBy     the name of the property that the result list is sorted by. This parameter is optional.
     * @param direction  the direction of the sort. Defaults to {@link Direction#ASC}.
     * @return a {@link ResponseEntity} containing a list of {@link CancerStudyMetadataDTO} objects
     *         and an HTTP status code {@link HttpStatus#OK}.
     *
     * @see ProjectionType
     * @see StudySortBy
     * @see Direction
     */
    @Hidden
    @GetMapping(value = "/studies", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CancerStudyMetadataDTO>> getAllStudies(
        @Parameter(description = "Search keyword that applies to name and cancer type of the studies")
        @RequestParam(required = false) String keyword,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") ProjectionType projection,
        @Parameter(description = "Name of the property that the result list is sorted by")
        @RequestParam(required = false) StudySortBy sortBy,
        @Parameter(description = "Direction of the sort")
        @RequestParam(defaultValue = "ASC") Direction direction) {

        return new ResponseEntity<>(CancerStudyMetadataMapper.INSTANCE.toDtos(getCancerStudyMetadataUseCase.execute(projection))
            ,HttpStatus.OK);
    }

}
