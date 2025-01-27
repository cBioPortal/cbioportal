package org.cbioportal.application.rest.vcolumnstore;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.cbioportal.cancerstudy.CancerStudyMetadata;
import org.cbioportal.cancerstudy.usecase.GetCancerStudyMetadataUseCase;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.PagingConstants;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.sort.StudySortBy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/column-store")
public class ColumnStoreStudyController {

    private final GetCancerStudyMetadataUseCase getCancerStudyMetadataUseCase;

    public ColumnStoreStudyController(GetCancerStudyMetadataUseCase getCancerStudyMetadataUseCase) {
        this.getCancerStudyMetadataUseCase = getCancerStudyMetadataUseCase;
    }

// Projection return same type best case would be to return types that reflect this
    @Hidden
    @GetMapping(value = "/studies", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CancerStudyMetadata>> getAllStudies(
        @Parameter(description = "Search keyword that applies to name and cancer type of the studies")
        @RequestParam(required = false) String keyword,
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
        @RequestParam(required = false) StudySortBy sortBy,
        @Parameter(description = "Direction of the sort")
        @RequestParam(defaultValue = "ASC") Direction direction) {

        return new ResponseEntity<>(getCancerStudyMetadataUseCase.execute()
            , HttpStatus.OK);
    }

}
