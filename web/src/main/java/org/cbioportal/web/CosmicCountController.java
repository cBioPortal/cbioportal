package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import javax.validation.constraints.Size;
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

@InternalApi
@RestController
@Validated
@Api(tags = "Cosmic Counts", description = " ")
public class CosmicCountController {
    private static final int COSMIC_COUNT_MAX_PAGE_SIZE = 50000;

    @Autowired
    private CosmicCountService cosmicCountService;

    @RequestMapping(
        value = "/cosmic-counts/fetch",
        method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiOperation("Get counts within the COSMIC database by keywords")
    public ResponseEntity<List<CosmicMutation>> fetchCosmicCounts(
        @ApiParam(required = true, value = "List of keywords") @Size(
            min = 1,
            max = COSMIC_COUNT_MAX_PAGE_SIZE
        ) @RequestBody List<String> keywords
    ) {
        return new ResponseEntity<>(
            cosmicCountService.fetchCosmicCountsByKeywords(keywords),
            HttpStatus.OK
        );
    }
}
