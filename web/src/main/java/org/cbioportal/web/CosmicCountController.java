package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.cbioportal.model.CosmicMutation;
import org.cbioportal.service.CosmicCountService;
import org.cbioportal.web.config.annotation.InternalApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Size;
import java.util.List;

@InternalApi
@RestController
@Validated
@Tag(name = "Cosmic Counts", description = " ")
public class CosmicCountController {

    private static final int COSMIC_COUNT_MAX_PAGE_SIZE = 50000;

    @Autowired
    private CosmicCountService cosmicCountService;

    @RequestMapping(value = "/api/cosmic-counts/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get counts within the COSMIC database by keywords")
    public ResponseEntity<List<CosmicMutation>> fetchCosmicCounts(
        @Parameter(required = true, description = "List of keywords")
        @Size(min = 1, max = COSMIC_COUNT_MAX_PAGE_SIZE)
        @RequestBody List<String> keywords) {

        return new ResponseEntity<>(cosmicCountService.fetchCosmicCountsByKeywords(keywords), HttpStatus.OK);
    }
}
