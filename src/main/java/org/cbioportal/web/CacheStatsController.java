package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.cbioportal.service.CacheStatisticsService;
import org.cbioportal.service.exception.CacheNotFoundException;
import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@Tag(name = "CacheStats",  description = " ")
@Hidden
@ConditionalOnProperty(name = "persistence.cache_type", havingValue = {"ehcache-heap", "ehcache-disk", "ehcache-hybrid", "redis"})
public class CacheStatsController {

    @Autowired
    public CacheStatisticsService cacheStatisticsService;

    @RequestMapping(value = "/api/{cache}/keysInCache", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get list of keys in cache")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class))))
    public ResponseEntity<List<String>> getKeysInCache(
        @Parameter(required = true, description = "Cache name")
        @PathVariable String cache) throws CacheNotFoundException {
        List<String> strings = cacheStatisticsService.getKeysInCache(cache);
        return new ResponseEntity<>(strings, HttpStatus.OK);
    }

    @RequestMapping(value = "/api/{cache}/keyCountsPerClass", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get counts of keys per repository class")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class))))
    public ResponseEntity<List<String>> getKeyCountsPerClass(
        @Parameter(required = true, description = "Cache name")
        @PathVariable String cache) throws CacheNotFoundException {
        List<String> strings = cacheStatisticsService.getKeyCountsPerClass(cache);
        return new ResponseEntity<>(strings, HttpStatus.OK);
    }

    @RequestMapping(value = "/api/cacheStatistics", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get general cache statistics")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(schema = @Schema(implementation = String.class)))
    public ResponseEntity<String> getCacheStatistics() throws CacheNotFoundException {
        return new ResponseEntity<>(cacheStatisticsService.getCacheStatistics(), HttpStatus.OK);
    }
}
